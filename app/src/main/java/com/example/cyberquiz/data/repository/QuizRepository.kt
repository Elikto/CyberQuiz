package com.example.cyberquiz.data.repository

import com.example.cyberquiz.data.database.*
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.Difficulty
import kotlinx.coroutines.flow.Flow

class QuizRepository(private val dao: QuizDao) {
    fun progress(): Flow<ProgressEntity?> = dao.progress()
    suspend fun init() {
        if (dao.questionCount() == 0) {
            dao.insertAll(seedQuestions())
            dao.insertProgress(ProgressEntity())
        }
    }
    suspend fun next(category: Category? = null): QuestionEntity? =
        if (category == null) dao.nextUnseen() else dao.nextUnseenInCategory(category.label) ?: dao.nextUnseen()
    suspend fun markSeen(id: Long) = dao.markSeen(id)
    suspend fun resetSeen() = dao.resetSeen()

    private fun q(c: Category, d: Difficulty, text:String, a:String,b:String,cc:String,d4:String,correct:Int,ex:String)=QuestionEntity(category=c.label,difficulty=d.name,question=text,answerA=a,answerB=b,answerC=cc,answerD=d4,correctIndex=correct,explanation=ex)

    private fun seedQuestions() = listOf(
        q(Category.RESEAUX, Difficulty.EASY, "Quel protocole traduit un nom de domaine en adresse IP ?", "DNS", "DHCP", "ARP", "ICMP", 0, "DNS associe les noms de domaine aux adresses IP."),
        q(Category.RESEAUX, Difficulty.EASY, "Quel port est classiquement utilisé par HTTPS ?", "21", "53", "443", "8080", 2, "HTTPS utilise normalement le port TCP 443."),
        q(Category.LINUX, Difficulty.EASY, "Quelle commande affiche le répertoire courant sous Linux ?", "cd", "pwd", "ls", "whoami", 1, "pwd affiche le chemin du répertoire de travail courant."),
        q(Category.LINUX, Difficulty.MEDIUM, "Quel fichier contient généralement les comptes locaux Linux ?", "/etc/passwd", "/etc/shadow", "/var/log/auth.log", "/etc/hosts", 0, "/etc/passwd contient les informations publiques des comptes; les mots de passe sont dans /etc/shadow."),
        q(Category.WEB, Difficulty.EASY, "Quelle attaque injecte du code SQL dans une requête ?", "XSS", "CSRF", "SQL injection", "SSRF", 2, "L'injection SQL manipule une requête SQL via des entrées contrôlées par l'utilisateur."),
        q(Category.WEB, Difficulty.MEDIUM, "Que protège principalement l'attribut HttpOnly d'un cookie ?", "Les requêtes DNS", "L'accès JavaScript au cookie", "Le chiffrement TLS", "Le cache navigateur", 1, "HttpOnly empêche l'accès au cookie via JavaScript côté client."),
        q(Category.CRYPTO, Difficulty.EASY, "Lequel est un algorithme de chiffrement symétrique ?", "RSA", "AES", "ECDSA", "SHA-256", 1, "AES est un chiffrement symétrique; RSA et ECDSA sont asymétriques, SHA-256 est un hash."),
        q(Category.CRYPTO, Difficulty.MEDIUM, "À quoi sert principalement une fonction de hachage cryptographique ?", "Réversibilité", "Compression", "Intégrité et empreinte", "Attribution d'une IP", 2, "Un hash produit une empreinte déterministe difficile à inverser et utile pour vérifier l'intégrité."),
        q(Category.MALWARE, Difficulty.EASY, "Quel type de malware chiffre les fichiers et demande une rançon ?", "Spyware", "Ransomware", "Rootkit", "Adware", 1, "Un ransomware bloque ou chiffre des données pour extorquer une rançon."),
        q(Category.SOCIAL, Difficulty.EASY, "Quel est le but classique du phishing ?", "Optimiser un routeur", "Tromper une victime pour obtenir une information", "Scanner un disque", "Compresser des fichiers", 1, "Le phishing repose sur l'ingénierie sociale pour obtenir des informations ou pousser à une action."),
        q(Category.FORENSICS, Difficulty.MEDIUM, "Pourquoi préserver une image disque originale en forensic ?", "Pour accélérer Internet", "Pour conserver l'intégrité de la preuve", "Pour supprimer les métadonnées", "Pour chiffrer le disque", 1, "La conservation d'un original et l'utilisation de copies de travail permettent de préserver la preuve."),
        q(Category.PENTEST, Difficulty.EASY, "Quel outil est couramment utilisé pour découvrir des services réseau ?", "Nmap", "Git", "Docker", "Gradle", 0, "Nmap sert notamment à découvrir des hôtes, ports et services réseau."),
        q(Category.AD, Difficulty.MEDIUM, "Quel service est central dans un domaine Active Directory ?", "DNS uniquement", "Kerberos", "FTP", "SMTP", 1, "Kerberos est un protocole d'authentification majeur utilisé par Active Directory."),
        q(Category.CLOUD, Difficulty.EASY, "Que signifie MFA ?", "Managed File Access", "Multi-Factor Authentication", "Main Firewall Access", "Memory Forensics Analysis", 1, "MFA combine plusieurs facteurs d'authentification indépendants."),
        q(Category.SYSTEM, Difficulty.MEDIUM, "Quel principe consiste à donner uniquement les droits nécessaires ?", "Zero trust", "Least privilege", "Fail open", "Defense by obscurity", 1, "Le principe du moindre privilège limite les permissions au strict nécessaire.")
    )
}
