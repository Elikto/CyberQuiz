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

internal fun buildCyberCourse(term: String, category: String): CyberCourse {
    val clean = term.trim()
    val key = clean.uppercase()
    val deepDive = buildCyberChoiceDeepDive(clean, category)

    val outputTitle: String
    val output: String

    when (key) {
        "DNS" -> {
            outputTitle = "RÉSULTAT ATTENDU · nslookup"
            output = """
> nslookup example.com
Serveur :  192.168.1.1
Address:   192.168.1.1

Réponse ne faisant pas autorité :
Nom :      example.com
Address:   93.184.216.34
            """.trimIndent()
        }
        "DHCP" -> {
            outputTitle = "RÉSULTAT ATTENDU · ipconfig /all"
            output = """
DHCP activé . . . . . . . . . : Oui
Adresse IPv4 . . . . . . . . : 192.168.1.42
Masque de sous-réseau . . . . : 255.255.255.0
Passerelle par défaut . . . . : 192.168.1.1
Serveur DHCP . . . . . . . . : 192.168.1.1
Serveurs DNS . . . . . . . . : 1.1.1.1
Bail obtenu . . . . . . . . .: 10:14
            """.trimIndent()
        }
        "ARP" -> {
            outputTitle = "RÉSULTAT ATTENDU · arp -a"
            output = """
> arp -a
Interface : 192.168.1.42
  Adresse Internet   Adresse physique      Type
  192.168.1.1        34-ab-37-12-56-90     dynamique
  192.168.1.20       b8-27-eb-44-11-2a     dynamique
            """.trimIndent()
        }
        "ICMP" -> {
            outputTitle = "RÉSULTAT ATTENDU · ping"
            output = """
> ping 1.1.1.1
Réponse de 1.1.1.1 : octets=32 temps=14 ms TTL=57
Réponse de 1.1.1.1 : octets=32 temps=13 ms TTL=57

Paquets : envoyés = 2, reçus = 2, perdus = 0
            """.trimIndent()
        }
        "HTTPS", "443" -> {
            outputTitle = "RÉSULTAT ATTENDU · curl"
            output = """
$ curl -I https://example.com
HTTP/2 200
content-type: text/html
strict-transport-security: max-age=31536000

La connexion HTTP est transportée dans un canal TLS chiffré.
            """.trimIndent()
        }
        "SSH" -> {
            outputTitle = "RÉSULTAT ATTENDU · ssh"
            output = """
$ ssh eleve@192.168.1.50
The authenticity of host '192.168.1.50' can't be established.
Are you sure you want to continue connecting? yes

Password:
eleve@serveur:~$
            """.trimIndent()
        }
        "SUDO" -> {
            outputTitle = "RÉSULTAT ATTENDU · sudo"
            output = """
$ sudo whoami
[sudo] Mot de passe de eleve :
root

La commande whoami s'exécute ici avec les privilèges accordés par sudo.
            """.trimIndent()
        }
        "GREP" -> {
            outputTitle = "RÉSULTAT ATTENDU · grep"
            output = """
$ grep "Failed password" auth.log
Sep 04 20:12 sshd[1842]: Failed password for user test
Sep 04 20:14 sshd[1920]: Failed password for user admin
            """.trimIndent()
        }
        "GET-PROCESS" -> {
            outputTitle = "RÉSULTAT ATTENDU · PowerShell"
            output = """
PS> Get-Process | Select-Object -First 3 Name, Id

Name       Id
----       --
explorer   4320
powershell 8116
svchost    1264
            """.trimIndent()
        }
        "4625" -> {
            outputTitle = "RÉSULTAT ATTENDU · PowerShell"
            output = """
PS> Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4625} -MaxEvents 1

Id          : 4625
LevelDisplayName : Information
Message     : An account failed to log on...
            """.trimIndent()
        }
        "BITLOCKER" -> {
            outputTitle = "RÉSULTAT ATTENDU · manage-bde"
            output = """
PS> manage-bde -status C:

Volume C: [OS]
Conversion Status: Fully Encrypted
Percentage Encrypted: 100.0%
Protection Status: Protection On
            """.trimIndent()
        }
        "SHA-256" -> {
            outputTitle = "RÉSULTAT ATTENDU · PowerShell"
            output = """
PS> Get-FileHash .\\preuve.img -Algorithm SHA256

Algorithm Hash                                                             Path
--------- ----                                                             ----
SHA256    76A8B1C2D3E4F5...                                                preuve.img
            """.trimIndent()
        }
        "NMAP" -> {
            outputTitle = "RÉSULTAT ATTENDU · LAB AUTORISÉ"
            output = """
$ nmap -sV 192.168.1.10

PORT    STATE SERVICE VERSION
22/tcp  open  ssh     OpenSSH
443/tcp open  https   nginx

À utiliser uniquement sur une machine ou un réseau que tu es autorisé à tester.
            """.trimIndent()
        }
        "KERBEROS" -> {
            outputTitle = "RÉSULTAT ATTENDU · klist"
            output = """
PS> klist

Cached Tickets: (2)
#0> Client: eleve @ LAB.LOCAL
    Server: krbtgt/LAB.LOCAL @ LAB.LOCAL
#1> Server: cifs/fileserver.lab.local @ LAB.LOCAL
            """.trimIndent()
        }
        else -> {
            outputTitle = "EXEMPLE DE RÉSULTAT"
            output = """
Observe surtout trois choses :
1. quelle donnée entre dans le mécanisme ;
2. quelle action est réalisée ;
3. quel résultat concret est produit.

Pour « $clean », compare toujours le résultat obtenu avec sa définition et son rôle dans $category.
            """.trimIndent()
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
                title = "Comprendre le fonctionnement du DHCP en moins de 2 minutes !",
                channel = "Formip - Certification IT",
                url = "https://www.youtube.com/watch?v=qYBmMgKUNa4"
            )
        )
        "NMAP" -> listOf(
            CyberCourseVideo(
                title = "Maîtriser NMAP [Débutant/Intermédiaire]",
                channel = "Hafnium - Sécurité informatique",
                url = "https://www.youtube.com/watch?v=m84RjRcjUSM"
            )
        )
        "BITLOCKER" -> listOf(
            CyberCourseVideo(
                title = "BitLocker en entreprise : configuration et déploiement",
                channel = "IT-Connect - Florian",
                url = "https://www.youtube.com/watch?v=Zs18TNlcLfg"
            )
        )
        else -> emptyList()
    }

    return CyberCourse(
        title = "Cours · $clean",
        subtitle = "Reprendre la notion depuis zéro et la comprendre concrètement",
        objectives = listOf(
            "Savoir expliquer $clean avec tes propres mots",
            "Comprendre ce que $clean fait réellement, étape par étape",
            "Reconnaître un résultat réel dans un terminal, PowerShell ou une situation concrète",
            "Savoir distinguer $clean des notions proches"
        ),
        deepDive = deepDive,
        expectedOutputTitle = outputTitle,
        expectedOutput = output,
        checkpoints = listOf(
            "Quel problème cette notion résout-elle ?",
            "Quelles données ou ressources reçoit-elle en entrée ?",
            "Quelles actions réalise-t-elle ?",
            "Quel résultat observable produit-elle ?",
            "Avec quelle autre notion pourrais-tu la confondre ?"
        ),
        videos = videos,
        videoSearchQuery = "$clean cours français cybersécurité"
    )
}
