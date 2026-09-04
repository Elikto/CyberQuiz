package com.example.cyberquiz.ui.screens

internal data class CyberCourseExtra(
    val stepByStep: List<String>,
    val secondSchemaTitle: String,
    val secondSchema: List<String>,
    val commonMistakes: List<String>,
    val securityWhy: String,
    val glossary: List<String>
)

internal fun buildCyberCourseExtra(term: String, category: String): CyberCourseExtra {
    val key = extraCourseKey(term)
    val deepDive = buildCyberChoiceDeepDive(term, category)

    val steps = when (key) {
        "DNS" -> listOf(
            "Tu saisis un nom de site, par exemple cyberquiz.fr.",
            "Ton appareil demande au service DNS quelle adresse correspond à ce nom.",
            "Si la réponse est déjà connue et encore valable, elle peut être rendue immédiatement.",
            "Sinon, la recherche continue auprès des serveurs DNS nécessaires.",
            "Une adresse IP est renvoyée à ton appareil.",
            "Ton navigateur peut alors contacter le serveur du site."
        )
        "DHCP" -> listOf(
            "Ton appareil se connecte au Wi-Fi ou au réseau sans avoir encore ses réglages IP.",
            "Il cherche un serveur DHCP capable de lui proposer une configuration.",
            "Le serveur choisit une adresse IP disponible et propose aussi les autres réglages utiles.",
            "L'appareil accepte la proposition.",
            "Le serveur confirme l'attribution pour une durée limitée appelée bail.",
            "L'appareil peut maintenant communiquer et renouvellera son bail plus tard."
        )
        "HTTPS" -> listOf(
            "Ton navigateur se connecte au serveur du site.",
            "Le serveur présente un certificat numérique afin de prouver son identité.",
            "Le navigateur vérifie que ce certificat est acceptable.",
            "Les deux côtés créent des clés temporaires pour protéger la session.",
            "Les requêtes et réponses Web sont ensuite chiffrées pendant leur transport.",
            "Un espion sur le réseau voit passer des données, mais ne doit pas pouvoir lire simplement leur contenu."
        )
        "PWD" -> listOf(
            "Tu ouvres un terminal Linux.",
            "Le terminal travaille toujours depuis un dossier courant.",
            "Tu tapes pwd.",
            "Linux affiche le chemin complet du dossier courant.",
            "Tu sais maintenant précisément où les commandes relatives vont agir."
        )
        "SUDO" -> listOf(
            "Tu lances une commande qui nécessite davantage de droits.",
            "sudo vérifie si ton utilisateur est autorisé à demander ces droits.",
            "Selon la configuration, ton mot de passe peut être demandé.",
            "La commande précise est exécutée avec les droits prévus.",
            "Quand elle se termine, tu ne restes pas automatiquement dans une session root permanente."
        )
        "BITLOCKER" -> listOf(
            "Windows chiffre les données enregistrées sur le volume.",
            "La clé permettant de déchiffrer le volume est elle-même protégée.",
            "Au démarrage normal, le mécanisme de protection autorisé peut déverrouiller le disque.",
            "Si le disque est retiré et lu depuis une autre machine, son contenu reste chiffré.",
            "Une clé de récupération peut être nécessaire si le déverrouillage normal échoue."
        )
        "AES" -> listOf(
            "On possède une donnée lisible et une clé secrète.",
            "AES transforme la donnée en contenu chiffré à l'aide de cette clé.",
            "Le contenu chiffré peut être stocké ou transmis.",
            "Pour retrouver la donnée lisible, il faut la clé appropriée.",
            "La sécurité dépend fortement de la protection de cette clé et de la bonne façon d'utiliser AES."
        )
        "PUBLIC_KEY" -> listOf(
            "Une paire de clés est créée : une publique et une privée.",
            "La clé publique peut être distribuée sans être gardée secrète.",
            "La clé privée reste uniquement chez son propriétaire.",
            "Selon le mécanisme utilisé, la clé publique peut servir à vérifier une signature ou à protéger une donnée destinée au propriétaire.",
            "La clé privée réalise l'opération sensible correspondante."
        )
        "XSS" -> listOf(
            "Une application Web reçoit du contenu provenant d'un utilisateur ou d'une source non fiable.",
            "L'application réaffiche ce contenu dans une page sans le traiter correctement.",
            "Le navigateur peut interpréter une partie de ce contenu comme du code au lieu de simple texte.",
            "Ce code s'exécute alors dans le contexte de la page.",
            "La protection consiste notamment à encoder correctement les sorties et à éviter les constructions dangereuses."
        )
        "SQL_INJECTION" -> listOf(
            "Une application reçoit une valeur saisie par un utilisateur.",
            "Elle doit envoyer une requête à sa base de données.",
            "Si elle mélange directement la valeur utilisateur avec le texte SQL, la structure de la requête peut être modifiée.",
            "La base de données peut alors recevoir une requête différente de celle prévue.",
            "Les requêtes paramétrées séparent clairement les données utilisateur de la syntaxe SQL."
        )
        "RANSOMWARE" -> listOf(
            "Le logiciel malveillant parvient à s'exécuter sur une machine.",
            "Il cherche des données accessibles localement ou parfois sur des partages réseau.",
            "Il chiffre ou rend indisponibles ces données.",
            "Une demande de rançon est affichée ou transmise.",
            "Les sauvegardes isolées, les mises à jour et la limitation des droits réduisent l'impact possible."
        )
        "PHISHING" -> listOf(
            "L'attaquant prépare un message qui paraît crédible ou urgent.",
            "La victime est poussée à cliquer, répondre, ouvrir un document ou saisir une information.",
            "L'attaquant exploite surtout la confiance, la peur, l'urgence ou l'habitude.",
            "La victime peut transmettre un secret ou réaliser une action dangereuse.",
            "La vérification de l'expéditeur, du contexte et du canal permet souvent de repérer le piège."
        )
        "NMAP" -> listOf(
            "On définit d'abord le périmètre que l'on est autorisé à analyser.",
            "Nmap envoie des sondes réseau vers les machines ou ports concernés.",
            "Il observe les réponses ou l'absence de réponse.",
            "Il classe les ports et peut essayer d'identifier les services visibles.",
            "L'analyste utilise ces résultats pour comprendre l'exposition réseau, jamais pour dépasser le périmètre autorisé."
        )
        "MFA" -> listOf(
            "Tu saisis d'abord une première preuve, souvent ton mot de passe.",
            "Le service demande ensuite une autre preuve différente.",
            "Cette seconde preuve peut être une application d'authentification, une clé physique ou un autre facteur.",
            "Le compte n'est ouvert que si les vérifications nécessaires réussissent.",
            "Ainsi, connaître uniquement le mot de passe ne suffit normalement plus."
        )
        "OSINT" -> listOf(
            "On définit la question à laquelle on cherche à répondre.",
            "On collecte uniquement des informations issues de sources ouvertes et accessibles légalement.",
            "On note l'origine et la date des informations.",
            "On compare plusieurs sources pour éviter de croire une donnée fausse ou dépassée.",
            "On transforme ensuite les informations vérifiées en conclusion utile."
        )
        "TROJAN" -> listOf(
            "Un programme est présenté comme légitime, utile ou attirant.",
            "L'utilisateur est convaincu de l'installer ou de l'exécuter.",
            "Une fois lancé, le programme réalise aussi une action cachée ou malveillante.",
            "Contrairement à un ver, son principe central est surtout la tromperie pour être exécuté.",
            "Télécharger depuis des sources fiables et vérifier les fichiers réduit le risque."
        )
        "VISHING" -> listOf(
            "L'attaquant contacte la victime par téléphone ou message vocal.",
            "Il se fait passer pour une personne ou une organisation crédible.",
            "Il crée souvent un sentiment d'urgence ou de peur.",
            "Il demande une information, une validation ou une action sensible.",
            "La bonne réaction est de raccrocher puis de recontacter l'organisation avec un numéro officiel connu."
        )
        "INTEGRITY_HASH" -> listOf(
            "On calcule une empreinte de la donnée originale.",
            "On conserve cette empreinte comme valeur de référence.",
            "Plus tard, on recalcule l'empreinte de la copie ou du fichier analysé.",
            "Si les deux empreintes correspondent, cela soutient l'idée que le contenu n'a pas changé.",
            "Si elles diffèrent, il faut comprendre pourquoi avant de considérer la donnée comme identique."
        )
        "PENTEST_SCOPE" -> listOf(
            "Le propriétaire du système donne une autorisation explicite.",
            "Le périmètre précise les machines, applications, horaires et techniques autorisés.",
            "Les limites et contacts d'urgence sont définis avant les tests.",
            "Le test est réalisé uniquement à l'intérieur de ces règles.",
            "Les résultats sont documentés avec leur impact et des recommandations de correction."
        )
        "DOMAIN_CONTROLLER" -> listOf(
            "Un utilisateur essaie d'accéder à une ressource de l'entreprise.",
            "Le domaine doit vérifier son identité et ses droits.",
            "Le contrôleur de domaine fournit les services centraux nécessaires à cette gestion.",
            "Il s'appuie notamment sur l'annuaire Active Directory et des mécanismes d'authentification.",
            "Les postes et utilisateurs peuvent ainsi être gérés de manière centralisée."
        )
        "SHARED_RESPONSIBILITY" -> listOf(
            "Le fournisseur cloud sécurise l'infrastructure qui lui appartient.",
            "Le client reste responsable d'une partie de ce qu'il configure et stocke dans le cloud.",
            "La frontière exacte dépend du service utilisé.",
            "Le client doit donc savoir quelles tâches de sécurité lui appartiennent encore.",
            "Une mauvaise configuration côté client peut créer un risque même si la plateforme cloud fonctionne correctement."
        )
        "SECURITY_PATCHES" -> listOf(
            "Un éditeur découvre ou reçoit le signalement d'un défaut dans son logiciel.",
            "Il prépare une correction et la publie sous forme de mise à jour.",
            "L'administrateur teste puis déploie cette mise à jour selon son contexte.",
            "La vulnérabilité corrigée devient plus difficile à exploiter sur les systèmes à jour.",
            "Les correctifs doivent rester associés à des sauvegardes, de la supervision et de bonnes configurations."
        )
        else -> listOf(
            "On part d'un besoin précis dans le domaine $category.",
            "La notion reçoit une information ou un contexte.",
            "Elle réalise l'action décrite dans le cours.",
            "Un résultat peut ensuite être observé ou utilisé par un autre composant.",
            "Pour bien la retenir, relie toujours le nom de la notion à son entrée, son action et son résultat."
        )
    }

    val schema = when (key) {
        "DNS" -> listOf("Nom du site", "Question DNS", "Recherche de l'adresse", "Adresse IP", "Connexion au serveur")
        "DHCP" -> listOf("Appareil sans configuration", "Demande DHCP", "Proposition de réglages", "Acceptation", "IP + passerelle + DNS", "Appareil connecté")
        "HTTPS" -> listOf("Navigateur", "Vérification du serveur", "Création d'un canal protégé", "Données Web chiffrées", "Serveur")
        "XSS" -> listOf("Entrée non fiable", "Application Web", "Contenu mal traité", "Navigateur l'interprète comme code", "Risque pour l'utilisateur")
        "MFA" -> listOf("Mot de passe", "+ deuxième preuve", "Vérification des facteurs", "Accès autorisé")
        "RANSOMWARE" -> listOf("Exécution du malware", "Accès aux données", "Chiffrement / blocage", "Données indisponibles", "Demande de rançon")
        else -> listOf("Besoin", term.trim(), "Action", "Résultat", "Utilisation du résultat")
    }

    val mistakes = when (key) {
        "DNS" -> listOf(
            "Confondre DNS et DHCP : DNS retrouve l'adresse liée à un nom ; DHCP configure un appareil sur le réseau.",
            "Croire que DNS chiffre les communications : DNS répond à une question de nom/adresse ; HTTPS/TLS protège les échanges Web.",
            "Penser qu'une réponse DNS reste valable pour toujours : elle peut être mise en cache seulement pendant une certaine durée."
        )
        "DHCP" -> listOf(
            "Confondre DHCP et DNS : DHCP fournit des réglages réseau ; DNS traduit des noms.",
            "Croire que l'adresse reçue est forcément définitive : elle est souvent attribuée pour une durée limitée.",
            "Croire que DHCP fournit seulement une IP : il peut aussi fournir passerelle, DNS et d'autres paramètres."
        )
        "HTTPS" -> listOf(
            "Croire que le cadenas signifie que le site lui-même est honnête : HTTPS protège surtout la connexion.",
            "Confondre le port 443 et le chiffrement : 443 est un numéro de port ; TLS réalise la protection cryptographique.",
            "Croire que HTTPS cache tout : certains éléments de la connexion peuvent encore rester observables."
        )
        "AES" -> listOf(
            "Confondre chiffrement et hachage : AES peut être déchiffré avec la clé ; SHA-256 produit une empreinte.",
            "Croire que l'algorithme suffit : la clé et le mode d'utilisation doivent aussi être correctement gérés.",
            "Partager la clé secrète sans protection annule une grande partie de l'intérêt du chiffrement."
        )
        "XSS" -> listOf(
            "Confondre XSS et injection SQL : XSS concerne surtout le navigateur ; l'injection SQL vise la construction des requêtes de base de données.",
            "Penser que seuls les champs de formulaire sont concernés : toute donnée non fiable réinjectée dans une page peut devoir être traitée correctement.",
            "Croire qu'un pare-feu classique remplace un code applicatif sûr : la correction doit aussi être faite dans l'application."
        )
        "NMAP" -> listOf(
            "Utiliser Nmap sur un système sans autorisation explicite.",
            "Croire qu'un port ouvert prouve à lui seul une vulnérabilité.",
            "Prendre l'identification d'un service comme une certitude absolue : certains résultats restent des estimations."
        )
        "MFA" -> listOf(
            "Croire que deux mots de passe différents constituent deux facteurs : ils restent le même type de preuve.",
            "Valider une notification MFA que l'on n'a pas soi-même déclenchée.",
            "Croire que MFA rend le compte invulnérable : il réduit fortement certains risques mais ne remplace pas les autres protections."
        )
        else -> listOf(
            "Mémoriser seulement le nom sans comprendre le rôle réel de la notion.",
            "Confondre cette notion avec un outil voisin parce qu'ils apparaissent dans le même domaine.",
            "Essayer de retenir trop de détails techniques avant d'avoir compris l'idée principale."
        )
    }

    val securityWhy = when (key) {
        "DNS" -> "Beaucoup de communications commencent par une résolution DNS. Comprendre DNS aide donc à diagnostiquer des pannes, repérer certaines redirections anormales et interpréter ce que fait une machine avant sa connexion à un service."
        "DHCP" -> "DHCP décide d'une partie importante de la configuration réseau d'un appareil. Une mauvaise configuration ou un serveur DHCP non autorisé peut orienter le trafic vers de mauvais équipements ou empêcher le réseau de fonctionner normalement."
        "HTTPS" -> "HTTPS protège les données Web pendant leur transport et aide le navigateur à vérifier l'identité du serveur. C'est une base essentielle de la sécurité des sites, des API et des connexions utilisateur."
        "PWD", "SUDO" -> "Les commandes Linux sont souvent utilisées en administration et en cybersécurité. Savoir précisément où l'on se trouve et avec quels droits on agit évite des erreurs et aide à comprendre les actions réalisées sur une machine."
        "BITLOCKER" -> "Le chiffrement du disque protège les données au repos. Si un ordinateur portable est perdu ou volé, un disque correctement chiffré réduit fortement le risque de lecture directe des fichiers."
        "AES", "PUBLIC_KEY" -> "La cryptographie protège des fichiers, connexions, sauvegardes, identités et signatures. Comprendre le rôle des clés évite de confondre chiffrement, hachage et signature numérique."
        "XSS", "SQL_INJECTION" -> "Ces vulnérabilités montrent pourquoi une application ne doit jamais faire confiance aveuglément aux données qu'elle reçoit. Elles sont fondamentales pour comprendre la sécurité des applications Web."
        "RANSOMWARE", "TROJAN" -> "Comprendre le comportement des logiciels malveillants aide à reconnaître les signes d'infection et surtout à mettre en place des protections qui limitent leur exécution et leur impact."
        "PHISHING", "VISHING" -> "Une grande partie des attaques vise directement les personnes. Comprendre la manipulation utilisée permet de reconnaître l'urgence artificielle, l'usurpation d'identité et les demandes inhabituelles."
        "NMAP", "PENTEST_SCOPE" -> "Les outils de test ne sont utiles que dans un cadre autorisé et maîtrisé. La cybersécurité professionnelle repose autant sur la méthode, le périmètre et la preuve que sur l'outil technique lui-même."
        "MFA" -> "Les vols de mots de passe sont fréquents. MFA ajoute une barrière supplémentaire afin qu'un mot de passe compromis ne suffise normalement pas à ouvrir le compte."
        "OSINT" -> "Les informations publiques peuvent révéler beaucoup sur une organisation ou une personne. Les équipes de sécurité utilisent l'OSINT pour comprendre leur exposition tout en respectant la loi et les règles du périmètre."
        else -> "Cette notion est importante parce qu'elle permet de comprendre une partie du fonctionnement réel des systèmes. En cybersécurité, savoir ce qui est normal aide ensuite à reconnaître ce qui est anormal, risqué ou mal configuré."
    }

    val glossary = when (key) {
        "DNS" -> listOf("Adresse IP : numéro utilisé pour identifier une machine ou une interface sur un réseau.", "Résolveur DNS : service qui cherche la réponse DNS pour ton appareil.", "Cache : copie temporaire d'une réponse gardée pour éviter de refaire immédiatement la même recherche.")
        "DHCP" -> listOf("Bail : durée pendant laquelle une configuration IP est attribuée à un appareil.", "Passerelle : équipement vers lequel l'appareil envoie le trafic destiné à d'autres réseaux.", "Masque réseau : information qui aide l'appareil à savoir quelles adresses sont considérées comme locales.")
        "HTTPS" -> listOf("TLS : mécanisme cryptographique qui protège la connexion utilisée par HTTPS.", "Certificat : document numérique utilisé notamment pour relier une identité à une clé publique.", "Port : numéro logique permettant de distinguer plusieurs services réseau sur une même machine.")
        "AES" -> listOf("Clé secrète : valeur qui doit rester connue uniquement des personnes ou systèmes autorisés.", "Chiffrement : transformation d'une donnée lisible en donnée illisible sans la clé appropriée.", "Déchiffrement : opération qui permet de retrouver la donnée lisible avec la clé correcte.")
        "XSS" -> listOf("Navigateur : programme utilisé pour consulter les sites Web.", "Script : petit programme exécuté dans un contexte donné, souvent JavaScript dans une page Web.", "Encoder une sortie : transformer certains caractères pour qu'ils soient affichés comme du texte et non interprétés comme du code.")
        "MFA" -> listOf("Facteur : type de preuve d'identité.", "Authentification : vérification de l'identité d'un utilisateur.", "Clé de sécurité : petit dispositif physique pouvant servir de facteur d'authentification.")
        else -> listOf(
            "Entrée : information reçue par un outil, service ou programme.",
            "Action : traitement réalisé à partir de cette entrée.",
            "Résultat : information ou effet obtenu après le traitement."
        )
    }

    return CyberCourseExtra(
        stepByStep = steps,
        secondSchemaTitle = "LE PARCOURS COMPLET, ÉTAPE PAR ÉTAPE",
        secondSchema = schema,
        commonMistakes = mistakes,
        securityWhy = securityWhy,
        glossary = glossary
    )
}

