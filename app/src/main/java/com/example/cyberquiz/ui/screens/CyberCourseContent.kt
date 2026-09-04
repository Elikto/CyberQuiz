package com.example.cyberquiz.ui.screens

internal data class CyberCourseVideo(
    val title: String,
    val channel: String,
    val url: String
)

internal data class CyberCourse(
    val title: String,
    val subtitle: String,
    val objectives: List<String>,
    val deepDive: CyberChoiceDeepDive,
    val expectedOutputTitle: String,
    val expectedOutput: String,
    val checkpoints: List<String>,
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
        "DNS" -> dnsExpertCourse()
        "DHCP" -> dhcpExpertCourse()
        "ARP" -> arpExpertCourse()
        "ICMP" -> icmpExpertCourse()
        "HTTPS" -> httpsExpertCourse()
        "SSH" -> sshExpertCourse()
        "FTP" -> ftpExpertCourse()
        "HTTP_8080" -> http8080ExpertCourse()
        else -> buildCyberChoiceDeepDive(original, category)
    }

    val displayName = when (key) {
        "HTTP_8080" -> "Port 8080 / HTTP alternatif"
        "FTP" -> if (original == "21") "Port 21 / FTP" else "FTP"
        "DNS" -> if (original == "53") "Port 53 / DNS" else "DNS"
        "HTTPS" -> if (original == "443") "Port 443 / HTTPS" else "HTTPS"
        else -> key
    }

    val outputTitle: String
    val output: String
    when (key) {
        "DNS" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · nslookup"
            output = """
> nslookup example.com
Serveur :  192.168.1.1
Address:   192.168.1.1

Réponse :
Nom :      example.com
Address:   [adresse IP renvoyée par le DNS]

À lire : le premier serveur est ton résolveur DNS ; la dernière adresse est la réponse recherchée. Les valeurs réelles peuvent varier.
            """.trimIndent()
        }
        "DHCP" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · ipconfig /all"
            output = """
DHCP activé . . . . . . . . . : Oui
Adresse IPv4 . . . . . . . . : 192.168.1.42
Masque de sous-réseau . . . . : 255.255.255.0
Passerelle par défaut . . . . : 192.168.1.1
Serveur DHCP . . . . . . . . : 192.168.1.1
Serveurs DNS . . . . . . . . : 1.1.1.1
Bail obtenu . . . . . . . . .: 22:10
Expiration du bail . . . . . : 22:10 demain

À lire : DHCP a fourni l'IP du poste, le masque, la passerelle, le DNS et une durée de bail.
            """.trimIndent()
        }
        "ARP" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · arp -a"
            output = """
> arp -a
Interface : 192.168.1.42
  Adresse Internet   Adresse physique      Type
  192.168.1.1        34-ab-37-12-56-90     dynamique
  192.168.1.20       b8-27-eb-44-11-2a     dynamique

À lire : chaque ligne associe une IPv4 locale à une adresse MAC connue par la machine.
            """.trimIndent()
        }
        "ICMP" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · ping"
            output = """
> ping 1.1.1.1
Réponse de 1.1.1.1 : octets=32 temps=14 ms TTL=57
Réponse de 1.1.1.1 : octets=32 temps=13 ms TTL=57

Paquets : envoyés = 2, reçus = 2, perdus = 0

À lire : la cible répond aux Echo Request ; le temps indique le délai aller-retour et TTL donne un indice sur le trajet restant.
            """.trimIndent()
        }
        "HTTPS" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · curl"
            output = """
> curl -I https://example.com
HTTP/2 200
content-type: text/html

La commande utilise HTTPS : HTTP circule à l'intérieur d'une session TLS. Le port standard est TCP 443.
            """.trimIndent()
        }
        "SSH" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · ssh"
            output = """
> ssh utilisateur@192.168.1.50
The authenticity of host '192.168.1.50' can't be established.
Are you sure you want to continue connecting? yes
utilisateur@192.168.1.50's password:
utilisateur@serveur:~$

À lire : le client vérifie d'abord l'identité du serveur, puis l'utilisateur s'authentifie avant d'obtenir le shell distant.
            """.trimIndent()
        }
        "FTP" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · connexion FTP"
            output = """
> ftp 192.168.1.50
Connected to 192.168.1.50.
220 FTP Server ready
Name: utilisateur
331 Password required
230 Login successful

FTP classique n'est pas chiffré par défaut. Pour un transfert sécurisé, on rencontre plutôt SFTP ou FTPS selon le besoin.
            """.trimIndent()
        }
        "HTTP_8080" -> {
            outputTitle = "RÉSULTAT À SAVOIR LIRE · service Web sur 8080"
            output = """
> curl -I http://192.168.1.50:8080
HTTP/1.1 200 OK
Content-Type: text/html

Le numéro 8080 indique seulement le port d'écoute choisi ici. Ce n'est pas un protocole de chiffrement.
            """.trimIndent()
        }
        else -> {
            outputTitle = "EXEMPLE DE RÉSULTAT"
            output = "Identifie l'entrée, l'action effectuée et le résultat produit par cette notion."
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
                title = "Le secret derrière ARP et les adresses MAC révélé !",
                channel = "Formip - Certification IT",
                url = "https://www.youtube.com/watch?v=F3Jn_aqloVQ"
            ),
            CyberCourseVideo(
                title = "Découvre le secret du protocole ARP en 3 minutes !",
                channel = "Formip - Certification IT",
                url = "https://www.youtube.com/watch?v=BOwYZkME8qY"
            )
        )
        else -> emptyList()
    }

    return CyberCourse(
        title = "Cours expert · $displayName",
        subtitle = "Du niveau débutant jusqu'aux détails utiles en administration réseau et cybersécurité",
        objectives = courseObjectives(key, displayName),
        deepDive = deepDive,
        expectedOutputTitle = outputTitle,
        expectedOutput = output,
        checkpoints = courseCheckpoints(key),
        videos = videos,
        videoSearchQuery = "$displayName cours réseau cybersécurité français"
    )
}

