#!/bin/bash

# ===============================================
# TESTS DES ENDPOINTS DE PLANNING
# ===============================================

echo "🧪 Testing Planning Endpoints"
echo "================================"

# 1. Test endpoint view-simple (tous les plannings)
echo "📋 Test 1: All plannings"
curl -s http://localhost:8080/api/planning/view-simple | jq '. | length' || echo "Endpoint failed"

# 2. Test avec date spécifique
echo "📅 Test 2: Plannings for specific date"
curl -s "http://localhost:8080/api/planning/view-simple?date=2025-06-01" | jq '. | length' || echo "Date filter failed"

# 3. Test génération
echo "🚀 Test 3: Generate new plannings"
curl -s -X POST http://localhost:8080/api/planning/generate \
  -H "Content-Type: application/json" \
  -d '{"startDate": "2025-06-01", "timePerCard": 3}' | jq '.planningsSaved' || echo "Generation failed"

# 4. Test debug
echo "🔍 Test 4: Debug info"
curl -s http://localhost:8080/api/planning/debug-real | jq '.planningCount' || echo "Debug failed"

# 5. Vérifier données SQL directement
echo "💾 Test 5: Direct SQL check"
mysql -u ia -pfoufafou dev -e "
SELECT
    COUNT(*) as total_plannings,
    COUNT(DISTINCT employee_id) as employees_used,
    COUNT(DISTINCT planning_date) as dates_covered,
    MIN(planning_date) as earliest_date,
    MAX(planning_date) as latest_date
FROM j_planning;
" 2>/dev/null || echo "SQL check failed"

echo "✅ Tests completed!"