# Spring Weather Broadcast

Backend Spring Boot per il sistema Weather Broadcast con notifiche push Firebase e invio newsletter email per gli admin.

## 🚀 Tecnologie

- **Spring Boot 3.5.0**
- **Java 17**
- **Maven**
- **Docker** (multi-stage build)

## 📦 Dipendenze

- **Spring Web** - REST API
- **Spring Mail** - Invio email SMTP
- **Lombok** - Riduzione boilerplate code
- **Firebase Admin SDK 9.4.2** - Integrazione Firebase (Firestore, Auth, Cloud Messaging)

## ✨ Features

- 📱 **Push Notifications** - Sistema completo notifiche push browser via FCM
- ✉️ **Admin Newsletter** - Invio email HTML a soli admin o a tutti gli utenti registrati
- 🔥 **Firebase Integration** - Firestore, Authentication, Cloud Messaging
- 🐳 **Docker Ready** - Container ottimizzato per deploy cloud
- 🌍 **CORS Configured** - Pronto per integrazione con frontend Angular
- 📊 **Health Check** - Endpoint per monitoring
- 🔒 **Multi-Environment** - Configurazioni separate per local/prod

## 📚 Documentazione

- 📱 [**NOTIFICATIONS.md**](NOTIFICATIONS.md) - Guida completa sistema notifiche push
- 🚀 [**DEPLOY-RENDER.md**](DEPLOY-RENDER.md) - Deploy su Render passo-passo
- ⚙️ [**CONFIG.md**](CONFIG.md) - Configurazione environment variables

## 🔧 Setup

### Prerequisiti

- Java 17 o superiore
- Maven (incluso wrapper mvnw)
- Firebase Project con Cloud Messaging abilitato

### Installazione

1. Clona il repository:

```bash
git clone <url-repository>
cd spring-weather-broadcast
```

2. Compila il progetto:

```bash
./mvnw clean install
```

3. Configura Firebase e SMTP:

```bash
# Usa lo script helper
./setup-config.sh

# Oppure manualmente:
# 1. Scarica firebase-service-account.json dalla Firebase Console
# 2. Salvalo in src/main/resources/
# 3. Copia application-example.properties in application.properties
# 4. Aggiorna application.properties con i tuoi valori Firebase e SMTP
```

**IMPORTANTE**: I file con credenziali sono già in `.gitignore`

## 🏃 Esecuzione

### Development

```bash
./mvnw spring-boot:run
```

L'applicazione sarà disponibile su `http://localhost:8080`

### Build per production

```bash
./mvnw clean package
java -jar target/spring-weather-broadcast-0.0.1-SNAPSHOT.jar
```

## � Deploy su Render

Il progetto è configurato per il deploy automatico su Render.

📖 **Guida completa**: [DEPLOY.md](DEPLOY.md)

**Quick start:**

1. Committa il codice su GitHub
2. Vai su [Render Dashboard](https://dashboard.render.com)
3. New > Blueprint
4. Seleziona il repository
5. Configura le variabili d'ambiente
6. Deploy! 🎉

Render farà automaticamente:

- ✅ Build del container Docker
- ✅ Deploy dell'applicazione
- ✅ Assegnazione URL pubblico
- ✅ Auto-deploy su ogni push

## 📡 API Endpoints

### Health Check

```
GET /api/health
```

### Notifiche Push

```
POST /api/notifications/send          # Invia a dispositivo specifico
POST /api/notifications/send-topic    # Invia a topic
POST /api/notifications/broadcast     # Broadcast a tutti
POST /api/notifications/subscribe     # Iscriviti a topic
POST /api/notifications/unsubscribe   # Disiscrivi da topic
POST /api/notifications/test          # Test notifica
```

### Newsletter Admin

```
POST /api/admin/newsletter/send       # Invia newsletter HTML agli admin o a tutti gli utenti
```

Richiede header `Authorization: Bearer <firebase-id-token>` di un utente con ruolo `admin` nella collezione Firestore `utenti`.

📖 Vedi [NOTIFICATIONS.md](NOTIFICATIONS.md) per esempi completi

## 📁 Struttura del progetto

```
spring-weather-broadcast/
├── src/main/java/com/weatherbroadcast/
│   ├── SpringWeatherBroadcastApplication.java
│   ├── config/
│   │   ├── FirebaseConfig.java          # Inizializzazione Firebase
│   │   └── CorsConfig.java              # Configurazione CORS
│   ├── controller/
│   │   ├── HealthController.java        # Health check endpoint
│   │   └── NotificationController.java  # API notifiche push
│   ├── service/
│   │   └── PushNotificationService.java # Logica FCM
│   └── dto/
│       ├── PushNotificationRequest.java
│       └── PushNotificationResponse.java
├── src/main/resources/
│   ├── application-local.properties     # Config locale (gitignored)
│   ├── application-prod.properties      # Template produzione
│   ├── application-example.properties   # Template esempio
│   � API Endpoints

### Health Check

```

GET /api/health

```

### Notifiche Push

```

POST /api/notifications/send # Invia a dispositivo specifico
POST /api/notifications/send-topic # Invia a topic
POST /api/notifications/broadcast # Broadcast a tutti
POST /api/notifications/subscribe # Iscriviti a topic
POST /api/notifications/unsubscribe # Disiscrivi da topic
POST /api/notifications/test # Test notifica

````

📖 Vedi [NOTIFICATIONS.md](NOTIFICATIONS.md) per esempi completi

## 🔐 Sicurezza

⚠️ **File da NON committare (già nel `.gitignore`):**

- `src/main/resources/firebase-service-account.json`
- `src/main/resources/application.properties`
- `src/main/resources/application-local.properties`

✅ **Safe per Git:**

- `application-example.properties` (template senza valori reali)
- `application-prod.properties` (solo placeholder ${ENV_VAR})

## 🌍 Configurazione Multi-Environment

Il progetto supporta configurazioni diverse per ogni ambiente:

| File | Scopo | Git |
|------|-------|-----|
| `application-local.properties` | Sviluppo locale | ❌ Gitignored |
| `application.properties` | Configurazione attiva | ❌ Gitignored |
| `application-prod.properties` | Template produzione | ✅ Committable |
| `application-example.properties` | Esempio per team | ✅ Committable |

Il profilo attivo viene selezionato con `SPRING_PROFILES_ACTIVE`:

- **Local**: usa `firebase-service-account.json` dal filesystem
- **Prod**: usa `FIREBASE_SERVICE_ACCOUNT_JSON` (base64 da env var)

## 📚 Risorse

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Firebase Admin SDK for Java](https://firebase.google.com/docs/admin/setup)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Lombok Documentation](https://projectlombok.org/)
- [Render Documentation](https://render.com/docs

- `firebase-service-account.json`
- `application-*.properties` con credenziali
- File `.env` con secrets

Questi file sono già inclusi nel `.gitignore`.

## 📚 Documentazione

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Firebase Admin SDK for Java](https://firebase.google.com/docs/admin/setup)
- [Lombok Documentation](https://projectlombok.org/)

## 🛠️ Comandi Maven utili

```bash
# Pulisci e compila
./mvnw clean compile

# Esegui i test
./mvnw test

# Crea il JAR
./mvnw package

# Salta i test durante la build
./mvnw package -DskipTests

# Pulisci target
./mvnw clean
````

## 🤝 Contribuire

1. Crea un branch per la feature
2. Fai commit delle modifiche
3. Push del branch
4. Apri una Pull Request