private fun courseObjectives(key: String, name: String): List<String> = when (key) {
    "DNS" -> listOf(
        "Savoir expliquer pourquoi Internet a besoin du DNS et ce qu'est une résolution de nom",
        "Comprendre résolveur récursif, cache, serveurs racine, TLD et serveur faisant autorité",
        "Reconnaître les principaux enregistrements DNS : A, AAAA, CNAME, MX, NS, TXT, PTR et SOA",
        "Savoir utiliser nslookup, Resolve-DnsName ou dig pour diagnostiquer une résolution",
        "Comprendre les différences entre DNS classique, DNSSEC, DoT et DoH"
    )
    "DHCP" -> listOf(
        "Savoir exactement ce que DHCP configure sur un appareil et pourquoi il évite la configuration manuelle",
        "Comprendre le cycle DORA : Discover, Offer, Request, ACK",
        "Connaître UDP 67/68, le broadcast initial, les baux, T1/T2 et le renouvellement",
        "Comprendre les options DHCP : masque, passerelle, DNS et durée de bail",
        "Savoir reconnaître un problème DHCP et comprendre les protections contre un serveur DHCP pirate"
    )
    "ARP" -> listOf(
        "Comprendre pourquoi une machine IPv4 a besoin d'une adresse MAC pour envoyer une trame sur son LAN",
        "Savoir lire une table ARP et différencier IP, MAC, paquet IP et trame Ethernet",
        "Comprendre la différence entre destination locale et destination distante via la passerelle",
        "Connaître le principe de l'ARP spoofing et les protections réseau associées"
    )
    "ICMP" -> listOf(
        "Comprendre qu'ICMP transporte des messages de contrôle réseau et n'utilise pas de ports TCP/UDP",
        "Comprendre ping, Echo Request/Echo Reply, TTL et Time Exceeded",
        "Voir le rôle d'ICMP dans traceroute et dans certains mécanismes de Path MTU Discovery",
        "Savoir pourquoi bloquer tout ICMP peut compliquer le diagnostic et parfois le fonctionnement réseau"
    )
    else -> listOf(
        "Savoir définir $name sans jargon inutile",
        "Comprendre les actions réalisées et le déroulement réel",
        "Savoir reconnaître la notion dans un terminal ou une capture réseau",
        "Savoir la distinguer des protocoles ou services voisins"
    )
}

