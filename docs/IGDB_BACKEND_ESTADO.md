# Estado del backend IGDB, juegos, reseñas y perfiles

Entrega: 5 de noviembre de 2026. Alcance: backend, pruebas y documentación.
No se modificó HTML/JS/CSS ni se insertaron, borraron o migraron datos de la base MySQL compartida.
No se crearon commits ni se sobrescribieron las eliminaciones de DTO/tests que ya estaban staged.

## Resultado implementado

- Cliente IGDB dedicado con DTO externo, errores saneados, timeout de conexión 3 s / lectura 5 s.
- OAuth Twitch client credentials con prioridad sobre token estático, caché por caducidad,
  renovación serializada, cooldown de fallos y una sola repetición tras 401.
- Límite local de 4 requests/s y 8 en vuelo por instancia, cooldown tras 429 y caché
  de hasta 200 consultas externas por 60 s, sin dormir hilos de producción.
- Búsqueda Unicode/escapada, exploración, filtros combinados de género/plataforma,
  ordenamiento permitido, lookups paginados y detalle externo sin exigir fila local.
- DTO Game con listas de géneros/plataformas, imágenes HTTPS, descripción completa,
  fecha/rating externo/slug/screenshots y rating LUDUS separado.
- Resolución explícita por id externo: UNIQUE(id_api), transacción de inserción independiente
  y recuperación del ganador al terminar una carrera de inserción. Repetir no duplica el juego.
- Publicacion sigue representando resenias; no se creó una entidad paralela.
  Publicar por IGDB ID resuelve el juego y guarda FK al id_juego interno y al usuario del JWT.
- Lectura paginada de reseñas por juego, autor, usuario actual y detalle; edición/borrado propios.
  Una sola fuente MySQL alimenta ficha y perfil; DTO sin email, teléfono ni contraseña.
- Perfiles públicos para usuarios autenticados y edición propia de nombre/biografía.
  Estadísticas SQL de cantidad de reseñas, juegos distintos, media emitida y likes recibidos.
- El JWT verificado expone su sujeto como atributo interno de request para resolver al usuario.
  Se conserva el registro servlet único de JwtFilter y sus 401; no se reescribió login/JWT.
- Endpoints heredados adaptados explícitamente: GET local de juegos conserva array/campos,
  POST local verifica IGDB, PUT/DELETE locales no admiten alterar catálogo arbitrariamente.
  Reseñas heredadas usan DTO/página y autor del JWT. Login incorrecto ahora devuelve 401.
- Regresión de auxiliares: comentarios/interacciones usan autor del JWT y descartan id entrante;
  borrado de interacciones/seguimientos y cambios de usuario verifican propiedad.
  Registro de usuario descarta id_usuario del payload y valida null antes de isBlank.
- Contraseña de Usuario sigue siendo solo escritura; también se eliminó del toString.
  La contraseña MySQL sale de application.properties y se configura con DB_PASSWORD.
- ddl-auto=none por defecto. SQL manual versionado, sin ejecutarlo sobre MySQL.

## Inspección y cambios preexistentes conservados

Baseline Git ya contenía cambios en build.gradle, JwtFilter, UsuarioController y application.properties;
eliminación de config/SecurityConfig de la corrección anterior; DTO CambioPasswordDto,
RegistroUsuarioDto y UsuarioServiceTest eliminados por trabajo previo; archivos IGDB/tests/docs sin trackear.
No se restauraron ni se agregaron al índice esas eliminaciones, ni se descartaron cambios ajenos.
Las ampliaciones a usuarios/auxiliares se limitaron a autenticación, propiedad y protección de datos,
con pruebas de regresión, porque las relaciones de reseñas los atraviesan.

### Archivos nuevos de producción

- igdb/HttpClients.java, igdb/IgdbGame.java, igdb/IgdbClient.java, igdb/TwitchTokenProvider.java.
- dtos/CatalogDtos.java, dtos/ReviewDtos.java.
- security/CurrentUser.java.
- services/GameResolver.java.
- controllers/ApiErrors.java, controllers/PerfilController.java.
- application-example.properties.

### Archivos existentes o previamente sin trackear ampliados

- services/IgdbService.java, services/JuegoService.java, services/PublicacionService.java,
  services/UsuarioService.java.
- controllers/IgdbController.java, controllers/JuegoController.java (reutilizado sin cambiar código),
  controllers/PublicacionController.java, controllers/UsuarioController.java,
  controllers/LoginController.java, controllers/ComentarioController.java,
  controllers/InteraccionController.java, controllers/SeguidorController.java.
