# ===============================================
# 🔍 DIAGNOSTIC APPROFONDI : 0 PLANIFICATIONS SAUVÉES
# ===============================================

echo "🔍 DIAGNOSTIC APPROFONDI DU PROBLÈME..."

# 1. Vérifier si le problème vient des IDs d'employés
echo "=== 1. VÉRIFICATION DES IDS EMPLOYÉS ==="
mysql -u ia -pfoufafou dev -e "
-- Comparer les IDs employés entre j_employee et employee
SELECT 'IDs dans j_employee:' as info, COUNT(*) as count FROM j_employee WHERE actif = 1;
SELECT 'IDs dans employee:' as info, COUNT(*) as count FROM employee WHERE active = 1;

-- Voir les IDs réels
SELECT 'Premiers IDs j_employee:' as info, HEX(id) as employee_id, CONCAT(prenom, ' ', nom) as nom
FROM j_employee WHERE actif = 1 LIMIT 3;

SELECT 'Premiers IDs employee:' as info, HEX(id) as employee_id, CONCAT(first_name, ' ', last_name) as nom
FROM employee WHERE active = 1 LIMIT 3;
"

# 2. Test d'insertion avec IDs réels du service employé
echo "=== 2. TEST AVEC DEBUG-SAVE ENDPOINT ==="
curl -X POST http://localhost:8080/api/planning/debug-save \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.'

# 3. Vérifier les dates utilisées dans la génération
echo "=== 3. VÉRIFICATION DES DATES ==="
mysql -u ia -pfoufafou dev -e "
-- Voir les planifications créées aujourd'hui
SELECT
    'Planifications créées aujourdhui:' as info,
    COUNT(*) as count,
    MIN(created_at) as premiere,
    MAX(created_at) as derniere
FROM j_planning
WHERE DATE(created_at) = CURDATE();

-- Voir les dernières planifications créées
SELECT
    HEX(id) as id,
    planning_date,
    start_time,
    notes,
    created_at
FROM j_planning
ORDER BY created_at DESC
LIMIT 5;
"

# 4. Test d'insertion simple via API
echo "=== 4. TEST INSERTION SIMPLE VIA API ==="
curl -X POST http://localhost:8080/api/planning/generate-force \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.planningsSaved, .message'

# 5. Vérifier si c'est un problème de logique dans le code Java
echo "=== 5. VÉRIFICATION LOGIQUE JAVA ==="
echo "📋 Cherchez dans les logs Spring Boot:"
echo "   - Messages 'Planning already exists'"
echo "   - Messages d'erreur d'insertion"
echo "   - Exceptions silencieuses"

# 6. Test manuel d'insertion avec les mêmes paramètres que l'API
echo "=== 6. TEST MANUEL AVEC PARAMÈTRES API ==="
mysql -u ia -pfoufafou dev -e "
-- Simuler exactement ce que fait l'API
SET @api_planning_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @api_order_id = (SELECT HEX(id) FROM \`order\` WHERE status IN (1,2,3) ORDER BY date DESC LIMIT 1);
SET @api_employee_id = (SELECT HEX(id) FROM j_employee WHERE actif = 1 LIMIT 1);

-- Convertir back en BINARY pour l'insertion
SET @api_order_binary = UNHEX(@api_order_id);
SET @api_employee_binary = UNHEX(@api_employee_id);

SELECT
    'Test manual avec IDs API:' as info,
    @api_order_id as order_hex,
    @api_employee_id as employee_hex;

-- Vérifier si planification existe déjà pour DEMAIN (pas aujourd'hui)
SELECT
    'Planifications existantes pour demain:' as info,
    COUNT(*) as count
FROM j_planning
WHERE order_id = @api_order_binary
AND employee_id = @api_employee_binary
AND planning_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY);

-- Tentative d'insertion pour demain
INSERT IGNORE INTO j_planning
(id, order_id, employee_id, planning_date, start_time, end_time,
 estimated_duration_minutes, priority, status, completed, created_at, updated_at, card_count, notes)
VALUES
(@api_planning_id, @api_order_binary, @api_employee_binary,
 DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '15:30:00',
 90, 'HIGH', 'SCHEDULED', 0, NOW(), NOW(), 1, 'Test manuel API simulation');

SELECT ROW_COUNT() as manual_insert_success;
"

echo ""
echo "==============================================="
echo "🎯 PROCHAINES ÉTAPES SELON LES RÉSULTATS:"
echo "==============================================="
echo "1. 📋 Si debug-save fonctionne → Problème dans la logique principale"
echo "2. 🔄 Si generate-force fonctionne → Problème avec vos IDs d'employés"
echo "3. 📅 Si insertion manuelle fonctionne → Problème de dates/doublons"
echo "4. 🚫 Si tout échoue → Problème de permissions/structure DB"