private fun courseCheckpoints(key: String): List<String> = when (key) {
    "DNS" -> listOf(
        "Quelle différence fais-tu maintenant entre DNS et DHCP ?",
        "À quoi servent un résolveur récursif et un serveur faisant autorité ?",
        "Que représentent A, AAAA, CNAME et MX ?",
        "Pourquoi un résultat DNS peut-il rester en cache ?",
        "DNSSEC et DoH protègent-ils exactement la même chose ?"
    )
    "DHCP" -> listOf(
        "Peux-tu citer les quatre messages DORA dans l'ordre ?",
        "Pourquoi le client utilise-t-il souvent un broadcast au tout début ?",
        "À quoi servent UDP 67 et UDP 68 ?",
        "Quelle différence entre l'adresse IP reçue, la passerelle et le serveur DNS ?",
        "Que se passe-t-il quand le bail doit être renouvelé ?",
        "Pourquoi un rogue DHCP représente-t-il un risque ?"
    )
    "ARP" -> listOf(
        "ARP traduit-il un nom en IP ou une IPv4 locale en MAC ?",
        "Pourquoi ARP reste-t-il limité au réseau local ?",
        "Si la cible est sur Internet, quelle adresse MAC ton PC cherche-t-il réellement ?",
        "À quoi sert le cache ARP ?"
    )
    "ICMP" -> listOf(
        "ICMP utilise-t-il un numéro de port ?",
        "Quelle différence entre un Echo Reply et un Time Exceeded ?",
        "Que mesure le temps affiché par ping ?",
        "Pourquoi un ping qui échoue ne prouve-t-il pas toujours que la machine est hors ligne ?"
    )
    else -> listOf(
        "Quel problème cette notion résout-elle ?",
        "À quelle couche ou partie du système intervient-elle ?",
        "Quelles actions réalise-t-elle ?",
        "Quel résultat observable produit-elle ?",
        "Avec quelle notion proche pourrais-tu la confondre ?"
    )
}

private fun dnsExpertCourse() = CyberChoiceDeepDive(
    definition = "DNS signifie Domain Name System. C'est le système de noms distribué qui permet de retrouver des informations associées à un nom, notamment l'adresse IP d'un serveur. Au lieu de retenir une IPv4 ou une IPv6 pour chaque service, les humains utilisent des noms comme example.com et le DNS fournit les données nécessaires aux applications.",
    action = "DNS sert principalement à résoudre des noms. Quand ton navigateur connaît un nom mais pas l'adresse du serveur, il demande une réponse DNS. Le DNS peut aussi indiquer les serveurs de messagerie d'un domaine, les serveurs faisant autorité, des alias, des textes de validation et d'autres informations. Il ne donne pas une adresse IP à ton PC : ça, c'est le rôle typique de DHCP.",
    actions = listOf(
        "reçoit une question contenant un nom et un type d'enregistrement recherché",
        "vérifie d'abord si une réponse valide existe déjà dans un cache",
        "si nécessaire, interroge d'autres serveurs DNS jusqu'à obtenir une réponse faisant autorité",
        "renvoie au client l'enregistrement et sa durée de validité appelée TTL",
        "permet de rechercher notamment A/AAAA pour les adresses, MX pour la messagerie, NS pour les serveurs DNS et PTR pour certaines résolutions inverses"
    ),
    details = "Une résolution classique implique plusieurs rôles. Ton appareil envoie généralement sa question à un résolveur récursif configuré sur l'interface réseau. S'il n'a pas la réponse en cache, ce résolveur peut commencer par la racine DNS, qui l'oriente vers les serveurs du TLD comme .com, puis vers le serveur faisant autorité du domaine. Ce dernier fournit l'enregistrement demandé. Le résolveur garde souvent la réponse en cache pendant le TTL afin de répondre plus vite aux prochaines demandes. DNS utilise historiquement UDP 53 pour de nombreuses requêtes et TCP 53 lorsque nécessaire, par exemple pour certaines réponses volumineuses et pour les transferts de zone. DNSSEC ajoute des signatures permettant de vérifier l'authenticité et l'intégrité des données DNS ; DoT et DoH chiffrent le transport des requêtes entre le client et un résolveur, mais ce ne sont pas les mêmes objectifs.",
    example = "Tu saisis https://example.com. Avant d'établir la connexion Web, ton navigateur doit savoir où envoyer les paquets. Le système consulte ses caches puis interroge DNS. Une fois l'adresse IP obtenue, la connexion réseau vers le serveur peut commencer. Si DNS est en panne, Internet peut sembler 'cassé' alors qu'une connexion directe à une IP fonctionne encore.",
    demoTitle = "TERMINAL / POWERSHELL · OBSERVER DNS",
    demo = """
Windows :
> nslookup example.com
> Resolve-DnsName example.com -Type A
> ipconfig /displaydns

Linux :
> dig example.com A
> resolvectl status

Wireshark :
dns
    """.trimIndent(),
    analogy = "Imagine le DNS comme un annuaire mondial distribué. Tu connais le nom d'une personne ou d'une entreprise, mais tu as besoin de son numéro pour la joindre. Le résolveur joue le rôle du service qui consulte l'annuaire pour toi. Le cache correspond aux numéros déjà notés récemment dans ton téléphone.",
    schema = listOf(
        "Navigateur : je veux joindre example.com",
        "Résolveur local : ai-je déjà la réponse en cache ?",
        "Sinon : racine DNS → serveur du TLD → serveur faisant autorité",
        "Réponse DNS : adresse / enregistrement + TTL",
        "Cache du résolveur et du client",
        "Connexion vers l'adresse obtenue"
    )
)