- models/Juego.java, models/Publicacion.java, models/Usuario.java.
- repositories/JuegoRepository.java, repositories/PublicacionRepository.java.
- security/JwtFilter.java, application.properties.
- build.gradle conserva la limpieza previa de dependencias repetidas y H2 de pruebas.
- Tests: IgdbSecurityIntegrationTest, JwtFilterTest, LiveIgdbTest, ReviewsIntegrationTest,
  services/IgdbServiceTest, igdb/IgdbClientTest, igdb/TwitchTokenProviderTest;
  application.properties de test conserva H2 aislado.

Documentos: IGDB_API_CONTRATO.md, IGDB_FRONTEND_GUIA.md, este estado,
sql/V001_igdb_catalogo_resenias.sql. IGDB_ESTADO.md queda como registro histórico con aviso de actualización.

## Verificaciones ejecutadas y evidencia real

Baseline: gradlew.bat test correcto (suite previa, up-to-date).
Durante cada incremento se compiló y ejecutó la suite, corrigiendo errores de compilación/mocks detectados.

Última verificación: **gradlew.bat test bootJar, BUILD SUCCESSFUL**.
Resultado actualizado de XML JUnit: **42 tests descubiertos, 41 aprobados, 0 fallos, 1 omitido**.
El omitido es LiveIgdbTest, optativo para la suite aislada.
JAR generado: build/libs/ludus-api-0.0.1-SNAPSHOT.jar.
git diff --check no detectó errores de whitespace (Git avisa normalización CRLF de algunos archivos).

Cobertura comprobada:

- Ausencia de JWT, formato inválido, expiración, firma incorrecta, JWT válido, escrituras protegidas
  y preflight CORS. Contexto completo confirma que el filtro real antecede al controlador.
- Login y registro reales con H2, password codificado, credenciales erróneas y registro con id falsificado.
- OAuth: formulario client credentials, prioridad de secret, caché/expiración, renovación concurrente
  compartida entre 8 callers, fallback estático, errores saneados y cooldown.
- Cliente HTTP: headers de IGDB, UTF-8, cuerpo inválido, errores 400/401/403/500,
  un solo retry tras 401, 429/Retry-After, timeout, límite local y expiración de caché.
- Mapeo de texto largo, listas múltiples, datos ausentes, imágenes seguras, paginación hasNext
  sin totales inventados, validación/escape de consultas y filtros presentes en APICalypse.
- Buscar/detallar/lookups no guardan filas. Error externo/404 no crea juego ni reseña.
- Seis resoluciones concurrentes más una repetida producen un único juego local en H2,
  con UNIQUE real y texto >255 caracteres persistido.
- POST de reseña con autor falso conserva usuario del JWT; la MISMA idResenia aparece en
  GET de juego y GET de perfil/me, y otro usuario puede verla desde la ficha.
- Otro usuario recibe 403 al editar/borrar; edición/borrado propios se reflejan en ambas vistas;
  promedio/cantidad de juego y estadísticas de perfil provienen de la misma base.
- Regla de una reseña: repetir POST por ruta IGDB o local devuelve 409 sin alterar la original;
  editar/borrar propios y volver a publicar después de borrar funciona. Usuarios distintos
  pueden reseñar el mismo juego y un usuario puede reseñar juegos distintos.
  Seis POST concurrentes producen un 201 y cinco 409; un INSERT directo también prueba
  la restricción UNIQUE sin depender de la comprobación del servicio, en H2 aislado.
- Borrado propio con comentarios/likes dependientes, transacción con rollback y FK real;
  no se deshabilitaron constraints.
- Comentarios, interacciones y seguidores siguen funcionando y rechazan suplantación.
- No aparecen contraseñas en DTO de reseñas/perfiles ni respuesta de registro.

### Smoke test real (solo lectura)

El encargo recibido registra evidencia inicial del usuario: Postman ya devolvía
200 para buscar Elden Ring, con JWT LUDUS y credenciales Twitch vigentes.
El 401 descrito en IGDB_ESTADO.md era histórico y no se usó como bloqueo actual.

Se hizo una nueva prueba real contra la Run Configuration del IDE, usando su token
estático vigente (sin imprimir credenciales), puerto temporal y ddl-auto=none.
Primera ejecución: búsqueda y detalle respondieron 200; exploración con filtros respondió 502.
Se corrigió una sintaxis de sort múltiple que IGDB no aceptó por ordenamiento simple soportado.
**Segunda ejecución de LiveIgdbTest: BUILD SUCCESSFUL**, comprobando:

1. Búsqueda sin JWT -> 401.
2. Búsqueda con JWT -> 200 y Elden Ring.
3. GET /ludus/igdb/juegos/119133 -> 200.
4. GET /ludus/igdb/juegos?genreId=12&platformId=6&size=2 -> 200.

