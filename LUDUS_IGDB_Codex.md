# Tarea para Codex — Integración IGDB ↔ LUDUS

## Rol y objetivo
Actúa como desarrollador Java/Spring Boot responsable de implementar y probar la integración de **IGDB (Twitch)** con **LUDUS**, una plataforma social de reseñas de videojuegos. Trabaja directamente sobre el repositorio abierto en IntelliJ; **inspecciona el código real antes de editar**. El resultado debe funcionar de extremo a extremo y mantener intactas las funcionalidades existentes de usuarios, autenticación, reseñas, comentarios, interacciones y seguidores.

**Objetivo funcional:** permitir buscar juegos en IGDB desde el backend de LUDUS; mostrar resultados en el frontend; almacenar de forma persistente en MySQL solo los juegos de IGDB que sean necesarios para funcionalidades locales (por ejemplo, crear una reseña), sin duplicados; asociar todas las reseñas al `id_juego` interno de LUDUS, no al identificador de IGDB.

## Contexto conocido del proyecto
- Stack: Java 21, Spring Boot (la versión observada fue 4.1.2), Gradle, Spring Data JPA, MySQL, Spring Security/JWT, JavaScript y Bootstrap.
- Paquete base: `com.ludus.api`.
- Tabla `juegos` vista en el modelo y esquema: `id_juego` PK autoincremental; `id_api` identificador externo único y no nulo; `nombre_juego`, `categoria`, `plataforma`, `descripcion`, `imagen_url`.
- `resenias.id_juego` es FK a `juegos.id_juego`. Existen también tablas de `usuarios`, `comentarios`, `interacciones`, `seguidores`.
- Se pretende partir de **una tabla de juegos vacía y poblarla exclusivamente con registros obtenidos de IGDB**. Actualmente hay un juego de prueba (Elden Ring, `id_juego=1`, `id_api=1001` ficticio) y reseñas que lo referencian. MySQL rechazó borrarlo (`Error 1451`) debido a la FK. **No eliminar datos ni alterar restricciones sin verificación, respaldo y aprobación explícita.** Si se necesita limpiar datos de desarrollo, ofrecer una migración o script seguro, reversible cuando sea posible, que considere las relaciones dependientes (reseñas, comentarios, likes); no usar `SET FOREIGN_KEY_CHECKS=0` como atajo.
- Frontend: `juegos.html` carga `JS/api.js` y `JS/catalogoJuegos.js`. La función `consumeApi(endpoint, {method='GET', body, auth=true})` tiene base URL `http://localhost:8080`, adjunta `Authorization: Bearer <JWT de LUDUS>` por defecto y maneja respuestas JSON/errores.
- `catalogoJuegos.js` actualmente carga todos los juegos desde `GET /ludus/juego/`, mapea `id_juego` a `id` string, renderiza tarjetas y una ficha dinámica con `juegos.html?id=...`, y filtra **solo en el navegador**. Importante: **las reseñas de esa página se guardan en `localStorage` (`ludus_catalogo_resenias`)**, no en MySQL; esto debe migrarse al backend de reseñas como etapa separada, conservando las reglas de identidad, autorización y los datos de prueba según política acordada.