private fun extraCourseKey(term: String): String {
    return when (term.trim().uppercase()) {
        "53" -> "DNS"
        "DHCP" -> "DHCP"
        "HTTPS", "443" -> "HTTPS"
        "PWD" -> "PWD"
        "SUDO" -> "SUDO"
        "BITLOCKER" -> "BITLOCKER"
        "AES" -> "AES"
        "LA CLÉ PUBLIQUE", "LA CLE PUBLIQUE", "CLÉ PUBLIQUE", "CLE PUBLIQUE" -> "PUBLIC_KEY"
        "XSS" -> "XSS"
        "SQL INJECTION" -> "SQL_INJECTION"
        "RANSOMWARE" -> "RANSOMWARE"
        "PHISHING", "TROMPER UNE VICTIME POUR OBTENIR UNE INFORMATION" -> "PHISHING"
        "NMAP" -> "NMAP"
        "MFA", "MULTI-FACTOR AUTHENTICATION" -> "MFA"
        "OSINT", "OPEN SOURCE INTELLIGENCE" -> "OSINT"
        "CHEVAL DE TROIE" -> "TROJAN"
        "VISHING" -> "VISHING"
        "POUR VÉRIFIER SON INTÉGRITÉ", "POUR VERIFIER SON INTEGRITE" -> "INTEGRITY_HASH"
        "LE PÉRIMÈTRE ET L'AUTORISATION DU TEST", "LE PERIMETRE ET L'AUTORISATION DU TEST" -> "PENTEST_SCOPE"
        "IL FOURNIT NOTAMMENT L'AUTHENTIFICATION ET LES SERVICES D'ANNUAIRE DU DOMAINE" -> "DOMAIN_CONTROLLER"
        "LA RÉPARTITION DES RESPONSABILITÉS DE SÉCURITÉ ENTRE LE FOURNISSEUR ET LE CLIENT", "LA REPARTITION DES RESPONSABILITES DE SECURITE ENTRE LE FOURNISSEUR ET LE CLIENT" -> "SHARED_RESPONSIBILITY"
        "POUR CORRIGER NOTAMMENT DES VULNÉRABILITÉS CONNUES", "POUR CORRIGER NOTAMMENT DES VULNERABILITES CONNUES" -> "SECURITY_PATCHES"
        else -> term.trim().uppercase()
    }
}
