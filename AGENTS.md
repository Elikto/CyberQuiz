# Règles du projet CyberQuiz

Ces règles s’appliquent à l’ensemble du dépôt.

- Ne jamais modifier directement `main`.
- Toujours travailler sur une branche dédiée.
- Pour une modification importante : branche → Pull Request → CI verte → squash merge.
- Les checks requis avant merge sont `Analyze Java/Kotlin` et `Analyze Python/JavaScript`.
- Ne jamais contourner les protections de branche.
- Le package Android publié est `com.elikto.cyberquiz`.
- Le namespace Kotlin interne `com.example.cyberquiz` est volontaire et ne doit pas être renommé sans raison explicite.
- Ne jamais changer la clé ou le certificat de signature APK.
- Ne jamais mettre de clé API, mot de passe ou secret dans le code, l’APK ou Git.
- Les secrets serveur doivent être fournis par variables d’environnement.
- Ne jamais désactiver une protection de sécurité simplement pour faire passer un test.
- Avant une PR Android, lancer au minimum le lint, les tests unitaires pertinents et vérifier la compilation.
- Avant une PR backend, lancer les tests Python pertinents.
- Respecter les migrations et schémas Room existants.
- Ne pas modifier ou supprimer de données utilisateur sans demande explicite.
- Le système de mise à jour APK et la vérification de signature sont sensibles et ne doivent pas être affaiblis.
- Toute nouvelle fonctionnalité réseau doit utiliser HTTPS.
- En cas d’échec CI, analyser et corriger la cause plutôt que contourner le check.
