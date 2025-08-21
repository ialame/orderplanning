# ===============================================
# DOCKERFILE FRONTEND VUE
# ===============================================
# Fichier: src/main/frontend/Dockerfile

# ========== ÉTAPE 1: BUILD ==========
FROM node:18-alpine AS builder

WORKDIR /app

# Copier les fichiers de dépendances
COPY package*.json ./

# Installer les dépendances
RUN npm ci --only=production

# Copier le code source
COPY . .

# Build de production
RUN npm run build

# ========== ÉTAPE 2: SERVEUR NGINX ==========
FROM nginx:alpine

# Copier la configuration nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copier les fichiers buildés
COPY --from=builder /app/dist /usr/share/nginx/html

# Exposer le port
EXPOSE 3000

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3000/ || exit 1

# Démarrer nginx
CMD ["nginx", "-g", "daemon off;"]