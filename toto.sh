#!/bin/bash

echo "🔍 Affichage des méthodes problématiques dans OrderService.java"
echo "=============================================================="

# Afficher la méthode getRecentOrdersAsMap (ligne ~475)
echo "📋 Méthode getRecentOrdersAsMap() autour de la ligne 475:"
echo "-------------------------------------------------------"
sed -n '470,520p' src/main/java/com/pcagrade/order/service/OrderService.java | nl -v470

echo ""
echo "📋 Méthode getRecentOrders() autour de la ligne 507:"
echo "---------------------------------------------------"
sed -n '505,560p' src/main/java/com/pcagrade/order/service/OrderService.java | nl -v505

echo ""
echo "🔍 Recherche de la ligne exacte qui cause l'erreur 470:"
echo "-------------------------------------------------------"
# Chercher les appels qui pourraient causer le problème
grep -n "List<Order>" src/main/java/com/pcagrade/order/service/OrderService.java | grep -E "(470|471|472|473|474|475)"