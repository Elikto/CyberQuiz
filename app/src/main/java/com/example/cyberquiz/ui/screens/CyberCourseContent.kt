package com.example.cyberquiz.ui.screens

internal data class CyberCourseVideo(
    val title: String,
    val channel: String,
    val url: String
)

internal data class CyberCourse(
    val title: String,
    val subtitle: String,
    val memorySentence: String,
    val objectives: List<String>,
    val deepDive: CyberChoiceDeepDive,
    val simpleSchemaTitle: String,
    val simpleSchema: List<String>,
    val expectedOutputTitle: String,
    val expectedOutput: String,
    val checkpoints: List<String>,
    val advancedNote: String,
    val videos: List<CyberCourseVideo>,
    val videoSearchQuery: String
)

private val courseAliases = mapOf(
    // Réseaux
    "DNS" to "DNS",
    "53" to "DNS",
    "RÉSOUDRE UN NOM DE DOMAINE" to "DNS",
    "RESOUDRE UN NOM DE DOMAINE" to "DNS",
    "DHCP" to "DHCP",
    "ARP" to "ARP",
    "ASSOCIER UNE ADRESSE IP À UNE ADRESSE MAC" to "ARP",
    "ASSOCIER UNE ADRESSE IP A UNE ADRESSE MAC" to "ARP",
    "ICMP" to "ICMP",
    "HTTPS" to "HTTPS",
    "443" to "HTTPS",
    "SSH" to "SSH",
    "FTP" to "FTP",
    "21" to "FTP",
    "8080" to "HTTP_8080",

    // Linux
    "CD" to "CD",
    "PWD" to "PWD",
    "LS" to "LS",
    "WHOAMI" to "WHOAMI",
    "SUDO" to "SUDO",
    "GREP" to "GREP",
    "NANO" to "NANO",

    // Windows
    "BITLOCKER" to "BITLOCKER",
    "DEFRAG" to "DEFRAG",
    "NOTEPAD" to "NOTEPAD",
    "TASK SCHEDULER" to "TASK_SCHEDULER",

    // Cryptographie
    "AES" to "AES",
    "RSA" to "RSA",
    "ECDSA" to "ECDSA",
    "SHA-256" to "SHA256",
    "LA CLÉ PUBLIQUE" to "PUBLIC_KEY",
    "LA CLE PUBLIQUE" to "PUBLIC_KEY",
    "CLÉ PUBLIQUE" to "PUBLIC_KEY",
    "CLE PUBLIQUE" to "PUBLIC_KEY",
    "LA CLÉ PRIVÉE" to "PRIVATE_KEY",
    "LA CLE PRIVEE" to "PRIVATE_KEY",
    "CLÉ PRIVÉE" to "PRIVATE_KEY",
    "CLE PRIVEE" to "PRIVATE_KEY",
    "LE MOT DE PASSE MAÎTRE" to "MASTER_PASSWORD",
    "LE MOT DE PASSE MAITRE" to "MASTER_PASSWORD",
    "LE SEL DU DISQUE" to "SALT",

    // Web
    "XSS" to "XSS",
    "CSRF" to "CSRF",
    "SQL INJECTION" to "SQL_INJECTION",
    "SSRF" to "SSRF",
    "DNSSEC" to "DNSSEC",
    "NAT" to "NAT",

    // Malware / protection
    "SPYWARE" to "SPYWARE",
    "RANSOMWARE" to "RANSOMWARE",
    "ROOTKIT" to "ROOTKIT",
    "ADWARE" to "ADWARE",
    "CHEVAL DE TROIE" to "TROJAN",
    "PARE-FEU" to "FIREWALL",
    "HYPERVISEUR" to "HYPERVISOR",
    "PROXY INVERSE" to "REVERSE_PROXY",

    // Ingénierie sociale
    "PHISHING" to "PHISHING",
    "TROMPER UNE VICTIME POUR OBTENIR UNE INFORMATION" to "PHISHING",
    "VISHING" to "VISHING",
    "SMURFING" to "SMURFING",
    "HASHING" to "HASHING",
    "HARDENING" to "HARDENING",

    // OSINT
    "OSINT" to "OSINT",
    "OPEN SOURCE INTELLIGENCE" to "OSINT",

    // Pentest / outils
    "NMAP" to "NMAP",
    "GIT" to "GIT",
    "DOCKER" to "DOCKER",
    "GRADLE" to "GRADLE",
    "LE PÉRIMÈTRE ET L'AUTORISATION DU TEST" to "PENTEST_SCOPE",
    "LE PERIMETRE ET L'AUTORISATION DU TEST" to "PENTEST_SCOPE",

    // Authentification / cloud / AD / mobile / système
    "MFA" to "MFA",
    "MULTI-FACTOR AUTHENTICATION" to "MFA",
    "POUR VÉRIFIER SON INTÉGRITÉ" to "INTEGRITY_HASH",
    "POUR VERIFIER SON INTEGRITE" to "INTEGRITY_HASH",
    "IL FOURNIT NOTAMMENT L'AUTHENTIFICATION ET LES SERVICES D'ANNUAIRE DU DOMAINE" to "DOMAIN_CONTROLLER",
    "LA RÉPARTITION DES RESPONSABILITÉS DE SÉCURITÉ ENTRE LE FOURNISSEUR ET LE CLIENT" to "SHARED_RESPONSIBILITY",
    "LA REPARTITION DES RESPONSABILITES DE SECURITE ENTRE LE FOURNISSEUR ET LE CLIENT" to "SHARED_RESPONSIBILITY",
    "N'ACCORDER QUE LES PERMISSIONS NÉCESSAIRES" to "LEAST_PRIVILEGE",
    "N'ACCORDER QUE LES PERMISSIONS NECESSAIRES" to "LEAST_PRIVILEGE",
    "POUR CORRIGER NOTAMMENT DES VULNÉRABILITÉS CONNUES" to "SECURITY_PATCHES",
    "POUR CORRIGER NOTAMMENT DES VULNERABILITES CONNUES" to "SECURITY_PATCHES"
)

