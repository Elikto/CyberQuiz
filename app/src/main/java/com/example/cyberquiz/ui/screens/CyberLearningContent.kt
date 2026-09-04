package com.example.cyberquiz.ui.screens

internal data class CyberDetailedLesson(
    val concept: String,
    val mechanism: String,
    val example: String,
    val confusion: String,
    val takeaway: String,
    val schema: List<String>
)

internal fun explainCyberChoice(term: String, category: String): String {
    val clean = term.trim()
    val key = clean.uppercase()

    val known = when (key) {
        "DNS" -> "DNS (Domain Name System) traduit des noms de domaine lisibles, comme example.com, en adresses IP utilisables par les machines."
        "DHCP" -> "DHCP (Dynamic Host Configuration Protocol) attribue automatiquement aux appareils des paramètres réseau comme l'adresse IP, la passerelle et les serveurs DNS."
        "ARP" -> "ARP (Address Resolution Protocol) permet, sur un réseau IPv4 local, d'associer une adresse IP à l'adresse MAC de la machine correspondante."
        "ICMP" -> "ICMP est un protocole de contrôle et de diagnostic réseau. Il est notamment utilisé par ping pour tester l'accessibilité d'un hôte."
        "HTTPS" -> "HTTPS est HTTP protégé par TLS. Il chiffre les échanges entre un client, souvent un navigateur, et un serveur Web."
        "SSH" -> "SSH (Secure Shell) permet d'administrer une machine distante via une connexion chiffrée, généralement en ligne de commande."
        "21" -> "Le port 21 est traditionnellement associé au canal de contrôle de FTP, un ancien protocole de transfert de fichiers."
        "53" -> "Le port 53 est principalement associé à DNS, en UDP pour beaucoup de requêtes courantes et en TCP dans certains cas."
        "443" -> "Le port TCP 443 est le port standard utilisé par HTTPS pour les communications Web chiffrées."
        "8080" -> "Le port 8080 est souvent utilisé comme port HTTP alternatif, par exemple pour des applications Web, des proxys ou des interfaces d'administration."
        "SUDO" -> "sudo permet à un utilisateur autorisé d'exécuter une commande avec les privilèges d'un autre compte, souvent root."
        "GREP" -> "grep recherche du texte correspondant à un motif dans un fichier ou dans la sortie d'une commande."
        "NANO" -> "nano est un éditeur de texte utilisable directement dans un terminal Linux."
        "PWD" -> "pwd affiche le chemin du répertoire de travail courant dans un shell Unix/Linux."
        "/ETC/PASSWD" -> "/etc/passwd contient les informations générales des comptes locaux Linux : nom, UID, GID, répertoire personnel et shell, entre autres."
        "/ETC/SHADOW" -> "/etc/shadow contient les informations sensibles d'authentification locales, notamment les empreintes de mots de passe et des paramètres d'expiration."
        "/VAR/LOG/AUTH.LOG" -> "/var/log/auth.log est, sur plusieurs distributions Linux, un journal lié à l'authentification et à certaines actions de sécurité."
        "/ETC/HOSTS" -> "/etc/hosts est un fichier local permettant d'associer manuellement des noms d'hôtes à des adresses IP."
        "RW-------" -> "rw------- signifie que seul le propriétaire peut lire et écrire le fichier ; le groupe et les autres utilisateurs n'ont aucun droit."
        "RW-R-----" -> "rw-r----- signifie : lecture/écriture pour le propriétaire, lecture pour le groupe, aucun droit pour les autres."
        "RW-RW-RW-" -> "rw-rw-rw- donne lecture et écriture au propriétaire, au groupe et aux autres utilisateurs. C'est très permissif."
        "R--R--R--" -> "r--r--r-- donne uniquement le droit de lecture au propriétaire, au groupe et aux autres utilisateurs."
        "BITLOCKER" -> "BitLocker est la technologie de chiffrement de volume intégrée à certaines éditions de Windows. Elle protège les données stockées sur le disque."
        "DEFRAG" -> "La défragmentation réorganise les blocs de fichiers sur certains supports ; elle n'est pas un mécanisme de chiffrement."
        "NOTEPAD" -> "Notepad est l'éditeur de texte simple intégré à Windows."
        "TASK SCHEDULER" -> "Task Scheduler est le Planificateur de tâches Windows. Il exécute automatiquement des programmes ou actions selon des déclencheurs."
        "GET-PROCESS" -> "Get-Process est une cmdlet PowerShell qui affiche les processus visibles et permet d'obtenir des informations sur eux."
        "GET-DNSCLIENT" -> "Get-DnsClient affiche des informations de configuration du client DNS Windows ; il ne sert pas à lister les processus."
        "NEW-ITEM" -> "New-Item est une cmdlet PowerShell qui crée un nouvel élément, par exemple un fichier, un dossier ou une clé selon le provider utilisé."
        "WRITE-HOST" -> "Write-Host affiche du texte directement dans la console PowerShell."
        "4624" -> "L'événement Windows Security 4624 indique généralement une ouverture de session réussie."
        "4625" -> "L'événement Windows Security 4625 indique généralement un échec d'ouverture de session et peut être utile pour détecter des tentatives d'authentification anormales."
        "4688" -> "L'événement Windows Security 4688 correspond à la création d'un nouveau processus lorsque l'audit concerné est activé."
        "7045" -> "L'événement System 7045 est couramment associé à l'installation d'un nouveau service Windows."
        "AES" -> "AES est un algorithme de chiffrement symétrique : une même clé secrète sert à chiffrer et à déchiffrer les données."
        "RSA" -> "RSA est un algorithme asymétrique fondé sur une paire clé publique / clé privée. Il peut servir au chiffrement ou à des mécanismes cryptographiques associés."
        "ECDSA" -> "ECDSA est un algorithme de signature numérique basé sur les courbes elliptiques. Il sert à vérifier authenticité et intégrité, pas à chiffrer un fichier."
        "SHA-256" -> "SHA-256 est une fonction de hachage cryptographique qui produit une empreinte de taille fixe. Un hachage n'est pas conçu pour être déchiffré."
        "HMAC" -> "HMAC combine une fonction de hachage avec une clé secrète afin de vérifier l'intégrité et l'authenticité d'un message."
        "BASE64" -> "Base64 est un encodage de données en caractères imprimables. Ce n'est ni un chiffrement ni un mécanisme d'authentification."
        "CRC32 UNIQUEMENT", "CRC32" -> "CRC32 est un code de détection d'erreurs utile contre des altérations accidentelles, mais il n'offre pas les garanties cryptographiques d'un HMAC."
        "XSS" -> "XSS (Cross-Site Scripting) permet d'injecter du contenu actif, souvent JavaScript, exécuté dans le navigateur d'une victime."
        "CSRF" -> "CSRF (Cross-Site Request Forgery) pousse le navigateur d'un utilisateur déjà authentifié à envoyer une requête qu'il n'avait pas l'intention d'effectuer."
        "SQL INJECTION" -> "Une injection SQL exploite une entrée mal contrôlée pour modifier la structure ou le comportement d'une requête SQL."
        "SSRF" -> "SSRF (Server-Side Request Forgery) pousse un serveur vulnérable à effectuer lui-même une requête vers une ressource choisie par l'attaquant."
        "SAMESITE" -> "SameSite est un attribut de cookie qui contrôle son envoi dans certains contextes inter-sites et peut réduire le risque de certaines attaques CSRF."
        "MAX-AGE UNIQUEMENT", "MAX-AGE" -> "Max-Age détermine la durée de vie d'un cookie. Il ne définit pas à lui seul son comportement inter-sites."
        "DOMAIN UNIQUEMENT", "DOMAIN" -> "L'attribut Domain précise pour quels domaines un cookie peut être envoyé. Il ne remplace pas SameSite pour la gestion du contexte inter-sites."
        "CONTENT-TYPE" -> "Content-Type est un en-tête HTTP décrivant le type de contenu transporté, par exemple application/json ou text/html."
        "SPYWARE" -> "Un spyware est un logiciel espion conçu pour collecter discrètement des informations sur une machine ou son utilisateur."
        "RANSOMWARE" -> "Un ransomware chiffre ou bloque des données ou systèmes afin d'exiger une rançon."
        "ROOTKIT" -> "Un rootkit regroupe des techniques permettant de maintenir un accès privilégié ou furtif et de masquer certaines activités malveillantes."
        "ADWARE" -> "Un adware affiche ou injecte de la publicité, souvent de manière intrusive. Tous les adwares ne sont pas des ransomwares ou des rootkits."
        "CHEVAL DE TROIE" -> "Un cheval de Troie se présente comme légitime ou utile afin d'inciter l'utilisateur à l'installer ou à l'exécuter."
        "PARE-FEU" -> "Un pare-feu filtre les communications réseau selon des règles. C'est un mécanisme de sécurité, pas un type de malware."
        "HYPERVISEUR" -> "Un hyperviseur permet d'exécuter et de gérer des machines virtuelles."
        "PROXY INVERSE" -> "Un proxy inverse reçoit des requêtes côté serveur puis les transmet à un ou plusieurs services en arrière-plan."
        "VISHING" -> "Le vishing est du phishing réalisé principalement par la voix, par exemple au téléphone."
        "SMURFING" -> "Une attaque Smurf est une ancienne forme de déni de service utilisant ICMP et l'amplification via des adresses de broadcast."
        "HASHING" -> "Le hashing consiste à calculer une empreinte à partir de données. Ce n'est pas une forme de phishing téléphonique."
        "HARDENING" -> "Le hardening, ou durcissement, consiste à réduire la surface d'attaque d'un système en renforçant sa configuration."
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "OSINT signifie Open Source Intelligence : collecte et analyse d'informations provenant de sources ouvertes accessibles légalement."
        "EXIF" -> "EXIF est un ensemble de métadonnées pouvant accompagner une image : date, appareil, paramètres de prise de vue et parfois coordonnées GPS."
        "NMAP" -> "Nmap est un outil de découverte et d'analyse réseau utilisé notamment pour identifier des hôtes, ports ouverts et services."
        "BURP SUITE" -> "Burp Suite est une plateforme de test de sécurité Web, connue notamment pour son proxy d'interception HTTP/HTTPS."
        "PAINT" -> "Paint est un logiciel de dessin Windows et n'est pas un outil de test de sécurité Web."
        "VLC" -> "VLC est un lecteur multimédia."
        "CALCULATOR" -> "Calculator est l'application calculatrice ; elle n'est pas un proxy d'interception."
        "KERBEROS" -> "Kerberos est un protocole d'authentification reposant sur des tickets. Il est central dans l'authentification des domaines Active Directory modernes."
        "LDAP" -> "LDAP est un protocole d'accès à des services d'annuaire. Active Directory l'utilise notamment pour consulter ou manipuler des objets d'annuaire selon les droits accordés."
        "NTLM" -> "NTLM est une famille de mécanismes d'authentification Microsoft plus anciens que Kerberos, encore présents dans certains environnements pour compatibilité."
        "GPO" -> "Une GPO (Group Policy Object) permet d'appliquer de manière centralisée des paramètres aux utilisateurs et ordinateurs d'un domaine Active Directory."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "MFA signifie authentification multifacteur : l'accès exige au moins deux facteurs indépendants, par exemple mot de passe et code temporaire."
        "IAM" -> "IAM (Identity and Access Management) regroupe les mécanismes permettant de gérer identités, authentification, rôles et autorisations."
        "SECURITY GROUP" -> "Dans de nombreux clouds, un Security Group agit comme un pare-feu logique contrôlant le trafic autorisé vers ou depuis des ressources."
        "BUCKET" -> "Un bucket est un conteneur de stockage objet dans plusieurs plateformes cloud. Une mauvaise configuration d'accès peut exposer des données."
        "VPN" -> "Un VPN crée un tunnel logique protégé entre deux points afin de transporter du trafic sur un réseau non maîtrisé."
        "MDM" -> "MDM (Mobile Device Management) permet d'administrer, configurer et sécuriser un parc d'appareils mobiles."
        "ROOTING" -> "Le rooting consiste à obtenir des privilèges élevés sur Android. Cela peut réduire certaines protections de sécurité si l'appareil est mal géré."
        "JAILBREAK" -> "Le jailbreak vise à contourner certaines restrictions d'iOS afin d'obtenir un contrôle plus large sur l'appareil."
        "SANDBOX" -> "Une sandbox isole une application ou un processus afin de limiter ses accès aux ressources du système."
        "LEAST PRIVILEGE" -> "Le principe du moindre privilège consiste à n'accorder que les permissions strictement nécessaires à une tâche."
        "ZERO TRUST" -> "Zero Trust est une approche de sécurité qui évite de faire confiance automatiquement à un utilisateur ou appareil seulement parce qu'il se trouve sur un réseau interne."
        "FAIL OPEN" -> "Fail open signifie qu'en cas de panne, un mécanisme laisse l'accès ou le trafic continuer. Cela favorise la disponibilité au détriment potentiel de la sécurité."
        "DEFENSE BY OBSCURITY" -> "La sécurité par l'obscurité repose principalement sur le secret du fonctionnement. Elle ne remplace pas des contrôles de sécurité robustes."
        "SIEM" -> "Un SIEM centralise et corrèle des journaux et événements de sécurité afin d'aider à détecter et investiguer des activités suspectes."
        "EDR" -> "Un EDR surveille les terminaux, collecte de la télémétrie et aide à détecter, investiguer et répondre à des menaces sur les postes et serveurs."
        "IDS" -> "Un IDS détecte des activités potentiellement malveillantes et génère des alertes ; selon le type, il peut analyser du trafic réseau ou l'activité d'un hôte."
        "IPS" -> "Un IPS inspecte du trafic et peut bloquer automatiquement certaines activités détectées comme malveillantes."
        "WAF" -> "Un WAF filtre le trafic HTTP/HTTPS à destination d'applications Web afin de bloquer certaines attaques applicatives."
        else -> null
    }

    if (known != null) return known

    if (clean.matches(Regex("^\\d+$"))) {
        return "« $clean » est une valeur numérique proposée dans cette question. Il faut l'interpréter dans le contexte « $category » : numéro de port, identifiant d'événement, quantité ou valeur technique selon l'énoncé."
    }

    if (clean.startsWith("/") || clean.contains("rw") || clean.contains("r--")) {
        return "« $clean » est une notation ou un chemin technique. Dans la catégorie $category, il faut regarder précisément ce que chaque symbole ou segment représente, car une petite différence peut changer les droits ou le rôle du fichier."
    }

    return if (clean.split(' ').size >= 4) {
        "Cette proposition signifie : « $clean ». C'est une description d'action ou de conséquence. Pour l'évaluer, compare précisément ce qu'elle affirme avec le mécanisme demandé dans la question et avec la fonction de la bonne réponse."
    } else {
        "« $clean » est une notion liée au domaine $category. Son rôle exact doit être distingué de celui des autres propositions : deux termes peuvent appartenir au même domaine tout en remplissant des fonctions très différentes."
    }
}

