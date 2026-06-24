# syntax=docker/dockerfile:1

# --- Stage 1: build statico Vite ----------------------------------------------
FROM node:24-alpine AS build
WORKDIR /app

# npm ci usa il package-lock.json -> build riproducibile.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# --- Stage 2: nginx che serve la SPA e fa da reverse proxy --------------------
FROM nginx:1.27-alpine AS runtime

# Sostituisco SOLO ${BACKEND_URL} nel template; cosi' le variabili runtime di
# nginx ($host, $uri, ...) NON vengono toccate da envsubst all'avvio.
ENV NGINX_ENVSUBST_FILTER=BACKEND_URL
ENV BACKEND_URL=http://backend:8080

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80
