# ==========================================
# DIAGNOSTIC: Vérifier quelles données l'API retourne vraiment
# ==========================================

echo "🔍 DIAGNOSTIC DES DONNÉES EMPLOYÉS"
echo "=================================="

# 1. Vérifier ce que l'API retourne vraiment
echo "1. Test direct API /api/employees:"
curl -s "http://localhost:8080/api/employees" | jq '.'

echo ""
echo "2. Compter les employés retournés:"
curl -s "http://localhost:8080/api/employees" | jq '. | length'

echo ""
echo "3. Structure du premier employé:"
curl -s "http://localhost:8080/api/employees" | jq '.[0]'

echo ""
echo "4. Noms des employés retournés:"
curl -s "http://localhost:8080/api/employees" | jq -r '.[] | "\(.firstName) \(.lastName)"'

# ==========================================
# TESTS POUR IDENTIFIER LE PROBLÈME
# ==========================================

echo ""
echo "📊 TESTS DE DIAGNOSTIC:"
echo "======================"

# Test si l'API retourne une array vide
EMPLOYEE_COUNT=$(curl -s "http://localhost:8080/api/employees" | jq '. | length')
echo "Nombre d'employés dans l'API: $EMPLOYEE_COUNT"

if [ "$EMPLOYEE_COUNT" = "0" ] || [ "$EMPLOYEE_COUNT" = "null" ]; then
    echo "❌ PROBLÈME: L'API retourne 0 employés"
    echo "   → Le frontend utilise les données mock par défaut"
    echo "   → Corrigez le backend pour retourner les vrais employés"
else
    echo "✅ L'API retourne $EMPLOYEE_COUNT employés"
    echo "   → Le problème est dans le mapping frontend"
fi

# Test du format des données
echo ""
echo "5. Format des données retournées:"
curl -s "http://localhost:8080/api/employees" | jq '.[0] | keys'