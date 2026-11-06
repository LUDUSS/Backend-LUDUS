# LUDUS — Encargo para Codex: terminar backend de videojuegos e integración IGDB

## 0. Objetivo y modalidad

Actúa como ingeniero backend Java/Spring Boot sobre el **repositorio actualmente abierto en IntelliJ**. **Implementa, verifica y documenta** la integración backend completa de IGDB para LUDUS; deja una **guía exacta de integración para el frontend existente**, pero **NO modifiques HTML, JS ni CSS**. Este es un encargo de ejecución, no únicamente de planificación. Revisa el código y tests existentes antes de editar; los archivos y nombres reales prevalecen sobre este contexto histórico. No solicites datos sensibles, no imprimas secretos y no atribuyas éxito a pruebas no ejecutadas.

**Meta de entrega:** backend listo para que el frontend busque/explore juegos, filtre, pagine, abra detalles y PUBLIQUE, EDITE, ELIMINE Y LEA reseñas persistidas en MySQL, asociadas al juego local y al usuario autenticado. Las reseñas deben poder consultarse tanto desde el perfil del autor como desde la ficha del juego. Resolver/registrar el juego desde IGDB una sola vez cuando sea necesario; las búsquedas nunca guardan resultados.

## 1. Estado comprobado y contexto

- Proyecto: LUDUS, red social de reseñas de videojuegos. Java 21, Gradle, Spring Boot 4.1.2 (comprobar versión real), Spring Data JPA, MySQL, Spring Security/JWT. Paquete base `com.ludus.api`.
- Ya existen `models/Juego.java`, `repositories/JuegoRepository.java`, `services/JuegoService.java`, `controllers/JuegoController.java`, `services/IgdbService.java`, `controllers/IgdbController.java`, `JwtFilter`, `JwtService`, `SecurityConfig`, `application.properties` y pruebas recientes. **Inspecciona el estado actual antes de reemplazar métodos o clases**.
- IGDB funciona **realmente**: el usuario confirmó en Postman `GET http://localhost:8080/ludus/igdb/buscar?nombre=Elden%20Ring` con **200 OK**, JWT LUDUS en el cliente y credenciales Twitch en el backend. Resultados contienen `id`, `name`, `genres`, `platforms`, `cover.image_id`, etc. No repitas el diagnóstico anterior de 401 como si siguiera bloqueando.
- En `IGDB_ESTADO.md` constaba que Codex corrigió `JwtFilter`: sin JWT válido devuelve 401 en rutas protegidas, no 500; el filtro está registrado vía `FilterRegistrationBean` sobre `/ludus/*`, aparte de `SecurityFilterChain`. Constaban 15 pruebas exitosas y una live test fallida en ese momento por credenciales; **es un estado previo al 200 real reciente**. Verifica código y tests actuales, conserva arreglos.
- Dos credenciales distintas: JWT LUDUS (frontend → backend) y token de aplicación Twitch (backend → IGDB). Variables usadas históricamente: `IGDB_CLIENT_ID` y `IGDB_ACCESS_TOKEN`, sin prefijo `Bearer` en el valor. El Client Secret fue expuesto en capturas de una conversación anterior: **tratarlo como comprometido, rotarlo y nunca reutilizarlo o copiarlo a código**. Para renovación automática usar `IGDB_CLIENT_SECRET` seguro vía variables de entorno; nunca insertar valores concretos.
- Esquema actual `juegos`: `id_juego` PK autoincremental, `id_api` ID externo UNIQUE NOT NULL, `nombre_juego`, `categoria`, `plataforma`, `descripcion`, `imagen_url`. El modelo `Publicacion` representa `resenias`; la FK `resenias.id_juego` apunta a `juegos.id_juego`. Hay usuarios, comentarios, interacciones y seguidores.
- Actualmente existe al menos un **juego local de prueba** con `id_api` ficticio y reseñas dependientes; un DELETE falló con MySQL 1451 (FK). **No borres filas, no hagas TRUNCATE, no desactives FK, no resetees schema ni ejecutes migraciones destructivas sin aprobación explícita**. La aspiración es que el catálogo productivo contenga solo juegos IGDB, pero esa intención no autoriza limpiar los datos actuales.
- Frontend existente (fuera del repositorio backend, posiblemente `../Proyecto-integrador`): `juegos.html` importa `JS/api.js` y `JS/catalogoJuegos.js`. `consumeApi(endpoint, {method='GET',body,auth=true})` usa `http://localhost:8080` y JWT LUDUS. `catalogoJuegos.js` hace `GET /ludus/juego/` (`findAll`) y filtra en memoria por nombre, categoría y plataforma; mapea `id_juego` a string local, ficha `juegos.html?id=<id_local>`; reseñas aún se almacenan en `localStorage` con key `ludus_catalogo_resenias`. **No modificar frontend ahora; preparar instrucciones precisas para reemplazar estas dependencias**.