private fun normalizeCourseTerm(term: String): String = term.trim().uppercase()

private fun resolveCourseKey(term: String): String? = courseAliases[normalizeCourseTerm(term)]

internal fun hasCyberExpertCourse(term: String): Boolean = resolveCourseKey(term) != null

internal fun buildCyberCourse(term: String, category: String): CyberCourse {
    val original = term.trim()
    val key = resolveCourseKey(original) ?: normalizeCourseTerm(original)
    val deepDive = beginnerDeepDive(key, original, category)
    val displayName = beginnerDisplayName(key, original)
    val memorySentence = beginnerMemory(key, original, category)
    val simpleSchema = beginnerSimpleSchema(key, deepDive)

    return CyberCourse(
        title = "Cours · $displayName",
        subtitle = "Explication simple, pensée pour quelqu'un qui débute",
        memorySentence = memorySentence,
        objectives = beginnerObjectives(displayName),
        deepDive = deepDive,
        simpleSchemaTitle = "$displayName EN UNE IMAGE",
        simpleSchema = simpleSchema,
        expectedOutputTitle = "CE QU'IL FAUT COMPRENDRE DANS L'EXEMPLE",
        expectedOutput = beginnerExpectedOutput(key, displayName),
        checkpoints = beginnerCheckpoints(displayName, memorySentence),
        advancedNote = beginnerAdvancedNote(key, displayName),
        videos = beginnerVideos(key),
        videoSearchQuery = "$displayName expliqué simplement français débutant"
    )
}