private fun dhcpExpertCourse() = CyberChoiceDeepDive(
    definition = "DHCP signifie Dynamic Host Configuration Protocol. Il automatise la configuration IP d'un client. Sur un réseau IPv4 courant, il permet à un PC, un téléphone, une VM ou une imprimante d'obtenir une adresse IP utilisable ainsi que d'autres paramètres sans qu'un humain configure chaque appareil à la main.",
    action = "DHCP sert à mettre automatiquement un appareil en état de communiquer correctement sur le réseau. Il peut fournir l'adresse IPv4, le masque de sous-réseau, la passerelle par défaut, un ou plusieurs serveurs DNS, la durée du bail et diverses options. Le serveur gère un pool d'adresses afin de savoir quelles adresses peuvent être proposées aux clients.",
    actions = listOf(
        "écoute les demandes des clients et identifie le réseau depuis lequel elles arrivent",
        "choisit une adresse disponible dans un pool ou applique une réservation connue pour ce client",
        "propose l'adresse et les paramètres réseau associés",
        "confirme le bail et mémorise temporairement que cette adresse est utilisée par ce client",
        "permet au client de renouveler son bail avant son expiration",
        "peut fonctionner à travers plusieurs sous-réseaux grâce à un DHCP Relay plutôt que d'installer un serveur DHCP partout"
    ),
    details = "Le scénario IPv4 classique est résumé par DORA. 1) DHCP Discover : le client ne connaît pas encore le serveur et n'a souvent pas d'adresse IPv4 utilisable ; il émet donc une découverte, fréquemment en broadcast. 2) DHCP Offer : un serveur répond avec une proposition contenant une adresse et des options. 3) DHCP Request : le client annonce l'offre qu'il souhaite accepter. 4) DHCP ACK : le serveur confirme le bail et le client configure son interface. Le service utilise UDP : le serveur écoute classiquement sur le port 67 et le client sur le port 68. Le bail n'est pas forcément permanent. Le client tente normalement de le renouveler avant expiration ; on rencontre les notions de T1 et T2 pour les phases de renouvellement/rebinding. Si aucun serveur n'est accessible, certains systèmes IPv4 peuvent s'auto-attribuer une adresse link-local 169.254.0.0/16, souvent appelée APIPA sous Windows. Sur un grand réseau, un routeur ou équipement L3 peut relayer les requêtes DHCP vers un serveur situé sur un autre sous-réseau. DHCPv6 existe aussi, mais son fonctionnement n'est pas simplement une copie exacte de DORA et il peut coexister avec SLAAC en IPv6.",
    example = "Tu arrives chez toi et connectes ton téléphone au Wi-Fi. Tu saisis seulement la clé Wi-Fi : tu ne renseignes ni 192.168.1.x, ni masque, ni passerelle, ni DNS. Après l'association Wi-Fi, le client DHCP obtient automatiquement ces paramètres. Sans DHCP, il faudrait les configurer manuellement et éviter soi-même les doublons d'adresses.",
    demoTitle = "POWERSHELL / TERMINAL · VOIR ET RENOUVELER LE BAIL",
    demo = """
Windows :
> ipconfig /all
> ipconfig /release
> ipconfig /renew
> Get-NetIPConfiguration

Linux :
> ip addr
> ip route
> resolvectl status

Wireshark :
dhcp

À observer : IP, masque, passerelle, DNS, serveur DHCP, début et expiration du bail.
    """.trimIndent(),
    analogy = "Imagine la réception d'un hôtel. Tu arrives sans numéro de chambre. La réception choisit une chambre libre, te donne le numéro, t'indique où se trouve la sortie et les services utiles, puis te précise jusqu'à quand la chambre t'est réservée. DHCP fait la même chose avec une configuration réseau : l'adresse IP est ta chambre et le bail est la durée de réservation.",
    schema = listOf(
        "Client connecté mais sans configuration IPv4",
        "DISCOVER → Qui peut me configurer ?",
        "OFFER ← Voici une IP + masque + passerelle + DNS + durée",
        "REQUEST → Je demande cette offre",
        "ACK ← Bail confirmé",
        "Interface configurée et trafic réseau possible",
        "Plus tard : renouvellement du bail avant expiration"
    )
)