## 2. Límites de alcance y criterios de diseño

1. **Solo backend + pruebas + documentación**. No modificar repositorios frontend, ni otras entidades/servicios de compañeros salvo necesidad justificada y con pruebas de regresión.
2. No reescribir login/JWT; conservar política actual por defecto: la búsqueda IGDB existente requiere JWT LUDUS. Documenta la política de cada endpoint. No abrir endpoints de escritura a usuarios anónimos ni permitir que se suplante `id_usuario` desde el cliente.
3. **No guardar nada por búsqueda o navegación**. Persistir solo con acción explícita de negocio (`resolver/registrar juego para reseña`). Buscar IDs por IGDB y verificar datos **desde IGDB**, no confiar en el JSON de videojuegos enviado por frontend.
4. `id_api` identifica el juego IGDB y `id_juego` la fila local. `resenias`/`Publicacion` siempre referencian `id_juego`; no reutilizar un ID IGDB como si fuera local.
5. Dar preferencia a contratos REST coherentes y compatibles. Se pueden agregar rutas modernas `/ludus/...`, pero **conservar o adaptar explícitamente los endpoints existentes**; no romper `GET /ludus/juego/` sin estrategia.
6. Evitar complejidad ornamental. Implementar filtros soportados de manera real (no aceptar un parámetro y luego ignorarlo), documentar exclusiones, defaults y límites. No mentir sobre paginación o conteos.

## 3. Funcionalidad que debe quedar implementada

### A. Cliente HTTP IGDB + OAuth Twitch

- Separar cliente HTTP IGDB, servicio de negocio/mapeo y controlador. Configurar timeouts y errores saneados. Consultas APICalypse con POST a `/v4/games` y demás endpoints necesarios.
- **Automatizar token** con Client Credentials: usar `IGDB_CLIENT_ID` + `IGDB_CLIENT_SECRET`; obtener token de `https://id.twitch.tv/oauth2/token`, cachear hasta cercanía de expiración y renovar de forma segura (incluida respuesta 401 apropiada, evitando reintentos infinitos y tormentas de tokens). Considerar estrategia transitoria para `IGDB_ACCESS_TOKEN` si sigue configurado; establecer precedencia clara y documentar migración. No poner secretos en repo, logs, tests reales ni respuestas.
- Control básico de frecuencia y/o cache con límites para respetar cuota de IGDB (documentación oficial: 4 solicitudes/s concurrentes sujetas a limitación). Respetar 429/Retry-After cuando corresponda; backoff acotado, sin bloquear innecesariamente los hilos.
- Distinguir timeout, 400, 401/403 proveedor, 429, 5xx; traducir a JSON de error estable (400 entradas inválidas, 404 no encontrado, 502 proveedor, 503 rate limit, 504 timeout, según corresponda). No filtrar tokens ni cuerpos sensibles. No convertir falta de JWT LUDUS en 500.
- Validar y escapar consultas APICalypse para evitar inyección y preservar títulos con Unicode, comillas, apóstrofes y símbolos legítimos. Validar `page`, `size`, `sort`, filtros, IDs y longitudes. Proteger frente a uso abusivo.

### B. DTOs / modelos y mapeo

- Crear DTOs tipados de entrada/salida IGDB y contratos públicos LUDUS desacoplados del proveedor; no entregar definitivamente un `String` JSON sin contrato. Respuestas con `igdbId`, `localGameId` (nullable si no se ha guardado), `nombre`, `descripcion`, `portadaUrl`, géneros y plataformas **como listas** (además de propiedades de compatibilidad si se justifica). Metadatos adicionales razonables: fecha de lanzamiento, developer/publisher, rating IGDB con su semántica, screenshots, slug, web si están disponibles. Diferenciar explícitamente rating IGDB de la calificación social de LUDUS.
- Portadas seguras HTTPS construidas desde `cover.image_id`, con resolución documentada para cards y detalle. Nulos/faltantes y listas vacías sin romper el frontend. Descripción larga sin truncamiento silencioso.
- No sobre-modelar todos los campos de IGDB; elegir set útil y documentado para la experiencia existente.

### C. Endpoints de lectura y exploración

Definir rutas finales inspeccionando rutas reales y evitar colisiones. Capacidades mínimas exigidas:

1. **Buscar** por nombre (`q` o `nombre`) con paginación y coincidencia relevante de IGDB; resultado no implica guardado.
2. **Explorar** sin texto: listado paginado ordenable (por ejemplo popularidad/rating/lanzamiento cuando IGDB lo soporta), con defaults razonables. Evitar traer toda la base externa.
3. **Filtrar** por `genreId`, `platformId` y combinaciones compatibles; proporcionar catálogo/endpoint de géneros y plataformas reales para los selects del frontend. Usar IDs estables, no etiquetas arbitrarias; paginar resultados y documentar relaciones y parámetros.
4. **Detalle por IGDB ID**: debe funcionar aunque el juego no exista localmente; incluir portada, descripción, géneros, plataformas, metadata seleccionada. 404 cuando no existe. Evitar depender de haber cargado previamente un catálogo.
5. **Detalle por ID local** (mantener lectura existente) para la relación con reseñas. Especificar rutas distintas y no ambiguas.
6. Asegurar formato consistente de paginación (`items`, `page`, `size`, `hasNext`, etc.). Si IGDB no devuelve total exacto en la consulta, no inventar `totalElements`; si se implementa conteo real, hacerlo conscientemente de su costo/cuota.
7. Si determinadas combinaciones de filtro y búsqueda son incompatibles con APICalypse, implementar estrategia real documentada o rechazar claramente con 400, nunca ignorar parámetros silenciosamente.

### D. Persistencia bajo demanda e integración con reseñas

- Operación autenticada **resolver/registrar por IGDB ID** que busque primero por `id_api` y retorne el registro local existente o recupere/verifique desde IGDB, mapee, persista y devuelva `id_juego`. Definir semántica idempotente. Prevenir duplicados con UNIQUE real y manejo correcto de condición de carrera/violación de constraint. No depender solo de `existsBy` (check-then-insert no es atómico).
- Revisar entidad, constraints y MySQL real. `descripcion` debe soportar texto largo (`TEXT` o equivalente), imágenes faltantes no deben forzar datos falsos, géneros/plataformas múltiples deben tener una representación coherente para filtros futuros. Si se necesita migración, preferir script versionado y pruebas con esquema en memoria o base de pruebas; **no ejecutar alteraciones de la base compartida**. Entregar SQL/migraciones y explicar aplicación manual y respaldo.
- Integración transaccional de la operación de **crear reseña/publicación**: inspeccionar `Publicacion`, controlador/servicio real. Si el modelo ya admite `id_juego` local, documentar flujo mínimo `resolver IGDB` → `POST reseña` con ese ID; si es seguro y apropiado, ofrecer operación atómica que resuelva juego y cree reseña con identidad derivada de JWT. No romper endpoints existentes ni permitir suplantación. La integración real de reseñas y perfil es **obligatoria dentro del alcance backend**; no declarar la entrega completa si alguno de esos flujos no funciona y no está probado.
- Evitar pérdida accidental de reseñas en `localStorage`: migración de datos navegador es **tarea frontend posterior**, no se puede asegurar que haya ocurrido aquí.

### E. Reseñas y perfiles: implementación backend completa (OBLIGATORIA)

