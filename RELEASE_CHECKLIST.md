# CyberQuiz — checklist de release

Cette checklist décrit le contrôle final avant de considérer `main` comme publiable.

## 1. Pull Request

- travailler sur une branche dédiée ;
- ne jamais pousser directement sur `main` ;
- vérifier que la PR cible `main` ;
- corriger la cause de toute CI rouge, sans contourner les protections ;
- exiger `Analyze Java/Kotlin` et `Analyze Python/JavaScript` au vert avant fusion ;
- utiliser un squash merge pour les changements importants.

## 2. Validation Android

Avant fusion d'un changement Android :

- `:app:lintDebug` ;
- `:app:testDebugUnitTest` ;
- `:app:assembleDebug` ;
- aucune modification de la clé ou de l'identité de signature Android ;
- aucune régression sur les migrations et schémas Room.

## 3. Validation backend

Pour tout changement FastAPI/social :
- exécuter tous les tests `backend/tests` ;
- vérifier les routes ajoutées avec un smoke test HTTP ;
- ne jamais journaliser ou commiter les secrets ;
- conserver PostgreSQL comme source de vérité pour les données de compte et l'économie ;
- les nouvelles fonctions réseau Android doivent utiliser HTTPS.

## 4. Publication APK

Après fusion sur `main`, le workflow `build-apk.yml` doit :

- réussir le lint release et les tests unitaires ;
- construire l'APK release ;
- signer avec la clé de distribution existante ;
- vérifier le package `com.elikto.cyberquiz` ;
- vérifier l'empreinte du certificat attendue ;
- publier `CyberQuiz.apk` et `update.json` sur la release stable `apk-latest`.

Le `update.json` publié doit référencer la version et le SHA-256 du nouvel APK.

## 5. Déploiement Render

Après un changement backend fusionné sur `main` :

- vérifier que le service suit bien la branche `main` ;
- vérifier que le déploiement correspond au commit courant de `main` ;
- vérifier que le déploiement atteint l'état `live` ;
- appeler `/api/social/config` et attendre HTTP 200 ;
- pour une route protégée nouvellement ajoutée, vérifier qu'un appel sans jeton retourne 401 ;
- vérifier l'absence de réponses HTTP 5xx juste après le déploiement.

Si l'auto-déploiement déclaré actif ne produit aucun déploiement après une fusion, traiter cela comme un incident d'intégration Render/GitHub : vérifier l'intégration puis utiliser un déploiement manuel uniquement comme récupération, jamais comme substitut permanent à une CI ou à une intégration cassée.

## 6. Contrôle final

- `main` local doit être propre et identique à `origin/main` ;
- la dernière release APK doit provenir du commit attendu ;
- le backend public doit exposer les routes du même état fonctionnel ;
- documenter toute anomalie opérationnelle restante avant de déclarer la release terminée.
