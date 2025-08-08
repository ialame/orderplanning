-- ===============================================
-- TEST SQL DIRECT DE LA TABLE j_planning
-- ===============================================

-- 1. Vérifier que la table existe
SHOW TABLES LIKE 'j_planning';

-- 2. Voir la structure complète
DESCRIBE j_planning;

-- 3. Voir les contraintes de clés étrangères
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'dev'
  AND TABLE_NAME = 'j_planning';

-- 4. Test d'insertion SQL direct
SET @test_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @order_id = UNHEX('0197D2BB478FE23DBAD530B0EC72D233');
SET @employee_id = UNHEX('E93263727DF943D78BD9B0F91845F358');

-- Essayer l'insertion la plus simple possible
INSERT INTO j_planning
(id, order_id, employee_id, planning_date, start_time, estimated_duration_minutes, status, completed, created_at, updated_at)
VALUES
    (@test_id, @order_id, @employee_id, '2025-08-15', '2025-08-15 10:00:00', 60, 'TEST', FALSE, NOW(), NOW());

-- 5. Vérifier le résultat
SELECT ROW_COUNT() as rows_inserted;
SELECT COUNT(*) as total_plannings FROM j_planning;

-- 6. Si l'insertion a marché, voir le contenu
SELECT
    HEX(id) as id,
    HEX(order_id) as order_id,
    HEX(employee_id) as employee_id,
    planning_date,
    start_time,
    status
FROM j_planning
         LIMIT 5;