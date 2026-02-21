# 🚀 Deploy su Render

Guida completa per il deploy dell'applicazione Spring Boot su Render.

## 📋 Prerequisiti

- ✅ Account Render ([render.com](https://render.com))
- ✅ Repository GitHub con il codice
- ✅ Firebase Service Account JSON
- ✅ Firebase Database URL

## 🔧 Step 1: Preparazione Credenziali Firebase

### 1.1 Scarica Service Account JSON

1. Vai alla [Firebase Console](https://console.firebase.google.com/)
2. Seleziona il progetto **weather-broadcast-pwa**
3. Settings ⚙️ → **Project settings**
4. Tab **Service accounts**
5. Click **Generate new private key**
6. Salva il file come `firebase-service-account.json`

### 1.2 Converti JSON in Base64

```bash
# Usa lo script helper
./setup-config.sh

# Seleziona opzione 2: "Converti firebase-service-account.json in base64"
# Copia l'output (stringa base64)
```

**Alternativa manuale:**

```bash
# macOS/Linux
base64 -i firebase-service-account.json | tr -d '\n' | pbcopy

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("firebase-service-account.json")) | Set-Clipboard
```

### 1.3 Ottieni Database URL

```
https://weather-broadcast-pwa-default-rtdb.firebaseio.com
```

## 🐙 Step 2: Preparazione Repository GitHub

### 2.1 Crea Repository

```bash
cd /path/to/spring-weather-broadcast

# Inizializza git (se non già fatto)
git init

# Aggiungi remote (SOSTITUISCI con il tuo username)
git remote add origin https://github.com/TUO_USERNAME/spring-weather-broadcast.git

# Verifica .gitignore
cat .gitignore
```

**Assicurati che `.gitignore` contenga:**

```gitignore
# Credenziali (NON committare mai!)
src/main/resources/application.properties
src/main/resources/application-local.properties
src/main/resources/firebase-service-account.json

# Build
target/
```

### 2.2 Commit e Push

```bash
# Aggiungi tutti i file (esclusi quelli in .gitignore)
git add .

# Commit
git commit -m "feat: Initial Spring Boot backend with FCM notifications"

# Push
git push -u origin main
```

## 🌐 Step 3: Deploy su Render

### 3.1 Crea Nuovo Web Service

1. Vai su [Render Dashboard](https://dashboard.render.com/)
2. Click **New +** → **Web Service**
3. Connetti il tuo repository GitHub
4. Seleziona **spring-weather-broadcast**

### 3.2 Configurazione Service

**Name:** `spring-weather-broadcast`

**Region:** Frankfurt (o Oregon per USA)

**Branch:** `main`

**Runtime:** Docker

**Dockerfile Path:** `./Dockerfile`

**Docker Build Context Directory:** `.` (root del repository)

**Plan:** Free (o Starter per 7$/mese)

**Auto-Deploy:** ✅ Yes

### 3.3 Configura Environment Variables

Clicca su **Advanced** e aggiungi le seguenti variabili:

| Key                             | Value                                                       | Note                       |
| ------------------------------- | ----------------------------------------------------------- | -------------------------- |
| `PORT`                          | `8080`                                                      | Porta del server           |
| `SPRING_PROFILES_ACTIVE`        | `prod`                                                      | Profilo Spring attivo      |
| `FIREBASE_DATABASE_URL`         | `https://weather-broadcast-pwa-default-rtdb.firebaseio.com` | URL Firebase               |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | `eyJ0eXBlIjoi...`                                           | Base64 del JSON (Step 1.2) |
| `CORS_ALLOWED_ORIGINS`          | `https://weather-broadcast-pwa.firebaseapp.com`             | URL frontend               |
| `JAVA_OPTS`                     | `-Xmx512m -Xms256m`                                         | Ottimizzazione memory      |

**⚠️ IMPORTANTE:**

- Il valore di `FIREBASE_SERVICE_ACCOUNT_JSON` deve essere la stringa base64 **senza spazi o newline**
- Il valore di `CORS_ALLOWED_ORIGINS` deve corrispondere esattamente all'URL del frontend

### 3.4 Configura Health Check

**Health Check Path:** `/api/health`

### 3.5 Deploy

1. Click **Create Web Service**
2. Render inizierà automaticamente il build
3. Attendi il completamento (5-10 minuti)
4. Verifica i log per confermare il successo

## ✅ Step 4: Verifica Deploy

### 4.1 Test Health Endpoint

```bash
# Sostituisci con il tuo URL Render
curl https://spring-weather-broadcast.onrender.com/api/health
```

**Risposta attesa:**

```json
{
  "status": "UP",
  "timestamp": "2026-02-21T22:48:00.123Z",
  "service": "spring-weather-broadcast"
}
```

### 4.2 Test Notification Endpoint

```bash
curl -X POST https://spring-weather-broadcast.onrender.com/api/notifications/test \
  -H "Content-Type: application/json" \
  -d '{
    "token": "dummy-token-for-testing"
  }'
```

**Risposta attesa (con token dummy):**

```json
{
  "success": false,
  "message": "Errore invio notifica",
  "error": "Invalid FCM token"
}
```

Questo è normale con un token dummy. Conferma che l'API funziona.

### 4.3 Verifica Log Render

1. Dashboard Render → Il tuo service
2. Tab **Logs**
3. Cerca:
   - ✅ `Started SpringWeatherBroadcastApplication`
   - ✅ `Firebase Admin SDK inizializzato con successo`
   - ✅ `Tomcat started on port 8080`

## 🔄 Step 5: Configurazione Automatica (Opzionale)

### 5.1 Deploy tramite render.yaml

Il file `render.yaml` nella root del progetto configura automaticamente il service:

```yaml
services:
  - type: web
    name: spring-weather-broadcast
    runtime: docker
    repo: https://github.com/TUO_USERNAME/spring-weather-broadcast
    dockerCommand: "" # usa il CMD del Dockerfile
    branch: main
    dockerfilePath: ./Dockerfile
    plan: free
    region: frankfurt

    envVars:
      - key: PORT
        value: 8080
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: FIREBASE_DATABASE_URL
        sync: false
      - key: FIREBASE_SERVICE_ACCOUNT_JSON
        sync: false
      - key: CORS_ALLOWED_ORIGINS
        value: https://weather-broadcast-pwa.firebaseapp.com
      - key: JAVA_OPTS
        value: "-Xmx512m -Xms256m"

    healthCheckPath: /api/health
    autoDeploy: true
```

**Per usare render.yaml:**

1. Dashboard Render → **New** → **Blueprint**
2. Connetti repository
3. Render leggerà automaticamente `render.yaml`
4. Configura manualmente solo `FIREBASE_DATABASE_URL` e `FIREBASE_SERVICE_ACCOUNT_JSON`

### 5.2 Auto-Deploy on Push

Con `autoDeploy: true`, ogni push su `main` triggera automaticamente un re-deploy.

```bash
# Fai modifiche
git add .
git commit -m "fix: Update notification logic"
git push origin main

# Render avvia automaticamente il deploy
```

## 🐛 Troubleshooting

### Problema: Build fallisce

**Sintomo:** `Error: Docker build failed`

**Soluzione:**

1. Verifica che `Dockerfile` sia presente nella root
2. Verifica che `mvnw` sia eseguibile:
   ```bash
   chmod +x mvnw
   git add mvnw
   git commit -m "fix: Make mvnw executable"
   git push
   ```

### Problema: Firebase non inizializza

**Sintomo:** `Firebase Admin SDK initialization failed`

**Soluzione:**

1. Verifica che `FIREBASE_SERVICE_ACCOUNT_JSON` sia base64 corretto:

   ```bash
   echo $FIREBASE_SERVICE_ACCOUNT_JSON | base64 -d | jq .
   ```

2. Controlla che non ci siano spazi o newline nella variabile d'ambiente

3. Verifica `FIREBASE_DATABASE_URL` sia corretto

### Problema: CORS errors nel frontend

**Sintomo:** `Access to XMLHttpRequest blocked by CORS policy`

**Soluzione:**

1. Verifica che `CORS_ALLOWED_ORIGINS` contenga l'URL esatto del frontend
2. Controlla che non ci sia trailing slash:
   - ✅ `https://weather-broadcast-pwa.firebaseapp.com`
   - ❌ `https://weather-broadcast-pwa.firebaseapp.com/`

3. Se usi più URL (prod + staging):
   ```
   CORS_ALLOWED_ORIGINS=https://weather-broadcast-pwa.firebaseapp.com,https://weather-broadcast-staging.firebaseapp.com
   ```

### Problema: Out of Memory

**Sintomo:** Container crashes con `OutOfMemoryError`

**Soluzione:**

1. Su piano Free (512MB), ottimizza `JAVA_OPTS`:

   ```
   JAVA_OPTS=-Xmx384m -Xms256m
   ```

2. Considera upgrade a Starter plan (512MB → 2GB)

### Problema: Notifiche non arrivano

**Sintomo:** API ritorna success ma notifiche non arrivano

**Soluzione:**

1. Verifica che il token FCM sia valido e non scaduto
2. Controlla che l'utente sia iscritto al topic corretto
3. Verifica permessi notifiche nel browser
4. Controlla Firebase Console → Cloud Messaging per errori

## 📊 Monitoraggio

### Render Dashboard

- **Metrics:** CPU, Memory, Request rate
- **Logs:** Real-time logs del container
- **Events:** Deploy history, restarts
- **Settings:** Environment variables, scaling

### Endpoint di Health

Monitora lo stato dell'applicazione:

```bash
curl https://spring-weather-broadcast.onrender.com/api/health
```

Configura un monitor esterno (es: UptimeRobot) per essere avvisato se il servizio va down.

## 💰 Costi

### Piano Free

- ✅ 750 ore/mese gratis
- ✅ Auto-sleep dopo 15 min inattività
- ✅ Wake-up automatico al primo request (~30 secondi)
- ✅ Certificato SSL gratuito
- ❌ 512MB RAM
- ❌ 0.1 CPU

### Piano Starter (7$/mese)

- ✅ Always-on (no sleep)
- ✅ 512MB RAM
- ✅ 0.5 CPU
- ✅ Deploy prioritario
- ✅ Nessun cold start

**Raccomandazione:** Inizia con Free, upgrade se necessario.

## 🔗 Link Utili

- [Render Documentation](https://render.com/docs)
- [Deploy Spring Boot on Render](https://render.com/docs/deploy-spring-boot)
- [Docker on Render](https://render.com/docs/docker)
- [Environment Variables](https://render.com/docs/configure-environment-variables)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)

## 📝 Checklist Deploy

Usa questa checklist prima del deploy:

- [ ] Firebase Service Account JSON scaricato
- [ ] JSON convertito in base64
- [ ] Repository GitHub creato e pushato
- [ ] `.gitignore` configurato correttamente
- [ ] Dockerfile presente nella root
- [ ] `mvnw` eseguibile
- [ ] Render account creato
- [ ] Environment variables configurate
- [ ] Health check path configurato
- [ ] Deploy completato con successo
- [ ] Health endpoint testato
- [ ] Notification endpoint testato
- [ ] CORS verificato dal frontend
- [ ] Logs controllati per errori

---

✅ **Deploy completato con successo!**

Il tuo backend è ora live su: `https://spring-weather-broadcast.onrender.com`
