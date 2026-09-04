package com.example.cyberquiz.ui.screens

internal data class CyberDetailedLesson(
    val concept: String,
    val mechanism: String,
    val example: String,
    val confusion: String,
    val takeaway: String,
    val schema: List<String>
)

internal data class CyberChoiceDeepDive(
    val definition: String,
    val action: String,
    val actions: List<String>,
    val details: String,
    val example: String,
    val demoTitle: String,
    val demo: String,
    val analogy: String,
    val schema: List<String>
)

internal fun explainCyberChoice(term: String, category: String): String {
    val clean = term.trim()
    val key = clean.uppercase()

    val known = when (key) {
        "DNS" -> "DNS signifie Domain Name System. C'est le service qui traduit un nom lisible comme cyberquiz.fr en adresse IP utilisable par les machines."
        "DHCP" -> "DHCP signifie Dynamic Host Configuration Protocol. Il configure automatiquement un appareil sur le réseau en lui fournissant notamment une adresse IP et d'autres paramètres réseau."
        "ARP" -> "ARP signifie Address Resolution Protocol. Sur un réseau IPv4 local, il permet de retrouver l'adresse MAC correspondant à une adresse IP."
        "ICMP" -> "ICMP signifie Internet Control Message Protocol. Il transporte des messages de contrôle, d'erreur et de diagnostic pour IP ; ping l'utilise notamment."
        "HTTPS" -> "HTTPS signifie HTTP Secure. Il transporte HTTP dans un canal protégé par TLS afin de chiffrer et authentifier la communication Web."
        "SSH" -> "SSH signifie Secure Shell. Il permet d'ouvrir une session distante et d'administrer une machine via un canal chiffré."
        "FTP" -> "FTP signifie File Transfer Protocol. C'est un ancien protocole de transfert de fichiers ; sa version classique ne chiffre pas les échanges."
        "21" -> "Le port TCP 21 est traditionnellement le port de contrôle de FTP."
        "53" -> "Le port 53 est principalement associé à DNS. DNS utilise souvent UDP 53 et peut aussi utiliser TCP 53."
        "443" -> "Le port TCP 443 est le port standard de HTTPS. Le numéro de port identifie le service ; c'est TLS qui réalise le chiffrement."
        "8080" -> "Le port 8080 est fréquemment utilisé comme port HTTP alternatif, par exemple pour un serveur Web de développement, un proxy ou une interface d'administration."
        "SUDO" -> "sudo signifie approximativement « superuser do ». Il permet à un utilisateur autorisé d'exécuter une commande avec les privilèges d'un autre compte, souvent root."
        "GREP" -> "grep est une commande de recherche de texte. Elle filtre des lignes en conservant celles qui correspondent à un mot ou à un motif."
        "NANO" -> "nano est un éditeur de texte utilisable directement dans un terminal Linux."
        "PWD" -> "pwd signifie print working directory. Il affiche le chemin du répertoire dans lequel le shell se trouve actuellement."
        "CD" -> "cd signifie change directory. Cette commande change le répertoire de travail courant du shell."
        "LS" -> "ls signifie list. Cette commande affiche le contenu d'un répertoire sous Linux et Unix."
        "WHOAMI" -> "whoami affiche le nom du compte utilisateur sous lequel la commande est exécutée."
        "/ETC/PASSWD" -> "/etc/passwd est un fichier Linux contenant les informations générales des comptes locaux : nom, UID, GID, répertoire personnel et shell."
        "/ETC/SHADOW" -> "/etc/shadow contient les informations sensibles d'authentification locales, notamment les empreintes de mots de passe et paramètres d'expiration."
        "/VAR/LOG/AUTH.LOG" -> "/var/log/auth.log est, sur plusieurs distributions Linux, un journal regroupant des événements d'authentification et certaines actions liées à la sécurité."
        "/ETC/HOSTS" -> "/etc/hosts est un fichier local permettant d'associer manuellement un nom d'hôte à une adresse IP sans demander DNS."
        "RW-------" -> "rw------- est une permission Linux : le propriétaire peut lire et écrire ; le groupe et les autres n'ont aucun droit."
        "RW-R-----" -> "rw-r----- signifie lecture/écriture pour le propriétaire, lecture pour le groupe et aucun droit pour les autres."
        "RW-RW-RW-" -> "rw-rw-rw- donne lecture et écriture au propriétaire, au groupe et aux autres utilisateurs."
        "R--R--R--" -> "r--r--r-- donne uniquement le droit de lecture au propriétaire, au groupe et aux autres utilisateurs."
        "BITLOCKER" -> "BitLocker est la technologie Windows de chiffrement de volume. Elle protège les données stockées sur un disque lorsqu'elles sont au repos."
        "DEFRAG" -> "Defrag réorganise les blocs de fichiers sur certains supports afin d'optimiser leur disposition. Ce n'est pas un mécanisme de chiffrement."
        "NOTEPAD" -> "Notepad est l'éditeur de texte simple intégré à Windows."
        "TASK SCHEDULER" -> "Task Scheduler est le Planificateur de tâches Windows. Il exécute automatiquement des programmes ou scripts selon un horaire ou un événement."
        "GET-PROCESS" -> "Get-Process est une cmdlet PowerShell qui liste les processus visibles et fournit des informations sur leur exécution."
        "GET-DNSCLIENT" -> "Get-DnsClient affiche des informations sur la configuration du client DNS Windows."
        "NEW-ITEM" -> "New-Item est une cmdlet PowerShell qui crée un élément, par exemple un fichier, un dossier ou une clé selon le provider utilisé."
        "WRITE-HOST" -> "Write-Host affiche du texte directement dans la console PowerShell."
        "4624" -> "L'événement Windows Security 4624 correspond généralement à une ouverture de session réussie."
        "4625" -> "L'événement Windows Security 4625 correspond généralement à un échec d'ouverture de session."
        "4688" -> "L'événement Windows Security 4688 indique la création d'un nouveau processus lorsque l'audit concerné est activé."
        "7045" -> "L'événement Windows System 7045 est couramment associé à l'installation d'un nouveau service."
        "AES" -> "AES signifie Advanced Encryption Standard. C'est un chiffrement symétrique : la même clé secrète sert à chiffrer et à déchiffrer."
        "RSA" -> "RSA est un algorithme cryptographique asymétrique utilisant une paire de clés : une publique et une privée."
        "ECDSA" -> "ECDSA signifie Elliptic Curve Digital Signature Algorithm. Il produit et vérifie des signatures numériques avec des clés sur courbes elliptiques."
        "SHA-256" -> "SHA-256 est une fonction de hachage cryptographique qui transforme une donnée en une empreinte de 256 bits."
        "HMAC" -> "HMAC signifie Hash-based Message Authentication Code. Il combine une fonction de hachage et une clé secrète pour vérifier intégrité et authenticité."
        "BASE64" -> "Base64 est un encodage qui transforme des octets en caractères imprimables. Ce n'est pas du chiffrement et il est facilement réversible."
        "CRC32 UNIQUEMENT", "CRC32" -> "CRC32 est un code de détection d'erreurs utile pour repérer des altérations accidentelles, mais il n'est pas conçu comme une protection cryptographique."
        "XSS" -> "XSS signifie Cross-Site Scripting. Une faille XSS permet à du contenu contrôlé par un attaquant d'être interprété comme du code dans le navigateur d'une victime."
        "CSRF" -> "CSRF signifie Cross-Site Request Forgery. L'attaque tente de faire exécuter au navigateur d'une victime authentifiée une action qu'elle n'avait pas voulue."
        "SQL INJECTION" -> "Une injection SQL consiste à faire entrer des données non fiables dans une requête SQL de manière à en modifier le comportement."
        "SSRF" -> "SSRF signifie Server-Side Request Forgery. Une faille SSRF pousse le serveur vulnérable à envoyer lui-même une requête vers une destination choisie."
        "SAMESITE" -> "SameSite est un attribut de cookie qui limite son envoi dans certains contextes inter-sites et peut réduire le risque de certaines attaques CSRF."
        "HTTPONLY" -> "HttpOnly est un attribut de cookie qui empêche JavaScript exécuté dans la page de lire directement ce cookie."
        "MAX-AGE UNIQUEMENT", "MAX-AGE" -> "Max-Age est un attribut de cookie indiquant pendant combien de secondes il doit rester valide."
        "DOMAIN UNIQUEMENT", "DOMAIN" -> "Domain est un attribut de cookie qui précise le domaine pour lequel ce cookie peut être envoyé."
        "CONTENT-TYPE" -> "Content-Type est un en-tête HTTP indiquant le type de contenu transporté, par exemple application/json ou text/html."
        "SPYWARE" -> "Un spyware est un logiciel espion conçu pour collecter discrètement des informations sur une machine ou son utilisateur."
        "RANSOMWARE" -> "Un ransomware est un logiciel malveillant qui bloque ou chiffre des données afin d'exiger une rançon."
        "ROOTKIT" -> "Un rootkit est un ensemble de techniques visant à maintenir un accès furtif ou privilégié et à masquer certaines activités sur un système."
        "ADWARE" -> "Un adware est un logiciel qui affiche ou injecte de la publicité, souvent de manière intrusive."
        "CHEVAL DE TROIE" -> "Un cheval de Troie est un programme qui se présente comme légitime ou utile afin d'inciter l'utilisateur à l'exécuter."
        "PARE-FEU" -> "Un pare-feu applique des règles pour autoriser ou bloquer des communications réseau."
        "HYPERVISEUR" -> "Un hyperviseur est la couche qui crée et gère des machines virtuelles en partageant les ressources du matériel physique."
        "PROXY INVERSE" -> "Un proxy inverse reçoit les requêtes destinées à un service côté serveur puis les transmet à un ou plusieurs serveurs en arrière-plan."
        "VISHING" -> "Le vishing est du phishing réalisé par la voix, le plus souvent lors d'un appel téléphonique."
        "SMURFING" -> "Une attaque Smurf est une ancienne forme de déni de service utilisant ICMP et l'amplification via des adresses de broadcast."
        "HASHING" -> "Le hashing est l'action de calculer une empreinte de taille fixe à partir d'une donnée."
        "HARDENING" -> "Le hardening, ou durcissement, consiste à réduire la surface d'attaque en supprimant ou sécurisant ce qui n'est pas nécessaire."
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "OSINT signifie Open Source Intelligence : collecte et analyse d'informations provenant de sources ouvertes accessibles légalement."
        "EXIF" -> "EXIF est un format de métadonnées associé à certaines images : date, appareil, paramètres de prise de vue et parfois coordonnées GPS."
        "NMAP" -> "Nmap est un outil de découverte et d'analyse réseau utilisé pour identifier des hôtes, ports ouverts et parfois services sur un périmètre autorisé."
        "BURP SUITE" -> "Burp Suite est une plateforme de test de sécurité Web, notamment utilisée comme proxy d'interception HTTP/HTTPS dans un environnement autorisé."
        "PAINT" -> "Paint est un logiciel de dessin Windows."
        "VLC" -> "VLC est un lecteur multimédia."
        "CALCULATOR" -> "Calculator est l'application calculatrice de Windows."
        "KERBEROS" -> "Kerberos est un protocole d'authentification par tickets, très utilisé dans les domaines Active Directory."
        "LDAP" -> "LDAP signifie Lightweight Directory Access Protocol. Il sert à consulter et, selon les droits, modifier des objets dans un service d'annuaire."
        "NTLM" -> "NTLM est une famille de mécanismes d'authentification Microsoft plus anciens que Kerberos, encore présents pour certains scénarios de compatibilité."
        "GPO" -> "GPO signifie Group Policy Object. Une GPO applique de manière centralisée des paramètres aux utilisateurs et ordinateurs d'un domaine Active Directory."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "MFA signifie Multi-Factor Authentication. L'accès nécessite plusieurs facteurs indépendants, par exemple un mot de passe et une validation sur téléphone."
        "IAM" -> "IAM signifie Identity and Access Management. Il regroupe la gestion des identités, rôles, authentification et autorisations."
        "SECURITY GROUP" -> "Dans de nombreux clouds, un Security Group est un pare-feu logique qui autorise ou refuse certains flux réseau vers ou depuis une ressource."
        "BUCKET" -> "Un bucket est un conteneur de stockage objet dans de nombreuses plateformes cloud. Il contient des objets, par exemple des fichiers de sauvegarde ou des médias."
        "VPN" -> "VPN signifie Virtual Private Network. Il crée un tunnel logique protégé pour transporter du trafic entre deux points à travers un réseau non maîtrisé."
        "MDM" -> "MDM signifie Mobile Device Management. Il permet d'administrer, configurer et sécuriser à distance un parc d'appareils mobiles."
        "ROOTING" -> "Le rooting consiste à obtenir des privilèges élevés sur Android afin de contrôler des parties normalement protégées du système."
        "JAILBREAK" -> "Le jailbreak contourne certaines restrictions d'iOS afin d'obtenir davantage de contrôle sur l'appareil."
        "SANDBOX" -> "Une sandbox isole une application ou un processus afin de limiter les ressources et données auxquelles il peut accéder."
        "LEAST PRIVILEGE" -> "Least privilege signifie moindre privilège : un compte ou processus ne reçoit que les permissions nécessaires à sa tâche."
        "ZERO TRUST" -> "Zero Trust est une approche où aucun utilisateur, appareil ou emplacement n'est automatiquement considéré comme fiable ; chaque accès doit être vérifié."
        "FAIL OPEN" -> "Fail open signifie qu'en cas de panne d'un contrôle, l'accès ou le trafic continue d'être autorisé."
        "DEFENSE BY OBSCURITY" -> "Defense by obscurity, ou sécurité par l'obscurité, consiste à compter principalement sur le secret du fonctionnement plutôt que sur des contrôles robustes."
        "SIEM" -> "SIEM signifie Security Information and Event Management. Il centralise et corrèle des journaux pour aider à détecter et investiguer des événements de sécurité."
        "EDR" -> "EDR signifie Endpoint Detection and Response. Il surveille les postes et serveurs, collecte leur activité et aide à détecter puis investiguer des comportements suspects."
        "IDS" -> "IDS signifie Intrusion Detection System. Il détecte des activités potentiellement malveillantes et génère des alertes."
        "IPS" -> "IPS signifie Intrusion Prevention System. Il inspecte le trafic et peut bloquer automatiquement certaines activités détectées comme malveillantes."
        "WAF" -> "WAF signifie Web Application Firewall. Il filtre les requêtes HTTP/HTTPS afin de bloquer certaines attaques dirigées contre une application Web."
        else -> null
    }

    if (known != null) return known

    if (clean.matches(Regex("^\\d+$"))) {
        return "« $clean » est une valeur numérique technique. Selon l'énoncé, elle peut représenter un port, un identifiant d'événement, une quantité d'adresses ou une autre valeur de configuration."
    }

    if (clean.startsWith("/") || clean.contains("rw") || clean.contains("r--")) {
        return "« $clean » est une notation ou un chemin technique. Dans $category, chaque symbole ou segment peut modifier précisément le fichier, le rôle ou les permissions concernés."
    }

    return if (clean.split(' ').size >= 4) {
        "Cette proposition décrit l'action ou le résultat suivant : « $clean ». Elle doit être comparée mot par mot à ce que demande l'énoncé."
    } else {
        "« $clean » est une notion du domaine $category. Elle possède une fonction propre qu'il faut distinguer des autres propositions."
    }
}

