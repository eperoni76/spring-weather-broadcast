#!/bin/bash

# ================================================================
# Script di setup configurazione Spring Weather Broadcast
# 
# ESECUZIONE:
#   ./setup-config.sh
#
# Se lo script non è eseguibile:
#   chmod +x setup-config.sh
#
# NOTA: Esegui questo script dal TERMINALE, non con "Run Code"
# ================================================================

set -e

RESOURCES_DIR="src/main/resources"
cd "$(dirname "$0")"

echo "🔧 Spring Weather Broadcast - Setup Configurazione"
echo "=================================================="
echo ""

# Verifica struttura
if [ ! -d "$RESOURCES_DIR" ]; then
    echo "❌ Errore: cartella $RESOURCES_DIR non trovata"
    exit 1
fi

# Menu
echo "Cosa vuoi fare?"
echo ""
echo "1) Setup sviluppo locale (copia application-local -> application.properties)"
echo "2) Converti Firebase credentials in base64 (per Render)"
echo "3) Verifica configurazione"
echo "4) Mostra variabili necessarie per Render"
echo ""
read -p "Scelta [1-4]: " choice

case $choice in
    1)
        echo "📋 Setup sviluppo locale..."
        if [ -f "$RESOURCES_DIR/application-local.properties" ]; then
            cp "$RESOURCES_DIR/application-local.properties" "$RESOURCES_DIR/application.properties"
            echo "✅ application.properties aggiornato da application-local.properties"
            echo ""
            echo "⚠️  Ricorda di configurare:"
            echo "   - firebase-service-account.json in $RESOURCES_DIR/"
            echo "   - spring.mail.host, spring.mail.port, spring.mail.username e spring.mail.password"
        else
            echo "❌ application-local.properties non trovato"
            echo "   Copia application-example.properties e configuralo:"
            echo "   cp $RESOURCES_DIR/application-example.properties $RESOURCES_DIR/application-local.properties"
        fi
        ;;
    
    2)
        echo ""
        if [ -f "$RESOURCES_DIR/firebase-service-account.json" ]; then
            echo "🔐 Conversione Firebase credentials in base64..."
            echo ""
            
            # Converti in base64
            if [[ "$OSTYPE" == "darwin"* ]]; then
                # macOS
                base64 -i "$RESOURCES_DIR/firebase-service-account.json" | pbcopy
                echo "✅ Credenziali copiate nella clipboard (macOS)"
            else
                # Linux
                base64 -w 0 "$RESOURCES_DIR/firebase-service-account.json"
                echo ""
                echo "✅ Copia il testo sopra"
            fi
            
            echo ""
            echo "📝 Usa questo valore per la variabile d'ambiente:"
            echo "   FIREBASE_SERVICE_ACCOUNT_JSON"
        else
            echo "❌ firebase-service-account.json non trovato in $RESOURCES_DIR/"
            echo ""
            echo "Come ottenerlo:"
            echo "1. Vai su Firebase Console: https://console.firebase.google.com/"
            echo "2. Seleziona progetto 'weather-broadcast-pwa'"
            echo "3. Project Settings > Service Accounts"
            echo "4. Generate new private key"
            echo "5. Salva come 'firebase-service-account.json' in $RESOURCES_DIR/"
        fi
        ;;
    
    3)
        echo ""
        echo "🔍 Verifica configurazione..."
        echo ""
        
        # Verifica file properties
        echo "📄 File di configurazione:"
        [ -f "$RESOURCES_DIR/application.properties" ] && echo "   ✅ application.properties" || echo "   ❌ application.properties (mancante)"
        [ -f "$RESOURCES_DIR/application-local.properties" ] && echo "   ✅ application-local.properties" || echo "   ⚠️  application-local.properties (opzionale)"
        [ -f "$RESOURCES_DIR/application-prod.properties" ] && echo "   ✅ application-prod.properties" || echo "   ❌ application-prod.properties (mancante)"
        [ -f "$RESOURCES_DIR/application-example.properties" ] && echo "   ✅ application-example.properties" || echo "   ❌ application-example.properties (mancante)"
        
        echo ""
        echo "🔑 Firebase:"
        [ -f "$RESOURCES_DIR/firebase-service-account.json" ] && echo "   ✅ firebase-service-account.json trovato" || echo "   ❌ firebase-service-account.json (NECESSARIO)"
        
        echo ""
        echo "🔒 Sicurezza .gitignore:"
        if git check-ignore "$RESOURCES_DIR/application-local.properties" > /dev/null 2>&1; then
            echo "   ✅ application-local.properties ignorato da git"
        else
            echo "   ⚠️  application-local.properties NON ignorato da git!"
        fi
        
        if git check-ignore "$RESOURCES_DIR/firebase-service-account.json" > /dev/null 2>&1; then
            echo "   ✅ firebase-service-account.json ignorato da git"
        else
            echo "   ⚠️  firebase-service-account.json NON ignorato da git!"
        fi
        ;;
    
    4)
        echo ""
        echo "☁️  Variabili d'ambiente per Render:"
        echo ""
        echo "Configura queste variabili nel dashboard di Render:"
        echo ""
        echo "┌─────────────────────────────────┬────────────────────────────────────────────┐"
        echo "│ Variabile                       │ Valore                                     │"
        echo "├─────────────────────────────────┼────────────────────────────────────────────┤"
        echo "│ SPRING_PROFILES_ACTIVE          │ prod                                       │"
        echo "│ PORT                            │ 8080                                       │"
        echo "│ FIREBASE_DATABASE_URL           │ https://weather-broadcast-pwa-default-rt...│"
        echo "│ FIREBASE_SERVICE_ACCOUNT_JSON   │ <base64 da script opzione 2>               │"
        echo "│ SPRING_MAIL_HOST                │ smtp.gmail.com                              │"
        echo "│ SPRING_MAIL_PORT                │ 587                                         │"
        echo "│ SPRING_MAIL_USERNAME            │ <account smtp>                              │"
        echo "│ SPRING_MAIL_PASSWORD            │ <password/app password smtp>                │"
        echo "│ CORS_ALLOWED_ORIGINS            │ https://weather-broadcast-pwa.firebaseap...│"
        echo "│ JAVA_OPTS                       │ -Xmx512m -Xms256m                          │"
        echo "└─────────────────────────────────┴────────────────────────────────────────────┘"
        echo ""
        echo "📝 Note:"
        echo "   - Per FIREBASE_SERVICE_ACCOUNT_JSON usa opzione 2 di questo script"
        echo "   - URL completo CORS: https://weather-broadcast-pwa.firebaseapp.com"
        echo "   - URL Firebase DB: https://weather-broadcast-pwa-default-rtdb.firebaseio.com"
        echo "   - Per Gmail usa una App Password dedicata come SPRING_MAIL_PASSWORD"
        ;;
    
    *)
        echo "❌ Scelta non valida"
        exit 1
        ;;
esac

echo ""
echo "✨ Completato!"
