#!/bin/bash

# ===============================================
# TEST BACKEND SPRING BOOT - macOS
# ===============================================

echo "🔍 TEST BACKEND SPRING BOOT"
echo "==========================="

# 1. Test simple sans timeout (macOS n'a pas timeout par défaut)
echo "📋 1. Test endpoints simples..."

echo "Test /api/employees/active:"
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\n" http://localhost:8080/api/employees/active || echo "❌ Erreur curl"

echo ""
echo "Test page racine Spring Boot:"
curl -s -w "\nHTTP Status: %{http_code}\n" http://localhost:8080/ || echo "❌ Erreur curl racine"

echo ""
echo "Test /api/employees/debug:"
curl -s -w "\nHTTP Status: %{http_code}\n" http://localhost:8080/api/employees/debug || echo "❌ Erreur curl debug"

# 2. Test avec verbosité pour voir les détails
echo ""
echo "📋 2. Test avec détails de connexion..."
curl -v http://localhost:8080/api/employees/active 2>&1 | head -20

# 3. Vérifier les logs Spring Boot
echo ""
echo "📋 3. Vérification processus Java..."
ps aux | grep java | grep -v grep

# 4. Test de l'actuator Spring Boot (si activé)
echo ""
echo "📋 4. Test endpoints Spring Boot standard..."
echo "Test /actuator/health:"
curl -s http://localhost:8080/actuator/health || echo "❌ Actuator non disponible"

echo ""
echo "Test endpoints possibles:"
for endpoint in "/api/employees" "/api/employes" "/api/employees/debug" "/api/employees/active"
do
    echo "Testing $endpoint:"
    curl -s -o /dev/null -w "  Status: %{http_code}, Time: %{time_total}s\n" http://localhost:8080$endpoint
done

# 5. Instructions pour vérifier les logs
echo ""
echo "🔧 ÉTAPES SUIVANTES:"
echo "==================="
echo "1. Vérifiez les logs Spring Boot dans votre console/IDE"
echo "2. Regardez si des erreurs apparaissent quand vous faites ces requêtes"
echo "3. Vérifiez que Spring Boot a bien démarré tous les controllers"
echo "4. Testez directement dans votre navigateur: http://localhost:8080/api/employees/active"