El proceso temporal se detuvo después. No se llamó a resolver/publicar/editar/borrar contra MySQL.
No se hicieron pruebas reales de OAuth client credentials porque el IDE solo tenía Client ID
y access token; OAuth se verificó con proveedor simulado y concurrencia.
Las últimas ampliaciones de estadísticas/seguridad se validaron con H2; no se revalidaron
escrituras en MySQL ni se aplicó la migración.

## Decisiones y límites explícitos

- Todas las lecturas nuevas requieren JWT; “público” significa datos públicos visibles a otro
  usuario autenticado. No se abrieron endpoints anónimos de escritura.
- Regla actual: máximo una reseña por usuario/juego, con UNIQUE(id_usuario,id_juego).
  Ambas rutas de creación verifican duplicados y responden 409; la restricción cubre carreras.
  Editar/eliminar propios sigue permitido, y eliminar libera la posibilidad de publicar otra vez.
  POST no es un upsert; resolver juego conserva su semántica idempotente.
- La resolución del juego usa una transacción independiente de la escritura de la reseña.
  Un fallo posterior puede dejar un juego IGDB verificado sin reseñas; no deja FK rota ni reseña parcial.
  Se valida texto/calificación y autor antes de resolver para evitar altas por payload inválido.
- Al borrar una reseña propia se borran sus comentarios/interacciones, de forma explícita y
  transaccional, tras autorización. No se alteraron FK ni se agregaron cascadas de esquema.
  Ese comportamiento existe para acciones futuras del usuario, no se ejecutó en producción.
- Géneros/plataformas locales se guardan como nombres separados por coma por compatibilidad.
  Las listas con IDs se conservan en DTO externo y los filtros se resuelven en IGDB,
  no intentando reconstruir IDs desde esas cadenas.
- No se guarda todo IGDB ni los resultados de búsquedas. Datos externos y lookups tienen caché
  limitada; las reseñas y sus agregados no se cachean.
- Por página de catálogo se consultan localGameId y agregados de cada juego; el coste está
  acotado por size<=50, aunque un catálogo de alto tráfico requeriría agregación SQL en lote.
- Cuota/caché/token son por instancia. Para varios procesos con mismo Client ID se necesita
  coordinación compartida; no se añadió infraestructura distribuida.
- Descripciones y reseñas usan TEXT, sin truncar silenciosamente. MySQL TEXT tiene límite en bytes;
  una respuesta extraordinaria que exceda ese límite falla al guardar (409), no se recorta.
- No se incluyen publisher/developer/web ni uploads de perfil: son campos opcionales no modelados.
- Los GET heredados de usuarios/auxiliares mantienen entidades y arrays sin paginar;
  el nuevo frontend debe usar Profile/Review para datos públicos. No se presentó esto como
  una modernización total de todas las entidades del proyecto.
- La clave JWT estática heredada no se cambió para no invalidar sesiones silenciosamente.
  Antes de despliegue público, externalizarla y planificar rotación de las sesiones existentes.
  No se copió su valor a documentación, logs ni nuevos archivos.

## Migración MySQL: preparada, NO ejecutada

Archivo: [sql/V001_igdb_catalogo_resenias.sql](sql/V001_igdb_catalogo_resenias.sql).
No hay Flyway/Liquibase en este proyecto: el archivo es versionado de aplicación **manual**,
no se ejecuta automáticamente. ddl-auto=none evita alteraciones implícitas.

Impacto previsto: id_api BIGINT; nombre_juego/categoria/plataforma/descripcion/imagen_url TEXT;
campos opcionales nullable; texto_resenia TEXT. Se conservan id_juego y todas sus FK.
El índice UNIQUE(id_api) existente debe mantenerse. El SQL incluye comprobaciones de índices,
duplicados y null; si el índice de juego falta, su creación está comentada y requiere revisión previa.
También incluye preflight de duplicados por autor/juego e índices de resenias y el ADD CONSTRAINT
uk_resenias_usuario_juego UNIQUE(id_usuario,id_juego). Si hay duplicados, detener todo el script
sin borrar filas. Si ya existe un UNIQUE equivalente, omitir ese ADD; revisar colisiones de nombre.
No se inspeccionó SHOW CREATE TABLE de la base compartida en esta ejecución; hay que contrastar
el script con su esquema real antes de aplicar. La conexión de smoke solo leyó datos de catálogo/agregados.

Procedimiento para quien administra MySQL:

1. Obtener aprobación y ventana de mantenimiento; preparar backup completo consistente de juegos,
   resenias, usuarios, comentarios, interacciones y seguidores (o backup completo de la BD).
   Verificar restauración en una BD aislada. No poner contraseñas en comandos/versionado.
