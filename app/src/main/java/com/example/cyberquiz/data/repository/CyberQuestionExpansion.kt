package com.example.cyberquiz.data.repository

import com.example.cyberquiz.data.database.QuestionEntity
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.Difficulty

internal const val CYBER_EXPANSION_SOURCE = "cyber_core_v2"

internal fun cyberExpansionQuestions(): List<QuestionEntity> {
    fun q(
        category: Category,
        difficulty: Difficulty,
        question: String,
        a: String,
        b: String,
        c: String,
        d: String,
        correctIndex: Int,
        explanation: String
    ) = QuestionEntity(
        quizType = "CYBERSECURITY",
        category = category.label,
        difficulty = difficulty.name,
        question = question,
        answerA = a,
        answerB = b,
        answerC = c,
        answerD = d,
        correctIndex = correctIndex,
        explanation = explanation,
        source = CYBER_EXPANSION_SOURCE
    )

    return listOf(
        q(
            Category.RESEAUX,
            Difficulty.EASY,
            "Quel protocole permet d'attribuer automatiquement une adresse IP à un appareil sur un réseau ?",
            "DNS",
            "DHCP",
            "HTTPS",
            "SSH",
            1,
            "DHCP peut attribuer automatiquement une adresse IP et d'autres paramètres réseau aux clients."
        ),
        q(
            Category.RESEAUX,
            Difficulty.MEDIUM,
            "À quoi sert principalement ARP sur un réseau IPv4 local ?",
            "Associer une adresse IP à une adresse MAC",
            "Chiffrer les paquets",
            "Résoudre un nom de domaine",
            "Attribuer un mot de passe Wi-Fi",
            0,
            "ARP permet notamment de retrouver l'adresse MAC correspondant à une adresse IPv4 sur le réseau local."
        ),
        q(
            Category.RESEAUX,
            Difficulty.HARD,
            "Combien d'adresses hôtes sont normalement utilisables dans un sous-réseau IPv4 /26 classique ?",
            "30",
            "62",
            "64",
            "126",
            1,
            "Un /26 contient 64 adresses au total ; en retirant l'adresse réseau et l'adresse de broadcast, 62 restent utilisables pour des hôtes."
        ),

        q(
            Category.LINUX,
            Difficulty.EASY,
            "Quelle commande permet généralement d'exécuter une commande avec des privilèges élevés sous Linux ?",
            "sudo",
            "grep",
            "nano",
            "pwd",
            0,
            "sudo permet à un utilisateur autorisé d'exécuter une commande avec les privilèges d'un autre utilisateur, souvent root."
        ),
        q(
            Category.LINUX,
            Difficulty.MEDIUM,
            "Que signifie généralement la permission numérique 600 appliquée à un fichier Linux ?",
            "Tout le monde peut lire et écrire",
            "Le propriétaire peut lire et écrire, les autres n'ont aucun droit",
            "Le propriétaire peut seulement exécuter",
            "Tous les utilisateurs peuvent uniquement lire",
            1,
            "600 correspond à rw------- : lecture et écriture pour le propriétaire uniquement."
        ),
        q(
            Category.LINUX,
            Difficulty.HARD,
            "Que signifie la permission Linux 640 sur un fichier ?",
            "rw-r-----",
            "rwxr-x---",
            "rw-rw-rw-",
            "r--r--r--",
            0,
            "640 correspond à lecture/écriture pour le propriétaire, lecture pour le groupe et aucun droit pour les autres."
        ),

        q(
            Category.WINDOWS,
            Difficulty.EASY,
            "Quelle technologie Windows chiffre un volume de disque complet ?",
            "BitLocker",
            "Defrag",
            "Notepad",
            "Task Scheduler",
            0,
            "BitLocker est la fonctionnalité Windows de chiffrement de volume."
        ),
        q(
            Category.WINDOWS,
            Difficulty.MEDIUM,
            "Quel cmdlet PowerShell affiche la liste des processus en cours ?",
            "Get-Process",
            "Get-DnsClient",
            "New-Item",
            "Write-Host",
            0,
            "Get-Process retourne les processus visibles sur la machine."
        ),
        q(
            Category.WINDOWS,
            Difficulty.HARD,
            "Quel événement de sécurité Windows est couramment associé à un échec d'ouverture de session ?",
            "4624",
            "4625",
            "4688",
            "7045",
            1,
            "L'événement 4625 correspond généralement à un échec d'ouverture de session, tandis que 4624 correspond à une ouverture réussie."
        ),

        q(
            Category.CRYPTO,
            Difficulty.EASY,
            "Dans un chiffrement asymétrique, quelle clé peut être partagée publiquement ?",
            "La clé publique",
            "La clé privée",
            "Le mot de passe maître",
            "Le sel du disque",
            0,
            "La clé publique est destinée à être diffusée, tandis que la clé privée doit rester secrète."
        ),
        q(
            Category.CRYPTO,
            Difficulty.MEDIUM,
            "Pourquoi ajoute-t-on un sel aléatoire avant de hacher un mot de passe ?",
            "Pour accélérer le réseau",
            "Pour éviter que des mots de passe identiques produisent systématiquement le même hash",
            "Pour rendre le mot de passe lisible",
            "Pour remplacer le chiffrement TLS",
            1,
            "Un sel unique complique notamment l'utilisation de tables précalculées et différencie les empreintes de mots de passe identiques."
        ),
        q(
            Category.CRYPTO,
            Difficulty.HARD,
            "Quel mécanisme combine une fonction de hachage avec une clé secrète pour authentifier un message ?",
            "HMAC",
            "DHCP",
            "Base64",
            "CRC32 uniquement",
            0,
            "HMAC utilise une fonction de hachage cryptographique avec une clé secrète afin de vérifier l'intégrité et l'authenticité d'un message."
        ),

        q(
            Category.WEB,
            Difficulty.EASY,
            "Quelle vulnérabilité permet typiquement d'exécuter du JavaScript malveillant dans le navigateur d'un utilisateur ?",
            "XSS",
            "DNSSEC",
            "NAT",
            "ARP",
            0,
            "Une faille XSS permet d'injecter du contenu actif, souvent du JavaScript, exécuté dans le contexte du navigateur de la victime."
        ),
        q(
            Category.WEB,
            Difficulty.MEDIUM,
            "Quel est l'objectif principal d'une attaque CSRF ?",
            "Forcer un utilisateur authentifié à effectuer une action non désirée",
            "Deviner une adresse MAC",
            "Chiffrer un disque local",
            "Scanner des ports UDP",
            0,
            "CSRF exploite la session déjà authentifiée d'une victime pour déclencher une requête qu'elle n'avait pas l'intention d'effectuer."
        ),
        q(
            Category.WEB,
            Difficulty.HARD,
            "Quel attribut de cookie peut contribuer à réduire le risque de CSRF en limitant l'envoi du cookie dans certains contextes inter-sites ?",
            "SameSite",
            "Max-Age uniquement",
            "Domain uniquement",
            "Content-Type",
            0,
            "SameSite contrôle dans quels contextes inter-sites le navigateur joint le cookie et peut ainsi réduire certaines attaques CSRF."
        ),

        q(
            Category.MALWARE,
            Difficulty.EASY,
            "Quel type de malware se présente souvent comme un programme légitime pour tromper l'utilisateur ?",
            "Cheval de Troie",
            "Pare-feu",
            "Hyperviseur",
            "Proxy inverse",
            0,
            "Un cheval de Troie cherche à paraître légitime ou utile afin d'inciter l'utilisateur à l'exécuter."
        ),
        q(
            Category.MALWARE,
            Difficulty.MEDIUM,
            "Quelle caractéristique distingue généralement un ver informatique ?",
            "Il peut se propager automatiquement entre systèmes",
            "Il ne fonctionne que hors ligne",
            "Il est toujours un document PDF",
            "Il sert uniquement à compresser des fichiers",
            0,
            "Un ver est conçu pour se répliquer et se propager, souvent via le réseau, sans nécessiter la même interaction utilisateur qu'un cheval de Troie."
        ),
        q(
            Category.MALWARE,
            Difficulty.HARD,
            "Dans le contexte d'un malware, à quoi sert généralement une infrastructure C2 ?",
            "À communiquer avec des systèmes compromis pour leur envoyer des instructions",
            "À attribuer des adresses IP par DHCP",
            "À signer des certificats TLS publics",
            "À effectuer des sauvegardes hors ligne",
            0,
            "Une infrastructure de command and control permet à un opérateur de communiquer avec des systèmes compromis et de leur transmettre des commandes."
        ),

        q(
            Category.SOCIAL,
            Difficulty.EASY,
            "Comment appelle-t-on le phishing réalisé principalement par téléphone ou appel vocal ?",
            "Vishing",
            "Smurfing",
            "Hashing",
            "Hardening",
            0,
            "Vishing vient de voice phishing et désigne les tentatives d'ingénierie sociale utilisant la voix ou le téléphone."
        ),
        q(
            Category.SOCIAL,
            Difficulty.MEDIUM,
            "Qu'est-ce que le pretexting en ingénierie sociale ?",
            "Créer un scénario crédible pour obtenir une information ou une action",
            "Mettre à jour un système d'exploitation",
            "Chiffrer une base de données",
            "Défragmenter un disque",
            0,
            "Le pretexting repose sur une histoire ou une identité inventée servant de prétexte pour gagner la confiance de la cible."
        ),
        q(
            Category.SOCIAL,
            Difficulty.HARD,
            "Qu'est-ce qu'une attaque de MFA fatigue ?",
            "Multiplier les demandes de validation MFA pour pousser la victime à en accepter une",
            "Supprimer automatiquement le second facteur",
            "Changer la fréquence Wi-Fi",
            "Renouveler un certificat expiré",
            0,
            "La MFA fatigue consiste à envoyer de nombreuses demandes d'approbation afin de provoquer une validation accidentelle ou par lassitude."
        ),

        q(
            Category.OSINT,
            Difficulty.EASY,
            "Que signifie OSINT ?",
            "Open Source Intelligence",
            "Operating System Internal Network Tool",
            "Online Secure Identity Token",
            "Open System Intrusion Technique",
            0,
            "OSINT désigne la collecte et l'analyse d'informations provenant de sources ouvertes et accessibles légalement."
        ),
        q(
            Category.OSINT,
            Difficulty.MEDIUM,
            "Quelle information peut parfois être présente dans les métadonnées EXIF d'une photo ?",
            "Des coordonnées GPS",
            "Le mot de passe du routeur par définition",
            "La clé privée TLS du site visité",
            "Le code PIN de la carte SIM par défaut",
            0,
            "Selon l'appareil et ses réglages, les métadonnées EXIF peuvent contenir notamment la date, le modèle de l'appareil et parfois la géolocalisation."
        ),
        q(
            Category.OSINT,
            Difficulty.HARD,
            "Pourquoi faut-il recouper une information trouvée en OSINT avec plusieurs sources indépendantes ?",
            "Pour réduire le risque de se fier à une information fausse ou sortie de son contexte",
            "Pour augmenter automatiquement les privilèges système",
            "Pour modifier les métadonnées d'origine",
            "Pour contourner le chiffrement des disques",
            0,
            "Le recoupement aide à évaluer la fiabilité d'une information et à détecter les erreurs, manipulations ou données obsolètes."
        ),

        q(
            Category.FORENSICS,
            Difficulty.EASY,
            "Pourquoi calcule-t-on souvent une empreinte cryptographique d'une image disque en investigation numérique ?",
            "Pour vérifier son intégrité",
            "Pour augmenter sa capacité",
            "Pour modifier les journaux",
            "Pour accélérer le processeur",
            0,
            "Une empreinte permet de vérifier que la copie analysée n'a pas été modifiée depuis son acquisition."
        ),
        q(
            Category.FORENSICS,
            Difficulty.MEDIUM,
            "À quoi sert un bloqueur d'écriture lors de l'acquisition d'un support ?",
            "À empêcher la modification du support source",
            "À accélérer la connexion Internet",
            "À supprimer les fichiers cachés",
            "À contourner un mot de passe",
            0,
            "Un write blocker empêche les écritures sur le support examiné afin de préserver l'intégrité de la preuve."
        ),
        q(
            Category.FORENSICS,
            Difficulty.HARD,
            "Pourquoi la mémoire vive est-elle souvent considérée comme une source de données volatile ?",
            "Son contenu peut disparaître lorsque la machine est arrêtée",
            "Elle est toujours stockée dans le cloud",
            "Elle ne peut contenir aucun processus",
            "Elle est identique sur toutes les machines",
            0,
            "La RAM contient des données temporaires pouvant être perdues lors d'un arrêt ou d'un redémarrage, ce qui influence l'ordre d'acquisition des preuves."
        ),

        q(
            Category.PENTEST,
            Difficulty.EASY,
            "Quel élément doit être défini avant de commencer un test d'intrusion autorisé ?",
            "Le périmètre et l'autorisation du test",
            "La couleur du bureau Windows",
            "Le fournisseur DNS public de l'attaquant",
            "Le format de compression préféré",
            0,
            "Un pentest doit commencer avec une autorisation explicite et un périmètre clairement défini."
        ),
        q(
            Category.PENTEST,
            Difficulty.MEDIUM,
            "Quel outil est couramment utilisé comme proxy d'interception pour tester des applications Web ?",
            "Burp Suite",
            "Paint",
            "VLC",
            "Calculator",
            0,
            "Burp Suite peut intercepter et analyser les échanges HTTP/HTTPS dans le cadre de tests d'applications Web autorisés."
        ),
        q(
            Category.PENTEST,
            Difficulty.HARD,
            "Pourquoi un rapport de pentest doit-il associer une vulnérabilité à son impact et à une recommandation ?",
            "Pour aider à prioriser et corriger le risque de façon exploitable",
            "Pour masquer les preuves techniques",
            "Pour empêcher toute reproduction du problème par l'équipe défensive",
            "Pour remplacer les sauvegardes",
            0,
            "Un bon rapport transforme un constat technique en information actionnable : risque, preuve suffisante, impact et piste de remédiation."
        ),

        q(
            Category.AD,
            Difficulty.EASY,
            "Quel rôle joue principalement un contrôleur de domaine dans Active Directory ?",
            "Il fournit notamment l'authentification et les services d'annuaire du domaine",
            "Il remplace tous les pare-feu",
            "Il héberge obligatoirement tous les sites Web",
            "Il sert uniquement de serveur d'impression",
            0,
            "Un contrôleur de domaine héberge Active Directory Domain Services et participe notamment à l'authentification des utilisateurs et machines du domaine."
        ),
        q(
            Category.AD,
            Difficulty.MEDIUM,
            "À quoi servent principalement les GPO dans un environnement Active Directory ?",
            "À appliquer de manière centralisée des paramètres et politiques aux utilisateurs et ordinateurs",
            "À convertir les adresses IPv4 en IPv6",
            "À remplacer Kerberos par FTP",
            "À chiffrer automatiquement tous les e-mails externes",
            0,
            "Les Group Policy Objects permettent de déployer et imposer de nombreux paramètres de configuration et de sécurité."
        ),
        q(
            Category.AD,
            Difficulty.HARD,
            "Pourquoi le compte KRBTGT est-il particulièrement sensible dans un domaine Active Directory ?",
            "Il est lié au service Kerberos du domaine et à la signature des tickets TGT",
            "Il gère exclusivement les imprimantes",
            "Il sert uniquement à la résolution DNS publique",
            "Il contient la configuration BitLocker de tous les postes par définition",
            0,
            "KRBTGT est le compte de service utilisé par le KDC Kerberos du domaine ; sa protection est donc particulièrement importante."
        ),

        q(
            Category.CLOUD,
            Difficulty.EASY,
            "Que décrit le modèle de responsabilité partagée dans le cloud ?",
            "La répartition des responsabilités de sécurité entre le fournisseur et le client",
            "Le partage obligatoire des mots de passe entre utilisateurs",
            "L'absence de responsabilité du client",
            "La suppression automatique de tous les journaux",
            0,
            "Dans le cloud, certaines couches sont sécurisées par le fournisseur et d'autres restent sous la responsabilité du client selon le service utilisé."
        ),
        q(
            Category.CLOUD,
            Difficulty.MEDIUM,
            "Quel risque peut provoquer une mauvaise configuration rendant un stockage objet public ?",
            "Une exposition involontaire de données",
            "Une amélioration automatique du chiffrement",
            "Une suppression du besoin d'IAM",
            "Une isolation réseau plus forte par défaut",
            0,
            "Un stockage rendu public par erreur peut exposer des fichiers ou données qui devaient rester privés."
        ),
        q(
            Category.CLOUD,
            Difficulty.HARD,
            "Pourquoi privilégier des identités ou rôles temporaires plutôt que des clés d'accès statiques de longue durée quand c'est possible ?",
            "Pour réduire l'exposition liée à des secrets persistants",
            "Pour désactiver la journalisation",
            "Pour rendre tous les services publics",
            "Pour empêcher l'utilisation du moindre privilège",
            0,
            "Des identifiants temporaires limitent la durée d'utilisation possible d'un secret compromis et s'intègrent mieux à des mécanismes de rotation et d'identité."
        ),

        q(
            Category.MOBILE,
            Difficulty.EASY,
            "Quel principe est recommandé lors de l'attribution des permissions à une application mobile ?",
            "N'accorder que les permissions nécessaires",
            "Accorder toutes les permissions par défaut",
            "Désactiver tous les mécanismes de verrouillage",
            "Partager le code PIN entre applications",
            0,
            "Le principe du moindre privilège s'applique aussi aux applications mobiles : elles ne devraient recevoir que les autorisations nécessaires."
        ),
        q(
            Category.MOBILE,
            Difficulty.MEDIUM,
            "Pourquoi un appareil rooté ou jailbreaké peut-il présenter davantage de risques ?",
            "Certaines protections du système peuvent être contournées ou affaiblies",
            "Il chiffre toujours mieux les données",
            "Il empêche toute installation d'application",
            "Il supprime automatiquement les connexions réseau",
            0,
            "Le root ou jailbreak modifie le modèle de sécurité du système et peut permettre à des applications ou attaquants d'obtenir des privilèges plus élevés."
        ),
        q(
            Category.MOBILE,
            Difficulty.HARD,
            "Quel est l'objectif principal du certificate pinning dans une application mobile ?",
            "Limiter l'acceptation des certificats ou clés attendus pour réduire certains risques d'interception TLS",
            "Remplacer totalement TLS",
            "Rendre toutes les API publiques",
            "Stocker les mots de passe en clair",
            0,
            "Le pinning permet à une application de vérifier plus strictement l'identité cryptographique attendue d'un serveur, en complément de la validation TLS classique."
        ),

        q(
            Category.SYSTEM,
            Difficulty.EASY,
            "Pourquoi appliquer régulièrement les correctifs de sécurité d'un système ?",
            "Pour corriger notamment des vulnérabilités connues",
            "Pour supprimer tous les comptes utilisateurs",
            "Pour désactiver les sauvegardes",
            "Pour rendre tous les ports accessibles",
            0,
            "Les mises à jour de sécurité corrigent des vulnérabilités et réduisent l'exposition à des attaques connues."
        ),
        q(
            Category.SYSTEM,
            Difficulty.MEDIUM,
            "Que signifie le principe de défense en profondeur ?",
            "Combiner plusieurs couches de contrôles de sécurité",
            "Utiliser un seul contrôle très puissant",
            "Désactiver la journalisation pour gagner en performance",
            "Placer tous les services sur le même compte administrateur",
            0,
            "La défense en profondeur repose sur plusieurs protections complémentaires afin qu'une seule défaillance ne compromette pas tout le système."
        ),
        q(
            Category.SYSTEM,
            Difficulty.HARD,
            "Quel est l'objectif principal de l'ASLR sur un système moderne ?",
            "Rendre moins prévisible l'emplacement en mémoire de certaines zones et composants",
            "Chiffrer les sauvegardes hors ligne",
            "Attribuer automatiquement une adresse IP",
            "Créer des comptes Active Directory",
            0,
            "ASLR randomise l'emplacement de régions mémoire afin de compliquer l'exploitation de certaines vulnérabilités mémoire."
        )
    )
}
