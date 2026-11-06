-- Migracion incremental MANUAL para MySQL 8. No se ejecuta desde Spring/Gradle.
-- No modifica V001, no borra filas y no desactiva llaves foraneas.
-- Respaldar y verificar restauracion en una base aislada antes de aplicar.
-- DDL MySQL hace COMMIT implicito: no se revierte con ROLLBACK.

-- FASE 1: ejecutar SOLO estas comprobaciones de lectura y revisar resultados.
SHOW CREATE TABLE interacciones;
SHOW INDEX FROM interacciones;

SELECT id_usuario, id_resenia, COUNT(*) AS cantidad
FROM interacciones
GROUP BY id_usuario, id_resenia
HAVING COUNT(*) > 1;

SELECT COUNT(*) AS filas_con_null
FROM interacciones
WHERE id_usuario IS NULL OR id_resenia IS NULL OR fecha_like IS NULL;

SELECT i.id_interaccion
FROM interacciones i
LEFT JOIN usuarios u ON u.id_usuario = i.id_usuario
LEFT JOIN resenias r ON r.id_resenia = i.id_resenia
WHERE u.id_usuario IS NULL OR r.id_resenia IS NULL;

SELECT INDEX_NAME, NON_UNIQUE,
       GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columnas,
       SUM(CASE WHEN SUB_PART IS NULL THEN 0 ELSE 1 END) AS columnas_con_prefijo
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interacciones'
GROUP BY INDEX_NAME, NON_UNIQUE;

-- DETENERSE ante duplicados, null, huerfanos o schema inesperado; no deduplicar
-- automaticamente. Resolver conservacion de datos con aprobacion por separado.
-- Un UNIQUE exacto sobre ambos IDs (en cualquier orden), sin prefijos ni columnas
-- adicionales, ya garantiza la regla: si existe, OMITIR la fase 2.
-- Si uk_interacciones_usuario_resenia existe con otra definicion, detenerse.

-- FASE 2: solo tras preflight correcto y si no hay UNIQUE equivalente.
-- Aplicar en ventana de mantenimiento sin escritores concurrentes.
ALTER TABLE interacciones
  ADD CONSTRAINT uk_interacciones_usuario_resenia UNIQUE (id_usuario, id_resenia);

-- Las FK de MySQL normalmente ya exigen un indice que empiece por id_resenia.
-- Verificar SHOW INDEX: la agregacion por resena aprovecha ese indice existente.
-- SOLO si no existe ningun indice con id_resenia en primera posicion:
-- CREATE INDEX idx_interacciones_resenia ON interacciones (id_resenia);

-- Verificacion posterior: SHOW INDEX FROM interacciones y repetir preflight.
-- No se cambian PK/FK ni fechas, ni se convierten filas existentes.
