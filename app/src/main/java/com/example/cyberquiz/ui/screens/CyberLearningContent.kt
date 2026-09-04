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
    val details: String,
    val example: String,
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
        "FTP" -> "FTP est un protocole de transfert de fichiers historique. Sa version classique n'est pas chiffrée par défaut."
        "21" -> "Le port 21 est traditionnellement associé au canal de contrôle de FTP."
        "53" -> "Le port 53 est principalement associé à DNS, en UDP pour beaucoup de requêtes courantes et en TCP dans certains cas."
        "443" -> "Le port TCP 443 est le port standard utilisé par HTTPS pour les communications Web chiffrées."
        "8080" -> "Le port 8080 est souvent utilisé comme port HTTP alternatif, par exemple pour des applications Web, des proxys ou des interfaces d'administration."
        "SUDO" -> "sudo permet à un utilisateur autorisé d'exécuter une commande avec les privilèges d'un autre compte, souvent root."
        "GREP" -> "grep recherche du texte correspondant à un motif dans un fichier ou dans la sortie d'une commande."
        "NANO" -> "nano est un éditeur de texte utilisable directement dans un terminal Linux."
        "PWD" -> "pwd affiche le chemin du répertoire de travail courant dans un shell Unix/Linux."
        "CD" -> "cd change le répertoire de travail courant dans un shell."
        "LS" -> "ls affiche le contenu d'un répertoire sous Linux et les systèmes Unix."
        "WHOAMI" -> "whoami affiche le nom du compte utilisateur actuellement utilisé."
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
        "4625" -> "L'événement Windows Security 4625 indique généralement un échec d'ouverture de session."
        "4688" -> "L'événement Windows Security 4688 correspond à la création d'un nouveau processus lorsque l'audit concerné est activé."
        "7045" -> "L'événement System 7045 est couramment associé à l'installation d'un nouveau service Windows."
        "AES" -> "AES est un algorithme de chiffrement symétrique : une même clé secrète sert à chiffrer et à déchiffrer les données."
        "RSA" -> "RSA est un algorithme asymétrique fondé sur une paire clé publique / clé privée."
        "ECDSA" -> "ECDSA est un algorithme de signature numérique basé sur les courbes elliptiques. Il sert à vérifier authenticité et intégrité."
        "SHA-256" -> "SHA-256 est une fonction de hachage cryptographique qui produit une empreinte de taille fixe."
        "HMAC" -> "HMAC combine une fonction de hachage avec une clé secrète afin de vérifier l'intégrité et l'authenticité d'un message."
        "BASE64" -> "Base64 est un encodage de données en caractères imprimables. Ce n'est pas un chiffrement."
        "CRC32 UNIQUEMENT", "CRC32" -> "CRC32 est un code de détection d'erreurs utile contre des altérations accidentelles, mais il n'offre pas les garanties cryptographiques d'un HMAC."
        "XSS" -> "XSS (Cross-Site Scripting) permet d'injecter du contenu actif, souvent JavaScript, exécuté dans le navigateur d'une victime."
        "CSRF" -> "CSRF (Cross-Site Request Forgery) pousse le navigateur d'un utilisateur déjà authentifié à envoyer une requête qu'il n'avait pas l'intention d'effectuer."
        "SQL INJECTION" -> "Une injection SQL exploite une entrée mal contrôlée pour modifier la structure ou le comportement d'une requête SQL."
        "SSRF" -> "SSRF (Server-Side Request Forgery) pousse un serveur vulnérable à effectuer lui-même une requête vers une ressource choisie par l'attaquant."
        "SAMESITE" -> "SameSite est un attribut de cookie qui contrôle son envoi dans certains contextes inter-sites et peut réduire le risque de certaines attaques CSRF."
        "HTTPONLY" -> "HttpOnly est un attribut de cookie qui empêche les scripts JavaScript d'y accéder directement depuis le navigateur."
        "MAX-AGE UNIQUEMENT", "MAX-AGE" -> "Max-Age détermine la durée de vie d'un cookie."
        "DOMAIN UNIQUEMENT", "DOMAIN" -> "L'attribut Domain précise pour quels domaines un cookie peut être envoyé."
        "CONTENT-TYPE" -> "Content-Type est un en-tête HTTP décrivant le type de contenu transporté, par exemple application/json ou text/html."
        "SPYWARE" -> "Un spyware est un logiciel espion conçu pour collecter discrètement des informations sur une machine ou son utilisateur."
        "RANSOMWARE" -> "Un ransomware chiffre ou bloque des données ou systèmes afin d'exiger une rançon."
        "ROOTKIT" -> "Un rootkit regroupe des techniques permettant de maintenir un accès privilégié ou furtif et de masquer certaines activités malveillantes."
        "ADWARE" -> "Un adware affiche ou injecte de la publicité, souvent de manière intrusive."
        "CHEVAL DE TROIE" -> "Un cheval de Troie se présente comme légitime ou utile afin d'inciter l'utilisateur à l'installer ou à l'exécuter."
        "PARE-FEU" -> "Un pare-feu filtre les communications réseau selon des règles."
        "HYPERVISEUR" -> "Un hyperviseur permet d'exécuter et de gérer des machines virtuelles."
        "PROXY INVERSE" -> "Un proxy inverse reçoit des requêtes côté serveur puis les transmet à un ou plusieurs services en arrière-plan."
        "VISHING" -> "Le vishing est du phishing réalisé principalement par la voix, par exemple au téléphone."
        "SMURFING" -> "Une attaque Smurf est une ancienne forme de déni de service utilisant ICMP et l'amplification via des adresses de broadcast."
        "HASHING" -> "Le hashing consiste à calculer une empreinte à partir de données."
        "HARDENING" -> "Le hardening, ou durcissement, consiste à réduire la surface d'attaque d'un système en renforçant sa configuration."
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "OSINT signifie Open Source Intelligence : collecte et analyse d'informations provenant de sources ouvertes accessibles légalement."
        "EXIF" -> "EXIF est un ensemble de métadonnées pouvant accompagner une image : date, appareil, paramètres de prise de vue et parfois coordonnées GPS."
        "NMAP" -> "Nmap est un outil de découverte et d'analyse réseau utilisé notamment pour identifier des hôtes, ports ouverts et services."
        "BURP SUITE" -> "Burp Suite est une plateforme de test de sécurité Web, connue notamment pour son proxy d'interception HTTP/HTTPS."
        "PAINT" -> "Paint est un logiciel de dessin Windows."
        "VLC" -> "VLC est un lecteur multimédia."
        "CALCULATOR" -> "Calculator est l'application calculatrice."
        "KERBEROS" -> "Kerberos est un protocole d'authentification reposant sur des tickets. Il est central dans l'authentification des domaines Active Directory modernes."
        "LDAP" -> "LDAP est un protocole d'accès à des services d'annuaire. Active Directory l'utilise notamment pour consulter ou manipuler des objets d'annuaire."
        "NTLM" -> "NTLM est une famille de mécanismes d'authentification Microsoft plus anciens que Kerberos, encore présents dans certains environnements."
        "GPO" -> "Une GPO (Group Policy Object) permet d'appliquer de manière centralisée des paramètres aux utilisateurs et ordinateurs d'un domaine Active Directory."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "MFA signifie authentification multifacteur : l'accès exige au moins deux facteurs indépendants."
        "IAM" -> "IAM (Identity and Access Management) regroupe les mécanismes permettant de gérer identités, authentification, rôles et autorisations."
        "SECURITY GROUP" -> "Dans de nombreux clouds, un Security Group agit comme un pare-feu logique contrôlant le trafic autorisé vers ou depuis des ressources."
        "BUCKET" -> "Un bucket est un conteneur de stockage objet dans plusieurs plateformes cloud."
        "VPN" -> "Un VPN crée un tunnel logique protégé entre deux points afin de transporter du trafic sur un réseau non maîtrisé."
        "MDM" -> "MDM (Mobile Device Management) permet d'administrer, configurer et sécuriser un parc d'appareils mobiles."
        "ROOTING" -> "Le rooting consiste à obtenir des privilèges élevés sur Android."
        "JAILBREAK" -> "Le jailbreak vise à contourner certaines restrictions d'iOS afin d'obtenir un contrôle plus large sur l'appareil."
        "SANDBOX" -> "Une sandbox isole une application ou un processus afin de limiter ses accès aux ressources du système."
        "LEAST PRIVILEGE" -> "Le principe du moindre privilège consiste à n'accorder que les permissions strictement nécessaires à une tâche."
        "ZERO TRUST" -> "Zero Trust est une approche de sécurité qui évite de faire confiance automatiquement à un utilisateur ou appareil."
        "FAIL OPEN" -> "Fail open signifie qu'en cas de panne, un mécanisme laisse l'accès ou le trafic continuer."
        "DEFENSE BY OBSCURITY" -> "La sécurité par l'obscurité repose principalement sur le secret du fonctionnement."
        "SIEM" -> "Un SIEM centralise et corrèle des journaux et événements de sécurité afin d'aider à détecter et investiguer des activités suspectes."
        "EDR" -> "Un EDR surveille les terminaux, collecte de la télémétrie et aide à détecter, investiguer et répondre à des menaces."
        "IDS" -> "Un IDS détecte des activités potentiellement malveillantes et génère des alertes."
        "IPS" -> "Un IPS inspecte du trafic et peut bloquer automatiquement certaines activités détectées comme malveillantes."
        "WAF" -> "Un WAF filtre le trafic HTTP/HTTPS à destination d'applications Web afin de bloquer certaines attaques applicatives."
        else -> null
    }

    if (known != null) return known

    if (clean.matches(Regex("^\\d+$"))) {
        return "« $clean » est une valeur numérique technique. Dans la catégorie $category, sa signification dépend du contexte : port, identifiant d'événement, quantité ou valeur de configuration."
    }

    if (clean.startsWith("/") || clean.contains("rw") || clean.contains("r--")) {
        return "« $clean » est une notation ou un chemin technique. Dans $category, chaque symbole ou segment peut changer précisément le rôle, le fichier ou les permissions concernées."
    }

    return if (clean.split(' ').size >= 4) {
        "« $clean » décrit une action, un comportement ou une conséquence technique. Il faut comparer précisément cette action avec celle demandée dans la question."
    } else {
        "« $clean » est une notion du domaine $category. Elle possède un rôle propre qu'il faut distinguer des autres propositions."
    }
}

