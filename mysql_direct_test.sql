-- ===============================================
-- TEST MYSQL DIRECT POUR IDENTIFIER LE PROBLÈME
-- ===============================================

-- 1. Vérifier la structure exacte de j_planning
SHOW CREATE TABLE j_planning;

-- 2. Vérifier les contraintes de clés étrangères
SELECT
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    UPDATE_RULE,
    DELETE_RULE
FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc
         JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
              ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
WHERE kcu.TABLE_SCHEMA = 'dev'
  AND kcu.TABLE_NAME = 'j_planning';

-- 3. Vérifier les triggers sur j_planning
SHOW TRIGGERS LIKE 'j_planning';

-- 4. Tenter l'insertion la plus simple possible
SET @test_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @employee_id = UNHEX('08C68C835C84420A88E7AEB56BFA8E6A');
SET @order_id = UNHEX('018120F0622D3B430CD52D3836B18CCE');

-- Test avec colonnes minimales obligatoires seulement
INSERT INTO j_planning
(id, order_id, employee_id, planning_date, start_time, estimated_duration_minutes, created_at, updated_at)
VALUES
    (@test_id, @order_id, @employee_id, '2025-09-01', '2025-09-01 10:00:00', 60, NOW(), NOW());

-- 5. Vérifier le résultat
SELECT ROW_COUNT() as rows_inserted;
SELECT COUNT(*) FROM j_planning;

-- 6. Si ça marche, essayer avec plus de colonnes
SET @test_id2 = UNHEX(REPLACE(UUID(), '-', ''));

INSERT INTO j_planning
(id, order_id, employee_id, planning_date, start_time, end_time,
 estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
VALUES
    (@test_id2, @order_id, @employee_id, '2025-09-02', '2025-09-02 11:00:00', '2025-09-02 12:00:00',
     60, 'HIGH', 'SCHEDULED', FALSE, 15, 'Test MySQL direct', NOW(), NOW());

-- 7. Résultat final
SELECT ROW_COUNT() as rows_inserted_full;
SELECT COUNT(*) FROM j_planning;

-- 8. Voir les données insérées
SELECT
    HEX(id) as id,
    HEX(order_id) as order_id,
    HEX(employee_id) as employee_id,
    planning_date,
    start_time,
    priority,
    status,
    notes
FROM j_planning
ORDER BY created_at DESC;

-- 9. Si les insertions échouent, essayer de voir les erreurs détaillées
SHOW WARNINGS;