- Inspecciona primero la entidad real `Publicacion` (tabla `resenias`), Usuario, sus repositories/services/controllers, DTOs y las relaciones FK; NO inventes una entidad `Resenia` paralela ni dupliques rutas ya implementadas.
- Implementa o completa endpoints documentados para: **publicar reseña de juego IGDB**, **listar reseñas de un juego por IGDB ID (aunque no haya reseñas ni fila local)**, **listar reseñas del usuario del JWT (`/me` o equivalente)**, **listar reseñas públicas de un perfil por id de usuario**, **detalle de reseña**, **editar reseña propia** y **eliminar reseña propia**. Proporcionar paginación estable y orden por fecha (recientes primero), no cargar todos los registros sin límite.
- Al publicar, el backend debe obtener el usuario desde el JWT verificado (identidad vinculada a `usuarios`), resolver el juego por ID IGDB, persistirlo bajo demanda si falta y crear la reseña con FK `id_usuario` + `id_juego`; no permitir que JSON de entrada elija otro autor. Diseñar transacciones para que un fallo no deje reseñas inconsistentes, cuidando concurrencia de altas de juegos.
- Los DTO de lectura deben incluir como mínimo: `idResenia`, `texto`, `calificacion`, `fechaPublicacion`, `autor` (`idUsuario`, `nombre`), `juego` (`localGameId`, `igdbId`, `nombre`, `portadaUrl`). Evitar ciclos de serialización JPA y NO devolver passwords ni datos privados. Respetar escala de calificación de la entidad actual y validar rangos/longitudes; no imponer arbitrariamente otra escala.
- Al consultar la ficha del juego, devolver sus reseñas de **todos** los autores con datos públicos de autor; al consultar perfil, devolver reseñas **de ese usuario** con datos de juego, reutilizando una misma fuente MySQL. Si una reseña recién publicada aparece en ambos endpoints, el flujo está integrado.
- Autorización: listar según la política de lectura vigente; crear requiere JWT; editar/eliminar solo autor (403 para otro usuario), incluso si se falsifica un `idUsuario` en el payload; 404 para IDs inexistentes; respuestas de error coherentes. Revisar también endpoints heredados para impedir cambios de autor o suplantación por actualización de objeto completo.
- Rating social: promedio + cantidad de reseñas por juego desde MySQL, sin mezclarlo con `rating` externo de IGDB. Considerar caso sin reseñas (sin inventar promedio). Una reseña se asocia con UN usuario y UN juego; documentar si se permite más de una reseña por usuario/juego y aplicar la regla elegida de forma coherente.
- **No modificar frontend todavía**. Documentar exhaustivamente cómo reemplazar `localStorage` en `catalogoJuegos.js`, cómo consultar la lista desde página del juego y desde perfil (revisar `perfil.html`/JS si accesible; si no, declarar qué archivos debe revisar quien integre), y cómo publicar utilizando `consumeApi()`. No eliminar reseñas guardadas en navegadores; planificar migración opcional y consentida, no importar identidades desde el cliente sin verificación.
- Pruebas obligatorias: mismo POST seguido de GET de juego y GET de perfil devuelve la MISMA reseña; usuarios distintos ven la reseña en ficha del juego; usuario A no puede editar/eliminar reseña de B; cuerpo con autor falsificado no altera dueño; duplicados/concurrencia de juego; validaciones; páginas vacías; borrado y actualización reflejados en ambas vistas; no exponer contraseña. Usar BD aislada (H2 o contenedor de prueba), sin tocar base MySQL compartida.

### F. Seguridad y operación

- Conservar JWT LUDUS y errores adecuados 401/403; lectura con política explícita; creación/resolución y modificaciones protegidas. Auditar endpoints POST/PUT/DELETE de Juego existentes: no permitir crear registros IGDB arbitrarios desde el cliente ni operaciones destructivas indiscriminadas.
- No imprimir secretos, tokens, headers Authorization ni respuestas sensibles; no exponer OAuth token en JSON del backend. No guardar secretos en `application.properties`, configuraciones compartidas ni capturas.
- Documentar `.env`/Run Configuration de IntelliJ y Gradle/CI. Las variables del IDE no pasan automáticamente a procesos Gradle externos. Ofrecer `application-example.properties` sin secretos si ayuda.

## 4. Estrategia de trabajo obligatoria

1. **Inspección breve**: `git status`, estructura del repositorio, documentación y `IGDB_ESTADO.md`, seguridad, build, entidades y tests. Registrar archivos ya modificados por compañeros; no sobrescribir cambios ajenos. Ejecutar baseline de pruebas.
2. **Diseñar contrato**: documentar antes de grandes cambios el endpoint elegido y un JSON de ejemplo por operación. Preferir el diseño más pequeño que satisfaga requisitos. No bloquear el trabajo por preguntas de diseño secundarias; adoptar decisiones razonables y explicarlas.
3. **Implementar por incrementos**: OAuth/cliente, DTOs/mapeos, búsqueda y exploración, filtros/lookup, detalle, persistencia bajo demanda, compatibilidad con reseñas y seguridad; ejecutar tests entre etapas.
4. **Verificaciones sin depender de proveedor real**: JUnit, Mockito, MockMvc, MockRestServiceServer/WireMock equivalente, H2/test DB aislada para pruebas. Probar OAuth token/refresh, conversión de DTO, 404, 400, paginación límites, Unicode/escape, rate limiting, timeouts, 401 LUDUS, errores upstream, no persistencia al buscar, concurrencia de find-or-create, esquema/constraint y FK de reseñas.
5. **Smoke test real opcional**: solo si hay credenciales vigentes en entorno, sin imprimirlas; consultar búsqueda, filtro y detalle con JWT LUDUS válido. No permitir que pruebas contra MySQL compartida inserten o borren datos sin aprobación. Si la prueba real no se puede ejecutar, reportarlo sin afirmar éxito.
6. **Documentación final verificable** y evaluación final con `./gradlew test` / `gradlew.bat test`, `bootJar` y estado Git. No cerrar con TODOs ocultos. Resolver errores detectados de alcance o describir bloqueos concretos.

## 5. Entregables obligatorios (Markdown dentro del repositorio)