internal fun summarizeCyberChoice(term: String, category: String): String {
    val full = explainCyberChoice(term, category).trim()
    val firstSentence = full.substringBefore(".").trim()
    return if (firstSentence.length >= 35) "$firstSentence." else full
}

internal fun buildCyberChoiceDeepDive(term: String, category: String): CyberChoiceDeepDive {
    val clean = term.trim()
    val key = clean.uppercase()
    val definition = explainCyberChoice(clean, category)

    val action = when (key) {
        "DNS" -> "Il répond à la question : « quelle adresse IP correspond à ce nom de domaine ? »."
        "DHCP" -> "Il fournit automatiquement une configuration réseau à un appareil lorsqu'il rejoint un réseau."
        "ARP" -> "Il retrouve l'adresse MAC correspondant à une adresse IPv4 sur le réseau local."
        "ICMP" -> "Il transporte des messages de contrôle et de diagnostic entre équipements réseau."
        "HTTPS", "443" -> "Il permet au navigateur et au serveur Web d'échanger des données chiffrées avec TLS."
        "SSH" -> "Il permet de contrôler ou administrer une machine distante à travers un canal chiffré."
        "AES" -> "Il transforme des données lisibles en données chiffrées avec une clé secrète, puis permet l'opération inverse avec cette clé."
        "RSA" -> "Il utilise une paire de clés publique et privée pour réaliser des opérations cryptographiques asymétriques."
        "SHA-256" -> "Il transforme une donnée en empreinte de taille fixe pour comparer ou vérifier son intégrité."
        "HMAC" -> "Il calcule une empreinte authentifiée à l'aide d'une clé secrète partagée."
        "XSS" -> "Il détourne le navigateur en lui faisant exécuter du contenu injecté dans une page Web."
        "CSRF" -> "Il exploite une session déjà authentifiée pour faire envoyer une requête non voulue par la victime."
        "SQL INJECTION" -> "Il détourne une requête SQL en profitant d'une entrée utilisateur mal sécurisée."
        "SSRF" -> "Il pousse le serveur à effectuer une requête réseau choisie par l'attaquant."
        "KERBEROS" -> "Il authentifie des utilisateurs et services à l'aide de tickets, sans renvoyer le mot de passe à chaque accès."
        "LDAP" -> "Il permet d'interroger et de manipuler un annuaire selon les droits disponibles."
        "GPO" -> "Elle applique des paramètres de configuration et de sécurité de manière centralisée dans un domaine."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "Il ajoute au moins un facteur d'authentification indépendant au mot de passe."
        "NMAP" -> "Il envoie des sondes réseau afin d'identifier les hôtes, ports et services visibles."
        "BURP SUITE" -> "Il intercepte et analyse les échanges HTTP/HTTPS d'une application Web pendant un test autorisé."
        "BITLOCKER" -> "Il chiffre un volume Windows afin que les données au repos ne soient pas lisibles sans la clé appropriée."
        "SIEM" -> "Il centralise les journaux puis les corrèle afin de faire ressortir des événements de sécurité intéressants."
        "EDR" -> "Il surveille les postes et serveurs, collecte leur activité et aide à détecter puis investiguer des comportements suspects."
        "WAF" -> "Il filtre les requêtes Web afin de bloquer certaines attaques dirigées contre une application HTTP/HTTPS."
        else -> "Cette proposition a une fonction précise dans le domaine $category. Pour la comprendre, il faut identifier ce qu'elle reçoit, ce qu'elle fait et le résultat qu'elle produit."
    }

    val details = when (key) {
        "DNS" -> "Quand une application connaît un nom comme example.com mais pas son IP, elle demande la résolution DNS. Un résolveur recherche la réponse, éventuellement en consultant plusieurs serveurs DNS, puis renvoie l'adresse IP. DNS ne distribue pas les adresses aux machines et ne chiffre pas à lui seul la connexion."
        "DHCP" -> "Lorsqu'un appareil arrive sur un réseau et ne possède pas encore sa configuration, il peut utiliser DHCP. Dans le fonctionnement IPv4 classique, le client diffuse une demande (DHCP Discover), un serveur propose une configuration (Offer), le client la demande (Request) puis le serveur la confirme (ACK). La configuration peut contenir l'adresse IP, le masque, la passerelle et les DNS."
        "ARP" -> "Avant d'envoyer une trame Ethernet vers une machine du même réseau IPv4, l'émetteur doit connaître son adresse MAC. Il envoie une requête ARP demandant qui possède l'adresse IP ciblée. La machine concernée répond avec sa MAC, qui peut ensuite être placée dans le cache ARP."
        "ICMP" -> "ICMP accompagne IP pour signaler certaines erreurs ou fournir des informations de diagnostic. Ping utilise des messages ICMP Echo Request et Echo Reply. Traceroute peut également s'appuyer sur des réponses ICMP selon le système. ICMP ne remplace ni TCP ni UDP pour transporter les données applicatives courantes."
        "HTTPS", "443" -> "HTTPS associe HTTP et TLS. Le client et le serveur négocient des paramètres cryptographiques, le certificat aide à authentifier le serveur, puis une clé de session protège les échanges. Le port 443 est le port TCP standard associé à ce service, mais le port n'est pas lui-même le mécanisme de chiffrement."
        "AES" -> "AES travaille sur des blocs de données avec une clé symétrique. En pratique, il est utilisé avec un mode d'opération ou un mode authentifié comme GCM. L'idée importante est que la même clé secrète doit rester connue des parties autorisées et protégée contre la divulgation."
        "RSA" -> "RSA repose sur deux clés liées mathématiquement. Ce qui est protégé ou signé avec une clé est vérifié ou traité avec l'autre selon l'usage. Dans les protocoles modernes, RSA n'est généralement pas utilisé pour chiffrer directement de gros volumes de données ; des clés symétriques sont souvent préférées pour cela."
        "SHA-256" -> "SHA-256 calcule une empreinte de 256 bits à partir d'une donnée de taille quelconque. Une petite modification de l'entrée change fortement l'empreinte. Contrairement au chiffrement, il n'existe pas de clé permettant de « déchiffrer » le hash pour retrouver l'entrée."
        "XSS" -> "Une faille XSS apparaît lorsque du contenu contrôlé par un utilisateur est réinjecté dans une page sans traitement adapté. Le navigateur de la victime peut alors interpréter ce contenu comme du code actif. Les protections dépendent du contexte : encodage de sortie, politiques de sécurité, frameworks et validation adaptée."
        "CSRF" -> "Le navigateur joint souvent automatiquement les cookies d'une session à une requête. Une attaque CSRF tente d'exploiter ce comportement en amenant la victime à déclencher une action sur un site où elle est déjà connectée. Des jetons anti-CSRF et certaines politiques SameSite peuvent réduire ce risque."
        "SQL INJECTION" -> "Si une application construit une requête SQL en concaténant directement une entrée non fiable, cette entrée peut modifier la structure de la requête. Les requêtes paramétrées séparent les données de la syntaxe SQL et constituent une défense essentielle."
        "KERBEROS" -> "Dans un domaine Active Directory, l'utilisateur obtient d'abord un ticket lui permettant ensuite de demander d'autres tickets pour accéder aux services. Cela évite de transmettre son mot de passe à chaque service et permet une authentification centralisée."
        "NMAP" -> "Nmap envoie différents types de sondes vers une cible autorisée. Les réponses permettent d'inférer si un hôte répond, quels ports semblent ouverts et parfois quel service ou système est présent. Le résultat dépend des filtres réseau et du type de scan."
        "BITLOCKER" -> "BitLocker chiffre le contenu d'un volume. Au démarrage, la clé peut être protégée notamment par le TPM, un code PIN ou une clé de récupération selon la configuration. Si le disque est retiré et lu hors de la machine, les données restent chiffrées sans la clé."
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "Un système MFA demande des preuves appartenant à des catégories différentes : quelque chose que l'on connaît, que l'on possède ou que l'on est. Un mot de passe plus un code temporaire est donc plus robuste qu'un mot de passe seul, même s'il existe encore des attaques de phishing ou de fatigue MFA."
        else -> "Commence par sa définition : $definition Ensuite demande-toi où cette notion intervient dans $category, quelles données ou ressources elle manipule et quel résultat elle produit. Cette méthode permet de la distinguer d'une autre proposition qui peut sembler proche."
    }

    val example = when (key) {
        "DNS" -> "Tu tapes cyberquiz.fr : ton appareil demande l'IP correspondant à ce nom avant de pouvoir contacter le serveur."
        "DHCP" -> "Tu connectes un téléphone au Wi-Fi : quelques secondes plus tard il reçoit automatiquement une IP, une passerelle et les DNS sans saisie manuelle."
        "ARP" -> "Ton PC veut joindre 192.168.1.20 sur le même LAN : il demande quelle MAC possède cette IP avant d'envoyer la trame Ethernet."
        "ICMP" -> "Tu exécutes ping 8.8.8.8 : des messages ICMP permettent de vérifier si la destination répond et de mesurer le délai aller-retour."
        "HTTPS", "443" -> "Tu ouvres ton site bancaire : le navigateur établit une session TLS puis les requêtes HTTP circulent chiffrées."
        "AES" -> "Une sauvegarde chiffrée avec AES reste illisible sans la clé secrète utilisée pour la protéger."
        "SHA-256" -> "Tu calcules le SHA-256 d'une image disque avant et après copie : si l'empreinte est identique, cela contribue à vérifier qu'elle n'a pas été modifiée."
        "XSS" -> "Un commentaire mal filtré contient du JavaScript : lorsqu'une victime affiche la page, son navigateur peut exécuter ce code."
        "CSRF" -> "Une victime connectée à un service clique sur une page malveillante qui tente de déclencher une action avec sa session existante."
        "KERBEROS" -> "Un utilisateur ouvre sa session de domaine puis accède à un partage de fichiers sans retaper son mot de passe pour chaque service."
        "NMAP" -> "Lors d'un audit autorisé, tu scans un serveur de test et observes que les ports 22 et 443 répondent."
        "BITLOCKER" -> "Un ordinateur portable est perdu : le disque chiffré BitLocker est beaucoup plus difficile à lire hors de la machine sans la clé."
        else -> "Imagine cette proposition utilisée dans une situation réelle de $category. Demande-toi quel problème elle résout et quel résultat observable elle produit."
    }

    val schema = when (key) {
        "DNS" -> listOf("Nom de domaine", "Requête DNS", "Résolveur / serveurs DNS", "Adresse IP", "Connexion au serveur")
        "DHCP" -> listOf("Appareil sans IP", "DHCP Discover", "DHCP Offer", "DHCP Request", "DHCP ACK + configuration")
        "ARP" -> listOf("IP locale connue", "Requête ARP", "Réponse avec adresse MAC", "Cache ARP", "Trame Ethernet")
        "ICMP" -> listOf("Machine A", "Message ICMP", "Réseau IP", "Machine B", "Réponse / erreur")
        "HTTPS", "443" -> listOf("Navigateur", "Connexion au serveur", "Négociation TLS", "Canal chiffré", "HTTP protégé")
        "AES" -> listOf("Donnée lisible", "Clé secrète", "AES", "Donnée chiffrée", "Même clé pour déchiffrer")
        "RSA" -> listOf("Paire de clés", "Clé publique", "Opération asymétrique", "Clé privée", "Vérification / déchiffrement selon l'usage")
        "SHA-256" -> listOf("Donnée", "SHA-256", "Empreinte 256 bits", "Comparaison d'intégrité")
        "XSS" -> listOf("Entrée mal contrôlée", "Page Web", "Navigateur victime", "JavaScript exécuté")
        "CSRF" -> listOf("Victime déjà connectée", "Page ou lien malveillant", "Requête envoyée avec la session", "Action non voulue")
        "SQL INJECTION" -> listOf("Entrée utilisateur", "Requête SQL mal construite", "Base de données", "Comportement détourné")
        "KERBEROS" -> listOf("Utilisateur", "Ticket initial", "Ticket de service", "Service demandé", "Accès authentifié")
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> listOf("Identifiant + mot de passe", "Second facteur", "Vérification", "Accès autorisé")
        "NMAP" -> listOf("Machine d'audit", "Sondes réseau", "Cible autorisée", "Réponses", "Ports / services observés")
        else -> defaultCyberSchema(category)
    }

    return CyberChoiceDeepDive(
        definition = definition,
        action = action,
        details = details,
        example = example,
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
    return if (isCorrect) {
        "« $chosen » était la bonne réponse parce que la question demandait précisément sa fonction. $explanation"
    } else {
        val chosenSummary = summarizeCyberChoice(chosen, category)
        val correctSummary = summarizeCyberChoice(correctAnswer, category)
        "« $chosen » n'était pas la bonne réponse. $chosenSummary Ici, l'énoncé demandait la fonction correspondant à « $correctAnswer » : $correctSummary La différence à retenir est donc la fonction exacte de chacun des deux termes."
    }
}

private fun defaultCyberSchema(category: String): List<String> = when (category.lowercase()) {
    "réseaux" -> listOf("Appareil", "Protocole / fonction réseau", "Échange", "Résultat réseau")
    "linux" -> listOf("Utilisateur", "Commande / fichier / permission", "Action système", "Résultat")
    "windows" -> listOf("Utilisateur / système", "Composant Windows", "Action / journal", "Résultat")
    "cryptographie" -> listOf("Donnée", "Mécanisme cryptographique", "Clé / fonction", "Résultat protégé")
    "sécurité web" -> listOf("Navigateur", "Requête", "Application Web", "Donnée / action")
    "malware" -> listOf("Entrée", "Exécution", "Comportement", "Impact")
    "ingénierie sociale" -> listOf("Attaquant", "Prétexte", "Victime", "Action / information")
    "osint" -> listOf("Source ouverte", "Collecte", "Recoupement", "Information")
    "forensics" -> listOf("Preuve", "Acquisition", "Intégrité", "Analyse")
    "pentest" -> listOf("Autorisation", "Test", "Constat", "Remédiation")
    "active directory" -> listOf("Identité", "Authentification / annuaire", "Politique", "Accès")
    "cloud security" -> listOf("Identité", "Contrôle", "Ressource cloud", "Journalisation")
    "mobile security" -> listOf("Appareil", "Application", "Protection", "Données")
    else -> listOf("Besoin", "Mécanisme", "Action", "Résultat")
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
