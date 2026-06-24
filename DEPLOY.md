# Deploy BugBoard

Frontend e backend su **due server separati**. Topologia: il browser parla solo
col frontend (nginx), che serve la SPA e fa da **reverse proxy** su `/api` verso
il backend. Niente CORS lato browser, nessuna modifica al codice applicativo.

```
browser ──► [server FE: nginx]  ──/api──►  [server BE: Spring Boot :8080]  ──►  PostgreSQL (remoto)
                 SPA statica
```

## Immagini
La pipeline pubblica su GitHub Container Registry:
- `ghcr.io/rccmolinari/bugboard2026/backend`
- `ghcr.io/rccmolinari/bugboard2026/frontend`

## Prova in locale (FE+BE insieme)
```bash
cp .env.example .env      # compila le credenziali del DB
docker compose up --build
# apri http://localhost:8080
```

## Setup una tantum dei server
Su **entrambi** i server:
1. Installa Docker + plugin compose.
2. Login a GHCR per poter fare `pull` (le immagini sono private):
   ```bash
   echo <PAT_con_read:packages> | docker login ghcr.io -u <user> --password-stdin
   ```
   In alternativa rendi pubblici i due package su GitHub e salti il login.
3. `sudo mkdir -p /opt/bugboard` (qui la pipeline copia il compose).

Solo sul **server backend** — `/opt/bugboard/.env`:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://<host-db>:5432/database_progetto
SPRING_DATASOURCE_USERNAME=<utente>
SPRING_DATASOURCE_PASSWORD=<password>
```

Solo sul **server frontend** — `/opt/bugboard/.env`:
```
BACKEND_URL=http://<IP-o-host-del-backend>:8080
```

## GitHub Secrets richiesti
| Secret | Descrizione |
|---|---|
| `BE_SSH_HOST` / `FE_SSH_HOST` | IP o hostname dei due server |
| `BE_SSH_USER` / `FE_SSH_USER` | utente SSH |
| `BE_SSH_KEY`  / `FE_SSH_KEY`  | chiave privata SSH (contenuto intero) |

Le credenziali DB e `BACKEND_URL` **non** stanno nei secrets: vivono nei `.env`
sui server, così non transitano dalla CI a ogni deploy.

## Rete / firewall
- Server frontend: porta **80** (e 443 con TLS) aperta al pubblico.
- Server backend: porta **8080** raggiungibile **solo** dal server frontend.
- HTTPS: non incluso qui. Metti un terminatore TLS davanti al frontend
  (es. Caddy/Traefik o certbot+nginx) e mappa la 443.

## Flusso pipeline (`.github/workflows/deploy.yml`)
1. **PR verso main** → test backend (JUnit) + build frontend (Vite).
2. **push su main** → oltre ai test: build immagini e push su GHCR, poi deploy
   SSH sui due server (`docker compose pull && up -d`).
