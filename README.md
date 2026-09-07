<div align="center">

<img src="app/src/main/res/drawable-nodpi/cyberquiz_app_icon.jpg" alt="CyberQuiz" width="120" />

# CyberQuiz

### Apprends Joue Progresse.

**Une application Android de quiz moderne pour apprendre la cybersécurité, la nutrition et bientôt bien plus encore.**

Des quiz rapides, des catégories ciblées, des statistiques, des révisions intelligentes et une progression qui reste sur ton téléphone.

[![Télécharger CyberQuiz](https://img.shields.io/badge/⬇️_TÉLÉCHARGER-CyberQuiz.apk-7c3aed?style=for-the-badge)](https://github.com/Elikto/CyberQuiz/releases/download/apk-latest/CyberQuiz.apk)

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](#installation)
[![APK signé](https://img.shields.io/badge/APK-signé_et_vérifié-2563eb?style=flat-square)](#mises-à-jour)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack_Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](#pour-les-curieux)

**Téléchargement gratuit · Installation directe · Aucune publicité intégrée**

</div>

---

## Pourquoi CyberQuiz ?

CyberQuiz n'est pas juste une suite de questions. L'application est pensée pour **apprendre en jouant**, voir sa progression et revenir facilement sur ce qui mérite d'être retravaillé.

| 🎯 Quiz personnalisés | 📊 Progression | 🔁 À revoir |
|---|---|---|
| Choisis le nombre de questions et les catégories que tu veux travailler. | Suis ton niveau, ton XP, ta réussite et tes performances par thème. | Retrouve les notions difficiles et entraîne-toi dessus. |

| 🗂️ Historique | 🧩 Catégories | ✨ Plusieurs univers |
|---|---|---|
| Consulte tes quiz terminés et relance une session. | Travaille précisément le sujet qui t'intéresse. | Passe d'un univers de quiz à un autre depuis ton profil. |

---

## Univers disponibles

### 🛡️ Cybersécurité

Réseaux, Linux, Windows, cryptographie, Web, malware, ingénierie sociale, OSINT, forensic, pentest, Active Directory, cloud, mobile, systèmes…

> Idéal pour apprendre, réviser ou tester régulièrement ses connaissances en cyber.


## Une interface faite pour donner envie de revenir

CyberQuiz propose notamment :

- un écran d'accueil adapté à l'univers sélectionné ;
- des quiz classiques ou ciblés par catégorie ;
- des sessions configurables ;
- un système de niveau et d'XP ;
- des statistiques détaillées ;
- une liste **À revoir** pour retravailler les difficultés ;
- un historique des quiz terminés ;
- un profil permettant de changer d'univers ;
- un historique des mises à jour directement dans l'application.

Les questions principales sont stockées localement dans l'application : **tu peux jouer sans dépendre d'un serveur de quiz distant**.

---

## Installation

### 1. Télécharger l'APK

<div align="center">

### [⬇️ Télécharger la dernière version de CyberQuiz](https://github.com/Elikto/CyberQuiz/releases/download/apk-latest/CyberQuiz.apk)

[Voir la page de la dernière version](https://github.com/Elikto/CyberQuiz/releases/tag/apk-latest)

</div>

### 2. Ouvrir `CyberQuiz.apk`

Android peut demander d'autoriser ton navigateur ou ton gestionnaire de fichiers à **installer cette application**. Cette autorisation dépend de la version d'Android et de ton appareil.

### 3. Installer

Une fois l'installation terminée, lance **CyberQuiz** depuis ton écran d'applications.

**Compatibilité : Android 8.0 (API 26) ou version ultérieure.**

---

## Mises à jour

Les nouvelles versions Android validées sont publiées automatiquement sur GitHub Releases.

CyberQuiz vérifie les mises à jour de manière discrète : lorsqu'une nouvelle version est disponible, une **petite pastille apparaît sur l'icône Paramètres**. Rien ne se télécharge et aucun installateur ne s'ouvre automatiquement.

C'est toi qui décides quand lancer la mise à jour depuis les Paramètres.

Avant installation, l'application vérifie notamment :

- le téléchargement via HTTPS ;
- l'intégrité SHA-256 de l'APK ;
- le nom du package Android ;
- la version de l'application ;
- l'identité de signature Android attendue.

---

## Vie privée et fonctionnement

CyberQuiz fonctionne principalement avec des données locales : progression, historique, révisions et sessions sont conservés sur l'appareil.

L'application Android n'a pas besoin du backend FastAPI pour lancer les quiz présents dans sa banque locale.

Le trafic HTTP en clair est désactivé dans l'application Android.

---

## Pour les curieux

CyberQuiz est construit avec :

- **Kotlin** ;
- **Jetpack Compose** pour l'interface Android ;
- **Room** pour les données locales ;
- **FastAPI** pour le backend optionnel ;
- **GitHub Actions** pour les tests, l'analyse et la publication de l'APK.

Les builds Android passent par des contrôles automatisés avant publication : lint, tests, construction de l'APK, signature et vérification de la signature.


---

<div align="center">

## Prêt à tester tes connaissances ?

[![Installer CyberQuiz](https://img.shields.io/badge/INSTALLER-CYBERQUIZ-8b5cf6?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Elikto/CyberQuiz/releases/download/apk-latest/CyberQuiz.apk)

**Télécharge, joue, progresse.**

</div>
