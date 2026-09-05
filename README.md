# CyberQuiz

Application Android de quiz cybersécurité, basée sur Kotlin + Jetpack Compose + Room, avec backend FastAPI pour les questions générées dynamiquement.

## Télécharger l'APK

La dernière version Android installable est publiée automatiquement dans **GitHub Releases** à chaque mise à jour de la branche `main`.

**[⬇️ Télécharger CyberQuiz.apk](https://github.com/Elikto/CyberQuiz/releases/download/apk-latest/CyberQuiz.apk)**

Page de la dernière version : https://github.com/Elikto/CyberQuiz/releases/latest

## Android
- minSdk 26
- target/compile SDK 37
- Kotlin 2.2.10
- Jetpack Compose
- Room

## Backend
1. Installer Python 3.11+.
2. Dans `backend/`: `python -m venv .venv` puis activer l'environnement.
3. `pip install -r requirements.txt`
4. Copier `.env.example` vers `.env` et renseigner la clé API **uniquement sur le serveur**.
5. `python main.py`

Le backend écoute sur `http://0.0.0.0:8000`. L'APK utilise par défaut `http://10.0.2.2:8000` pour l'émulateur Android.

## Automatisation Windows
Utiliser `setup_cyberquiz.ps1` pour préparer l'environnement et `run_cyberquiz.ps1` pour démarrer le backend, construire l'APK et installer sur un appareil ADB autorisé.