private fun beginnerDisplayName(key: String, original: String): String = when (key) {
    "HTTP_8080" -> "Port 8080"
    "TASK_SCHEDULER" -> "Task Scheduler"
    "PUBLIC_KEY" -> "Clé publique"
    "PRIVATE_KEY" -> "Clé privée"
    "MASTER_PASSWORD" -> "Mot de passe maître"
    "SALT" -> "Sel cryptographique"
    "SQL_INJECTION" -> "Injection SQL"
    "TROJAN" -> "Cheval de Troie"
    "FIREWALL" -> "Pare-feu"
    "HYPERVISOR" -> "Hyperviseur"
    "REVERSE_PROXY" -> "Proxy inverse"
    "PENTEST_SCOPE" -> "Périmètre d'un pentest"
    "INTEGRITY_HASH" -> "Empreinte et intégrité"
    "DOMAIN_CONTROLLER" -> "Contrôleur de domaine"
    "SHARED_RESPONSIBILITY" -> "Responsabilité partagée du cloud"
    "LEAST_PRIVILEGE" -> "Moindre privilège"
    "SECURITY_PATCHES" -> "Correctifs de sécurité"
    "SHA256" -> "SHA-256"
    else -> if (original.length <= 40) original else key.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun beginnerMemory(key: String, original: String, category: String): String = when (key) {
    "DNS" -> "DNS transforme un nom facile à retenir, comme example.com, en une adresse que l'ordinateur peut utiliser."
    "DHCP" -> "DHCP donne automatiquement à ton appareil les réglages réseau dont il a besoin pour communiquer."
    "ARP" -> "ARP aide ton ordinateur à retrouver l'appareil correspondant à une adresse IP sur le réseau local."
    "ICMP" -> "ICMP sert surtout à envoyer des messages de contrôle et de diagnostic, comme ceux utilisés par ping."
    "HTTPS" -> "HTTPS protège les échanges entre ton navigateur et un site Web afin qu'ils ne circulent pas en clair."
    "SSH" -> "SSH permet de contrôler un ordinateur distant à travers une connexion protégée."
    "FTP" -> "FTP sert à transférer des fichiers entre un client et un serveur."
    "HTTP_8080" -> "8080 est un numéro de porte réseau souvent utilisé par un service Web."
    "PWD" -> "pwd te dit simplement dans quel dossier tu te trouves dans le terminal Linux."
    "CD" -> "cd sert à changer de dossier dans le terminal."
    "LS" -> "ls affiche les fichiers et dossiers présents à l'endroit où tu te trouves."
    "WHOAMI" -> "whoami affiche quel utilisateur est actuellement utilisé par le terminal."
    "SUDO" -> "sudo permet à un utilisateur autorisé d'exécuter une commande avec davantage de droits."
    "GREP" -> "grep cherche du texte dans un fichier ou dans la sortie d'une commande."
    "NANO" -> "nano est un petit éditeur de texte directement utilisable dans le terminal."
    "BITLOCKER" -> "BitLocker chiffre un disque Windows pour protéger les données si le support est volé ou lu ailleurs."
    "DEFRAG" -> "Defrag réorganise des données sur certains disques ; il ne chiffre pas les fichiers."
    "NOTEPAD" -> "Notepad est simplement l'éditeur de texte de base de Windows."
    "TASK_SCHEDULER" -> "Task Scheduler lance automatiquement des programmes ou scripts à une heure ou lors d'un événement."
    "AES" -> "AES chiffre des données avec une clé secrète partagée entre le chiffrement et le déchiffrement."
    "RSA" -> "RSA utilise deux clés liées : une peut être publique et l'autre doit rester privée."
    "ECDSA" -> "ECDSA sert surtout à créer et vérifier des signatures numériques."
    "SHA256" -> "SHA-256 crée une empreinte d'une donnée afin de pouvoir vérifier si elle a changé."
    "PUBLIC_KEY" -> "Une clé publique peut être partagée ; elle fonctionne avec une clé privée qui, elle, reste secrète."
    "PRIVATE_KEY" -> "Une clé privée doit rester secrète : elle représente la partie sensible d'une paire de clés."
    "MASTER_PASSWORD" -> "Un mot de passe maître protège généralement l'accès à un ensemble de secrets, par exemple un coffre de mots de passe."
    "SALT" -> "Un sel est une valeur aléatoire ajoutée avant de hacher un mot de passe pour rendre les empreintes plus difficiles à réutiliser."
    "XSS" -> "XSS est une faille Web où du code non prévu peut être exécuté dans le navigateur d'un utilisateur."
    "CSRF" -> "CSRF pousse le navigateur d'un utilisateur déjà connecté à effectuer une action qu'il n'avait pas voulue."
    "SQL_INJECTION" -> "Une injection SQL arrive quand une entrée utilisateur modifie dangereusement une requête envoyée à une base de données."
    "SSRF" -> "SSRF pousse un serveur vulnérable à faire une requête vers une destination qu'il ne devrait pas contacter."
    "DNSSEC" -> "DNSSEC ajoute des vérifications cryptographiques aux réponses DNS pour aider à détecter une réponse falsifiée."
    "NAT" -> "NAT traduit des adresses réseau, par exemple pour permettre à plusieurs appareils privés de partager une adresse publique."
    "SPYWARE" -> "Un spyware est un logiciel espion qui cherche à collecter discrètement des informations."
    "RANSOMWARE" -> "Un ransomware bloque ou chiffre des données puis réclame généralement une rançon."
    "ROOTKIT" -> "Un rootkit cherche surtout à rester caché dans un système et à conserver un accès privilégié."
    "ADWARE" -> "Un adware affiche ou injecte de la publicité, parfois de façon très intrusive."
    "TROJAN" -> "Un cheval de Troie paraît légitime ou utile afin de convaincre l'utilisateur de l'exécuter."
    "FIREWALL" -> "Un pare-feu décide quelles communications réseau sont autorisées et lesquelles sont bloquées."
    "HYPERVISOR" -> "Un hyperviseur permet de faire fonctionner plusieurs machines virtuelles sur une même machine physique."
    "REVERSE_PROXY" -> "Un proxy inverse reçoit une requête Web puis la transmet au vrai serveur placé derrière lui."
    "PHISHING" -> "Le phishing cherche à tromper une personne pour lui faire révéler une information ou effectuer une action."
    "VISHING" -> "Le vishing est simplement du phishing réalisé par téléphone ou par la voix."
    "SMURFING" -> "Smurfing désigne une ancienne attaque réseau qui amplifie du trafic ICMP pour saturer une cible."
    "HASHING" -> "Le hashing transforme une donnée en une empreinte courte utilisée notamment pour comparer ou vérifier des données."
    "HARDENING" -> "Le hardening consiste à désactiver ou sécuriser ce qui n'est pas nécessaire afin de réduire les possibilités d'attaque."
    "OSINT" -> "OSINT consiste à chercher et analyser des informations provenant de sources publiques et accessibles légalement."
    "NMAP" -> "Nmap aide à voir quels appareils, ports et services sont visibles sur un réseau que tu es autorisé à analyser."
    "GIT" -> "Git garde l'historique des modifications d'un projet et permet de revenir à des versions précédentes."
    "DOCKER" -> "Docker exécute une application dans un conteneur isolé avec ce dont elle a besoin."
    "GRADLE" -> "Gradle automatise la construction d'un projet, par exemple compiler une application Android et gérer ses dépendances."
    "MFA" -> "MFA demande plusieurs preuves différentes avant d'autoriser une connexion, par exemple mot de passe + validation sur téléphone."
    "INTEGRITY_HASH" -> "Une empreinte cryptographique permet de vérifier qu'une copie de preuve n'a pas été modifiée."
    "PENTEST_SCOPE" -> "Avant un pentest, on définit exactement ce qui peut être testé et on obtient une autorisation claire."
    "DOMAIN_CONTROLLER" -> "Un contrôleur de domaine aide une organisation à gérer de façon centrale les comptes, ordinateurs et connexions."
    "SHARED_RESPONSIBILITY" -> "Dans le cloud, le fournisseur protège certaines choses et le client reste responsable d'autres réglages et données."
    "LEAST_PRIVILEGE" -> "Le moindre privilège signifie donner uniquement les permissions nécessaires, et pas davantage."
    "SECURITY_PATCHES" -> "Un correctif de sécurité répare une faiblesse connue afin de réduire le risque qu'elle soit exploitée."
    else -> summarizeCyberChoice(original, category)
}

private fun beginnerSimpleSchema(key: String, deepDive: CyberChoiceDeepDive): List<String> = when (key) {
    "DNS" -> listOf("Tu écris un nom de site", "DNS cherche son adresse", "DNS renvoie l'adresse", "Le navigateur contacte le site")
    "DHCP" -> listOf("Ton appareil rejoint le réseau", "Il demande ses réglages", "DHCP donne IP + passerelle + DNS", "L'appareil peut communiquer")
    "ARP" -> listOf("Ton PC connaît une IP locale", "Il cherche quel appareil possède cette IP", "ARP obtient son adresse matérielle", "Le PC peut lui envoyer les données")
    "HTTPS" -> listOf("Tu ouvres un site HTTPS", "Le navigateur vérifie le serveur", "Une connexion protégée est créée", "Les données circulent chiffrées")
    "SSH" -> listOf("Tu demandes une connexion distante", "Le serveur est vérifié", "Tu t'authentifies", "Tu obtiens un terminal distant protégé")
    "PHISHING" -> listOf("Un message paraît crédible", "Il crée de l'urgence ou de la confiance", "La victime est poussée à agir", "L'attaquant cherche une information ou un accès")
    "MFA" -> listOf("Tu saisis ton mot de passe", "Un second facteur est demandé", "Le second facteur est vérifié", "L'accès est accordé")
    "LEAST_PRIVILEGE" -> listOf("On identifie la tâche", "On donne seulement les droits nécessaires", "La tâche peut être faite", "Le risque est limité")
    else -> deepDive.schema.take(5).ifEmpty { listOf("Un besoin apparaît", "La notion agit", "Un résultat est produit") }
}

private fun beginnerExpectedOutput(key: String, name: String): String = when (key) {
    "DNS" -> "Avec nslookup, repère surtout le nom demandé puis l'adresse IP renvoyée. C'est la traduction réalisée par DNS."
    "DHCP" -> "Avec ipconfig /all, regarde l'adresse IPv4, la passerelle, le serveur DHCP et le DNS : ce sont des réglages que DHCP peut fournir."
    "ARP" -> "Avec arp -a, tu vois des associations entre adresses IP locales et adresses matérielles."
    "ICMP" -> "Avec ping, regarde si une réponse revient, le temps de réponse et le nombre de paquets perdus."
    "PWD" -> "La commande renvoie un chemin, par exemple /home/alex/Documents : c'est le dossier courant."
    "LS" -> "La commande affiche les noms des fichiers et dossiers présents."
    "WHOAMI" -> "La commande affiche un nom d'utilisateur : c'est le compte qui exécute la commande."
    "BITLOCKER" -> "Le statut indique si le volume est chiffré et si la protection BitLocker est active."
    "SHA256", "INTEGRITY_HASH" -> "Deux fichiers identiques produisent la même empreinte SHA-256 ; si le contenu change, l'empreinte change aussi."
    "NMAP" -> "Dans un labo autorisé, la sortie peut montrer un port ouvert et le service associé. Le but est d'observer, pas d'attaquer."
    "MFA" -> "Il n'y a pas forcément une commande : dans la vraie vie, tu vois une deuxième étape après le mot de passe, par exemple un code ou une validation."
    else -> "Regarde surtout trois choses : ce qui entre, l'action réalisée par $name, puis le résultat obtenu. Les valeurs exactes peuvent changer selon le système."
}

private fun beginnerAdvancedNote(key: String, name: String): String = when (key) {
    "DNS" -> "Optionnel : DNS utilise souvent le port 53. Il existe plusieurs types de réponses DNS, mais tu n'as pas besoin de les mémoriser pour comprendre son rôle de base."
    "DHCP" -> "Optionnel : l'échange classique est souvent résumé par DORA. Le serveur utilise généralement UDP 67 et le client UDP 68. Un bail signifie simplement que l'adresse est prêtée pour une durée."
    "ARP" -> "Optionnel : l'adresse matérielle est appelée adresse MAC. ARP concerne surtout IPv4 sur le réseau local."
    "HTTPS" -> "Optionnel : HTTPS utilise TLS pour protéger les données et utilise généralement le port 443."
    "SSH" -> "Optionnel : SSH utilise généralement le port 22 et peut utiliser des clés au lieu d'un mot de passe."
    "AES" -> "Optionnel : AES est un chiffrement symétrique, ce qui signifie que la même clé secrète sert pour chiffrer et déchiffrer."
    "RSA" -> "Optionnel : RSA est asymétrique : il fonctionne avec une paire clé publique / clé privée."
    "XSS" -> "Optionnel : on distingue plusieurs formes de XSS. Pour l'instant, retiens surtout que du contenu non fiable finit exécuté comme code dans le navigateur."
    "NMAP" -> "Optionnel : Nmap possède beaucoup d'options. Pour débuter, retiens seulement qu'il sert à observer les hôtes, ports et services d'un périmètre autorisé."
    else -> "Optionnel : lorsque l'idée principale est claire, tu peux apprendre le vocabulaire et les détails techniques associés à $name."
}

private fun beginnerObjectives(name: String): List<String> = listOf(
    "Pouvoir expliquer $name avec des mots simples",
    "Comprendre à quoi cette notion sert réellement",
    "Reconnaître un exemple concret sans apprendre une définition par cœur"
)

private fun beginnerCheckpoints(name: String, memory: String): List<String> = listOf(
    "Peux-tu dire avec tes propres mots ce qu'est $name ?",
    "Quel problème $name aide-t-il à résoudre ?",
    "Peux-tu donner un exemple du quotidien ou sur un ordinateur ?",
    "Phrase à comparer avec ta réponse : $memory"
)

private fun beginnerVideos(key: String): List<CyberCourseVideo> = when (key) {
    "DNS" -> listOf(
        CyberCourseVideo(
            title = "Le DNS pour les débutants",
            channel = "IT-Connect - Florian",
            url = "https://www.youtube.com/watch?v=tyDxzzdKnsU"
        )
    )
    "DHCP" -> listOf(
        CyberCourseVideo(
            title = "Comment fonctionne le DHCP expliqué simplement !",
            channel = "Formip - Certification IT",
            url = "https://www.youtube.com/watch?v=qtfW_CbGL9g"
        )
    )
    "ARP" -> listOf(
        CyberCourseVideo(
            title = "Découvre le protocole ARP simplement",
            channel = "Formip - Certification IT",
            url = "https://www.youtube.com/watch?v=BOwYZkME8qY"
        )
    )
    else -> emptyList()
}

private fun beginnerDeepDive(key: String, original: String, category: String): CyberChoiceDeepDive = when (key) {
    "PUBLIC_KEY" -> simpleDeepDive(
        definition = "Une clé publique est une donnée cryptographique conçue pour pouvoir être partagée.",
        role = "Elle permet à d'autres personnes ou logiciels de réaliser certaines opérations avec toi sans connaître ta clé privée.",
        actions = listOf("elle peut être diffusée", "elle fonctionne avec une clé privée liée", "elle peut servir à chiffrer vers toi ou à vérifier certaines signatures selon le système"),
        how = "Imagine une boîte aux lettres : tout le monde peut connaître l'ouverture permettant de déposer un message, mais seule la personne qui possède la clé privée peut effectuer l'opération secrète correspondante.",
        example = "Un serveur peut publier une clé publique tandis que sa clé privée reste protégée sur le serveur.",
        demoTitle = "EXEMPLE VISUEL",
        demo = "Clé publique  → partageable\nClé privée    → secrète\nLes deux sont liées mathématiquement.",
        analogy = "La clé publique ressemble à l'adresse de ta boîte aux lettres : tu peux la donner à tout le monde sans donner la clé qui ouvre la boîte.",
        schema = listOf("Création d'une paire de clés", "Clé publique partagée", "Clé privée gardée secrète", "Les deux servent ensemble selon le mécanisme")
    )
    "PRIVATE_KEY" -> simpleDeepDive(
        definition = "Une clé privée est la partie secrète d'une paire de clés cryptographiques.",
        role = "Elle doit rester protégée car celui qui la possède peut effectuer les opérations réservées au propriétaire de la paire de clés.",
        actions = listOf("elle reste secrète", "elle peut servir à déchiffrer ou signer selon le système", "elle ne doit pas être envoyée comme une clé publique"),
        how = "Une paire de clés est créée ensemble. La partie publique peut être diffusée ; la partie privée reste sur ton appareil ou dans un stockage protégé.",
        example = "Dans SSH, une clé privée peut rester sur ton ordinateur tandis que la clé publique correspondante est installée sur le serveur.",
        demoTitle = "EXEMPLE VISUEL",
        demo = "TON PC : clé privée [SECRÈTE]\nSERVEUR : clé publique [PARTAGEABLE]",
        analogy = "C'est la vraie clé de ta maison : tu peux donner ton adresse à quelqu'un, mais tu ne distribues pas la clé de la porte.",
        schema = listOf("Paire de clés créée", "Partie publique partagée", "Partie privée protégée", "La preuve cryptographique utilise la partie privée")
    )
    "MASTER_PASSWORD" -> simpleDeepDive(
        definition = "Un mot de passe maître est un mot de passe principal utilisé pour déverrouiller un coffre contenant d'autres secrets.",
        role = "Il évite d'avoir à mémoriser chaque mot de passe tout en protégeant l'accès au coffre.",
        actions = listOf("déverrouille le coffre", "protège indirectement plusieurs secrets", "doit être particulièrement fort et unique"),
        how = "Tu saisis le mot de passe maître au gestionnaire de mots de passe. S'il est correct, le coffre local peut être déverrouillé.",
        example = "Un gestionnaire contient tes mots de passe de sites ; tu mémorises surtout le mot de passe maître du gestionnaire.",
        demoTitle = "SCHÉMA",
        demo = "Mot de passe maître → coffre chiffré → mots de passe enregistrés",
        analogy = "C'est la clé du coffre-fort qui contient toutes les autres clés.",
        schema = listOf("Tu saisis le mot de passe maître", "Le coffre est déverrouillé", "Les secrets deviennent accessibles", "Tu refermes le coffre")
    )
    "SALT" -> simpleDeepDive(
        definition = "Un sel cryptographique est une valeur aléatoire ajoutée à un mot de passe avant de calculer son empreinte.",
        role = "Il fait en sorte que deux utilisateurs ayant le même mot de passe n'obtiennent pas forcément la même empreinte stockée.",
        actions = listOf("un sel aléatoire est créé", "il est combiné au mot de passe", "le tout est haché", "le sel peut être stocké avec l'empreinte"),
        how = "Le système ne cherche pas à cacher le sel. Son intérêt est de rendre chaque calcul différent et de compliquer les attaques basées sur des résultats déjà préparés.",
        example = "Alice et Bob utilisent le même mot de passe mais ont deux sels différents : leurs empreintes stockées seront différentes.",
        demoTitle = "EXEMPLE SIMPLE",
        demo = "motdepasse + sel_A → empreinte_A\nmotdepasse + sel_B → empreinte_B",
        analogy = "C'est comme ajouter un ingrédient aléatoire différent à deux recettes identiques : le résultat final n'est plus exactement le même.",
        schema = listOf("Mot de passe", "+ sel aléatoire", "Calcul de l'empreinte", "Empreinte unique stockée")
    )
    "PHISHING" -> simpleDeepDive(
        definition = "Le phishing est une technique de manipulation où quelqu'un se fait passer pour une personne ou un service de confiance.",
        role = "Le but est de te pousser à donner une information, ouvrir un lien, envoyer de l'argent ou valider une action que tu n'aurais pas faite normalement.",
        actions = listOf("crée un message crédible", "utilise souvent urgence, peur ou curiosité", "te dirige vers une action", "récupère l'information ou l'accès si tu te laisses tromper"),
        how = "L'attaque vise surtout la personne. Le message peut imiter une banque, un collègue, une livraison ou un service connu.",
        example = "Tu reçois : « Votre colis est bloqué, payez 1,99 € ici ». Le lien mène vers un faux site qui demande tes coordonnées.",
        demoTitle = "EXEMPLE DE MESSAGE SUSPECT",
        demo = "URGENT : votre compte sera fermé aujourd'hui.\nCliquez ici et confirmez votre mot de passe.",
        analogy = "C'est quelqu'un déguisé en livreur qui essaie de te convaincre de lui donner les clés de la maison.",
        schema = listOf("Faux message crédible", "Création d'urgence", "Victime clique ou répond", "Information ou action récupérée")
    )
    "INTEGRITY_HASH" -> simpleDeepDive(
        definition = "Une empreinte cryptographique est une courte valeur calculée à partir d'un fichier ou d'une donnée.",
        role = "En investigation numérique, elle permet de vérifier qu'une copie analysée est restée identique à la preuve d'origine.",
        actions = listOf("on calcule l'empreinte de la preuve", "on travaille sur une copie", "on recalcule l'empreinte", "on compare les deux valeurs"),
        how = "Si le contenu change, même légèrement, l'empreinte doit normalement changer. Une empreinte identique permet donc de vérifier l'intégrité de la copie.",
        example = "Une image disque est copiée. On calcule SHA-256 avant puis après le transfert et on vérifie que les deux valeurs correspondent.",
        demoTitle = "POWERSHELL",
        demo = "Get-FileHash .\\preuve.img -Algorithm SHA256",
        analogy = "L'empreinte est comme un sceau numérique : si le contenu du colis change, le sceau calculé ne correspond plus.",
        schema = listOf("Preuve originale", "Calcul d'une empreinte", "Copie de travail", "Nouveau calcul", "Comparaison des empreintes")
    )
    "PENTEST_SCOPE" -> simpleDeepDive(
        definition = "Le périmètre d'un pentest définit précisément ce que le testeur a le droit de tester.",
        role = "Il protège le client et le testeur en évitant de toucher à des systèmes ou méthodes qui n'ont pas été autorisés.",
        actions = listOf("on obtient une autorisation écrite", "on liste les systèmes concernés", "on définit les méthodes et horaires permis", "on précise les contacts en cas de problème"),
        how = "Avant toute action technique, les deux parties se mettent d'accord sur les limites. Le test doit rester à l'intérieur de ces limites.",
        example = "Le contrat autorise les tests sur app.exemple.test entre 20 h et 6 h, mais interdit tout test sur le serveur de production principal.",
        demoTitle = "EXEMPLE DE PÉRIMÈTRE",
        demo = "Autorisé : app.exemple.test\nInterdit : production.exemple.test\nPériode : 20:00 → 06:00",
        analogy = "C'est comme un plombier autorisé à travailler dans ta salle de bain mais pas à démonter l'installation électrique de toute la maison.",
        schema = listOf("Autorisation", "Périmètre défini", "Test dans les limites", "Résultats documentés")
    )
    "DOMAIN_CONTROLLER" -> simpleDeepDive(
        definition = "Un contrôleur de domaine est un serveur central d'un environnement Active Directory.",
        role = "Il aide à vérifier les comptes et à fournir les informations de l'annuaire pour les utilisateurs et ordinateurs de l'organisation.",
        actions = listOf("conserve des informations de l'annuaire", "participe à la vérification des connexions", "aide à appliquer une gestion centralisée"),
        how = "Quand un utilisateur se connecte à un ordinateur du domaine, l'environnement peut demander au contrôleur de domaine de vérifier son identité et ses droits.",
        example = "Dans une entreprise, Alice utilise le même compte professionnel pour se connecter à plusieurs postes gérés par le domaine.",
        demoTitle = "SCHÉMA SIMPLE",
        demo = "PC d'Alice → contrôleur de domaine → compte vérifié → session ouverte",
        analogy = "C'est l'accueil central d'une entreprise qui connaît les employés et vérifie leur badge avant de leur donner accès.",
        schema = listOf("Utilisateur demande une connexion", "Le domaine vérifie son compte", "Les informations d'accès sont contrôlées", "La session est autorisée ou refusée")
    )
    "SHARED_RESPONSIBILITY" -> simpleDeepDive(
        definition = "La responsabilité partagée signifie que la sécurité du cloud est répartie entre le fournisseur et le client.",
        role = "Elle rappelle que mettre un service dans le cloud ne transfère pas automatiquement toute la sécurité au fournisseur.",
        actions = listOf("le fournisseur protège son infrastructure", "le client protège ses comptes et données", "la répartition exacte dépend du service utilisé"),
        how = "Le fournisseur gère les couches qu'il contrôle ; le client reste responsable de ses configurations, accès et données selon le contrat du service.",
        example = "Le fournisseur protège le datacenter, mais si le client rend lui-même un stockage public, cette mauvaise configuration reste de sa responsabilité.",
        demoTitle = "SCHÉMA SIMPLE",
        demo = "Fournisseur : infrastructure\nClient : comptes + réglages + données\nResponsabilité : partagée",
        analogy = "Dans un immeuble, le propriétaire sécurise l'entrée commune, mais tu dois encore fermer la porte de ton appartement.",
        schema = listOf("Service cloud choisi", "Le fournisseur protège sa partie", "Le client configure sa partie", "Les deux responsabilités forment la sécurité globale")
    )
    "LEAST_PRIVILEGE" -> simpleDeepDive(
        definition = "Le moindre privilège consiste à donner uniquement les droits nécessaires pour accomplir une tâche.",
        role = "Il réduit les dégâts possibles si un compte ou une application se trompe, est piraté ou fait quelque chose d'imprévu.",
        actions = listOf("identifier la tâche", "donner les droits minimum", "retirer les droits inutiles", "réévaluer les permissions lorsque le besoin change"),
        how = "Au lieu de donner un compte administrateur à tout le monde, on donne seulement les permissions dont chaque personne ou application a besoin.",
        example = "Une application lampe torche n'a pas besoin d'accéder à tes contacts : cette permission devrait être refusée.",
        demoTitle = "EXEMPLE MOBILE",
        demo = "Lampe torche → caméra/flash : nécessaire\nLampe torche → contacts : inutile",
        analogy = "Un employé reçoit la clé de son bureau, pas un passe-partout ouvrant toutes les pièces de l'entreprise.",
        schema = listOf("Tâche à réaliser", "Permissions nécessaires identifiées", "Seulement ces droits sont accordés", "Risque réduit")
    )
    "SECURITY_PATCHES" -> simpleDeepDive(
        definition = "Un correctif de sécurité est une mise à jour qui répare une faiblesse connue dans un logiciel ou un système.",
        role = "Il ferme une porte déjà identifiée avant qu'elle ne puisse être utilisée contre la machine.",
        actions = listOf("l'éditeur découvre ou reçoit un problème", "il développe une correction", "la mise à jour est distribuée", "les utilisateurs l'installent"),
        how = "Une vulnérabilité connue peut devenir une cible facile si la machine reste longtemps sans mise à jour. Installer les correctifs réduit cette exposition.",
        example = "Windows Update propose une mise à jour de sécurité. Après test, l'organisation la déploie pour corriger une vulnérabilité publiée.",
        demoTitle = "EXEMPLE WINDOWS",
        demo = "Paramètres → Windows Update → Rechercher des mises à jour",
        analogy = "C'est comme réparer rapidement une serrure dont on vient de découvrir le défaut, avant qu'un cambrioleur profite de la faiblesse.",
        schema = listOf("Faiblesse découverte", "Correctif créé", "Mise à jour installée", "Faiblesse réduite ou supprimée")
    )
    "GIT" -> simpleDeepDive(
        definition = "Git est un outil qui garde l'historique des modifications d'un projet de fichiers, très utilisé pour le code.",
        role = "Il permet de savoir ce qui a changé, revenir en arrière et travailler à plusieurs sans remplacer aveuglément les fichiers des autres.",
        actions = listOf("enregistre des versions appelées commits", "compare les changements", "permet de créer des branches", "synchronise avec un dépôt distant comme GitHub"),
        how = "Tu modifies des fichiers, tu choisis les changements à enregistrer, puis tu crées un commit qui devient un point dans l'historique.",
        example = "CyberQuiz utilise Git : après une modification, un commit permet de retrouver exactement la version précédente si nécessaire.",
        demoTitle = "TERMINAL",
        demo = "git status\ngit add .\ngit commit -m \"Ma modification\"",
        analogy = "Git ressemble à une série de sauvegardes nommées avec un historique précis de ce qui a changé entre chaque version.",
        schema = listOf("Fichiers modifiés", "Changements sélectionnés", "Commit créé", "Historique conservé")
    )
    "DOCKER" -> simpleDeepDive(
        definition = "Docker est un outil permettant d'exécuter des applications dans des conteneurs.",
        role = "Un conteneur regroupe l'application et son environnement afin qu'elle fonctionne de façon plus prévisible d'une machine à l'autre.",
        actions = listOf("construit ou télécharge une image", "démarre un conteneur", "isole une partie de l'environnement", "permet de recréer facilement l'application"),
        how = "Une image sert de modèle. Docker démarre à partir de cette image un conteneur qui exécute l'application.",
        example = "Un développeur lance une base de données de test dans un conteneur sans l'installer directement dans Windows.",
        demoTitle = "TERMINAL",
        demo = "docker ps",
        analogy = "C'est comme une boîte de transport qui contient tout ce dont une petite application a besoin pour voyager d'un ordinateur à l'autre.",
        schema = listOf("Image Docker", "Conteneur démarré", "Application exécutée", "Environnement reproductible")
    )
    "GRADLE" -> simpleDeepDive(
        definition = "Gradle est un outil d'automatisation de construction de projets logiciels.",
        role = "Dans Android, il aide notamment à télécharger les dépendances, compiler le code et produire l'application.",
        actions = listOf("lit la configuration du projet", "récupère les dépendances", "lance les tâches de compilation", "produit des fichiers comme l'APK"),
        how = "Android Studio demande à Gradle d'exécuter une suite de tâches. Chaque tâche réalise une partie de la construction de l'application.",
        example = "Quand tu cliques sur Run dans CyberQuiz, Gradle participe à la création de l'APK avant son installation sur le téléphone.",
        demoTitle = "TERMINAL",
        demo = ".\\gradlew assembleDebug",
        analogy = "Gradle est comme le chef d'atelier qui suit la recette de fabrication et appelle chaque machine dans le bon ordre.",
        schema = listOf("Projet Android", "Gradle lit la configuration", "Compilation", "APK produit")
    )
    else -> buildCyberChoiceDeepDive(original, category)
}

private fun simpleDeepDive(
    definition: String,
    role: String,
    actions: List<String>,
    how: String,
    example: String,
    demoTitle: String,
    demo: String,
    analogy: String,
    schema: List<String>
) = CyberChoiceDeepDive(
    definition = definition,
    action = role,
    actions = actions,
    details = how,
    example = example,
    demoTitle = demoTitle,
    demo = demo,
    analogy = analogy,
    schema = schema
)
