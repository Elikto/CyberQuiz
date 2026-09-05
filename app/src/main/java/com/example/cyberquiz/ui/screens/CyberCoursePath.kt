package com.example.cyberquiz.ui.screens

internal data class CyberCourseStep(
    val term: String,
    val category: String
)

private val cyberCoursePath = listOf(
    // 1. Réseaux : comprendre comment les machines communiquent.
    CyberCourseStep("DNS", "Réseaux"),
    CyberCourseStep("DHCP", "Réseaux"),
    CyberCourseStep("ARP", "Réseaux"),
    CyberCourseStep("ICMP", "Réseaux"),
    CyberCourseStep("HTTPS", "Réseaux"),
    CyberCourseStep("SSH", "Réseaux"),
    CyberCourseStep("FTP", "Réseaux"),

    // 2. Linux : se repérer puis administrer proprement.
    CyberCourseStep("PWD", "Linux"),
    CyberCourseStep("LS", "Linux"),
    CyberCourseStep("CD", "Linux"),
    CyberCourseStep("WHOAMI", "Linux"),
    CyberCourseStep("SUDO", "Linux"),
    CyberCourseStep("GREP", "Linux"),
    CyberCourseStep("NANO", "Linux"),

    // 3. Windows : automatisation puis protection des données.
    CyberCourseStep("Task Scheduler", "Windows"),
    CyberCourseStep("BitLocker", "Windows"),

    // 4. Cryptographie : empreinte, chiffrement symétrique puis asymétrique.
    CyberCourseStep("Hashing", "Cryptographie"),
    CyberCourseStep("SHA-256", "Cryptographie"),
    CyberCourseStep("AES", "Cryptographie"),
    CyberCourseStep("RSA", "Cryptographie"),
    CyberCourseStep("ECDSA", "Cryptographie"),

    // 5. Web : les grandes familles de failles côté navigateur et serveur.
    CyberCourseStep("XSS", "Sécurité Web"),
    CyberCourseStep("CSRF", "Sécurité Web"),
    CyberCourseStep("SQL Injection", "Sécurité Web"),
    CyberCourseStep("SSRF", "Sécurité Web"),

    // 6. Malware : du plus visible au plus furtif.
    CyberCourseStep("Adware", "Malware"),
    CyberCourseStep("Spyware", "Malware"),
    CyberCourseStep("Cheval de Troie", "Malware"),
    CyberCourseStep("Ransomware", "Malware"),
    CyberCourseStep("Rootkit", "Malware"),

    // 7. Ingénierie sociale.
    CyberCourseStep("Phishing", "Ingénierie sociale"),
    CyberCourseStep("Vishing", "Ingénierie sociale"),

    // 8. Recherche et investigation.
    CyberCourseStep("OSINT", "OSINT"),
    CyberCourseStep("EXIF", "Forensics"),
    CyberCourseStep("SHA-256", "Forensics"),

    // 9. Tests de sécurité autorisés.
    CyberCourseStep("Nmap", "Pentest"),
    CyberCourseStep("Burp Suite", "Pentest"),

    // 10. Identité et environnement d'entreprise.
    CyberCourseStep("Kerberos", "Active Directory"),
    CyberCourseStep("LDAP", "Active Directory"),
    CyberCourseStep("GPO", "Active Directory"),

    // 11. Cloud et authentification.
    CyberCourseStep("MFA", "Cloud Security"),
    CyberCourseStep("IAM", "Cloud Security"),
    CyberCourseStep("Security Group", "Cloud Security"),

    // 12. Mobile puis principes système transverses.
    CyberCourseStep("MDM", "Mobile Security"),
    CyberCourseStep("Sandbox", "Mobile Security"),
    CyberCourseStep("Pare-feu", "Sécurité système"),
    CyberCourseStep("Moindre privilège", "Sécurité système"),
    CyberCourseStep("Zero Trust", "Sécurité système"),
    CyberCourseStep("SIEM", "Sécurité système"),
    CyberCourseStep("EDR", "Sécurité système")
)

internal fun nextCyberCourseStep(term: String, category: String): CyberCourseStep? {
    val normalizedTerm = term.trim().uppercase()
    val normalizedCategory = category.trim().uppercase()
    val index = cyberCoursePath.indexOfFirst { step ->
        step.term.uppercase() == normalizedTerm && step.category.uppercase() == normalizedCategory
    }

    if (index >= 0) return cyberCoursePath.getOrNull(index + 1)

    // Pour un cours ouvert depuis une notion qui n'est pas encore dans le parcours,
    // on reprend au début logique de son thème plutôt que de choisir au hasard.
    return cyberCoursePath.firstOrNull { it.category.uppercase() == normalizedCategory }
}