private fun arpExpertCourse() = CyberChoiceDeepDive(
    definition = "ARP signifie Address Resolution Protocol. En IPv4 sur un réseau local Ethernet, ARP permet de connaître l'adresse MAC à laquelle envoyer une trame lorsqu'on connaît l'adresse IPv4 de la destination locale — ou l'adresse IPv4 de la passerelle si la destination finale se trouve sur un autre réseau.",
    action = "ARP fait le lien pratique entre l'adressage IPv4 de couche réseau et l'adressage MAC utilisé pour livrer une trame sur le segment local. Il ne traduit pas un nom de domaine : DNS s'en charge. Il n'attribue pas non plus l'adresse IP : DHCP peut le faire.",
    actions = listOf(
        "consulte d'abord le cache ARP pour voir si l'association IPv4 ↔ MAC est déjà connue",
        "si elle manque, diffuse une ARP Request sur le réseau local",
        "la machine qui possède l'IPv4 demandée répond avec son adresse MAC",
        "le demandeur mémorise temporairement cette association dans son cache",
        "utilise ensuite la MAC trouvée comme destination de la trame Ethernet"
    ),
    details = "Avant l'envoi, le système détermine si l'IPv4 de destination est dans le même sous-réseau grâce à son IP et son masque. Si la destination est locale, il cherche la MAC de cette destination. Si elle est distante, il ne cherche pas la MAC du serveur Internet : il cherche celle de sa passerelle par défaut, car c'est le prochain saut local. La requête ARP est diffusée à tous sur le LAN ; la réponse est normalement envoyée par le propriétaire de l'IPv4. Les entrées sont stockées dans un cache et expirent. ARP n'est pas routé au-delà du segment local et concerne IPv4 ; IPv6 utilise Neighbor Discovery avec ICMPv6 à la place. Côté sécurité, ARP ne fournit pas d'authentification forte par lui-même, ce qui explique les attaques d'ARP spoofing/poisoning. Dans des réseaux administrés, des protections comme Dynamic ARP Inspection, souvent associée aux informations de DHCP Snooping, peuvent aider à bloquer des associations incohérentes.",
    example = "Ton PC 192.168.1.42 veut envoyer un paquet à 192.168.1.20. Les deux sont sur le même /24. Ton PC doit construire une trame Ethernet ; s'il ne connaît pas la MAC de 192.168.1.20, il demande en broadcast 'Qui a 192.168.1.20 ?'. La cible répond avec sa MAC, puis le PC peut envoyer la trame. Pour joindre 8.8.8.8, il résout plutôt la MAC de 192.168.1.1, sa passerelle.",
    demoTitle = "TERMINAL / POWERSHELL · OBSERVER LES VOISINS",
    demo = """
Windows :
> arp -a
> Get-NetNeighbor -AddressFamily IPv4

Linux :
> ip neigh

Test simple :
> ping 192.168.1.1
> arp -a

Wireshark :
arp
    """.trimIndent(),
    analogy = "Imagine un immeuble. L'adresse IP indique l'appartement logique que tu cherches, mais pour remettre physiquement un colis dans le hall local, tu as besoin de savoir quelle porte/boîte correspond à cette personne. ARP demande aux voisins 'qui possède cette adresse IP ?' puis mémorise la réponse quelque temps.",
    schema = listOf(
        "Paquet IPv4 prêt à être envoyé",
        "Destination dans mon sous-réseau ? oui → IPv4 de la cible ; non → IPv4 de la passerelle",
        "Cache ARP : association déjà connue ?",
        "Sinon ARP Request en broadcast",
        "ARP Reply : voici mon adresse MAC",
        "Cache mis à jour",
        "Trame Ethernet envoyée à cette MAC"
    )
)

