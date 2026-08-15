# --- Build stage ---
FROM node:22-alpine AS build
WORKDIR /build

COPY package*.json ./
RUN npm ci

COPY . .

# Empty base URL -> the app calls same-origin "/api/..." which nginx proxies
# to the backend container. Override at build time for other deployments.
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

# --- Runtime stage ---
FROM nginx:1.27-alpine AS runtime

# Which host:port nginx proxies "/api/*" to. Baked in at build time (rather
# than templated at container start) so the final image is a fully static,
# immutable artifact -- the Jenkinsfile passes a different value per target
# (docker-compose's "backend:8080" vs. a Kubernetes Service DNS name).
ARG BACKEND_UPSTREAM="backend:8080"
COPY nginx.conf.template /tmp/nginx.conf.template
RUN BACKEND_UPSTREAM="$BACKEND_UPSTREAM" envsubst '${BACKEND_UPSTREAM}' \
      < /tmp/nginx.conf.template > /etc/nginx/conf.d/default.conf \
    && rm /tmp/nginx.conf.template

COPY --from=build /build/dist /usr/share/nginx/html

# nginx:alpine already ships an unprivileged "nginx" user (uid/gid 101) with
# /var/cache/nginx, /var/run and /var/log/nginx pre-owned by it -- run as
# that user so the container also works under a non-root Kubernetes
# PodSecurityContext (which is why nginx.conf.template listens on 8080
# instead of the privileged port 80).
USER nginx

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
    CMD wget -qO- http://localhost:8080/healthz || exit 1

CMD ["nginx", "-g", "daemon off;"]
