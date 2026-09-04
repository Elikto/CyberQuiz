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

internal fun hasCyberExpertCourse(term: String): Boolean = canonicalCourseKey(term) != null

private fun canonicalCourseKey(term: String): String? {
    val key = term.trim().uppercase()
    return when (key) {
        "DNS", "53", "RÉSOUDRE UN NOM DE DOMAINE", "RESOUDRE UN NOM DE DOMAINE" -> "DNS"
        "DHCP" -> "DHCP"
        "ARP", "ASSOCIER UNE ADRESSE IP À UNE ADRESSE MAC", "ASSOCIER UNE ADRESSE IP A UNE ADRESSE MAC" -> "ARP"
        "ICMP" -> "ICMP"
        "HTTPS", "443" -> "HTTPS"
        "SSH" -> "SSH"
        "21", "FTP" -> "FTP"
        "8080" -> "HTTP_8080"
        else -> null
    }
}

internal fun buildCyberCourse(term: String, category: String): CyberCourse {
    val original = term.trim()
    val key = canonicalCourseKey(original) ?: original.uppercase()

    val deepDive = when (key) {
        "DNS" -> dnsBeginnerCourse()
        "DHCP" -> dhcpBeginnerCourse()
        "ARP" -> arpBeginnerCourse()
        "ICMP" -> icmpBeginnerCourse()
        "HTTPS" -> httpsBeginnerCourse()
        "SSH" -> sshBeginnerCourse()
        "FTP" -> ftpBeginnerCourse()
        "HTTP_8080" -> http8080BeginnerCourse()
        else -> buildCyberChoiceDeepDive(original, category)
    }

    val displayName = when (key) {
        "HTTP_8080" -> "Port 8080"
        "FTP" -> if (original == "21") "Port 21 / FTP" else "FTP"
        "DNS" -> if (original == "53") "Port 53 / DNS" else "DNS"
        "HTTPS" -> if (original == "443") "Port 443 / HTTPS" else "HTTPS"
        else -> key
    }

    val memorySentence = when (key) {
        "DNS" -> "DNS transforme un nom facile à retenir, comme example.com, en une adresse que l'ordinateur peut utiliser."
        "DHCP" -> "DHCP donne automatiquement à ton appareil les réglages réseau dont il a besoin pour communiquer."
        "ARP" -> "ARP aide ton ordinateur à trouver, sur le réseau local, quel appareil correspond à une adresse IP."
        "ICMP" -> "ICMP sert surtout à envoyer des messages de contrôle et de diagnostic, comme ceux utilisés par ping."
        "HTTPS" -> "HTTPS protège les échanges entre ton navigateur et un site Web afin qu'ils ne circulent pas en clair."
        "SSH" -> "SSH permet de contrôler un autre ordinateur à distance dans une connexion protégée."
        "FTP" -> "FTP sert à transférer des fichiers entre un client et un serveur."
        "HTTP_8080" -> "8080 est simplement un numéro de porte réseau souvent utilisé par un service Web."
        else -> "Cette notion réalise une fonction précise dans le domaine $category."
    }

    val simpleSchemaTitle: String
    val simpleSchema: List<String>
    val outputTitle: String
    val output: String
    val advancedNote: String

    when (key) {
        "DNS" -> {
            simpleSchemaTitle = "LE DNS EN UNE IMAGE"
            simpleSchema = listOf(
                "Tu écris : example.com",
                "Ton ordinateur demande : « Quelle est son adresse ? »",
                "Le DNS cherche la réponse",
                "Le DNS renvoie une adresse IP",
                "Ton navigateur peut contacter le site"
            )
            outputTitle = "EXEMPLE · CE QUE TU PEUX VOIR"
            output = """
> nslookup example.com

Nom :     example.com
Address:  [adresse IP du site]

À comprendre :
example.com = le nom humain
Address = l'adresse trouvée par DNS
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : DNS utilise souvent le port 53. Il existe plusieurs types de réponses DNS, par exemple A pour une adresse IPv4 et MX pour la messagerie. Tu n'as pas besoin de les mémoriser pour comprendre le rôle de base du DNS."
        }
        "DHCP" -> {
            simpleSchemaTitle = "LE DHCP EN UNE IMAGE"
            simpleSchema = listOf(
                "Ton téléphone rejoint le Wi-Fi",
                "Il demande : « Quels réglages réseau dois-je utiliser ? »",
                "Le DHCP choisit une adresse disponible",
                "Il donne IP + passerelle + DNS",
                "Ton téléphone peut communiquer sur le réseau"
            )
            outputTitle = "EXEMPLE · CE QUE DHCP A DONNÉ"
            output = """
> ipconfig /all

DHCP activé : Oui
Adresse IPv4 : 192.168.1.42
Passerelle : 192.168.1.1
Serveur DHCP : 192.168.1.1
Serveur DNS : 1.1.1.1

À comprendre :
DHCP a rempli ces réglages automatiquement.
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : l'échange DHCP classique suit quatre messages que l'on résume par DORA. Le serveur utilise généralement UDP 67 et le client UDP 68. Un « bail » signifie simplement que l'adresse est prêtée pour une certaine durée."
        }
        "ARP" -> {
            simpleSchemaTitle = "ARP EN UNE IMAGE"
            simpleSchema = listOf(
                "Ton PC veut parler à un appareil du réseau local",
                "Il connaît son adresse IP",
                "Il demande : « Quel appareil possède cette IP ? »",
                "L'appareil répond avec son adresse matérielle",
                "Ton PC peut lui envoyer les données localement"
            )
            outputTitle = "EXEMPLE · TABLE ARP"
            output = """
> arp -a

192.168.1.1    34-ab-37-12-56-90
192.168.1.20   b8-27-eb-44-11-2a

À comprendre :
à gauche = adresse IP
à droite = adresse matérielle de l'appareil local
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : l'adresse matérielle est appelée adresse MAC. ARP concerne surtout IPv4 sur le réseau local. Il peut être détourné par de fausses réponses, ce qu'on appelle souvent ARP spoofing."
        }
        "ICMP" -> {
            simpleSchemaTitle = "PING ET ICMP EN UNE IMAGE"
            simpleSchema = listOf(
                "Ton PC envoie : « Est-ce que tu me reçois ? »",
                "Le message traverse le réseau",
                "La machine distante répond si elle l'autorise",
                "Ton PC mesure le temps de réponse",
                "Tu obtiens un indice sur l'état de la connexion"
            )
            outputTitle = "EXEMPLE · PING"
            output = """
> ping 1.1.1.1

Réponse de 1.1.1.1 : temps=14 ms
Réponse de 1.1.1.1 : temps=13 ms

Paquets perdus : 0

À comprendre :
la machine répond et le réseau fonctionne jusqu'à elle.
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : ping utilise des messages ICMP appelés Echo Request et Echo Reply. Un ping bloqué ne veut pas forcément dire que la machine est éteinte : un pare-feu peut simplement refuser ce type de message."
        }
        "HTTPS" -> {
            simpleSchemaTitle = "HTTPS EN UNE IMAGE"
            simpleSchema = listOf(
                "Tu ouvres un site en https://",
                "Ton navigateur vérifie le site",
                "Une connexion protégée est créée",
                "Tes données sont chiffrées pendant le trajet",
                "Le serveur et ton navigateur peuvent échanger"
            )
            outputTitle = "EXEMPLE · UNE RÉPONSE HTTPS"
            output = """
> curl -I https://example.com

HTTP/2 200
content-type: text/html

À comprendre :
la connexion vers le site utilise HTTPS.
Le contenu circule dans une connexion protégée.
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : HTTPS utilise TLS pour protéger les données et utilise généralement le port 443. Le cadenas signifie que la connexion est protégée, pas forcément que le site lui-même est digne de confiance."
        }
        "SSH" -> {
            simpleSchemaTitle = "SSH EN UNE IMAGE"
            simpleSchema = listOf(
                "Tu es devant ton ordinateur",
                "Tu demandes une connexion vers un serveur",
                "Le serveur prouve son identité",
                "Tu t'authentifies",
                "Tu obtiens un terminal distant protégé"
            )
            outputTitle = "EXEMPLE · CONNEXION SSH"
            output = """
> ssh utilisateur@192.168.1.50

Password:
utilisateur@serveur:~$

À comprendre :
tu écris sur ton PC,
mais les commandes sont exécutées sur le serveur distant.
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : SSH utilise généralement le port 22. On peut se connecter avec un mot de passe ou avec des clés. Les clés sont souvent préférées pour l'administration professionnelle."
        }
        "FTP" -> {
            simpleSchemaTitle = "FTP EN UNE IMAGE"
            simpleSchema = listOf(
                "Ton ordinateur se connecte à un serveur de fichiers",
                "Tu t'identifies si nécessaire",
                "Tu demandes un fichier ou tu en envoies un",
                "Le fichier est transféré",
                "La connexion se termine"
            )
            outputTitle = "EXEMPLE · CONNEXION FTP"
            output = """
> ftp 192.168.1.50

Connected to 192.168.1.50
Name: utilisateur
Password:
Login successful

À comprendre :
le client est connecté au serveur de fichiers.
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : FTP classique utilise généralement le port 21 pour les commandes et n'est pas chiffré par défaut. Pour un transfert sécurisé, on préfère souvent SFTP ou FTPS selon le système."
        }
        "HTTP_8080" -> {
            simpleSchemaTitle = "UN PORT EN UNE IMAGE"
            simpleSchema = listOf(
                "Une machine possède une adresse IP",
                "Plusieurs programmes peuvent y fonctionner",
                "Chaque programme peut écouter sur un numéro de port",
                "Le client vise ici le port 8080",
                "Le programme qui écoute sur 8080 reçoit la connexion"
            )
            outputTitle = "EXEMPLE · PORT 8080"
            output = """
> curl http://192.168.1.50:8080

[réponse du service Web]

À comprendre :
192.168.1.50 = la machine
8080 = le point d'entrée choisi sur cette machine
            """.trimIndent()
            advancedNote = "Quand tu seras à l'aise : 8080 est souvent utilisé pour du Web alternatif, du développement ou certaines interfaces. Le numéro de port ne garantit pas à lui seul quel programme fonctionne derrière."
        }
        else -> {
            simpleSchemaTitle = "IDÉE PRINCIPALE"
            simpleSchema = deepDive.schema
            outputTitle = "EXEMPLE"
            output = deepDive.demo
            advancedNote = "Pour aller plus loin, retiens d'abord la fonction principale avant d'ajouter les détails techniques."
        }
    }

    val videos = when (key) {
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
            ),
            CyberCourseVideo(
                title = "Comprendre le fonctionnement du DHCP en moins de 2 minutes !",
                channel = "Formip - Certification IT",
                url = "https://www.youtube.com/watch?v=qYBmMgKUNa4"
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

    return CyberCourse(
        title = "Cours · $displayName",
        subtitle = "Explication simple, pensée pour quelqu'un qui débute",
        memorySentence = memorySentence,
        objectives = beginnerObjectives(key, displayName),
        deepDive = deepDive,
        simpleSchemaTitle = simpleSchemaTitle,
        simpleSchema = simpleSchema,
        expectedOutputTitle = outputTitle,
        expectedOutput = output,
        checkpoints = beginnerCheckpoints(key),
        advancedNote = advancedNote,
        videos = videos,
        videoSearchQuery = "$displayName expliqué simplement français débutant"
    )
}

private fun beginnerObjectives(key: String, name: String): List<String> = when (key) {
    "DNS" -> listOf(
        "Comprendre pourquoi on utilise des noms comme google.com au lieu d'adresses numériques",
        "Savoir dire en une phrase ce que fait DNS",
        "Ne plus confondre DNS et DHCP"
    )
    "DHCP" -> listOf(
        "Comprendre pourquoi ton téléphone reçoit automatiquement une adresse réseau",
        "Savoir ce que DHCP donne réellement à un appareil",
        "Ne plus confondre DHCP et DNS"
    )
    "ARP" -> listOf(
        "Comprendre pourquoi un ordinateur doit reconnaître les appareils proches",
        "Savoir ce qu'ARP cherche",
        "Ne plus confondre ARP, DNS et DHCP"
    )
    "ICMP" -> listOf(
        "Comprendre ce que teste la commande ping",
        "Savoir à quoi servent les messages ICMP",
        "Comprendre pourquoi un ping peut parfois être bloqué"
    )
    "HTTPS" -> listOf(
        "Comprendre ce que protège HTTPS",
        "Savoir ce que signifie le cadenas du navigateur",
        "Retenir que HTTPS protège le trajet des données"
    )
    "SSH" -> listOf(
        "Comprendre comment on peut administrer un serveur à distance",
        "Savoir pourquoi SSH protège cette connexion",
        "Reconnaître une commande SSH simple"
    )
    else -> listOf(
        "Comprendre simplement ce qu'est $name",
        "Savoir à quoi il sert dans la vraie vie",
        "Savoir le distinguer des notions proches"
    )
}

private fun beginnerCheckpoints(key: String): List<String> = when (key) {
    "DNS" -> listOf(
        "Si tu connais google.com mais pas son adresse IP, quel service t'aide ?",
        "DNS donne-t-il une adresse IP à ton PC ou cherche-t-il l'adresse d'un nom ?",
        "Peux-tu expliquer DNS avec l'image d'un annuaire ?"
    )
    "DHCP" -> listOf(
        "Quand ton téléphone rejoint le Wi-Fi, qui peut lui donner automatiquement ses réglages réseau ?",
        "Cite au moins deux informations que DHCP peut fournir.",
        "Peux-tu expliquer DHCP avec l'image d'une réception d'hôtel ?"
    )
    "ARP" -> listOf(
        "ARP sert-il surtout à trouver un appareil proche ou un site Internet par son nom ?",
        "Que connaît ton PC au départ : une IP ou l'adresse matérielle ?",
        "Peux-tu expliquer ARP sans utiliser de jargon ?"
    )
    "ICMP" -> listOf(
        "Quelle commande très connue utilise ICMP pour tester une réponse ?",
        "Si ping ne répond pas, la machine est-elle forcément éteinte ?",
        "Que t'indique approximativement le temps affiché par ping ?"
    )
    "HTTPS" -> listOf(
        "Que protège principalement HTTPS ?",
        "Le cadenas garantit-il qu'un site est honnête ?",
        "Quel est le numéro de port habituel de HTTPS si tu veux aller un peu plus loin ?"
    )
    "SSH" -> listOf(
        "SSH permet-il surtout de naviguer sur un site ou de contrôler une machine à distance ?",
        "Pourquoi protège-t-on la connexion SSH ?",
        "À quoi ressemble une commande SSH simple ?"
    )
    else -> listOf(
        "Peux-tu expliquer cette notion en une phrase simple ?",
        "À quoi sert-elle concrètement ?",
        "Quel exemple du quotidien t'aide à la retenir ?"
    )
}

private fun dnsBeginnerCourse() = CyberChoiceDeepDive(
    definition = "DNS signifie Domain Name System. Son rôle le plus simple est de faire le lien entre un nom facile à retenir, comme example.com, et l'adresse utilisée par les ordinateurs pour trouver le bon serveur.",
    action = "DNS sert à répondre à une question très simple : « Je connais le nom du site, mais où se trouve-t-il sur le réseau ? ». Sans DNS, il faudrait souvent retenir des adresses numériques au lieu de noms.",
    actions = listOf(
        "reçoit le nom que ton appareil veut trouver",
        "cherche si l'adresse correspondante est déjà connue",
        "sinon demande la réponse à d'autres serveurs DNS",
        "renvoie l'adresse trouvée à ton appareil"
    ),
    details = "Quand tu ouvres un site, ton ordinateur commence souvent par regarder s'il connaît déjà l'adresse. S'il ne la connaît pas, il demande à un serveur DNS. Ce serveur cherche la réponse et la renvoie. Ton navigateur peut alors contacter directement le serveur du site. L'idée importante est seulement : nom → recherche DNS → adresse → connexion.",
    example = "Tu écris example.com dans ton navigateur. Ton ordinateur ne peut pas envoyer les données uniquement avec ce nom. DNS trouve l'adresse correspondant à example.com, puis le navigateur utilise cette adresse pour joindre le site.",
    demoTitle = "COMMANDE SIMPLE",
    demo = """
> nslookup example.com

Cherche surtout la ligne :
Address: ...

C'est l'adresse trouvée pour le nom demandé.
    """.trimIndent(),
    analogy = "DNS ressemble au répertoire de ton téléphone. Tu appuies sur le nom « Maman » ; ton téléphone retrouve le numéro enregistré et l'utilise pour appeler. Toi, tu retiens le nom. Le système retrouve l'adresse.",
    schema = listOf(
        "Nom du site",
        "Question envoyée au DNS",
        "DNS trouve l'adresse",
        "Adresse renvoyée",
        "Connexion au site"
    )
)

private fun dhcpBeginnerCourse() = CyberChoiceDeepDive(
    definition = "DHCP signifie Dynamic Host Configuration Protocol. Son rôle est de donner automatiquement à un appareil les informations dont il a besoin pour fonctionner correctement sur un réseau.",
    action = "DHCP évite que tu aies à remplir toi-même les réglages réseau de ton téléphone ou de ton ordinateur. Il peut donner une adresse IP, la passerelle pour sortir du réseau local et l'adresse du service DNS.",
    actions = listOf(
        "repère qu'un nouvel appareil demande une configuration",
        "choisit une adresse IP disponible",
        "envoie les principaux réglages réseau",
        "garde en mémoire que cette adresse est utilisée pendant un certain temps"
    ),
    details = "Quand ton appareil rejoint un réseau, il demande automatiquement s'il existe un service capable de le configurer. Le serveur DHCP lui propose des réglages. L'appareil les accepte, puis il peut communiquer. L'adresse donnée n'est pas forcément définitive : elle est généralement prêtée pour une durée appelée bail. Pour commencer, retiens surtout : DHCP configure automatiquement l'appareil.",
    example = "Tu arrives chez un ami et connectes ton téléphone au Wi-Fi. Tu tapes seulement le mot de passe Wi-Fi. Tu ne renseignes aucune adresse réseau à la main. Quelques secondes plus tard, Internet fonctionne : DHCP a fourni les réglages nécessaires en arrière-plan.",
    demoTitle = "COMMANDE SIMPLE SOUS WINDOWS",
    demo = """
> ipconfig /all

Cherche :
DHCP activé
Adresse IPv4
Passerelle par défaut
Serveur DHCP
Serveurs DNS
    """.trimIndent(),
    analogy = "Imagine un hôtel. Tu arrives à la réception sans numéro de chambre. La réception te donne une chambre disponible, t'indique la sortie et les services utiles. DHCP fait pareil avec un appareil : il lui donne les informations nécessaires pour trouver sa place sur le réseau.",
    schema = listOf(
        "Appareil rejoint le réseau",
        "Il demande une configuration",
        "DHCP choisit une adresse",
        "DHCP envoie les réglages",
        "Appareil prêt à communiquer"
    )
)

private fun arpBeginnerCourse() = CyberChoiceDeepDive(
    definition = "ARP signifie Address Resolution Protocol. Il aide un ordinateur à reconnaître physiquement un autre appareil qui se trouve sur le même réseau local.",
    action = "Ton ordinateur peut connaître l'adresse IP d'un appareil proche sans encore savoir exactement à quel matériel local envoyer les données. ARP lui permet de demander : « Qui possède cette adresse IP ? ».",
    actions = listOf(
        "regarde si la réponse est déjà mémorisée",
        "sinon demande sur le réseau local qui possède l'adresse IP recherchée",
        "reçoit la réponse de l'appareil concerné",
        "mémorise cette correspondance pendant un moment"
    ),
    details = "Sur ton réseau local, plusieurs appareils sont connectés au même routeur ou au même équipement réseau. Quand ton PC veut parler directement à l'un d'eux, ARP l'aide à retrouver le bon appareil à partir de son adresse IP. Tu peux donc voir ARP comme un petit service de mise en relation entre l'adresse IP et l'appareil local.",
    example = "Ton PC veut envoyer quelque chose à l'imprimante 192.168.1.20. Il demande sur le réseau : « Quel appareil possède 192.168.1.20 ? ». L'imprimante répond. Ton PC sait alors à quel appareil local envoyer les données.",
    demoTitle = "COMMANDE SIMPLE",
    demo = """
> arp -a

Tu verras des lignes du type :
192.168.1.1    34-ab-37-12-56-90

La première valeur est l'adresse IP.
La seconde identifie le matériel local.
    """.trimIndent(),
    analogy = "Imagine une résidence. Tu connais le numéro d'appartement, mais tu veux savoir quelle boîte aux lettres correspond à cet appartement. ARP pose la question aux voisins et mémorise la réponse.",
    schema = listOf(
        "Je connais l'IP de l'appareil",
        "ARP demande qui possède cette IP",
        "L'appareil concerné répond",
        "La réponse est mémorisée",
        "Les données peuvent être envoyées localement"
    )
)

private fun icmpBeginnerCourse() = CyberChoiceDeepDive(
    definition = "ICMP est un système de messages utilisé par les appareils réseau pour donner des informations simples sur l'état d'une communication. La commande ping utilise ICMP.",
    action = "ICMP sert surtout au diagnostic. Il peut aider à savoir si une machine répond ou signaler qu'un problème est survenu pendant le trajet des données.",
    actions = listOf(
        "envoie un petit message de test",
        "attend une réponse",
        "mesure le temps nécessaire pour l'aller-retour",
        "peut signaler certains problèmes de réseau"
    ),
    details = "Avec ping, ton ordinateur envoie un petit message. Si la machine distante accepte de répondre, elle renvoie un message. Ton PC affiche alors le temps de réponse. C'est un test utile, mais pas une preuve absolue : certains pare-feu bloquent volontairement ces messages.",
    example = "Internet semble lent. Tu lances ping vers une adresse connue. Si les réponses arrivent, tu sais qu'une partie du réseau fonctionne. Si elles n'arrivent pas, il faut continuer le diagnostic au lieu de conclure immédiatement que la machine distante est éteinte.",
    demoTitle = "COMMANDE SIMPLE",
    demo = """
> ping 1.1.1.1

Cherche :
Réponse de ...
temps=... ms
perdus=0

Le temps indique approximativement la durée de l'aller-retour.
    """.trimIndent(),
    analogy = "C'est comme crier « Tu m'entends ? » dans une pièce et attendre « Oui ! ». Tu sais qu'une réponse revient, et tu peux estimer combien de temps elle a mis.",
    schema = listOf(
        "Ton PC envoie un message de test",
        "Le réseau transporte le message",
        "La machine distante répond",
        "La réponse revient",
        "Ton PC affiche le temps"
    )
)

private fun httpsBeginnerCourse() = CyberChoiceDeepDive(
    definition = "HTTPS est la version protégée des échanges Web. Quand l'adresse d'un site commence par https://, le navigateur crée une connexion chiffrée avec le serveur.",
    action = "HTTPS sert à empêcher qu'une personne placée sur le trajet puisse lire facilement ce que tu envoies ou modifier les données sans être détectée.",
    actions = listOf(
        "contacte le serveur du site",
        "vérifie que le serveur présente une identité numérique valable",
        "crée une connexion chiffrée",
        "fait ensuite circuler les pages et formulaires dans cette connexion protégée"
    ),
    details = "Avant d'envoyer la page ou ton mot de passe, le navigateur et le serveur mettent en place une connexion protégée. Ensuite, les données sont transformées de façon à ne pas être lisibles simplement pendant leur trajet. C'est la partie essentielle à retenir.",
    example = "Tu te connectes à un site bancaire depuis un Wi-Fi public. HTTPS protège le contenu envoyé entre ton navigateur et le serveur. Quelqu'un qui observe seulement le réseau ne devrait pas voir ton mot de passe en clair.",
    demoTitle = "EXEMPLE SIMPLE",
    demo = """
Adresse protégée :
https://example.com

Indice visuel :
le navigateur affiche généralement un symbole indiquant que la connexion est sécurisée.
    """.trimIndent(),
    analogy = "Imagine que ton message voyage dans une enveloppe fermée plutôt que sur une carte postale. Les personnes sur le trajet voient qu'une enveloppe circule, mais pas directement ce qu'elle contient.",
    schema = listOf(
        "Navigateur contacte le site",
        "Le site prouve son identité",
        "Connexion protégée créée",
        "Données chiffrées pendant le trajet",
        "Page reçue par le navigateur"
    )
)

private fun sshBeginnerCourse() = CyberChoiceDeepDive(
    definition = "SSH signifie Secure Shell. Il permet d'utiliser à distance le terminal d'un autre ordinateur à travers une connexion protégée.",
    action = "SSH sert beaucoup à administrer des serveurs. Tu peux être devant ton ordinateur personnel et envoyer des commandes à un serveur situé ailleurs.",
    actions = listOf(
        "contacte l'ordinateur distant",
        "vérifie l'identité du serveur",
        "demande à l'utilisateur de s'authentifier",
        "ouvre un terminal distant protégé"
    ),
    details = "Une fois connecté, ce que tu tapes dans ton terminal local est envoyé au serveur. Le serveur exécute les commandes puis renvoie le résultat. La connexion est chiffrée pour protéger les échanges pendant le trajet.",
    example = "Un administrateur doit vérifier l'espace disque d'un serveur qui se trouve dans un datacenter. Il utilise SSH depuis son PC, ouvre un terminal distant et lance la commande sans se déplacer devant le serveur.",
    demoTitle = "COMMANDE SIMPLE",
    demo = """
> ssh utilisateur@192.168.1.50

Puis, après connexion :
utilisateur@serveur:~$

Tu es maintenant dans le terminal du serveur distant.
    """.trimIndent(),
    analogy = "SSH ressemble à une télécommande sécurisée pour ordinateur. Tu restes chez toi, mais tes actions sont exécutées sur la machine distante.",
    schema = listOf(
        "Ton ordinateur",
        "Connexion SSH protégée",
        "Authentification",
        "Serveur distant",
        "Terminal distant disponible"
    )
)

private fun ftpBeginnerCourse() = CyberChoiceDeepDive(
    definition = "FTP signifie File Transfer Protocol. C'est un protocole ancien utilisé pour envoyer et récupérer des fichiers sur un serveur.",
    action = "FTP sert à déplacer des fichiers entre deux machines, par exemple envoyer un fichier vers un serveur ou télécharger un fichier depuis ce serveur.",
    actions = listOf(
        "se connecte au serveur de fichiers",
        "authentifie l'utilisateur si nécessaire",
        "permet de lister les fichiers",
        "envoie ou télécharge les fichiers demandés"
    ),
    details = "Le principe est simple : un client FTP se connecte au serveur, l'utilisateur s'identifie, puis il peut gérer des fichiers selon ses droits. FTP classique est ancien et ne protège pas automatiquement les données pendant le trajet.",
    example = "Une ancienne entreprise possède un serveur FTP pour recevoir des fichiers. Un utilisateur s'y connecte puis dépose un document. Sur un réseau non fiable, on préfère aujourd'hui une solution de transfert protégée.",
    demoTitle = "COMMANDE SIMPLE",
    demo = """
> ftp 192.168.1.50

Connected to 192.168.1.50
Name: utilisateur
Password:
    """.trimIndent(),
    analogy = "FTP ressemble à un comptoir de dépôt de colis : tu t'identifies, tu déposes un fichier ou tu en récupères un.",
    schema = listOf(
        "Client FTP",
        "Connexion au serveur",
        "Authentification",
        "Choix du fichier",
        "Envoi ou téléchargement"
    )
)

private fun http8080BeginnerCourse() = CyberChoiceDeepDive(
    definition = "8080 est un numéro de port. Un port est comme un numéro de porte qui permet de savoir quel programme d'une machine doit recevoir une connexion réseau.",
    action = "Le port 8080 est souvent utilisé par des applications Web quand elles n'utilisent pas le port Web habituel. Il ne chiffre rien et n'est pas un protocole à lui seul.",
    actions = listOf(
        "identifie un point d'entrée sur une machine",
        "dirige la connexion vers le programme qui écoute sur ce numéro",
        "permet à plusieurs programmes réseau de fonctionner sur la même adresse IP"
    ),
    details = "Une machine peut faire fonctionner plusieurs services en même temps. Ils partagent la même adresse IP, mais utilisent des ports différents. Si une application écoute sur 8080, le client précise :8080 dans l'adresse pour demander ce service précis.",
    example = "Tu ouvres http://192.168.1.50:8080. 192.168.1.50 désigne la machine. 8080 indique quel service de cette machine tu veux joindre.",
    demoTitle = "EXEMPLE SIMPLE",
    demo = """
http://192.168.1.50:8080

192.168.1.50 = machine
8080 = numéro de porte réseau
    """.trimIndent(),
    analogy = "L'adresse IP est l'adresse d'un immeuble. Le port est le numéro d'une porte à l'intérieur. 8080 est simplement l'une de ces portes possibles.",
    schema = listOf(
        "Adresse de la machine",
        "Port 8080 demandé",
        "Programme qui écoute sur 8080",
        "Connexion acceptée",
        "Réponse envoyée au client"
    )
)