private fun icmpExpertCourse() = CyberChoiceDeepDive(
    definition = "ICMP signifie Internet Control Message Protocol. C'est un protocole de contrôle associé à IP. Il sert à transporter des informations sur l'état du réseau, des erreurs et des messages de diagnostic. Il n'est ni TCP ni UDP et n'utilise donc pas de numéros de ports TCP/UDP.",
    action = "ICMP aide les équipements et les administrateurs à comprendre ce qui se passe pendant l'acheminement IP. Ping utilise typiquement Echo Request et Echo Reply. D'autres messages peuvent indiquer qu'une destination est inaccessible ou que le TTL d'un paquet est arrivé à zéro.",
    actions = listOf(
        "envoie et reçoit des messages Echo pour certains tests d'accessibilité",
        "signale certaines destinations ou communications impossibles",
        "indique qu'un TTL a expiré, mécanisme exploité par traceroute/tracert pour révéler les sauts",
        "participe à des mécanismes nécessaires au bon fonctionnement IP, notamment certains scénarios de découverte de MTU",
        "fournit des informations de diagnostic sans transporter le contenu applicatif principal"
    ),
    details = "Avec ping en IPv4, la machine envoie un ICMP Echo Request ; la cible peut renvoyer un Echo Reply si elle est joignable et si les politiques de filtrage l'autorisent. Le temps affiché correspond approximativement au round-trip time, l'aller-retour. Le TTL se décrémente à chaque routeur ; lorsqu'il atteint zéro, un équipement peut renvoyer ICMP Time Exceeded. Traceroute exploite précisément cette propriété pour faire apparaître les sauts successifs. ICMPv4 correspond au numéro de protocole IP 1 et ICMPv6 au numéro 58. ICMPv6 a un rôle encore plus central en IPv6, notamment pour Neighbor Discovery. Un pare-feu peut filtrer certains types ICMP ; donc 'ping ne répond pas' ne signifie pas automatiquement 'machine éteinte'. Bloquer indistinctement tout ICMP peut aussi supprimer des messages utiles au fonctionnement et au dépannage.",
    example = "Un site ne répond pas. Tu peux tester l'IP de ta passerelle avec ping pour vérifier le LAN, puis une IP distante pour vérifier le routage Internet, puis un nom de domaine pour distinguer un problème DNS d'un problème réseau. Si le ping est filtré, tu dois compléter le diagnostic avec d'autres outils plutôt que conclure immédiatement que l'hôte est hors ligne.",
    demoTitle = "TERMINAL / POWERSHELL · PING ET CHEMIN RÉSEAU",
    demo = """
Windows :
> ping 1.1.1.1
> tracert 1.1.1.1
> Test-Connection 1.1.1.1 -Count 2

Linux :
> ping -c 2 1.1.1.1
> traceroute 1.1.1.1

Wireshark :
icmp
    """.trimIndent(),
    analogy = "ICMP ressemble aux messages de service d'une société de livraison. Les colis représentent les données normales ; ICMP est plutôt le message 'adresse inaccessible', 'délai dépassé' ou 'je suis bien là'. Il t'aide à comprendre la livraison, mais ce n'est pas lui qui transporte ton site Web ou ton fichier.",
    schema = listOf(
        "Machine A crée un Echo Request",
        "Paquet IP traverse les routeurs ; TTL diminue à chaque saut",
        "Machine B reçoit la demande si le chemin et le filtrage le permettent",
        "Echo Reply revient vers A",
        "A mesure le temps aller-retour",
        "Si un problème survient, un équipement peut produire un autre message ICMP"
    )
)