## Archivos existentes y estado conocido
Antes de editar, inspeccionar los archivos actuales (pueden haber cambiado desde esta descripción):
1. `models/Juego.java`: entidad JPA con `id_juego` autogenerado, `id_api` único y obligatorio, cadenas `nombre_juego`, `categoria`, `plataforma`, `descripcion`, `imagen_url`. Constructor vacío y getters/setters.
2. `repositories/JuegoRepository.java`: extiende `JpaRepository<Juego, Integer>`; no tiene aún `findByIdApi`/`existsByIdApi`.
3. `services/JuegoService.java`: CRUD con `findAll`, `findById`, `save`, `delete`, `update`. Validación de `addJuego` con `.isBlank()` sin verificación previa de null; puede devolver objeto sin persistir; `getJuego` muestra por error texto «usuario»; uso de `findAll` sin paginación.
4. `controllers/JuegoController.java`: base `/ludus/juego/`, endpoints GET listado, GET id, POST, PUT id y DELETE id. Revisar autorización antes de permitir escrituras públicas.
5. `services/IgdbService.java`: nuevo servicio con `RestClient` hacia `https://api.igdb.com/v4`; lee `@Value("${igdb.client-id}")`, `@Value("${igdb.access-token}")`; configura headers `Client-ID` y `Authorization: Bearer `; `buscarJuegos(nombre)` envía POST a `/games` con cuerpo APICalypse:
   ```text
   search "<nombre>";
   fields name,summary,genres.name,platforms.name,cover.image_id;
   limit 10;
   ```
   Devuelve la respuesta externa como `String`. Hay validación muy restrictiva del nombre; revisar escape correcto de comillas, barras y caracteres especiales, longitudes y errores upstream.
6. `controllers/IgdbController.java`: `@RequestMapping("/ludus/igdb")`, `GET /buscar?nombre=...` delega a `IgdbService.buscarJuegos`.
7. `security/SecurityConfig.java`: configuración observada con `.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().anyRequest().permitAll()`; CORS en dos lugares con orígenes locales `localhost:5502` y `127.0.0.1:5502`. **Este `permitAll` NO impide que un filtro JWT personalizado rechace peticiones antes del controlador**. Revisar orden y registro real del filtro, rutas públicas y protegidas.
8. `security/JwtFilter.java` y servicio JWT: **inspeccionar** (aún no se han proporcionado en este hilo). No cambiar la autenticación indiscriminadamente.
9. `build.gradle`: contiene o contenía duplicados de `spring-boot-starter-data-jpa` y `mysql-connector-j`; limpiar solo si corresponde al estado real y no afecta compilación.
10. `application.properties`: referencias `igdb.client-id=${IGDB_CLIENT_ID}` y `igdb.access-token=${IGDB_ACCESS_TOKEN}`. Variables configuradas en la Run Configuration de IntelliJ; **nunca imprimir ni versionar valores**.

## Incidente actual: error 500 ya diagnosticado
La aplicación inicialmente no arrancaba por falta de la variable `IGDB_CLIENT_ID`; se configuraron variables de entorno y ahora **arranca**.

La petición de Postman:
`GET http://localhost:8080/ludus/igdb/buscar?nombre=Elden%20Ring`
responde `500 Internal Server Error`.

**Traza raíz de IntelliJ:**
```text
jakarta.servlet.ServletException: Token invalido. Encabezado nulo o no contiene {Bearer TOKEN}
    at com.ludus.api.security.JwtFilter.obtenerToken(JwtFilter.java:61)
    at com.ludus.api.security.JwtFilter.doFilter(JwtFilter.java:34)
```
**Diagnóstico confirmado:** la petición es rechazada por el filtro JWT de LUDUS, **antes de llegar a `IgdbController`**. No hay evidencia aún de error de IGDB ni de MySQL. El 500 se produce por el tratamiento incorrecto de falta de autenticación. Distinguir dos tokens independientes:
- **JWT LUDUS**: cliente (browser/Postman) → API LUDUS.
- **Access token Twitch/IGDB**: API LUDUS → IGDB. El header externo lo construye `IgdbService` con `Bearer `; el valor de `IGDB_ACCESS_TOKEN` NO debe incluir `Bearer`.

**Prioridad inmediata:** revisar `JwtFilter`, su registro en SecurityFilterChain y toda la seguridad. Hacer que las rutas que se decidan públicas (por ejemplo, GET de búsqueda/catalogo/ficha) permitan solicitudes sin JWT, y las operaciones protegidas requieran JWT válido, emitiendo códigos correctos (`401` para credenciales ausentes/invalidas en rutas protegidas, `403` para permisos insuficientes). Evitar un `500` para fallos esperados de autenticación. No omitir el filtro para todas las rutas ni quitar seguridad a POST/PUT/DELETE. Verificar compatibilidad con el login y frontend existentes. Si el producto decide que las búsquedas solo estarán disponibles tras login, mantenerlas protegidas y probar con un JWT LUDUS válido; documentar explícitamente esa decisión.