2. Ejecutar solo los SELECT/SHOW de preflight del archivo; revisar tipos actuales, índices y FK.
   Detenerse ante duplicados/null, columnas mayores al tipo previsto o divergencias.
3. Aplicar primero ALTER en la copia aislada y probar resolver + reseñas con datos de prueba.
4. Con aprobación, aplicar ALTER a la base de destino. MySQL DDL hace COMMIT implícito:
   no prometer rollback transaccional. No usar FOREIGN_KEY_CHECKS=0.
5. Verificar UNIQUE(id_api), FK a id_juego y lectura de registros históricos.
   No borrar ni corregir automáticamente el juego ficticio ni sus reseñas.
6. Ante incidencia, restaurar en base aparte y comparar antes de sustituir datos;
   no hacer downgrade de TEXT/BIGINT si hay valores nuevos que no caben.

**Antes de habilitar escrituras con este backend hay que aplicar/verificar la migración.**
Dejar el schema VARCHAR(255)/NOT NULL anterior puede producir 409 con texto largo o imágenes nulas.
La protección de concurrencia de una reseña por usuario/juego requiere también el UNIQUE
compuesto en MySQL: la comprobación previa del servicio no sustituye ese índice.
No se ejecutó esta migración ni se eliminaron reseñas históricas duplicadas.

## Configuración exacta y comandos

application.properties ahora consume:

- DB_URL (default jdbc:mysql://localhost:3306/ludus).
- DB_USERNAME (default root).
- **DB_PASSWORD** (sin password incrustado; configurar antes del próximo arranque).
- IGDB_CLIENT_ID.
- IGDB_CLIENT_SECRET: secret nuevo/rotado; activa renovación OAuth y tiene prioridad.
- IGDB_ACCESS_TOKEN: opción transitoria si no hay secret, solo valor sin “Bearer”.
- La configuración de ejemplo está en src/main/resources/application-example.properties.

En IntelliJ: Run -> Edit Configurations -> LudusApiApplication -> Environment variables.
Añadir DB_PASSWORD y las variables de IGDB mediante el editor local seguro. No compartir capturas.
El secret históricamente expuesto debe rotarse, no reutilizarse.
No hace falta configurar IGDB para pruebas aisladas: usan valores ficticios y MockRestServiceServer.
Spring Boot no carga .env por sí solo; si se usa un archivo local ignorado, un lanzador debe exportarlo.

En PowerShell, tests y JAR (sin secretos ni MySQL):

```powershell
$env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE '.gradle'
.\gradlew.bat test
.\gradlew.bat bootJar
```

En CI usar Java 21 y los mismos targets. test/resources/application.properties apunta a H2
mem:ludus_test en modo MySQL y create-drop **solo allí**. No reemplazarlo por URL de producción.
Las variables del IDE no se heredan automáticamente a una terminal Gradle externa.

Prueba real opcional, únicamente con aplicación ya iniciada en otro puerto y DDL desactivado:

```text
--server.port=8081 --spring.jpa.hibernate.ddl-auto=none --spring.jpa.show-sql=false
```

```powershell
$env:LUDUS_LIVE_URL='http://localhost:8081'
.\gradlew.bat test --tests com.ludus.api.LiveIgdbTest --rerun-tasks
Remove-Item Env:LUDUS_LIVE_URL
```

LiveIgdbTest firma en memoria un JWT de comprobación y solo hace GET.
No crea usuarios ni imprime el JWT, no toca filas y no requiere un usuario real para catálogo.

## Pendientes antes de desplegar e integrar UI

1. Aprobar y aplicar/verificar SQL en base de pruebas y después destino; no se hicieron
   escrituras ni ensayos de concurrencia con MySQL real (sí con UNIQUE/FK de H2).
2. Configurar DB_PASSWORD en el IDE/entorno y credenciales Twitch rotadas para OAuth automático.
   Fallback estático funciona, pero caduca y requiere renovación manual.
3. Hardening de clave JWT heredada y coordinación de cuotas si hay varias instancias.
4. Integrar el frontend siguiendo [IGDB_FRONTEND_GUIA.md](IGDB_FRONTEND_GUIA.md),
   sustituyendo localStorage y el intento de llamada a /ludus/resenia/ inexistente.
5. Importación opcional y consentida de reseñas de navegador, con respaldo y selección de juegos;
   no se importaron ni borraron datos locales en esta entrega.
6. Los endpoints/formatos finales y cambios de compatibilidad están en
   [IGDB_API_CONTRATO.md](IGDB_API_CONTRATO.md).

Fuentes: [API IGDB](https://api-docs.igdb.com/) y
[OAuth client credentials de Twitch](https://dev.twitch.tv/docs/authentication/getting-tokens-oauth/#client-credentials-grant-flow).