private fun httpsExpertCourse() = CyberChoiceDeepDive(
    definition = "HTTPS signifie Hypertext Transfer Protocol Secure. Techniquement, il s'agit de HTTP transporté dans une session TLS. Le navigateur et le serveur utilisent donc le protocole Web habituel, mais le canal apporte confidentialité, intégrité et authentification du serveur via le certificat TLS.",
    action = "HTTPS sert à protéger les échanges Web contre la lecture et la modification par un intermédiaire sur le réseau. Il permet aussi au navigateur de vérifier que le serveur présente un certificat valide pour le nom demandé. Le port standard est TCP 443, mais le numéro de port n'est pas ce qui réalise le chiffrement : c'est TLS.",
    actions = listOf("ouvre la connexion vers le service Web", "négocie une version TLS et des paramètres cryptographiques", "reçoit et valide le certificat du serveur", "établit des clés de session", "transporte ensuite les requêtes/réponses HTTP protégées"),
    details = "Lors du handshake TLS, le client et le serveur conviennent des algorithmes et établissent des secrets de session. Le certificat contient notamment l'identité du serveur et une clé publique ; le client vérifie la chaîne de confiance et le nom de domaine. Une fois le handshake terminé, un chiffrement symétrique rapide protège les données de la session. HTTPS protège le transport mais ne garantit pas qu'un site est honnête : un site malveillant peut lui aussi avoir un certificat valide pour son propre domaine.",
    example = "Lorsque tu te connectes à une banque, le mot de passe et les pages transitent dans le tunnel TLS. Une personne simplement présente sur le même Wi-Fi ne doit pas pouvoir lire le contenu en clair uniquement en capturant les paquets.",
    demoTitle = "TERMINAL · OBSERVER HTTPS",
    demo = """
> curl -I https://example.com
> curl -v https://example.com

À observer avec -v : connexion, négociation TLS, certificat et protocole HTTP.
    """.trimIndent(),
    analogy = "HTTP est la lettre ; TLS est l'enveloppe scellée et vérifiée. Le port 443 ressemble au guichet habituel où remettre cette enveloppe.",
    schema = listOf("Navigateur", "Connexion TCP vers 443", "Handshake TLS + certificat", "Clés de session", "Canal chiffré", "Requêtes/réponses HTTP")
)

private fun sshExpertCourse() = CyberChoiceDeepDive(
    definition = "SSH signifie Secure Shell. C'est un protocole permettant d'administrer à distance une machine à travers un canal chiffré. Il est souvent utilisé pour obtenir un shell distant, exécuter des commandes, faire du tunneling ou transférer des fichiers via des mécanismes associés comme SFTP.",
    action = "SSH sert à remplacer des méthodes d'administration distantes non chiffrées. Il authentifie d'abord le serveur afin de réduire le risque de parler au mauvais hôte, puis authentifie l'utilisateur par mot de passe, clé publique ou autre méthode autorisée.",
    actions = listOf("négocie les algorithmes cryptographiques", "vérifie la clé d'hôte du serveur", "authentifie l'utilisateur", "ouvre un canal chiffré", "transporte un shell, une commande ou un sous-service"),
    details = "SSH utilise classiquement TCP 22. Lors de la première connexion, le client ne connaît pas toujours la clé d'hôte et demande confirmation ; ensuite cette clé est mémorisée dans known_hosts. L'authentification par clé repose sur une clé privée conservée par le client et une clé publique autorisée côté serveur. En administration, on limite l'accès, on protège les clés privées et on désactive les méthodes inutiles.",
    example = "Un administrateur se connecte à un serveur Linux dans un datacenter sans être physiquement devant la machine. Le terminal local devient une interface vers un shell exécuté sur le serveur, les échanges restant chiffrés sur le réseau.",
    demoTitle = "TERMINAL · CONNEXION SSH",
    demo = """
> ssh utilisateur@192.168.1.50
> ssh -v utilisateur@192.168.1.50

-v affiche les étapes de négociation et aide au diagnostic.
    """.trimIndent(),
    analogy = "SSH ressemble à un tunnel privé entre ton clavier et le serveur : le tunnel vérifie l'autre extrémité, contrôle qui peut entrer et masque le contenu aux observateurs du trajet.",
    schema = listOf("Client SSH", "Connexion TCP 22", "Vérification de la clé d'hôte", "Négociation chiffrée", "Authentification utilisateur", "Shell / commande distante")
)

