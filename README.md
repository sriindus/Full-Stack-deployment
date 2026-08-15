# java-fsd-hello-world

A full-stack project with a Spring Boot REST API, a React + TypeScript frontend, PostgreSQL,
Docker images, Kubernetes manifests, and a Jenkins CI/CD pipeline. Every tool used is open
source.

| Layer | Choice | Skill area demonstrated |
| --- | --- | --- |
| Backend | Java 21, Spring Boot 3.3, Spring Data JPA | Backend Development, API Development & Integration |
| API | REST, `springdoc-openapi` (Swagger UI) | API Development & Integration |
| Frontend | ReactJS 19, TypeScript, Redux Toolkit | Frontend Development |
| Database | PostgreSQL 16 (H2 for local dev) | Database Management |
| Tests | JUnit 5 + Mockito (unit), MockMvc (integration), Vitest + React Testing Library | Testing & Quality |
| Containers | Docker, multi-stage builds, non-root, read-only rootfs | DevOps & CI/CD |
| Orchestration | Kubernetes (Kustomize): Deployments, Services, HPA, PDB, Ingress | DevOps & CI/CD |
| CI/CD | Jenkinsfile: test → build → push → deploy → smoke test | DevOps & CI/CD, Agile Collaboration |
| Hardening | non-root containers, dropped capabilities, validated input, resource limits | Performance & Security |

## Layout

```
backend/                Spring Boot REST API
  src/main/java/...      domain, repository, service, web (controller + DTOs), config, exception
  src/test/java/...      GreetingServiceTest (unit), GreetingControllerIT (integration)
  Dockerfile              multi-stage: maven build -> jre runtime, non-root
frontend/                React + TypeScript + Redux Toolkit
  src/api/                fetch client for the backend
  src/features/greetings/ Redux slice + GreetingBoard component (+ tests)
  Dockerfile              multi-stage: npm build -> nginx runtime, non-root, non-privileged port
docker-compose.yml       postgres + backend + frontend, wired together
k8s/                     Namespace, ConfigMap/Secret, Postgres StatefulSet, backend/frontend
                         Deployments + Services + HPA, Ingress, PodDisruptionBudgets, kustomization
Jenkinsfile              test -> lint manifests -> build & push images -> deploy -> smoke test
```

## Coordinates

- Image registry: `ghcr.io/sriindus/hello-fsd-backend` and `ghcr.io/sriindus/hello-fsd-frontend`
- Kubernetes namespace: `hello-fsd`
- Ingress host: `hello-fsd.local` — change it in [k8s/ingress.yaml](k8s/ingress.yaml) once you
  have a real domain

If you fork this under a different GitHub user, update the image owner in
[Jenkinsfile](Jenkinsfile), [k8s/kustomization.yaml](k8s/kustomization.yaml),
[k8s/backend.yaml](k8s/backend.yaml) and [k8s/frontend.yaml](k8s/frontend.yaml).

## 1. Run it locally (no Docker)

```bash
# Backend — Spring Boot with an in-memory H2 database, no setup required
cd backend
mvn spring-boot:run          # http://localhost:8080
mvn clean verify             # unit tests (surefire) + integration tests (failsafe)

# Frontend — in another terminal
cd frontend
cp .env.example .env.local   # points the app at http://localhost:8080
npm install
npm run dev                  # http://localhost:5173
npm test                     # Vitest + React Testing Library
```

Open `http://localhost:8080/swagger-ui.html` for interactive API docs, or
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:hellodb`) to browse the embedded database.

## 2. Run it as containers (Docker Compose + PostgreSQL)

```bash
cp .env.example .env         # DB_NAME / DB_USER / DB_PASSWORD, defaults work out of the box
docker compose up --build
```

- Frontend: <http://localhost:8081>
- Backend API directly: <http://localhost:8080/api/v1/greetings>
- Postgres: `localhost:5432` (credentials from `.env`)

The frontend's nginx proxies `/api/*` to the backend container, so the browser only ever talks
to one origin and CORS never comes into play in this deployment path.

## 3. Deploy to Kubernetes

Requires a cluster with the [ingress-nginx](https://github.com/kubernetes/ingress-nginx)
controller installed (`kind`/`minikube`/`k3d` all work locally).

```bash
kubectl apply -k k8s/
kubectl -n hello-fsd get pods -w
echo "127.0.0.1 hello-fsd.local" | sudo tee -a /etc/hosts   # local clusters only
curl http://hello-fsd.local/api/v1/greetings
```

`k8s/secret.yaml` ships demo-only Postgres credentials that match the docker-compose defaults so
the manifests apply cleanly out of the box. Replace them with a real secret (`kubectl create
secret generic ... --dry-run=client -o yaml`, sealed-secrets, or a secret manager) before this
goes anywhere near production.

## 4. CI/CD

[Jenkinsfile](Jenkinsfile) runs on every push: Maven build + unit/integration tests for the
backend, npm install/lint/test/build for the frontend, a dry-run render of the Kustomize
manifests, then (on `main`) builds and pushes both Docker images to GHCR, applies the manifests,
and runs a smoke test against the deployed backend. It expects two Jenkins credentials:
`ghcr-credentials` (GHCR username + PAT with `write:packages`) and `kubeconfig` (a kubeconfig
file for the target cluster).

## API

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/greetings` | List all greetings |
| `GET` | `/api/v1/greetings/{id}` | Fetch one greeting |
| `POST` | `/api/v1/greetings` | Create a greeting — `{"author": "...", "message": "..."}` |
| `DELETE` | `/api/v1/greetings/{id}` | Delete a greeting |
| `GET` | `/actuator/health` | Liveness/readiness health |

## Testing strategy

- **Unit** — `GreetingServiceTest` mocks the repository with Mockito and exercises only the
  service layer's own logic, no Spring context.
- **Integration** — `GreetingControllerIT` boots the real Spring context and dispatches real
  HTTP requests through MockMvc into a real (in-memory H2) database, verifying the full
  controller → service → repository → database round trip, validation, and error handling.
  Maven's `failsafe` plugin (bound to `mvn verify`) runs these separately from the fast unit
  tests, which is the standard Maven convention for that split.
- **Frontend** — Vitest unit-tests the Redux slice's reducer logic in isolation, and React
  Testing Library drives `GreetingBoard` through a real render + user interaction with the API
  layer mocked, covering the loading/success/error states a real user would hit.
- **End-to-end** — not included in this hello-world scope; the natural next step would be a
  Playwright/Cypress suite driving the docker-compose stack (frontend → real backend → real
  Postgres), run as an additional Jenkins stage after the images are built.

## Notes on how this was built

This project was scaffolded and verified in a sandboxed cloud environment with network access
to npm but not to Maven Central or Docker Hub, and no running Docker daemon. That means:

- The **frontend** was fully installed, type-checked, unit/component-tested (9 passing tests),
  and production-built in that sandbox — those results are real.
- The **backend**, **Docker images**, and **Kubernetes manifests** were written and carefully
  reviewed by hand (including catching and fixing a couple of real bugs along the way — a
  missing `maven-failsafe-plugin` that would have silently skipped the integration test, and a
  Kubernetes probe using `$(VAR)` substitution where it isn't supported), and the YAML/XML was
  syntax-validated, but `mvn verify`, `docker compose up`, and `kubectl apply -k k8s/` have not
  actually been executed anywhere yet. Run the commands in sections 1–3 above on a machine with
  normal internet access and Docker installed to confirm everything end to end.
