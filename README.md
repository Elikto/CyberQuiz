# CyberQuiz

Application Android de quiz cybersécurité, basée sur Kotlin + Jetpack Compose + Room, avec un backend FastAPI optionnel pour la génération administrative de questions.

## Télécharger l'APK

La dernière version Android installable est publiée automatiquement dans **GitHub Releases** à chaque mise à jour Android validée de la branche `main`.

**[⬇️ Télécharger CyberQuiz.apk](https://github.com/Elikto/CyberQuiz/releases/download/apk-latest/CyberQuiz.apk)**

Page de la dernière version : https://github.com/Elikto/CyberQuiz/releases/latest

## Android
- minSdk 26
- target/compile SDK 37
- Kotlin 2.2.10
- Jetpack Compose
- Room
- trafic HTTP en clair désactivé
- mise à jour APK vérifiée par SHA-256 et identité de signature Android

L'application Android utilise actuellement sa banque de questions locale et n'appelle pas le backend FastAPI pour fonctionner.

## Backend
1. Installer Python 3.11+.
2. Dans `backend/`: `python -m venv .venv` puis activer l'environnement.
3. `pip install -r requirements.txt`
4. Copier `.env.example` vers `.env`.
5. Renseigner `OPENAI_API_KEY` uniquement sur le serveur.
6. Pour activer volontairement la génération, définir `CYBERQUIZ_ENABLE_GENERATION=true` et une longue valeur aléatoire dans `CYBERQUIZ_ADMIN_KEY`.
7. `python main.py`

Le endpoint `POST /api/questions` est désactivé par défaut. Lorsqu'il est activé, il exige le header HTTP `X-CyberQuiz-Admin-Key` correspondant au secret serveur. Ne jamais intégrer ce secret dans l'APK, la PWA ou un dépôt Git.

Le endpoint `/health` reste disponible pour les contrôles de disponibilité du service.

## Automatisation Windows
Utiliser `setup_cyberquiz.ps1` pour préparer l'environnement et `run_cyberquiz.ps1` pour démarrer les composants de développement locaux, construire l'APK et installer sur un appareil ADB autorisé.