private fun ftpExpertCourse() = CyberChoiceDeepDive(
    definition = "FTP signifie File Transfer Protocol. C'est un ancien protocole conçu pour transférer et gérer des fichiers entre un client et un serveur. Le canal de contrôle utilise traditionnellement TCP 21.",
    action = "FTP sert à lister, envoyer, télécharger, renommer ou supprimer des fichiers selon les droits du compte. FTP classique ne chiffre pas naturellement les identifiants ni les données.",
    actions = listOf("ouvre un canal de contrôle", "authentifie le client", "échange des commandes FTP", "ouvre un canal de données séparé pour certains transferts", "transfère les fichiers"),
    details = "FTP sépare le canal de contrôle et le canal de données. Les modes actif et passif déterminent comment le canal de données est établi, ce qui a des conséquences avec NAT et pare-feu. Pour protéger les échanges, on rencontre FTPS, qui ajoute TLS à FTP, ou SFTP, qui est un protocole différent fonctionnant au-dessus de SSH.",
    example = "Un ancien serveur de fichiers accepte des connexions FTP sur TCP 21. Un client s'authentifie puis télécharge un fichier. Sur un réseau non fiable, FTP classique est à éviter car le trafic peut être observé en clair.",
    demoTitle = "TERMINAL · IDENTIFIER LE SERVICE",
    demo = """
> ftp 192.168.1.50

Pour un transfert moderne sécurisé, vérifier si le service attendu est SFTP ou FTPS plutôt que supposer qu'il s'agit de FTP classique.
    """.trimIndent(),
    analogy = "FTP est un ancien quai de chargement de fichiers. Le port 21 est le guichet des instructions ; un autre passage sert au chargement lui-même.",
    schema = listOf("Client", "TCP 21 : canal de contrôle", "Authentification + commandes", "Canal de données", "Fichiers transférés")
)

private fun http8080ExpertCourse() = CyberChoiceDeepDive(
    definition = "8080 est un numéro de port TCP souvent choisi comme port HTTP alternatif. Ce n'est pas un protocole et il n'impose pas à lui seul le contenu qui l'utilise.",
    action = "Le port permet au système d'exploitation d'orienter une connexion vers le processus qui écoute à cet endroit. 8080 est fréquent pour des serveurs de développement, des proxys ou des interfaces d'administration, mais un service peut utiliser un autre protocole si l'administrateur l'a configuré ainsi.",
    actions = listOf("identifie un point d'écoute TCP", "permet à plusieurs services d'une même machine de partager la même adresse IP", "est souvent utilisé pour HTTP alternatif", "doit être interprété avec le service réellement détecté"),
    details = "Une adresse IP identifie une machine ou interface, tandis qu'un port identifie un point de communication au sein de cette machine. Les ports dits bien connus ou conventions facilitent l'usage, mais une application n'est pas obligée de respecter 8080. En diagnostic, il faut donc vérifier le protocole réellement servi et ne pas conclure uniquement à partir du numéro.",
    example = "Une application de test écoute sur http://localhost:8080 alors que le serveur Web principal utilise 80 ou 443. Le navigateur ajoute :8080 pour indiquer explicitement le port cible.",
    demoTitle = "TERMINAL · TESTER UN SERVICE WEB LOCAL",
    demo = """
> curl -I http://127.0.0.1:8080

Si aucun service n'écoute sur 8080, la connexion échoue ; le numéro seul ne crée aucun service.
    """.trimIndent(),
    analogy = "L'adresse IP est l'adresse d'un immeuble ; le port est le numéro du guichet à l'intérieur. 8080 est simplement un guichet souvent choisi pour du Web alternatif.",
    schema = listOf("Adresse IP de la machine", "Port TCP 8080", "Processus en écoute", "Protocole réellement servi", "Réponse au client")
)
