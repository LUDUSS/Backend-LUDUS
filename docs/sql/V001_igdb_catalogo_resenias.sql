-- MANUAL: no se ejecuta desde Spring ni Gradle. Respaldar y aprobar antes de aplicar.
-- MySQL 8; confirmar SHOW CREATE TABLE y que no existen valores mayores al tipo destino.
-- DDL MySQL hace COMMIT implicito: no prometer ROLLBACK transaccional.
-- Estas consultas de preflight son de solo lectura:
SHOW CREATE TABLE juegos;
SHOW CREATE TABLE resenias;
SHOW INDEX FROM juegos;
SHOW INDEX FROM resenias;
SELECT id_usuario, id_juego, COUNT(*) AS cantidad
FROM resenias GROUP BY id_usuario, id_juego HAVING COUNT(*) > 1;
SELECT INDEX_NAME, NON_UNIQUE,
       GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columnas
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resenias'
GROUP BY INDEX_NAME, NON_UNIQUE;
SELECT id_api, COUNT(*) AS cantidad FROM juegos GROUP BY id_api HAVING COUNT(*) > 1;
SELECT COUNT(*) AS sin_id_externo FROM juegos WHERE id_api IS NULL;

-- DETENERSE si falta UNIQUE(id_api), hay duplicados/null, o el schema difiere del esperado.
-- Si hay resenas repetidas por usuario/juego, DETENER TODO el script: no borrar ni
-- elegir ganadores automaticamente; solicitar una decision de conservacion de datos.
-- Revisar si ya existe un indice UNIQUE EXACTO (id_usuario,id_juego), sin columnas
-- adicionales ni prefijos. Si existe, omitir solo el ADD CONSTRAINT de abajo.
-- Si uk_resenias_usuario_juego existe con otra definicion, detenerse y revisar.
-- Conservar el indice UNIQUE existente al ampliar id_api; NO recrear ni borrar filas.
ALTER TABLE juegos
  MODIFY COLUMN id_api BIGINT NOT NULL,
  MODIFY COLUMN nombre_juego TEXT NOT NULL,
  MODIFY COLUMN categoria TEXT NULL,
  MODIFY COLUMN plataforma TEXT NULL,
  MODIFY COLUMN descripcion TEXT NULL,
  MODIFY COLUMN imagen_url TEXT NULL;

ALTER TABLE resenias MODIFY COLUMN texto_resenia TEXT NOT NULL;

-- Ejecutar solo si el preflight no devuelve duplicados y no existe un UNIQUE equivalente.
ALTER TABLE resenias ADD CONSTRAINT uk_resenias_usuario_juego UNIQUE (id_usuario, id_juego);

-- Si el indice UNIQUE(id_api) no existe, crearlo SOLO despues de revisar preflight:
-- ALTER TABLE juegos ADD CONSTRAINT uk_juegos_id_api UNIQUE (id_api);
-- id_juego y resenias.id_juego se conservan; no tocar PK, FK ni filas dependientes.
-- Reversion: restaurar respaldo en una BD aparte y comparar; no reducir columnas
-- en uso, porque valores nuevos pueden exceder VARCHAR(255)/INTEGER originales.
