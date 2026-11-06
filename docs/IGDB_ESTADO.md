# Integracion IGDB: estado y reproduccion

> Registro historico de la etapa inicial. El usuario confirmo despues 200 en Postman
> y la nueva prueba real de busqueda/detalle/filtros paso. El estado vigente esta en
> [IGDB_BACKEND_ESTADO.md](IGDB_BACKEND_ESTADO.md); el 401 antiguo ya no es un bloqueo.

## Diagnostico y politica

El filtro servlet `JwtFilter` esta registrado una vez, mediante `FilterRegistrationBean`
en `LudusApiApplication`, sobre `/ludus/*`. No forma parte de `SecurityFilterChain`;
por eso `permitAll()` no evita su validacion. No se cambio esa arquitectura.
El filtro lanzaba `ServletException` ante ausencia o invalidez del JWT y el contenedor
la convertia en 500. Ahora responde 401 JSON con `WWW-Authenticate: Bearer`, sin
ejecutar el controlador ni registrar identidad o tokens. El manejo de errores se
limita a la validacion del token, sin capturar fallos de los controladores.

Se conserva la busqueda protegida por JWT LUDUS. Solo POST `/ludus/login`, POST
`/ludus/login/`, POST `/ludus/usuario/` y OPTIONS omiten esa validacion. Las rutas
se comparan exactamente; un POST en una subruta de usuarios no equivale a registro.
No se agregaron roles ni reglas de propiedad; la autorizacion de resenias requiere
una etapa posterior antes de migrar la identidad del navegador al backend.

Hay dos credenciales independientes: JWT LUDUS del cliente al backend y token
de aplicacion Twitch del backend a IGDB. `IGDB_ACCESS_TOKEN` debe contener solo
el valor del token, sin `Bearer`, y corresponder al `IGDB_CLIENT_ID` configurado.

## Verificaciones del 5 de noviembre de 2026

- Estado inicial: compilacion correcta; `contextLoads` fallaba por falta de
  `IGDB_CLIENT_ID` en el proceso Gradle (las variables del IDE no se heredan).
- Pruebas automaticas usan H2 en memoria y credenciales ficticias de IGDB;
  no requieren MySQL ni conexion externa.
- Verificacion final: `gradlew.bat test bootJar` correcto, 15 pruebas aprobadas,
  cero fallos y una prueba real optativa omitida. La prueba real se ejecuto por
  separado y fallo al esperar 200 y recibir 502, como se detalla abajo.
- MockMvc verifica autenticacion, errores JWT, escrituras protegidas y CORS.
  Una prueba con contexto completo verifica el registro real del filtro y el
  acceso al controlador con JWT valido y servicio IGDB simulado.
- MockRestServiceServer verifica POST APICalypse, escape, Unicode, validacion,
  respuesta vacia de busqueda y errores externos 400/401/403/429/500 y timeout.
- Prueba real contra la aplicacion local: sin JWT devuelve 401; con JWT LUDUS
  valido devuelve 502. Un logpoint no suspensivo en el catch del cliente confirmo
  que **IGDB devuelve HTTP 401**. No se inspeccionaron ni imprimieron cuerpos ni
  cabeceras externas. Los logpoints se retiraron al terminar.
- Los arranques de comprobacion usaron `--spring.jpa.hibernate.ddl-auto=none`;
  no se ejecutaron migraciones, borrados ni escrituras de juegos/resenias.

## Contrato temporal de la etapa 1

`GET /ludus/igdb/buscar?nombre=Elden%20Ring` requiere JWT LUDUS y devuelve JSON.
Se conserva temporalmente la lista de IGDB como texto JSON; los DTO quedan para
la etapa siguiente, una vez verificada la conexion real.

- JWT ausente/invalido: 401.
- Nombre vacio, mayor de 100 caracteres o con controles: 400.
- IGDB 400/401/403/5xx: 502 sin reenviar detalles sensibles.
- IGDB 429: 503 (limite del proveedor, no fallo del JWT LUDUS).
- Fallo de acceso/timeout externo: 504.
- Conexion: 3 segundos; lectura: 5 segundos. Sin reintentos automaticos.
- Los logs externos contienen unicamente el codigo HTTP, nunca el cuerpo.
- Las consultas conservan Unicode mediante UTF-8 y escapan barras/comillas.

## Ejecutar pruebas

En PowerShell, con Java 21 y una cache de Gradle accesible:

```powershell
.\gradlew.bat test bootJar
```

La prueba real es optativa y se omite en CI. Primero iniciar `LudusApiApplication`
con las variables de IGDB vigentes en el IDE y estos argumentos:

```text
--server.port=8081 --spring.jpa.hibernate.ddl-auto=none
```

Luego ejecutar:

```powershell
$env:LUDUS_LIVE_URL='http://localhost:8081'
.\gradlew.bat test --tests com.ludus.api.LiveIgdbTest --rerun-tasks
Remove-Item Env:LUDUS_LIVE_URL
```

La prueba firma un JWT de comprobacion solo en memoria y hace dos GET; no crea
usuarios ni juegos y nunca imprime ese token. Debe obtener 401 y luego 200 con
Elden Ring. Un 502 mantiene esta prueba en rojo y no prueba conexion exitosa.
En Postman usar Authorization -> Bearer Token con un JWT LUDUS obtenido del login;
las credenciales externas permanecen exclusivamente en la Run Configuration.

## Bloqueo y siguientes incrementos

Actualizar/rotar las credenciales de Twitch y repetir la prueba real. Un HTTP 401
no permite distinguir por si solo token expirado, token incorrecto o Client ID
incompatible. No se cambiaron ni se intentaron renovar credenciales automaticamente.
Los tokens compartidos previamente deben rotarse; no pegarlos en el chat.

El plan original exige verificar la etapa anterior antes de continuar. Quedan:

1. Cliente IGDB dedicado, DTO externo y DTO LUDUS, paginacion, detalle por ID,
   mapeo de generos/plataformas/portadas y cache/control de frecuencia.
2. Persistencia bajo demanda con id externo unico, recuperacion desde IGDB y
   transacciones independientes para resolver carreras. Probar concurrencia,
   rollback y FK. Ninguna busqueda debe guardar automaticamente sus resultados.
3. Revisar el esquema real y preparar una migracion aprobada para descripciones
   TEXT, IDs externos y campos opcionales/multiples. No se modifico `Juego` ni
   se confio en `ddl-auto=update` para aplicar estos cambios.
4. Frontend localizado en `../Proyecto-integrador`: esta fuera de este backend
   y de sus directorios de escritura autorizados. Integrar busqueda, detalle,
   debounce, paginacion y estados conservando el CSS.
5. El modelo real de resenias es `Publicacion`, tabla `resenias`, con FK a
   `Juego.id_juego`. Migrar localStorage por separado, sin perder datos y con
   identidad/propiedad derivadas del JWT. No existe una entidad `Resenia`.
6. Renovacion controlada del token Twitch con client credentials cuando las
   pruebas base reales funcionen.

No se limpio el juego de prueba, no se alteraron FK y no se deshabilitaron
restricciones. Los cambios preexistentes de usuarios, DTO y pruebas eliminadas
se conservaron.

Referencia del contrato HTTP externo: [documentacion oficial de IGDB](https://api-docs.igdb.com/).
