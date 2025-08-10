#!/bin/bash

# ===============================================
# 🚀 TEST RAPIDE DES SOLUTIONS
# ===============================================

echo "🚀 TEST RAPIDE DES SOLUTIONS"

# ===============================================
# 1. TEST AVEC DATES SPÉCIFIQUES
# ===============================================

echo "=== 1. TEST AVEC DATES SPÉCIFIQUES ==="

dates=("2025-09-01" "2025-09-02" "2025-09-03")

for date in "${dates[@]}"; do
    echo "📅 Test avec date: $date"

    response=$(curl -s "http://localhost:8080/api/planning/view-simple?date=$date")
    count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "ERROR")

    echo "  📋 Planifications trouvées: $count"

    if [ "$count" != "0" ] && [ "$count" != "ERROR" ]; then
        echo "  ✅ SUCCÈS ! Données trouvées pour $date"
        echo "  📊 Exemple de données:"
        echo "$response" | jq '.[0]' 2>/dev/null | head -10
        break
    fi
done

# ===============================================
# 2. TEST ENDPOINT DEBUG-PLANNINGS
# ===============================================

echo ""
echo "=== 2. TEST ENDPOINT DEBUG-PLANNINGS ==="

debug_response=$(curl -s "http://localhost:8080/api/planning/debug-plannings")
debug_count=$(echo "$debug_response" | jq '. | length' 2>/dev/null || echo "ERROR")

echo "📊 Debug endpoint résultats: $debug_count items"

if [ "$debug_count" != "0" ] && [ "$debug_count" != "ERROR" ]; then
    echo "✅ Debug endpoint fonctionne !"
    echo "📋 Échantillon de données debug:"
    echo "$debug_response" | jq '.[0:2]' 2>/dev/null
fi

# ===============================================
# 3. TEST SQL DIRECT POUR VÉRIFIER LES DATES
# ===============================================

echo ""
echo "=== 3. VÉRIFICATION DATES EN BASE ==="

mysql -u ia -pfoufafou dev << 'EOF'
-- Voir toutes les dates de planification disponibles
SELECT 'DATES DISPONIBLES:' as info;
SELECT
    planning_date,
    COUNT(*) as count_plannings,
    MIN(TIME(start_time)) as first_time,
    MAX(TIME(start_time)) as last_time
FROM j_planning
GROUP BY planning_date
ORDER BY planning_date;

-- Voir aujourd'hui spécifiquement
SELECT 'PLANIFICATIONS AUJOURD\'HUI (2025-08-10):' as info;
SELECT COUNT(*) as count_today FROM j_planning WHERE planning_date = '2025-08-10';

-- Voir les planifications récentes
SELECT 'PLANIFICATIONS RÉCENTES:' as info;
SELECT
    planning_date,
    COUNT(*) as count
FROM j_planning
WHERE planning_date >= '2025-09-01'
GROUP BY planning_date;
EOF

# ===============================================
# 4. SOLUTION TEMPORAIRE : APPEL AVEC DATE
# ===============================================

echo ""
echo "=== 4. SOLUTION TEMPORAIRE FRONTEND ==="

echo "💡 SOLUTION RAPIDE POUR LE FRONTEND :"
echo ""
echo "Dans votre composant Vue.js, remplacez l'appel API par :"
echo ""
echo "// Au lieu de :"
echo "fetch('/api/planning/view-simple')"
echo ""
echo "// Utilisez :"
echo "fetch('/api/planning/view-simple?date=2025-09-01')"
echo "// ou"
echo "fetch('/api/planning/debug-plannings')  // qui retourne tout"
echo ""

# ===============================================
# 5. TEST SOLUTION TEMPORAIRE
# ===============================================

echo "=== 5. TEST SOLUTION TEMPORAIRE ==="

echo "🧪 Test appel avec date spécifique..."

temp_response=$(curl -s "http://localhost:8080/api/planning/view-simple?date=2025-09-01")
temp_count=$(echo "$temp_response" | jq '. | length' 2>/dev/null || echo "ERROR")

if [ "$temp_count" != "0" ] && [ "$temp_count" != "ERROR" ]; then
    echo "✅ SOLUTION TEMPORAIRE FONCTIONNE !"
    echo "📊 Avec date 2025-09-01: $temp_count planifications"
    echo ""
    echo "🎯 POUR RÉPARER IMMÉDIATEMENT LE FRONTEND :"
    echo ""
    echo "1. Dans votre api.ts, remplacez :"
    echo "   fetch('/api/planning/view-simple')"
    echo "   par :"
    echo "   fetch('/api/planning/view-simple?date=2025-09-01')"
    echo ""
    echo "2. Ou utilisez l'endpoint debug :"
    echo "   fetch('/api/planning/debug-plannings')"
    echo ""
else
    echo "❌ Solution temporaire ne fonctionne pas"
fi

# ===============================================
# 6. INSTRUCTIONS CORRECTIF BACKEND
# ===============================================

echo ""
echo "=== 6. INSTRUCTIONS CORRECTIF BACKEND ==="

echo ""
echo "🔧 POUR CORRIGER DÉFINITIVEMENT LE BACKEND :"
echo ""
echo "1. Dans PlanningController.java, méthode viewPlanningsSimple():"
echo "   - Remplacer la logique de date par défaut"
echo "   - Au lieu de LocalDate.now(), ne pas filtrer par date"
echo "   - Ou retourner les planifications des derniers 30 jours"
echo ""
echo "2. Code à changer :"
echo "   String targetDate = date != null ? date : LocalDate.now().toString();"
echo "   Par :"
echo "   // Retourner toutes les planifications si pas de date"
echo "   if (date != null) { /* filtrer */ } else { /* tout retourner */ }"
echo ""

echo "🎯 Test terminé à $(date)"