internal fun summarizeCyberChoice(term: String, category: String): String {
    val full = explainCyberChoice(term, category).trim()
    val first = full.substringBefore(".").trim()
    val second = full.substringAfter(".", "").substringBefore(".").trim()
    return when {
        first.length > 145 -> first.take(142) + "…"
        second.isNotBlank() && first.length < 85 -> "$first. $second."
        else -> "$first."
    }
}

internal fun buildCyberChoiceDeepDive(term: String, category: String): CyberChoiceDeepDive {
    val clean = term.trim()
    val key = clean.uppercase()
    val definition = explainCyberChoice(clean, category)

    val action = when (key) {
        "DNS" -> "DNS sert à retrouver l'adresse IP correspondant à un nom. Sans cette résolution, une application connaissant seulement cyberquiz.fr ne saurait pas directement quelle adresse réseau contacter."
        "DHCP" -> "DHCP sert à automatiser la configuration IP d'un client. Il évite de saisir manuellement l'adresse IP, le masque, la passerelle, les DNS et la durée de validité de cette configuration."
        "ARP" -> "ARP sert à préparer l'envoi d'une trame Ethernet sur le réseau local : lorsqu'une machine connaît l'IP de la destination mais pas sa MAC, ARP permet de découvrir cette MAC."
        "ICMP" -> "ICMP sert au contrôle et au diagnostic du réseau IP. Il permet notamment de signaler certaines erreurs et de tester si une machine répond."
        "HTTPS", "443" -> "HTTPS sert à protéger les échanges Web. TLS chiffre les données en transit, permet au client de vérifier l'identité du serveur via son certificat et protège l'intégrité des échanges."
        "SSH" -> "SSH sert à administrer une machine distante de façon chiffrée : ouvrir un shell, lancer des commandes, transférer des données via des outils compatibles et créer des tunnels."
        "FTP", "21" -> "FTP sert à transférer des fichiers entre un client et un serveur. Le port 21 transporte traditionnellement les commandes de contrôle du protocole FTP."
        "SUDO" -> "sudo sert à exécuter ponctuellement une commande avec des privilèges supplémentaires sans rester connecté en permanence comme root."
        "GREP" -> "grep sert à retrouver rapidement des lignes contenant un mot ou correspondant à un motif dans un fichier ou dans la sortie d'une autre commande."
        "NANO" -> "nano sert à créer ou modifier un fichier texte directement depuis un terminal."
        "PWD" -> "pwd sert à vérifier exactement dans quel répertoire le shell travaille actuellement."
        "CD" -> "cd sert à déplacer le contexte du shell vers un autre répertoire."
        "LS" -> "ls sert à voir les fichiers et dossiers présents dans un répertoire et, avec des options, leurs droits, propriétaires et tailles."
        "WHOAMI" -> "whoami sert à vérifier sous quel compte utilisateur la session ou la commande s'exécute."
        "/ETC/PASSWD" -> "/etc/passwd sert de base locale pour décrire les comptes : identifiant, UID, groupe principal, répertoire personnel et shell."
        "/ETC/SHADOW" -> "/etc/shadow sert à stocker de façon plus protégée les informations d'authentification locales, notamment les empreintes de mots de passe."
        "/VAR/LOG/AUTH.LOG" -> "/var/log/auth.log sert à conserver une trace de nombreux événements d'authentification afin d'aider au diagnostic et à l'investigation."
        "/ETC/HOSTS" -> "/etc/hosts sert à créer des correspondances locales nom→IP prioritaires dans de nombreux scénarios, sans interroger un serveur DNS."
        "BITLOCKER" -> "BitLocker sert à protéger les données d'un volume lorsque le disque est volé, retiré ou lu hors du système autorisé."
        "GET-PROCESS" -> "Get-Process sert à observer les processus en cours, leur identifiant et différentes informations d'exécution."
        "4624" -> "L'événement 4624 sert à tracer une authentification réussie ; il permet de savoir qu'un compte a obtenu une session et fournit du contexte sur cette connexion."
        "4625" -> "L'événement 4625 sert à tracer un échec d'ouverture de session. Une série inhabituelle de 4625 peut aider à repérer une erreur de configuration ou des tentatives d'authentification suspectes."
        "4688" -> "L'événement 4688 sert à tracer la création de processus quand l'audit correspondant est activé ; il aide à comprendre quels programmes ont été lancés."
        "7045" -> "L'événement 7045 sert à signaler l'installation d'un service Windows et peut être utile pour suivre des changements système."
        "AES" -> "AES sert à rendre des données illisibles sans la clé secrète appropriée. On l'utilise pour protéger des fichiers, sauvegardes, disques ou flux lorsqu'un chiffrement symétrique est adapté."
        "RSA" -> "RSA sert à réaliser des opérations asymétriques avec une paire de clés, par exemple certains mécanismes de chiffrement de petites données ou de signature selon le protocole."
        "ECDSA" -> "ECDSA sert à prouver qu'une donnée a été signée avec la clé privée correspondante et qu'elle n'a pas été modifiée après signature."
        "SHA-256" -> "SHA-256 sert à produire une empreinte déterministe d'une donnée. On peut comparer deux empreintes pour vérifier si le contenu est identique."
        "HMAC" -> "HMAC sert à vérifier à la fois l'intégrité d'un message et le fait qu'il provient d'une partie connaissant une clé secrète partagée."
        "BASE64" -> "Base64 sert à représenter des données binaires avec des caractères textuels, par exemple pour les transporter dans certains formats. Il ne protège pas la confidentialité."
        "XSS" -> "XSS décrit une vulnérabilité où du contenu non fiable finit exécuté dans le navigateur. Comprendre XSS sert à sécuriser la façon dont une application réaffiche les données utilisateurs."
        "CSRF" -> "CSRF décrit un abus de la session d'une victime. Comprendre CSRF sert à protéger les actions sensibles contre des requêtes déclenchées depuis un autre site."
        "SQL INJECTION" -> "L'injection SQL décrit un défaut de séparation entre données utilisateur et syntaxe SQL. La comprendre sert à construire des accès base de données sûrs avec des requêtes paramétrées."
        "SSRF" -> "SSRF décrit un serveur qui effectue une requête réseau qu'il n'aurait pas dû accepter de faire. La protection consiste notamment à contrôler les destinations et entrées autorisées."
        "SAMESITE" -> "SameSite sert à contrôler dans quels contextes inter-sites le navigateur envoie un cookie."
        "HTTPONLY" -> "HttpOnly sert à empêcher le JavaScript exécuté dans une page d'accéder directement à un cookie sensible."
        "KERBEROS" -> "Kerberos sert à authentifier des utilisateurs et services à l'aide de tickets afin d'éviter de transmettre le mot de passe à chaque ressource."
        "LDAP" -> "LDAP sert à interroger un annuaire : rechercher un utilisateur, un groupe, un ordinateur ou d'autres objets et lire leurs attributs selon les droits disponibles."
        "NTLM" -> "NTLM sert à authentifier un utilisateur ou service dans certains scénarios Microsoft, notamment lorsque Kerberos n'est pas utilisé."
        "GPO" -> "Une GPO sert à pousser de façon centralisée des paramètres : stratégie de mot de passe, configuration de sécurité, scripts, restrictions ou paramètres système."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "MFA sert à éviter qu'un mot de passe volé suffise à lui seul pour se connecter. Le système exige une seconde preuve indépendante."
        "NMAP" -> "Nmap sert à cartographier un périmètre autorisé : vérifier quels hôtes répondent, quels ports sont ouverts et parfois quels services sont exposés."
        "BURP SUITE" -> "Burp Suite sert à observer et modifier, dans un laboratoire ou un test autorisé, les requêtes HTTP/HTTPS entre un navigateur et une application Web."
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "L'OSINT sert à construire une connaissance à partir de sources ouvertes : sites publics, documents, métadonnées, registres ou publications accessibles légalement."
        "EXIF" -> "EXIF sert à stocker des informations techniques avec une photo. Ces métadonnées peuvent aider à comprendre quand, comment et parfois où une image a été prise."
        "SIEM" -> "Un SIEM sert à regrouper des logs provenant de nombreuses sources, les rechercher et les corréler pour identifier des événements intéressants."
        "EDR" -> "Un EDR sert à surveiller finement les terminaux et à fournir de la télémétrie pour détecter, comprendre et traiter des comportements suspects."
        "IDS" -> "Un IDS sert à détecter et alerter lorsqu'un trafic ou comportement correspond à une activité suspecte."
        "IPS" -> "Un IPS sert à détecter puis bloquer automatiquement certains trafics jugés malveillants."
        "WAF" -> "Un WAF sert à inspecter les requêtes Web et à bloquer certaines attaques applicatives avant qu'elles n'atteignent l'application."
        "FIREWALL", "PARE-FEU" -> "Un pare-feu sert à contrôler les communications réseau en appliquant des règles basées notamment sur les adresses, ports, protocoles et états de connexion."
        "VPN" -> "Un VPN sert à protéger le trafic entre deux points en créant un tunnel chiffré à travers un réseau non maîtrisé."
        "MDM" -> "Un MDM sert à administrer un parc mobile : imposer des réglages, déployer des profils, appliquer des politiques et parfois effacer à distance des données professionnelles."
        "SANDBOX" -> "Une sandbox sert à limiter ce qu'une application peut lire, modifier ou exécuter afin qu'un problème dans cette application ait moins d'impact sur le reste du système."
        "LEAST PRIVILEGE" -> "Le moindre privilège sert à réduire l'impact d'une erreur ou compromission en limitant chaque compte ou processus au strict nécessaire."
        "ZERO TRUST" -> "Zero Trust sert à réduire la confiance implicite : identité, appareil, contexte et droit demandé sont vérifiés avant l'accès."
        "HARDENING" -> "Le hardening sert à réduire la surface d'attaque en désactivant les services inutiles, appliquant les mises à jour, renforçant les configurations et limitant les privilèges."
        else -> "Cette notion sert à réaliser une fonction précise dans le domaine $category. Pour la comprendre, il faut identifier ce qu'elle reçoit en entrée, l'action qu'elle réalise et le résultat qu'elle produit."
    }

    val actions = when (key) {
        "DNS" -> listOf("reçoit un nom de domaine", "cherche ou récupère l'enregistrement correspondant", "renvoie une adresse IP ou une autre information DNS", "met souvent le résultat en cache pendant une durée définie")
        "DHCP" -> listOf("détecte la demande d'un client sans configuration", "propose une adresse IP disponible", "fournit masque, passerelle, DNS et durée de bail", "enregistre le bail pour éviter d'attribuer la même IP simultanément")
        "ARP" -> listOf("émet une requête sur le LAN pour une IP cible", "reçoit la MAC du propriétaire de cette IP", "place la correspondance IP↔MAC en cache", "permet ensuite de construire la trame Ethernet")
        "ICMP" -> listOf("transporte des messages de diagnostic", "signale certaines erreurs réseau", "permet des Echo Request / Echo Reply utilisés par ping")
        "HTTPS", "443" -> listOf("établit une connexion vers le service Web", "négocie TLS", "vérifie le certificat du serveur", "crée des clés de session", "chiffre et authentifie les échanges HTTP")
        "SSH" -> listOf("établit un canal chiffré", "authentifie le serveur", "authentifie l'utilisateur", "ouvre un shell ou exécute une commande distante")
        "SUDO" -> listOf("vérifie que l'utilisateur est autorisé", "applique la politique sudoers", "lance uniquement la commande demandée avec les privilèges prévus", "journalise généralement l'action")
        "GET-PROCESS" -> listOf("interroge les processus visibles", "retourne leurs noms et PID", "permet de filtrer ou trier les résultats dans PowerShell")
        "BITLOCKER" -> listOf("chiffre les secteurs du volume", "protège la clé de volume", "déverrouille le volume lorsque les conditions d'accès sont satisfaites", "conserve les données illisibles sans la clé de récupération appropriée")
        "AES" -> listOf("prend des données et une clé secrète", "applique les transformations AES", "produit des données chiffrées", "effectue l'opération inverse avec la clé correcte")
        "SHA-256" -> listOf("lit la donnée d'entrée", "calcule une empreinte de 256 bits", "produit toujours la même empreinte pour exactement la même entrée", "permet de comparer l'intégrité sans stocker une copie complète")
        "KERBEROS" -> listOf("authentifie l'utilisateur auprès du service d'authentification", "délivre un ticket initial", "émet ensuite des tickets pour les services autorisés", "présente ces tickets aux ressources sans renvoyer le mot de passe")
        "NMAP" -> listOf("envoie des sondes sur un périmètre autorisé", "observe les réponses", "classe les ports selon leur état", "peut estimer le service visible derrière certains ports")
        "SIEM" -> listOf("collecte des logs", "normalise ou indexe les événements", "permet des recherches", "corrèle des signaux provenant de plusieurs sources", "génère des alertes ou tableaux de bord")
        "EDR" -> listOf("collecte les activités des endpoints", "observe processus, fichiers, connexions et autres événements", "applique des règles ou analyses", "aide l'analyste à investiguer et répondre")
        else -> listOf("reçoit une entrée ou un contexte", "applique sa fonction technique", "produit un résultat observable", "interagit avec les autres composants de $category selon le scénario")
    }

    val details = when (key) {
        "DNS" -> "Une application envoie une question DNS à un résolveur. Si la réponse n'est pas déjà en cache, le résolveur peut interroger la hiérarchie DNS jusqu'au serveur faisant autorité pour le domaine. Il renvoie ensuite l'enregistrement demandé, par exemple une adresse IPv4 de type A. Le client peut alors contacter cette IP. DNS répond donc à « où se trouve ce nom ? », pas à « quelle IP dois-je attribuer à mon PC ? »."
        "DHCP" -> "Sur IPv4, on retient souvent le cycle DORA. 1) Discover : le client, qui n'a pas encore d'IP utilisable, cherche un serveur DHCP. 2) Offer : le serveur propose une configuration. 3) Request : le client demande officiellement cette offre. 4) ACK : le serveur confirme le bail. L'appareil configure alors son interface avec l'IP, le masque, la passerelle et les DNS reçus. Plus tard, il renouvelle son bail avant expiration."
        "ARP" -> "Une machine compare d'abord l'IP cible à son propre sous-réseau. Si la destination est locale, elle doit envoyer une trame Ethernet directement à la MAC de la cible. Si elle ne connaît pas cette MAC, elle diffuse une requête ARP. La machine possédant l'IP répond. La correspondance est mise en cache pour éviter de redemander à chaque paquet. Si la destination est distante, la machine résout plutôt la MAC de sa passerelle."
        "ICMP" -> "ICMP fonctionne avec IP et transporte des messages de contrôle. Avec ping, l'émetteur envoie un Echo Request ; si la destination et les règles réseau le permettent, elle renvoie un Echo Reply. D'autres messages ICMP peuvent indiquer qu'une destination est inaccessible ou qu'un délai de vie a expiré. ICMP ne transporte pas la page Web ou le fichier lui-même : il donne des informations sur le fonctionnement du réseau."
        "HTTPS", "443" -> "Le client ouvre d'abord une connexion vers le serveur, souvent TCP 443. Vient ensuite le handshake TLS : négociation des algorithmes, réception du certificat, validation de l'identité du serveur et établissement de secrets de session. Une fois le canal TLS établi, les requêtes et réponses HTTP sont chiffrées. Le port 443 indique où joindre le service ; TLS est le mécanisme qui protège réellement les données."
        "SSH" -> "Le client contacte le service SSH, généralement sur TCP 22. Une négociation cryptographique établit un canal chiffré et le client vérifie la clé d'hôte du serveur. L'utilisateur s'authentifie ensuite, par exemple avec une clé ou un mot de passe selon la configuration. Le canal sécurisé transporte alors le terminal distant ou d'autres sous-services SSH."
        "FTP", "21" -> "Le client FTP se connecte généralement au port 21 pour envoyer des commandes de contrôle. Les transferts de données utilisent un canal séparé selon le mode actif ou passif. Dans FTP classique, les identifiants et données ne sont pas protégés par TLS ; on préfère souvent SFTP ou FTPS lorsqu'un transfert sécurisé est nécessaire."
        "SUDO" -> "Quand tu tapes sudo devant une commande, sudo consulte sa politique pour savoir si ton compte est autorisé à exécuter cette commande comme un autre utilisateur. Il peut demander ton mot de passe, puis crée le processus avec l'identité et les privilèges prévus. L'intérêt est de n'élever les droits que pour l'action nécessaire."
        "GREP" -> "grep lit un fichier ou un flux ligne par ligne, teste chaque ligne contre un texte ou une expression régulière, puis affiche les lignes correspondantes. Il est souvent placé après un pipe pour filtrer la sortie d'une autre commande."
        "BITLOCKER" -> "BitLocker chiffre le volume avec une clé de chiffrement. Cette clé est elle-même protégée par un ou plusieurs protecteurs : TPM, PIN, mot de passe ou clé de récupération selon le scénario. Lorsqu'un démarrage autorisé réussit, le volume est déverrouillé. Si quelqu'un retire le disque et tente de le lire ailleurs sans le protecteur requis, les secteurs restent chiffrés."
        "GET-PROCESS" -> "PowerShell interroge le système pour récupérer les processus accessibles à l'utilisateur. Chaque objet retourné contient des propriétés comme le nom, le PID et différentes mesures. Comme ce sont de vrais objets PowerShell, tu peux les filtrer, trier ou sélectionner sans parser du texte brut."
        "4625" -> "Lorsqu'une tentative d'ouverture de session Windows échoue et que l'audit le permet, Windows écrit un événement 4625 dans le journal Security. L'événement contient notamment le compte ciblé, le type de connexion et des informations de provenance selon le scénario. Un événement isolé peut être banal ; une série répétée mérite davantage d'analyse."
        "AES" -> "AES chiffre des blocs avec une clé de 128, 192 ou 256 bits. En pratique, AES est utilisé dans un mode comme GCM qui définit comment traiter un message plus long que le bloc et peut aussi fournir une authentification. On génère généralement un nonce ou IV selon le mode. Le résultat est un ciphertext illisible sans la clé. La sécurité dépend énormément de la protection de la clé et de l'utilisation correcte du mode."
        "RSA" -> "RSA crée une paire de clés mathématiquement liées. La clé publique peut être diffusée ; la clé privée reste secrète. Selon le protocole, RSA peut intervenir dans le chiffrement de petites données ou dans les signatures. Pour de gros volumes, les systèmes modernes utilisent plutôt un chiffrement symétrique rapide et emploient l'asymétrique pour protéger ou négocier les clés."
        "ECDSA" -> "ECDSA prend le condensat d'un message et une clé privée pour calculer une signature. Le destinataire utilise la clé publique correspondante pour vérifier que la signature est valide. ECDSA ne rend pas le message secret : il sert à vérifier authenticité et intégrité."
        "SHA-256" -> "SHA-256 prend une entrée de taille quelconque et produit 256 bits. L'algorithme est déterministe et conçu pour qu'il soit très difficile de retrouver l'entrée à partir de l'empreinte ou de fabriquer deux contenus différents avec la même empreinte. Il ne faut donc pas confondre hachage et chiffrement : un hash n'est pas fait pour être déchiffré."
        "HMAC" -> "HMAC mélange une clé secrète et un message avec une fonction de hachage selon une construction définie. Le destinataire possédant la même clé recalcule le HMAC et compare le résultat. Si le message change ou si l'expéditeur ne connaît pas la clé, la vérification échoue."
        "BASE64" -> "Base64 regroupe les données binaires en blocs puis les représente avec un alphabet de caractères imprimables. C'est pratique pour intégrer des octets dans du texte, mais n'importe qui peut décoder la valeur : il n'y a aucune clé secrète."
        "XSS" -> "Une faille XSS apparaît lorsque l'application réinjecte une donnée contrôlée par l'utilisateur dans un contexte HTML/JavaScript sans traitement adapté. Le navigateur de la victime interprète alors la donnée comme du contenu actif. La défense dépend du contexte : encodage de sortie, templates sûrs, Content Security Policy et limitation des APIs dangereuses."
        "CSRF" -> "Le navigateur envoie souvent automatiquement les cookies de session au site concerné. Une page externe tente alors de provoquer une requête vers ce site. Si le serveur vérifie seulement le cookie, il peut croire que l'utilisateur a volontairement demandé l'action. Des jetons anti-CSRF et une politique SameSite adaptée aident à empêcher cela."
        "SQL INJECTION" -> "Le problème apparaît lorsqu'une application construit la requête SQL en mélangeant directement la syntaxe et des données non fiables. Une requête paramétrée envoie séparément le texte SQL et les valeurs : la base sait alors que la valeur utilisateur est une donnée, pas un morceau de syntaxe à exécuter."
        "SSRF" -> "Dans une SSRF, l'application accepte par exemple une URL à charger et fait la requête côté serveur. Sans validation suffisante, l'utilisateur peut influencer la destination. La défense consiste à limiter les destinations autorisées, valider précisément les URLs et segmenter les accès réseau du serveur."
        "KERBEROS" -> "L'utilisateur s'authentifie auprès du KDC et reçoit un Ticket Granting Ticket. Lorsqu'il veut accéder à un service, il utilise ce ticket pour demander un ticket de service. Il présente ensuite ce ticket au service concerné. Le mot de passe n'est donc pas renvoyé à chaque serveur, et l'authentification reste centralisée."
        "LDAP" -> "Un client se connecte à un service d'annuaire, s'authentifie selon le scénario puis envoie des recherches LDAP avec une base et un filtre. Le serveur parcourt l'annuaire et renvoie les objets et attributs autorisés. LDAP décrit l'accès à l'annuaire ; ce n'est pas, à lui seul, le protocole de tickets Kerberos."
        "GPO" -> "Les GPO sont stockées et liées à des sites, domaines ou unités d'organisation. Les machines et utilisateurs récupèrent les stratégies qui leur sont applicables, puis Windows calcule l'ordre de traitement et applique les paramètres. Cela permet de configurer des centaines de postes sans modifier chacun manuellement."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "Après le premier facteur, par exemple le mot de passe, le service demande une preuve appartenant à une autre catégorie : possession d'un appareil, clé physique ou biométrie selon le système. Un attaquant possédant uniquement le mot de passe reste donc bloqué, sauf s'il parvient aussi à contourner ou tromper le second facteur."
        "NMAP" -> "Nmap envoie différents types de sondes vers des hôtes autorisés puis interprète les réponses. Il peut distinguer des ports ouverts, fermés ou filtrés et, avec certaines options, interroger le service pour estimer sa version. Un pare-feu ou un filtrage peut modifier ce que Nmap observe."
        "BURP SUITE" -> "On configure un navigateur de laboratoire pour faire passer son trafic Web par le proxy Burp. Burp affiche alors chaque requête et réponse. L'analyste peut les inspecter, les envoyer dans d'autres outils de test et comprendre précisément les paramètres, cookies, en-têtes et réponses de l'application autorisée."
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "Une démarche OSINT définit d'abord la question à résoudre, collecte des sources ouvertes, évalue leur fiabilité, recoupe les informations et conserve le contexte. Une donnée publique n'est pas automatiquement vraie : la validation et la provenance sont essentielles."
        "EXIF" -> "Lorsqu'un appareil enregistre une photo, il peut ajouter des champs EXIF. Un outil peut les lire sans modifier les pixels de l'image. Selon les réglages, on peut trouver la date, le modèle d'appareil, les paramètres optiques et parfois une position GPS."
        "SIEM" -> "Les équipements, serveurs et applications envoient leurs journaux vers le SIEM. Il les indexe, permet des recherches et applique des règles ou corrélations. Par exemple, un échec de connexion suivi d'une connexion réussie puis d'une action sensible peut être rapproché dans une même enquête."
        "EDR" -> "Un agent EDR installé sur les terminaux collecte de la télémétrie : création de processus, accès fichiers, connexions et autres événements selon le produit. Ces données alimentent des détections et une console d'investigation. L'analyste peut suivre une chronologie et, selon les droits et le produit, isoler un poste ou lancer des actions de réponse."
        else -> "Commence par la définition : $definition Ensuite identifie précisément l'entrée de cette notion, les actions qu'elle effectue et la sortie qu'elle produit. Dans $category, compare aussi les composants voisins : deux termes peuvent intervenir dans la même chaîne sans avoir la même responsabilité."
    }

    val example = when (key) {
        "DNS" -> "Tu saisis www.example.com. Ton PC ne connaît que ce nom. DNS lui renvoie l'IP du serveur, puis le navigateur peut établir la connexion."
        "DHCP" -> "Tu arrives chez un ami et connectes ton téléphone au Wi-Fi. Tu n'entres ni IP, ni masque, ni passerelle : le serveur DHCP du routeur te fournit automatiquement ces paramètres pour une durée appelée bail."
        "ARP" -> "Ton PC veut parler à 192.168.1.20 sur le même Wi-Fi. Il connaît l'IP mais pas la MAC : ARP demande sur le LAN qui possède 192.168.1.20, puis mémorise la réponse."
        "ICMP" -> "Tu soupçonnes qu'un serveur ne répond plus. Un ping envoie des Echo Request. Si des Echo Reply reviennent, tu sais qu'un chemin IP fonctionne au moins pour ce test."
        "HTTPS", "443" -> "Tu te connectes à ta banque. Le navigateur vérifie le certificat du serveur, établit TLS puis chiffre les identifiants et les pages échangées."
        "SSH" -> "Tu dois administrer un serveur Linux dans ton laboratoire. Tu ouvres une session SSH depuis ton PC au lieu de te déplacer devant la machine."
        "SUDO" -> "Tu veux installer une mise à jour sans travailler toute la journée en root. Tu élèves les privilèges uniquement pour la commande nécessaire avec sudo."
        "GREP" -> "Un journal contient des milliers de lignes. Tu utilises grep pour n'afficher que celles contenant le mot Failed."
        "BITLOCKER" -> "Un ordinateur portable professionnel est perdu dans un train. Le disque retiré de la machine reste chiffré ; sans le protecteur approprié, les fichiers ne sont pas directement lisibles."
        "GET-PROCESS" -> "Une application semble consommer beaucoup de ressources. Tu listes les processus PowerShell pour vérifier son PID et son état."
        "4625" -> "Un compte échoue à se connecter cinquante fois en quelques minutes. Les événements 4625 permettent de voir la répétition et d'examiner le contexte avant de conclure."
        "AES" -> "Une sauvegarde contient des données confidentielles. Elle est chiffrée avec AES avant d'être stockée ; sans la clé correcte, le contenu reste illisible."
        "SHA-256" -> "Tu copies une image disque forensic. Tu calcules l'empreinte avant et après la copie ; des SHA-256 identiques indiquent que le contenu comparé est identique bit à bit avec une très forte assurance."
        "XSS" -> "Une application réaffiche un commentaire utilisateur dans une page. Si elle traite mal cette donnée, le navigateur peut l'interpréter comme du code au lieu de simple texte."
        "CSRF" -> "Tu es connecté à un site. Une autre page essaie de déclencher une action vers ce site. Si le serveur n'exige aucune preuve supplémentaire de l'intention de l'utilisateur, la session existante peut être abusée."
        "KERBEROS" -> "Tu ouvres une session sur ton PC de domaine puis accèdes à un partage de fichiers : des tickets Kerberos permettent au serveur de vérifier ton identité sans te redemander ton mot de passe."
        "NMAP" -> "Dans ton laboratoire, tu vérifies une VM de test et constates que seuls SSH et HTTPS sont exposés. Tu peux ensuite contrôler si cette exposition correspond à la configuration prévue."
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "Tu dois vérifier si une adresse e-mail professionnelle est publiée officiellement. Tu consultes le site de l'entreprise et d'autres sources ouvertes, puis tu recoupes avant de conclure."
        "EXIF" -> "Une photo de test contient encore une position GPS. En lisant ses métadonnées EXIF, tu comprends pourquoi publier une image originale peut parfois révéler plus que les pixels visibles."
        else -> "Imagine que tu utilises « $clean » dans un environnement réel de $category. Le bon réflexe est d'identifier le problème de départ, l'action réalisée puis le résultat que tu peux observer."
    }

    val demoPair = when (key) {
        "DNS" -> "TERMINAL — résolution DNS" to "nslookup example.com"
        "DHCP" -> "WINDOWS — voir la configuration reçue" to "ipconfig /all"
        "ARP" -> "TERMINAL — afficher le cache ARP" to "arp -a"
        "ICMP" -> "TERMINAL — test ICMP" to "ping 8.8.8.8"
        "HTTPS", "443" -> "TERMINAL — observer la négociation HTTPS" to "curl -v https://example.com"
        "SSH" -> "TERMINAL — connexion à une machine de laboratoire" to "ssh user@192.168.1.10"
        "FTP", "21" -> "TERMINAL — vérifier qu'un service écoute sur un hôte de lab" to "nmap -p 21 192.168.1.10"
        "SUDO" -> "LINUX — exécuter une commande privilégiée" to "sudo systemctl status ssh"
        "GREP" -> "LINUX — filtrer un journal" to "grep 'Failed' /var/log/auth.log"
        "NANO" -> "LINUX — modifier un fichier texte" to "nano notes.txt"
        "PWD" -> "LINUX — afficher le dossier courant" to "pwd"
        "CD" -> "LINUX — changer de dossier" to "cd /var/log"
        "LS" -> "LINUX — voir les fichiers et leurs droits" to "ls -la"
        "WHOAMI" -> "LINUX / WINDOWS — vérifier le compte courant" to "whoami"
        "/ETC/PASSWD" -> "LINUX — lire les premières lignes" to "head /etc/passwd"
        "/ETC/SHADOW" -> "LINUX — vérifier les droits sans afficher le contenu" to "ls -l /etc/shadow"
        "/VAR/LOG/AUTH.LOG" -> "LINUX — suivre les événements" to "sudo tail -f /var/log/auth.log"
        "/ETC/HOSTS" -> "LINUX — voir les associations locales" to "cat /etc/hosts"
        "BITLOCKER" -> "WINDOWS — vérifier l'état BitLocker" to "manage-bde -status C:"
        "GET-PROCESS" -> "POWERSHELL — afficher quelques processus" to "Get-Process | Select-Object -First 5"
        "4624" -> "POWERSHELL — lire des connexions réussies" to "Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4624} -MaxEvents 3"
        "4625" -> "POWERSHELL — lire des échecs de connexion" to "Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4625} -MaxEvents 3"
        "4688" -> "POWERSHELL — lire des créations de processus" to "Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4688} -MaxEvents 3"
        "7045" -> "POWERSHELL — voir les installations de services" to "Get-WinEvent -FilterHashtable @{LogName='System'; Id=7045} -MaxEvents 3"
        "AES" -> "TERMINAL — exemple de chiffrement d'un fichier de test" to "openssl enc -aes-256-cbc -salt -in notes.txt -out notes.enc"
        "SHA-256" -> "LINUX — calculer une empreinte" to "sha256sum image.dd"
        "HMAC" -> "PYTHON — calculer un HMAC de démonstration" to "hmac.new(key, message, hashlib.sha256).hexdigest()"
        "BASE64" -> "TERMINAL — encoder puis décoder du texte" to "printf 'CyberQuiz' | base64"
        "XSS" -> "CODE SÛR — afficher une donnée comme texte" to "element.textContent = userInput"
        "SQL INJECTION" -> "CODE SÛR — requête paramétrée" to "cursor.execute('SELECT * FROM users WHERE id = ?', (user_id,))"
        "SAMESITE" -> "HTTP — cookie plus restrictif" to "Set-Cookie: session=...; Secure; HttpOnly; SameSite=Lax"
        "HTTPONLY" -> "HTTP — cookie inaccessible au JavaScript" to "Set-Cookie: session=...; Secure; HttpOnly"
        "KERBEROS" -> "WINDOWS — afficher les tickets Kerberos" to "klist"
        "LDAP" -> "POWERSHELL AD — lire un utilisateur de test" to "Get-ADUser -Identity alice"
        "GPO" -> "WINDOWS — voir les stratégies appliquées" to "gpresult /r"
        "NMAP" -> "LAB AUTORISÉ — identifier les services exposés" to "nmap -sV 192.168.1.10"
        "BURP SUITE" -> "LAB WEB — chemin du trafic" to "Navigateur -> Proxy Burp 127.0.0.1:8080 -> Application de test"
        "EXIF" -> "TERMINAL — lire les métadonnées d'une photo" to "exiftool photo.jpg"
        "SIEM" -> "REQUÊTE CONCEPTUELLE — rechercher un événement" to "event.id:4625 AND user.name:'alice'"
        "FIREWALL", "PARE-FEU" -> "LINUX — voir les règles locales" to "sudo nft list ruleset"
        else -> "MISE EN SITUATION" to "Entrée -> $clean -> action -> résultat observable"
    }

    val analogy = when (key) {
        "DNS" -> "Comme l'annuaire de ton téléphone : tu connais le nom d'une personne, l'annuaire retrouve son numéro. DNS connaît le nom d'un service et retrouve son adresse réseau."
        "DHCP" -> "Comme la réception d'un hôtel : tu arrives sans numéro de chambre. La réception t'attribue temporairement une chambre et te donne les informations utiles pour circuler dans l'hôtel."
        "ARP" -> "Comme demander dans une salle : « qui est Alice ? ». Tu connais le nom logique de la personne recherchée, puis quelqu'un te montre physiquement où elle se trouve."
        "ICMP" -> "Comme dire « tu m'entends ? » et attendre « oui ». Cela ne transporte pas toute la conversation ; cela vérifie surtout que le chemin et l'interlocuteur répondent."
        "HTTPS", "443" -> "Comme parler dans une pièce fermée après avoir vérifié l'identité de ton interlocuteur : les autres voient qu'une conversation existe, mais pas facilement son contenu."
        "SSH" -> "Comme disposer d'un clavier et d'un écran à distance, mais à travers un tunnel protégé entre ton poste et le serveur."
        "BITLOCKER" -> "Comme mettre tous les documents d'un coffre dans une boîte verrouillée avant de la transporter : même si quelqu'un récupère la boîte, il lui manque la clé."
        "AES" -> "Comme un coffre dont les deux personnes autorisées possèdent la même clé secrète : cette même clé ferme et ouvre le coffre."
        "RSA" -> "Comme une boîte aux lettres : tout le monde peut déposer quelque chose grâce à l'ouverture publique, mais seule la personne possédant la clé privée peut effectuer l'opération réservée correspondante selon le mécanisme."
        "SHA-256" -> "Comme une empreinte digitale du fichier : elle ne contient pas le fichier lui-même, mais permet de vérifier si le contenu a changé."
        "KERBEROS" -> "Comme recevoir à l'accueil un badge temporaire, puis utiliser ce badge pour obtenir l'accès aux salles autorisées sans montrer à nouveau ton mot de passe à chaque porte."
        "GPO" -> "Comme le règlement central d'une entreprise : au lieu d'aller configurer chaque poste un par un, l'administrateur publie des règles qui s'appliquent automatiquement aux groupes concernés."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "Comme une porte qui exige à la fois quelque chose que tu sais et quelque chose que tu possèdes : voler un seul élément ne suffit plus."
        "SIEM" -> "Comme une salle de contrôle qui reçoit les journaux de dizaines de bâtiments et rapproche les événements pour repérer une séquence inhabituelle."
        else -> "Pense toujours en trois cases : ce que la notion reçoit, l'action qu'elle effectue et ce qu'elle produit. Cette image mentale aide à la distinguer des termes voisins."
    }

    val schema = when (key) {
        "DNS" -> listOf("Utilisateur saisit example.com", "PC demande l'adresse à DNS", "Résolveur cherche l'enregistrement", "DNS répond avec l'IP", "Le navigateur contacte cette IP")
        "DHCP" -> listOf("Client sans configuration", "DISCOVER : qui peut me configurer ?", "OFFER : voici une IP et des paramètres", "REQUEST : je demande cette offre", "ACK : bail confirmé", "IP + masque + passerelle + DNS appliqués")
        "ARP" -> listOf("IP de destination locale connue", "Adresse MAC inconnue", "Requête ARP diffusée", "La cible répond avec sa MAC", "Association placée en cache", "Trame Ethernet envoyée")
        "ICMP" -> listOf("Echo Request", "Réseau IP", "Machine distante", "Echo Reply", "Délai et accessibilité observés")
        "HTTPS", "443" -> listOf("Navigateur -> TCP 443", "Handshake TLS", "Certificat vérifié", "Clés de session établies", "HTTP circule chiffré")
        "SSH" -> listOf("Client SSH", "Connexion au serveur", "Vérification clé d'hôte", "Authentification utilisateur", "Shell distant chiffré")
        "BITLOCKER" -> listOf("Données du volume", "Chiffrement BitLocker", "Clé protégée par TPM/PIN/récupération", "Volume déverrouillé si contrôle valide", "Sinon données illisibles")
        "AES" -> listOf("Texte clair", "Clé secrète", "AES + mode", "Texte chiffré", "Même clé autorisée -> déchiffrement")
        "RSA" -> listOf("Génération de paire", "Clé publique distribuée", "Opération asymétrique", "Clé privée conservée", "Résultat vérifié/déchiffré selon l'usage")
        "SHA-256" -> listOf("Fichier ou message", "Fonction SHA-256", "Empreinte 256 bits", "Comparaison avec empreinte attendue")
        "XSS" -> listOf("Entrée utilisateur", "Application réaffiche mal la donnée", "Navigateur interprète du contenu actif", "Code exécuté dans le contexte de la page")
        "CSRF" -> listOf("Victime déjà connectée", "Page externe déclenche une requête", "Cookie de session envoyé automatiquement", "Serveur doit vérifier l'intention de l'utilisateur")
        "SQL INJECTION" -> listOf("Entrée non fiable", "Mauvais mélange données + SQL", "Requête modifiée", "Requête paramétrée sépare données et syntaxe")
        "KERBEROS" -> listOf("Utilisateur s'authentifie", "Obtient un TGT", "Demande un ticket de service", "Présente le ticket au service", "Accès selon les droits")
        "LDAP" -> listOf("Client annuaire", "Connexion/authentification", "Recherche LDAP", "Annuaire", "Objets et attributs renvoyés")
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> listOf("Mot de passe", "Second facteur indépendant", "Les deux sont vérifiés", "Accès autorisé")
        "NMAP" -> listOf("Machine d'audit autorisée", "Sondes réseau", "Cible de laboratoire", "Réponses analysées", "Ports/services observés")
        "SIEM" -> listOf("Serveurs + postes + réseau", "Logs centralisés", "Indexation et corrélation", "Alerte / recherche", "Investigation analyste")
        "EDR" -> listOf("Endpoint", "Agent collecte la télémétrie", "Moteur de détection", "Alerte", "Investigation / réponse")
        else -> defaultCyberSchema(category, clean)
    }

    return CyberChoiceDeepDive(
        definition = definition,
        action = action,
        actions = actions,
        details = details,
        example = example,
        demoTitle = demoPair.first,
        demo = demoPair.second,
        analogy = analogy,
        schema = schema
    )
}