## Diseño de integración requerido
1. **Separación de responsabilidades**: cliente IGDB dedicado (configuración HTTP, credenciales, timeouts, gestión de errores) + servicio de catálogo/persistencia + controladores/DTOs. No conectar el navegador directamente con IGDB ni exponer Client Secret/tokens externos.
2. **Búsqueda externa**: paginación y límites razonables; DTOs tipados para respuesta (evitar devolver `String` de manera definitiva), portada construida desde `cover.image_id` usando la convención de imágenes de IGDB; géneros y plataformas múltiples; campos faltantes/null; títulos internacionales; errores HTTP externos y tiempo de espera. Respuestas estables hacia JS.
3. **Persistencia bajo demanda**: **no copiar todo IGDB ni guardar cada búsqueda**. Al elegir un juego para dejar una reseña o usarlo localmente, resolver por `id_api`/IGDB ID y hacer un `find-or-create` transaccional; conservar PK autoincremental `id_juego` para FK de reseñas. Asegurar `UNIQUE(id_api)` e implementar comportamiento idempotente y protección frente a concurrencia (dos peticiones simultáneas). No aceptar libremente desde el cliente datos de juego falsificados: recuperar/verificar desde IGDB o usar reglas de confianza explícitas.
4. **Esquema**: dado que el catálogo local será exclusivamente IGDB, `id_api` puede seguir obligatorio y único. Revisar si `Integer` alcanza; usar tipo adecuado para IDs externos. `descripcion` puede superar VARCHAR(255): definir tipo MySQL apropiado (`TEXT`/equivalente) y migración segura. Para `categoria` y `plataforma` existen valores múltiples: decidir si usar tablas relacionales, JSON o representación simplificada temporal, considerando filtros y mantenibilidad. Manejar campos no disponibles sin insertar valores inventados. Usar migraciones versionadas si el proyecto ya tiene Flyway/Liquibase; evitar confiar ciegamente en `ddl-auto=update` en ambientes compartidos.
5. **Frontend**: conservar interfaz y CSS existente. Cambiar búsqueda limitada a `findAll()`/filtrado local por un endpoint escalable; distinguir resultados externos (IGDB ID) de juegos ya registrados (ID local). La vista de detalle debe funcionar sin depender de que el juego esté previamente en la lista cargada. Paginación, debounce, estados de carga/errores/vacío y URLs seguras. Mantener JWT LUDUS solo donde corresponda.
6. **Reseñas**: migrar posteriormente el uso de `localStorage` a los endpoints reales de `resenias` y obtener la identidad del usuario del backend/JWT; no confiar en un `userEmail` proporcionado por el navegador. Revisar modelo real de `Resenia` antes de implementar; evitar perder reseñas locales o crear migraciones automáticas inesperadas.
7. **Gestión del token Twitch**: fase inicial puede usar `IGDB_ACCESS_TOKEN` actual vía entorno, pero documentar caducidad; proponer/implementar de manera controlada mecanismo de client credentials, caché y renovación segura con `IGDB_CLIENT_SECRET` cuando haya pruebas base estables. Nunca incluir secretos en ejemplos, registros o commits.
8. **Límites y robustez**: IGDB tiene cuotas y posibles interrupciones; aplicar timeout, manejo de 400/401/403/429/5xx, control de frecuencia, caching apropiado y respuestas no sensibles.

## Plan de trabajo y pruebas (ejecutar incrementalmente)
### Etapa 0 — Inspección y diagnóstico
- Revisar Git status y estructura; no sobrescribir cambios de compañeros.
- Ejecutar/build/test existentes, registrar estado inicial.
- Localizar `JwtFilter`, verificar si está registrado una o dos veces, cuándo exige encabezado, cómo se integra con Spring Security, qué rutas deberían ser públicas, y su tratamiento de errores.
- Explicar causa exacta del error 500 y proponer corrección **mínima** antes de cambios de arquitectura.