### `docs/IGDB_BACKEND_ESTADO.md`
- Resumen de implementación, decisiones y tradeoffs, listado de archivos tocados, pruebas ejecutadas con sus resultados, pendientes/bloqueos, detalles de migración SQL sin ejecutar, advertencias de seguridad. Evidencia explícita de que búsqueda real llegó a IGDB en el estado inicial y de cualquier nueva prueba real efectuada.

### `docs/IGDB_API_CONTRATO.md`
- Tabla de **todos los endpoints finales**: método, ruta exacta, propósito, parámetros y tipos, rangos, autenticación requerida, respuesta JSON realista (campos con nombres exactos), HTTP 200/201/400/401/403/404/429/502/503/504 según aplique, ejemplos curl/Postman con placeholders sin secretos.
- Explicación detallada de diferencias `igdbId` versus `localGameId` y flujo para vincular reseñas.
- Política de qué se cachea y qué se guarda en MySQL. Ejemplos de filtros múltiples y paginación.

### `docs/IGDB_FRONTEND_GUIA.md` — **lo más importante para el siguiente trabajo**
- Instrucciones **específicas para el frontend YA EXISTENTE** (`juegos.html`, `JS/api.js`, `JS/catalogoJuegos.js`; estilos deben preservarse): qué función reemplazar, qué endpoint consumir, cómo mapear las propiedades antiguas `id_juego`, `nombre_juego`, `categoria`, `plataforma`, `descripcion`, `imagen_url` a DTOs nuevos; qué hacer con `juegos.html?id=...`; cómo consultar detalle externo sin requerir `findAll()`; cómo construir filtros/selector género/plataforma; búsqueda con debounce y abort/cancelación; estados loading/empty/error; paginación/load more; evitar XSS y manejar imágenes nulas.
- Diagrama de secuencia (Mermaid) de búsqueda → ficha IGDB → resolución/alta local → reseña con `id_juego`, y un ejemplo de JS usando **consumeApi() existente** con sus opciones `method`, `body`, `auth`.
- Incluir flujos y ejemplos **con nombres EXACTOS de endpoints finales** para publicar una reseña autenticada, listar reseñas por videojuego y por perfil (propio y ajeno), editar/eliminar propias, y actualizar promedio/cantidad en cards y detalle. Cómo migrar reseñas de `localStorage` (`ludus_catalogo_resenias`) hacia endpoints MySQL sin suplantar identidad y sin destruir reseñas antiguas; identificar qué acciones siguen pendientes. No afirmar que el frontend está integrado hasta que se modifique y pruebe.
- Checklist manual de aceptación del frontend y un orden de integración incremental para que podamos realizarlo después con ayuda del asistente.

## 6. Criterios de aceptación no negociables

- `gradlew.bat test` y `gradlew.bat bootJar` pasan o se reportan errores y bloqueos precisos.
- La consulta externa ya confirmada 200 sigue funcionando con credenciales válidas; no dejarla rota después del refactor.
- Buscar/filtrar/explorar/detallar **no** inserta registros. Resolver por `igdbId` crea exactamente un juego local bajo solicitudes repetidas/concurrentes y expone su `id_juego`.
- Los DTOs funcionan con datos faltantes y listas múltiples; los filtros filtran realmente; paginación y errores documentados.
- No exponer credenciales; renovación de token segura; no regresiones JWT ni endpoints CRUD de compañeros.
- No alterar datos o llaves foráneas de MySQL existente sin aprobación, ni utilizar `ddl-auto=update` como único plan de migración.
- Demostrar con pruebas: publicar reseña desde IGDB ID crea/asocia `id_juego` + `id_usuario`; aparece tanto en ficha de juego como en perfil del usuario, y no permite edición ajena.
- Entregar la guía frontend **orientada a los nombres, estructura y JS existentes**, incluyendo páginas de perfil y reseñas, no documentación genérica.
- Termina con un informe breve: implementado, pruebas pasadas, pruebas no realizadas, migraciones pendientes de aprobación y pasos concretos para integrar frontend.

## 7. Fuentes oficiales

- IGDB: https://api-docs.igdb.com/ (consultas APICalypse `search`, `fields`, `where`, `limit`, `offset`; imágenes `cover.image_id`; autenticación y límites; confirmar campos reales antes de elegirlos).
- OAuth Twitch: https://dev.twitch.tv/docs/authentication/getting-tokens-oauth/#client-credentials-grant-flow

**Comienza ya inspeccionando el estado del repositorio y ejecuta por etapas. No te detengas tras redactar un plan: implementa, prueba y documenta dentro del alcance autorizado.**
