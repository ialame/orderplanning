#!/bin/bash

# ===============================================
# 🔄 MIGRATION VERS j_planning
# ===============================================

echo "🔄 MIGRATION: j_planification → j_planning"

# 1. Sauvegarder les données existantes (si elles existent)
echo "=== 1. SAUVEGARDE DES DONNÉES EXISTANTES ==="
mysql -u ia -pfoufafou dev -e "
-- Sauvegarder les planifications existantes
CREATE TABLE IF NOT EXISTS backup_planifications_$(date +%Y%m%d) AS
SELECT * FROM j_planification;

-- Vérifier le contenu sauvegardé
SELECT 'Planifications sauvegardées:' as info, COUNT(*) as count
FROM backup_planifications_$(date +%Y%m%d);
" 2>/dev/null || echo "Pas de table j_planification à sauvegarder"

# 2. Créer le nouveau fichier de changeset
echo "=== 2. CRÉATION DU NOUVEAU CHANGESET ==="
cat > src/main/resources/db/changelog/changes/002-fix-planning-table.yml << 'EOF'
# Votre contenu du fichier 002-fix-planning-table.yml ici
EOF

# 3. Mettre à jour le changelog master
echo "=== 3. MISE À JOUR DU CHANGELOG PRINCIPAL ==="
cat > src/main/resources/db/changelog/db.changelog-master.yml << 'EOF'
databaseChangeLog:
  - include:
      file: db/changelog/changes/001-create-tables.yml
  - include:
      file: db/changelog/changes/002-fix-planning-table.yml
EOF

# 4. Exécuter la migration Liquibase
echo "=== 4. EXÉCUTION DE LA MIGRATION ==="
echo "⚠️  Arrêtez votre application Spring Boot avant de continuer"
read -p "Appuyez sur Entrée quand l'application est arrêtée..."

# Option A: Via Maven (si disponible)
if command -v mvn &> /dev/null; then
    echo "🔄 Exécution via Maven..."
    mvn liquibase:update
else
    echo "⚠️  Maven non trouvé - redémarrez l'application pour appliquer les changements"
fi

# 5. Vérifier la nouvelle table
echo "=== 5. VÉRIFICATION DE LA NOUVELLE TABLE ==="
mysql -u ia -pfoufafou dev -e "
-- Vérifier que j_planning existe
SHOW TABLES LIKE 'j_planning';

-- Structure de la nouvelle table
DESCRIBE j_planning;

-- Compter les enregistrements
SELECT 'Planifications dans j_planning:' as info, COUNT(*) as count FROM j_planning;

-- Vérifier que j_planification n'existe plus
SHOW TABLES LIKE 'j_planification';
"

# 6. Instructions finales
echo "=== 6. ÉTAPES SUIVANTES ==="
echo "✅ 1. Redémarrez votre application Spring Boot"
echo "✅ 2. Testez l'API: curl -X POST http://localhost:8080/api/planning/generate -H 'Content-Type: application/json' -d '{}'"
echo "✅ 3. Vérifiez les logs pour confirmer que tout fonctionne"

echo "🎉 Migration terminée !"