### Etapa 1 — Autenticación y conexión IGDB
- Corregir filtro/config para evitar 500 por JWT ausente, según política de endpoints elegida.
- Pruebas de integración de seguridad con MockMvc: GET público sin JWT si así se decide; endpoint protegido sin JWT → 401; JWT inválido → 401; JWT válido → acceso; OPTIONS/CORS.
- Hacer la prueba local de `GET /ludus/igdb/buscar?nombre=Elden%20Ring` y verificar que llega a IGDB. No declarar conexión exitosa solo porque el backend arranca.
- Mockear upstream para pruebas automatizadas; no necesitar credenciales reales en CI.
- Verificar respuesta 200 con datos y manejo de 401/429/5xx/timeouts externos sin secretos ni 500 genérico evitables.

### Etapa 2 — DTO y transformación
- Definir DTOs de respuesta IGDB y DTOs de API LUDUS (campos explícitos y estables).
- Pruebas unitarias de mapeo: géneros/plataformas múltiples, portada faltante, summary nulo/largo, resultado vacío, caracteres Unicode y búsqueda malformada.

### Etapa 3 — Persistencia MySQL
- Crear búsqueda por ID externo en repositorio, lógica `find-or-create` y restricciones de unicidad.
- Probar: primer guardado, segunda consulta sin duplicación, dos solicitudes concurrentes, FK con reseñas, fallo externo, y rollback ante error.
- Evitar borrados en cascada sin decisión explícita; no limpiar datos en base de uso compartido. Preparar script de limpieza SOLO para entorno de prueba y tras aprobación.

### Etapa 4 — Frontend y reseñas
- Probar de extremo a extremo búsqueda, selección, guardado y detalle.
- Revisar compatibilidad del esquema JSON con `catalogoJuegos.js`.
- Después integrar reseñas reales en MySQL con pruebas de autenticación, autorización y asociacion a `id_juego`.

## Criterios de aceptación
- `./gradlew test` y compilación funcionan (o reportar con precisión qué falla y por qué).
- Búsqueda de IGDB accesible de acuerdo con la política JWT explícita; **no falla con 500 por falta de encabezado**.
- No se filtran Client Secret, Access Token ni JWT en código, logs o respuestas.
- Búsqueda externa responde DTO estable; errores del proveedor se traducen a estados adecuados.
- Seleccionar un juego IGDB lo crea en MySQL **una sola vez**; reseñas usan el ID local; historial de usuarios/reseñas no se corrompe.
- La búsqueda no inserta automáticamente todos los resultados; filtros y detalles siguen funcionando.
- Se entregan pruebas reproducibles y ejemplos de Postman/cURL sin credenciales reales.

## Instrucciones de ejecución para Codex
1. **Empieza ahora por etapa 0 y etapa 1**: inspecciona los archivos reales, corrige el error del filtro JWT con el mínimo cambio seguro y prueba la solicitud IGDB. Luego avanza a etapas posteriores solo si las verificaciones anteriores pasan y el alcance es claro.
2. Antes de cambios destructivos, cambios de política de permisos o migraciones de esquema con datos existentes, explica el impacto y solicita aprobación. No fuerces borrado de datos de prueba relacionados con FK.
3. Reutiliza convenciones del proyecto; no reescribas ni reorganices todo el backend. Ajusta el código de producción y añade pruebas unitarias/de integración útiles.
4. No afirmes que ejecutaste pruebas si no las corriste; distingue resultados locales reales de pruebas con mocks.
5. Al terminar, resume: diagnóstico, archivos modificados, por qué, pruebas ejecutadas/resultados, limitaciones pendientes y siguientes pasos.
6. **Seguridad**: jamás vuelques secretos en el chat, logs, pruebas o commits. Las credenciales de Twitch compartidas anteriormente deben tratarse como expuestas y rotarse.