internal fun buildCyberDetailedLesson(
    question: String,
    category: String,
    answers: List<String>,
    correctIndex: Int,
    explanation: String
): CyberDetailedLesson {
    val correct = answers.getOrElse(correctIndex) { "la bonne réponse" }
    val other = answers.filterIndexed { index, _ -> index != correctIndex }

    val concept = "La notion centrale ici est « $correct ». ${explainCyberChoice(correct, category)}"

    val mechanism = when (category.lowercase()) {
        "réseaux" -> "Pour raisonner, suis le trajet d'une communication : une machine possède des paramètres réseau, identifie une destination, résout les informations nécessaires puis échange des paquets. La bonne réponse correspond à l'étape ou au protocole précisément demandé par l'énoncé."
        "linux" -> "Sous Linux, distingue toujours les commandes, les fichiers système et les permissions. Une commande réalise une action, un fichier stocke une information, et une permission détermine qui peut lire, écrire ou exécuter."
        "windows" -> "Sous Windows, sépare les fonctions du système : chiffrement, processus, journaux d'événements, services et administration PowerShell. L'énoncé donne généralement un indice sur la couche concernée."
        "cryptographie" -> "En cryptographie, commence par identifier l'objectif : confidentialité, intégrité, authenticité ou stockage d'une empreinte. Ensuite seulement choisis le mécanisme adapté : chiffrement, hachage, signature ou MAC."
        "sécurité web" -> "Pour une vulnérabilité Web, demande-toi où agit l'attaque : navigateur, requête vers le serveur, base de données, cookie ou requête émise par le serveur. Cette localisation permet souvent d'éliminer plusieurs réponses."
        "malware" -> "Pour différencier les malwares, observe leur comportement principal : espionner, se propager, se cacher, chiffrer ou recevoir des commandes. Le nom du malware décrit souvent ce comportement dominant."
        "ingénierie sociale" -> "En ingénierie sociale, le mécanisme cible surtout l'humain. Identifie le canal utilisé, le scénario de manipulation et l'action que l'attaquant cherche à provoquer."
        "osint" -> "En OSINT, la méthode consiste à collecter légalement des informations ouvertes, évaluer leur provenance, les recouper et conserver leur contexte pour éviter les conclusions trompeuses."
        "forensics" -> "En investigation numérique, la priorité est de préserver la preuve. On acquiert les données sans altérer la source, on vérifie l'intégrité et on documente les opérations réalisées."
        "pentest" -> "Un pentest est un test autorisé. Le raisonnement doit toujours rester dans un périmètre défini : découverte, validation contrôlée d'un risque, mesure de l'impact puis recommandation de correction."
        "active directory" -> "Dans Active Directory, distingue identité, authentification, annuaire et politiques. Kerberos gère notamment l'authentification par tickets, LDAP l'accès à l'annuaire et les GPO la configuration centralisée."
        "cloud security" -> "Dans le cloud, sépare identité, réseau et stockage. Une erreur IAM concerne les droits, un Security Group concerne le trafic réseau, et un bucket concerne le stockage objet."
        "mobile security" -> "En sécurité mobile, distingue gestion de flotte, privilèges système, isolation des applications et protection des données. Ces mécanismes agissent à des niveaux différents."
        "sécurité système" -> "En sécurité système, on réduit le risque en limitant les privilèges, en durcissant la configuration, en surveillant les événements et en segmentant les responsabilités."
        else -> "Repère le verbe principal de la question, identifie le rôle demandé puis compare chaque proposition à ce rôle précis."
    }

    val example = when (category.lowercase()) {
        "réseaux" -> "Exemple : lorsque tu ouvres un site, plusieurs mécanismes peuvent intervenir successivement. DNS peut résoudre le nom, ARP peut aider sur le réseau local, puis HTTPS peut protéger la connexion. Ils participent tous au réseau mais ne font pas la même chose."
        "linux" -> "Exemple : /etc/passwd, /etc/shadow et les permissions rw-r----- concernent tous Linux, mais l'un décrit des comptes, l'autre des informations d'authentification et le troisième des droits d'accès."
        "windows" -> "Exemple : BitLocker protège les données au repos, Get-Process observe des processus et l'événement 4625 renseigne sur un échec d'ouverture de session. Ce sont trois fonctions de sécurité très différentes."
        "cryptographie" -> "Exemple : AES chiffre des données, SHA-256 produit une empreinte et HMAC ajoute une clé secrète à un mécanisme de hachage pour authentifier un message."
        "sécurité web" -> "Exemple : XSS vise le navigateur, SQL injection la requête vers la base, CSRF exploite la session d'une victime et SSRF détourne les requêtes effectuées par un serveur."
        "active directory" -> "Exemple : un utilisateur ouvre une session, Kerberos peut fournir les tickets d'authentification, puis les droits dans l'annuaire et les GPO déterminent ce qu'il peut faire et quelle configuration s'applique."
        else -> "Exemple pratique : imagine que tu dois expliquer chaque proposition à quelqu'un qui débute. Si tu peux dire « ce terme sert à... » pour chaque choix, la différence avec la bonne réponse devient beaucoup plus évidente."
    }

    val confusion = "Le piège est de choisir une proposition simplement parce qu'elle appartient au même domaine. ${other.joinToString(prefix = "Les distracteurs ici sont « ", separator = " », « ", postfix = " ».")} Ils peuvent être techniquement réels, mais ils ne répondent pas exactement à ce que demande l'énoncé."

    val takeaway = "$explanation Retenir surtout l'association : « $correct » ↔ fonction précise demandée par la question."

    val schema = when (category.lowercase()) {
        "réseaux" -> listOf("Application / appareil", "Résolution ou configuration", "Communication réseau", "Service distant")
        "linux" -> listOf("Utilisateur", "Commande / fichier / permission", "Contrôle du système", "Résultat")
        "windows" -> listOf("Utilisateur ou système", "Composant Windows", "Journal / processus / protection", "Événement ou résultat")
        "cryptographie" -> listOf("Donnée d'origine", "Mécanisme cryptographique", "Clé ou fonction", "Donnée protégée / empreinte")
        "sécurité web" -> listOf("Navigateur", "Requête HTTP(S)", "Application serveur", "Données / ressource")
        "malware" -> listOf("Vecteur d'entrée", "Exécution", "Comportement malveillant", "Impact / contrôle")
        "ingénierie sociale" -> listOf("Attaquant", "Prétexte / message / appel", "Décision de la victime", "Action ou information obtenue")
        "osint" -> listOf("Source ouverte", "Collecte", "Recoupement", "Information validée")
        "forensics" -> listOf("Support original", "Acquisition protégée", "Vérification d'intégrité", "Analyse sur copie")
        "pentest" -> listOf("Autorisation", "Périmètre", "Test contrôlé", "Rapport et remédiation")
        "active directory" -> listOf("Utilisateur / machine", "Authentification", "Annuaire et politiques", "Accès aux ressources")
        "cloud security" -> listOf("Identité", "Autorisation", "Ressource cloud", "Journalisation / contrôle")
        "mobile security" -> listOf("Appareil", "Application", "Sandbox / permissions", "Données et services")
        else -> listOf("Besoin", "Contrôle de sécurité", "Vérification", "Risque réduit")
    }

    return CyberDetailedLesson(
        concept = concept,
        mechanism = mechanism,
        example = example,
        confusion = confusion,
        takeaway = takeaway,
        schema = schema
    )
}
