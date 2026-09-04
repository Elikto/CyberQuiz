package com.example.cyberquiz.ui.screens

internal data class BeginnerChoiceExplanation(
    val what: String,
    val purpose: String,
    val distinction: String
)

internal fun buildBeginnerChoiceExplanation(
    question: String,
    term: String,
    category: String,
    correctAnswer: String,
    isCorrect: Boolean
): BeginnerChoiceExplanation {
    val clean = term.trim()
    val key = clean.uppercase()
    val base = buildCyberChoiceDeepDive(clean, category)

    val what = when (key) {
        "DNS" -> "DNS est le service qui transforme un nom facile à retenir, comme cyberquiz.fr, en adresse IP utilisable par un ordinateur."
        "DHCP" -> "DHCP est le service qui donne automatiquement à un appareil ses réglages réseau lorsqu'il rejoint un réseau."
        "ARP" -> "ARP aide un appareil à retrouver, sur son réseau local, l'adresse matérielle correspondant à une adresse IP."
        "ICMP" -> "ICMP transporte des messages de contrôle et de diagnostic réseau. La commande ping l'utilise notamment."
        "HTTPS" -> "HTTPS est la version protégée des échanges Web entre ton navigateur et un site."
        "SSH" -> "SSH est un moyen sécurisé d'ouvrir une session sur un ordinateur distant et d'y lancer des commandes."
        "FTP" -> "FTP est un ancien protocole principalement utilisé pour transférer des fichiers entre un client et un serveur."
        "21" -> "21 est un numéro de port réseau traditionnellement associé au contrôle du protocole FTP."
        "53" -> "53 est le numéro de port le plus souvent associé au service DNS."
        "443" -> "443 est le numéro de port standard utilisé par HTTPS."
        "8080" -> "8080 est un port souvent utilisé comme alternative pour des services Web, mais ce n'est pas le port standard de HTTPS."
        "CD" -> "cd est une commande Linux qui sert à changer de dossier dans le terminal."
        "PWD" -> "pwd est une commande Linux qui affiche le dossier dans lequel tu te trouves actuellement."
        "LS" -> "ls est une commande Linux qui affiche les fichiers et dossiers présents à l'endroit où tu te trouves."
        "WHOAMI" -> "whoami est une commande qui affiche le nom de l'utilisateur actuellement utilisé."
        "SUDO" -> "sudo permet à un utilisateur autorisé de lancer ponctuellement une commande avec davantage de droits."
        "GREP" -> "grep est une commande qui recherche du texte dans un fichier ou dans le résultat d'une autre commande."
        "NANO" -> "nano est un petit éditeur de texte utilisable directement dans le terminal."
        "BITLOCKER" -> "BitLocker est la fonction Windows qui chiffre un disque pour protéger les données qu'il contient."
        "DEFRAG" -> "Defrag réorganise certaines données sur un disque. Il ne sert pas à chiffrer les fichiers."
        "NOTEPAD" -> "Notepad est l'éditeur de texte simple fourni avec Windows."
        "TASK SCHEDULER" -> "Task Scheduler, ou Planificateur de tâches, lance automatiquement des programmes ou scripts selon des conditions prévues."
        "AES" -> "AES est un algorithme de chiffrement symétrique : la même clé secrète sert à chiffrer et à déchiffrer."
        "RSA" -> "RSA est un système cryptographique asymétrique utilisant une clé publique et une clé privée."
        "ECDSA" -> "ECDSA sert principalement à créer et vérifier des signatures numériques."
        "SHA-256" -> "SHA-256 crée une empreinte numérique d'une donnée. Il ne sert pas à déchiffrer cette donnée ensuite."
        "LA CLÉ PUBLIQUE", "LA CLE PUBLIQUE", "CLÉ PUBLIQUE", "CLE PUBLIQUE" -> "Une clé publique est faite pour pouvoir être partagée. Elle fonctionne avec une clé privée qui doit rester secrète."
        "LA CLÉ PRIVÉE", "LA CLE PRIVEE", "CLÉ PRIVÉE", "CLE PRIVEE" -> "Une clé privée est la partie secrète d'une paire de clés cryptographiques. Elle ne doit pas être diffusée."
        "XSS" -> "XSS est une faille Web qui peut permettre à du code non prévu de s'exécuter dans le navigateur d'un utilisateur."
        "CSRF" -> "CSRF cherche à faire effectuer une action non voulue par le navigateur d'un utilisateur déjà connecté."
        "SQL INJECTION" -> "Une injection SQL apparaît quand une entrée utilisateur peut modifier dangereusement une requête envoyée à une base de données."
        "SSRF" -> "SSRF est une faille où un serveur est poussé à effectuer lui-même une requête vers une destination qu'il ne devrait pas contacter."
        "SPYWARE" -> "Un spyware est un logiciel espion qui cherche à collecter discrètement des informations."
        "RANSOMWARE" -> "Un ransomware bloque ou chiffre des données puis réclame généralement une rançon."
        "ROOTKIT" -> "Un rootkit cherche surtout à rester caché dans un système et à conserver un accès privilégié."
        "ADWARE" -> "Un adware est un logiciel qui affiche ou injecte de la publicité, parfois de façon très intrusive."
        "CHEVAL DE TROIE" -> "Un cheval de Troie se présente comme un programme légitime ou utile afin de pousser l'utilisateur à l'exécuter."
        "PARE-FEU" -> "Un pare-feu contrôle les communications réseau et applique des règles pour les autoriser ou les bloquer."
        "HYPERVISEUR" -> "Un hyperviseur permet de faire fonctionner plusieurs machines virtuelles sur un même ordinateur physique."
        "PROXY INVERSE" -> "Un proxy inverse reçoit des requêtes destinées à un service puis les transmet à un ou plusieurs serveurs en arrière-plan."
        "PHISHING" -> "Le phishing consiste à tromper une personne pour lui faire révéler une information ou effectuer une action dangereuse."
        "VISHING" -> "Le vishing est du phishing réalisé principalement par téléphone ou par la voix."
        "SMURFING" -> "Smurfing désigne une ancienne technique de déni de service utilisant ICMP. Ce n'est pas une forme de phishing."
        "HASHING" -> "Le hashing, ou hachage, consiste à calculer une empreinte à partir d'une donnée."
        "HARDENING" -> "Le hardening, ou durcissement, consiste à renforcer la configuration d'un système et à réduire ce qui peut être attaqué."
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "OSINT désigne la recherche et l'analyse d'informations provenant de sources ouvertes et accessibles légalement."
        "NMAP" -> "Nmap est un outil d'analyse réseau utilisé, dans un cadre autorisé, pour repérer des machines, ports et services accessibles."
        "GIT" -> "Git est un outil de gestion de versions utilisé pour suivre les modifications de fichiers et de code."
        "DOCKER" -> "Docker sert à lancer des applications dans des conteneurs isolés avec leur environnement."
        "GRADLE" -> "Gradle est un outil d'automatisation de compilation et de construction de projets logiciels."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "MFA signifie authentification multifacteur : on demande au moins deux preuves différentes pour vérifier l'identité d'une personne."
        else -> simpleSentence(base.definition)
    }

    val purpose = when (key) {
        "DNS" -> "Il sert à trouver où contacter un service à partir de son nom. Exemple : ton navigateur demande l'adresse IP de cyberquiz.fr avant de pouvoir s'y connecter."
        "DHCP" -> "Il sert à éviter de configurer chaque appareil à la main. Il peut fournir une adresse IP, une passerelle et les serveurs DNS."
        "ARP" -> "Il sert à préparer une communication avec un autre appareil du même réseau local en retrouvant son adresse matérielle."
        "ICMP" -> "Il sert surtout à vérifier ou signaler l'état d'une communication IP, par exemple pour savoir si une machine répond."
        "HTTPS", "443" -> "Il sert à protéger une connexion Web afin que les données échangées ne circulent pas simplement en clair."
        "PWD" -> "Il sert uniquement à savoir où tu te trouves dans l'arborescence des dossiers. Il ne change pas de dossier et ne liste pas son contenu."
        "CD" -> "Il sert à se déplacer vers un autre dossier dans le terminal."
        "LS" -> "Il sert à voir le contenu d'un dossier."
        "WHOAMI" -> "Il sert à vérifier quel compte exécute actuellement les commandes."
        "SUDO" -> "Il sert à effectuer une action qui nécessite des droits supplémentaires, si ton compte y est autorisé."
        "BITLOCKER" -> "Il sert à rendre les données du disque illisibles sans la clé ou le mécanisme de déverrouillage approprié."
        "AES" -> "Il sert à rendre des données illisibles sans la clé secrète correcte."
        "RSA" -> "Il sert à certaines opérations utilisant deux clés différentes, notamment dans des mécanismes de chiffrement ou de signature."
        "ECDSA" -> "Il sert à prouver qu'une donnée a bien été signée par le détenteur de la clé privée correspondante."
        "SHA-256" -> "Il sert à produire une empreinte que l'on peut comparer pour vérifier si une donnée a changé."
        "XSS" -> "Comprendre XSS sert à empêcher une application Web de traiter du contenu utilisateur comme du code exécutable dans le navigateur."
        "SQL INJECTION" -> "Comprendre l'injection SQL sert à empêcher une entrée utilisateur de modifier la structure d'une requête vers la base de données."
        "RANSOMWARE" -> "Il sert au criminel à rendre les données indisponibles pour exercer une pression et réclamer de l'argent."
        "PHISHING" -> "Il sert à manipuler la victime plutôt qu'à attaquer directement une faiblesse technique d'un ordinateur."
        "NMAP" -> "Il sert à comprendre quels services réseau sont visibles sur un périmètre que l'on a l'autorisation d'analyser."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "Il sert à éviter qu'un mot de passe volé suffise à lui seul pour accéder à un compte."
        else -> simpleSentence(base.action)
    }

    val distinction = if (isCorrect) {
        "Dans cette question, c'est le bon choix : « $clean » correspond précisément à ce qui est demandé."
    } else {
        val correct = correctAnswer.ifBlank { "la bonne réponse" }
        "Dans cette question, « $clean » n'est pas le bon choix. « $correct » répond directement à la question, alors que « $clean » a le rôle expliqué juste au-dessus."
    }

    return BeginnerChoiceExplanation(
        what = what,
        purpose = purpose,
        distinction = distinction
    )
}

private fun simpleSentence(text: String): String {
    val clean = text.replace("\n", " ").trim()
    if (clean.isBlank()) return "C'est une notion utilisée dans ce domaine de la cybersécurité."
    val first = clean.substringBefore(". ").trim()
    return if (first.endsWith('.')) first else "$first."
}