internal fun buildCyberChoiceConclusion(
    chosen: String,
    category: String,
    correctAnswer: String,
    isCorrect: Boolean,
    explanation: String
): String {
    val chosenInfo = buildCyberChoiceDeepDive(chosen, category)
    val correctInfo = buildCyberChoiceDeepDive(correctAnswer, category)
    return if (isCorrect) {
        "« $chosen » était la bonne réponse parce que sa fonction correspond exactement à ce que demande l'énoncé. ${chosenInfo.action} $explanation La méthode à retenir : repère le verbe de la question puis associe-le à l'action précise du terme."
    } else {
        "« $chosen » n'était pas la bonne réponse, même si c'est une vraie notion de $category. ${chosenInfo.action} Ici, l'énoncé demandait plutôt la fonction de « $correctAnswer » : ${correctInfo.action} La différence essentielle est donc : « $chosen » = ${shortAction(chosenInfo.action)} ; « $correctAnswer » = ${shortAction(correctInfo.action)}."
    }
}

private fun shortAction(text: String): String {
    val clean = text.substringAfter(" sert à ", text).substringAfter(" sert de ", text)
    return clean.substringBefore(".").replaceFirstChar { it.lowercase() }
}

private fun defaultCyberSchema(category: String, term: String): List<String> = when (category.lowercase()) {
    "réseaux" -> listOf("Appareil", term, "Action réseau", "Résultat observable")
    "linux" -> listOf("Utilisateur", term, "Action sur le système", "Sortie / état modifié")
    "windows" -> listOf("Utilisateur / Windows", term, "Action / événement", "Résultat")
    "cryptographie" -> listOf("Donnée", term, "Transformation cryptographique", "Résultat")
    "sécurité web" -> listOf("Navigateur", "Requête HTTP(S)", term, "Application / donnée")
    "malware" -> listOf("Système", term, "Comportement", "Impact")
    "ingénierie sociale" -> listOf("Attaquant", term, "Interaction avec la victime", "Action / information")
    "osint" -> listOf("Source ouverte", term, "Analyse", "Information recoupée")
    "forensics" -> listOf("Preuve", term, "Acquisition / analyse", "Constat documenté")
    "pentest" -> listOf("Autorisation", term, "Test contrôlé", "Constat / remédiation")
    "active directory" -> listOf("Identité", term, "Authentification / annuaire / politique", "Accès")
    "cloud security" -> listOf("Identité / réseau", term, "Contrôle cloud", "Ressource protégée")
    "mobile security" -> listOf("Appareil", term, "Contrôle mobile", "Données / application")
    else -> listOf("Entrée", term, "Action", "Résultat")
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
    val deepDive = buildCyberChoiceDeepDive(correct, category)

    return CyberDetailedLesson(
        concept = "La notion centrale ici est « $correct ». ${deepDive.definition}",
        mechanism = deepDive.details,
        example = deepDive.example,
        confusion = "Le piège est de choisir une proposition simplement parce qu'elle appartient au même domaine. ${other.joinToString(prefix = "Les autres choix sont « ", separator = " », « ", postfix = " ».")} Ils peuvent être réels sans répondre exactement à l'énoncé.",
        takeaway = "$explanation Retenir surtout l'association entre « $correct » et sa fonction précise.",
        schema = deepDive.schema
    )
}
