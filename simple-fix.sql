-- ===============================================
-- APPROCHE SIMPLE : IGNORER LIQUIBASE ET UTILISER LA TABLE EXISTANTE
-- ===============================================

-- 1. Vérifier que la table j_planning existe
SELECT 'Table j_planning status:' as info,
       CASE WHEN COUNT(*) > 0 THEN 'EXISTS' ELSE 'MISSING' END as status
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'dev' AND TABLE_NAME = 'j_planning';

-- 2. Vérifier sa structure
DESCRIBE j_planning;

-- 3. Si elle manque des colonnes importantes, les ajouter
ALTER TABLE j_planning
    ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'MEDIUM',
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'SCHEDULED',
    ADD COLUMN IF NOT EXISTS completed BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS card_count INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS notes TEXT,
    ADD COLUMN IF NOT EXISTS progress_percentage INT DEFAULT 0;

-- 4. Vider la table pour un nouveau départ
TRUNCATE TABLE j_planning;

-- 5. Vérifier le résultat final
SELECT 'Setup complete - j_planning ready for use' as message;
SELECT COUNT(*) as planning_count FROM j_planning;