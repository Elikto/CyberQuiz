package com.example.cyberquiz.data.repository

import com.example.cyberquiz.data.database.ProgressEntity
import com.example.cyberquiz.data.database.QuestionEntity
import com.example.cyberquiz.data.database.QuizDao
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.Difficulty
import com.example.cyberquiz.model.NutritionCategory
import kotlinx.coroutines.flow.Flow

class QuizRepository(private val dao: QuizDao) {
    fun progress(quizType: String): Flow<ProgressEntity?> = dao.progress(quizType)

    suspend fun progressSnapshot(quizType: String): ProgressEntity? = dao.progressSnapshot(quizType)

    suspend fun init() {
        if (dao.questionCount(CYBERSECURITY) == 0) {
            dao.insertAll(cybersecurityQuestions())
        }
        if (dao.questionCountBySource(CYBERSECURITY, CYBER_EXPANSION_SOURCE) == 0) {
            dao.insertAll(cyberExpansionQuestions())
        }
        if (dao.questionCount(NUTRITION) == 0) {
            dao.insertAll(nutritionQuestions())
        }

        // Met aussi à jour les formulations déjà enregistrées sur les téléphones
        // qui avaient installé une version précédente de la banque Cyber.
        applyCyberWordingFixes(dao)

        ensureProgress(CYBERSECURITY, 1)
        ensureProgress(NUTRITION, 2)
    }

    private suspend fun ensureProgress(quizType: String, id: Int) {
        if (dao.progressSnapshot(quizType) == null) {
            dao.insertProgress(ProgressEntity(id = id, quizType = quizType))
        }
    }

    suspend fun next(quizType: String, category: String? = null): QuestionEntity? =
        if (category == null) {
            dao.nextUnseen(quizType)
        } else {
            dao.nextUnseenInCategory(quizType, category)
        }

    suspend fun markSeen(id: Long) = dao.markSeen(id)

    suspend fun resetSeen(quizType: String, category: String? = null) {
        if (category == null) {
            dao.resetSeen(quizType)
        } else {
            dao.resetSeenInCategory(quizType, category)
        }
    }

    private fun q(
        quizType: String,
        category: String,
        difficulty: Difficulty,
        text: String,
        a: String,
        b: String,
        c: String,
        d: String,
        correct: Int,
        explanation: String
    ) = QuestionEntity(
        quizType = quizType,
        category = category,
        difficulty = difficulty.name,
        question = text,
        answerA = a,
        answerB = b,
        answerC = c,
        answerD = d,
        correctIndex = correct,
        explanation = explanation
    )

    private fun cyber(
        category: Category,
        difficulty: Difficulty,
        text: String,
        a: String,
        b: String,
        c: String,
        d: String,
        correct: Int,
        explanation: String
    ) = q(CYBERSECURITY, category.label, difficulty, text, a, b, c, d, correct, explanation)

    private fun nutrition(
        category: NutritionCategory,
        difficulty: Difficulty,
        text: String,
        a: String,
        b: String,
        c: String,
        d: String,
        correct: Int,
        explanation: String
    ) = q(NUTRITION, category.label, difficulty, text, a, b, c, d, correct, explanation)

    private fun cybersecurityQuestions() = listOf(
        cyber(Category.RESEAUX, Difficulty.EASY, "Quel protocole traduit un nom de domaine en adresse IP ?", "DNS", "DHCP", "ARP", "ICMP", 0, "DNS associe les noms de domaine aux adresses IP."),
        cyber(Category.RESEAUX, Difficulty.EASY, "Quel port est classiquement utilisé par HTTPS ?", "21", "53", "443", "8080", 2, "HTTPS utilise normalement le port TCP 443."),
        cyber(Category.LINUX, Difficulty.EASY, "Quelle commande affiche le répertoire courant sous Linux ?", "cd", "pwd", "ls", "whoami", 1, "pwd affiche le chemin du répertoire de travail courant."),
        cyber(Category.LINUX, Difficulty.MEDIUM, "Quel fichier contient généralement les comptes locaux Linux ?", "/etc/passwd", "/etc/shadow", "/var/log/auth.log", "/etc/hosts", 0, "/etc/passwd contient les informations publiques des comptes ; les mots de passe sont dans /etc/shadow."),
        cyber(Category.WEB, Difficulty.EASY, "Quelle attaque injecte du code SQL dans une requête ?", "XSS", "CSRF", "SQL injection", "SSRF", 2, "L'injection SQL manipule une requête SQL via des entrées contrôlées par l'utilisateur."),
        cyber(Category.WEB, Difficulty.MEDIUM, "Que protège principalement l'attribut HttpOnly d'un cookie ?", "Les requêtes DNS", "L'accès JavaScript au cookie", "Le chiffrement TLS", "Le cache navigateur", 1, "HttpOnly empêche l'accès au cookie via JavaScript côté client."),
        cyber(Category.CRYPTO, Difficulty.EASY, "Lequel est un algorithme de chiffrement symétrique ?", "RSA", "AES", "ECDSA", "SHA-256", 1, "AES est un chiffrement symétrique ; RSA et ECDSA sont asymétriques, SHA-256 est un hash."),
        cyber(Category.CRYPTO, Difficulty.MEDIUM, "À quoi sert principalement une fonction de hachage cryptographique ?", "Réversibilité", "Compression", "Intégrité et empreinte", "Attribution d'une IP", 2, "Une fonction de hachage produit une empreinte difficile à inverser et utile pour vérifier l'intégrité d'une donnée."),
        cyber(Category.MALWARE, Difficulty.EASY, "Quel type de malware chiffre les fichiers et demande une rançon ?", "Spyware", "Ransomware", "Rootkit", "Adware", 1, "Un ransomware bloque ou chiffre des données pour extorquer une rançon."),
        cyber(Category.SOCIAL, Difficulty.EASY, "Quel est le but classique du phishing ?", "Optimiser un routeur", "Tromper une victime pour obtenir une information", "Scanner un disque", "Compresser des fichiers", 1, "Le phishing repose sur l'ingénierie sociale pour obtenir des informations ou pousser à une action."),
        cyber(Category.FORENSICS, Difficulty.MEDIUM, "Pourquoi faut-il préserver l'image disque originale lors d'une investigation numérique ?", "Pour accélérer Internet", "Pour conserver l'intégrité de la preuve", "Pour supprimer les métadonnées", "Pour chiffrer le disque", 1, "On conserve l'original intact et on travaille sur une copie. Cela permet de montrer que la preuve d'origine n'a pas été modifiée pendant l'analyse."),
        cyber(Category.PENTEST, Difficulty.EASY, "Quel outil est couramment utilisé pour découvrir des services réseau ?", "Nmap", "Git", "Docker", "Gradle", 0, "Nmap sert notamment à découvrir des hôtes, ports et services réseau."),
        cyber(Category.AD, Difficulty.MEDIUM, "Quel service est central dans un domaine Active Directory ?", "DNS uniquement", "Kerberos", "FTP", "SMTP", 1, "Kerberos est un protocole d'authentification majeur utilisé par Active Directory."),
        cyber(Category.CLOUD, Difficulty.EASY, "Que signifie MFA ?", "Managed File Access", "Multi-Factor Authentication", "Main Firewall Access", "Memory Forensics Analysis", 1, "MFA combine plusieurs facteurs d'authentification indépendants."),
        cyber(Category.SYSTEM, Difficulty.MEDIUM, "Quel principe consiste à donner uniquement les droits nécessaires ?", "Zero trust", "Least privilege", "Fail open", "Defense by obscurity", 1, "Le principe du moindre privilège limite les permissions au strict nécessaire.")
    )

    private fun nutritionQuestions() = listOf(
        nutrition(NutritionCategory.MACRONUTRIENTS, Difficulty.EASY, "Combien d'énergie fournissent environ 1 g de glucides ?", "2 kcal", "4 kcal", "7 kcal", "9 kcal", 1, "Les glucides fournissent environ 4 kcal par gramme."),
        nutrition(NutritionCategory.MACRONUTRIENTS, Difficulty.EASY, "Combien d'énergie fournissent environ 1 g de protéines ?", "4 kcal", "6 kcal", "7 kcal", "9 kcal", 0, "Les protéines fournissent environ 4 kcal par gramme."),
        nutrition(NutritionCategory.MACRONUTRIENTS, Difficulty.EASY, "Quel macronutriment est le plus énergétique par gramme ?", "Les protéines", "Les glucides", "Les lipides", "L'eau", 2, "Les lipides fournissent environ 9 kcal par gramme, contre environ 4 kcal pour les protéines et les glucides."),
        nutrition(NutritionCategory.MICRONUTRIENTS, Difficulty.EASY, "La vitamine C appartient à quelle grande famille ?", "Vitamines hydrosolubles", "Vitamines liposolubles", "Minéraux", "Macronutriments", 0, "La vitamine C est une vitamine hydrosoluble."),
        nutrition(NutritionCategory.MICRONUTRIENTS, Difficulty.MEDIUM, "Quelle vitamine contribue notamment à l'absorption normale du calcium ?", "Vitamine A", "Vitamine C", "Vitamine D", "Vitamine K uniquement", 2, "La vitamine D participe à l'absorption et à l'utilisation normales du calcium et du phosphore."),
        nutrition(NutritionCategory.MICRONUTRIENTS, Difficulty.EASY, "Quel minéral est un composant important de l'hémoglobine ?", "Fer", "Sodium", "Potassium", "Iode", 0, "Le fer entre dans la composition de l'hémoglobine, qui transporte l'oxygène dans le sang."),
        nutrition(NutritionCategory.HYDRATION, Difficulty.EASY, "Quel composé constitue la base de l'hydratation du corps ?", "L'eau", "Les protéines", "Les fibres", "Le cholestérol", 0, "L'eau est essentielle au fonctionnement normal de l'organisme et constitue la base de l'hydratation."),
        nutrition(NutritionCategory.HYDRATION, Difficulty.EASY, "L'eau pure fournit combien de kilocalories ?", "0 kcal", "2 kcal", "4 kcal", "9 kcal", 0, "L'eau pure n'apporte pas d'énergie et fournit 0 kcal."),
        nutrition(NutritionCategory.HYDRATION, Difficulty.MEDIUM, "Les besoins en eau sont-ils identiques pour tout le monde ?", "Oui, toujours", "Non, ils varient selon plusieurs facteurs", "Uniquement selon l'âge", "Uniquement selon la taille", 1, "Les besoins hydriques varient notamment selon l'activité, la température, l'alimentation et les caractéristiques individuelles."),
        nutrition(NutritionCategory.BALANCE, Difficulty.EASY, "Quel groupe d'aliments est généralement une bonne source de fibres ?", "Fruits, légumes et légumineuses", "Beurre et huiles", "Sucre et confiseries", "Sel", 0, "Les fruits, légumes, légumineuses et céréales complètes sont des sources courantes de fibres alimentaires."),
        nutrition(NutritionCategory.BALANCE, Difficulty.EASY, "Les sucres ajoutés sont-ils indispensables au fonctionnement de l'organisme ?", "Oui", "Non", "Seulement le soir", "Seulement après le sport", 1, "Les sucres ajoutés ne sont pas indispensables ; les glucides peuvent provenir de nombreux aliments sans ajout de sucre."),
        nutrition(NutritionCategory.BALANCE, Difficulty.MEDIUM, "Quel choix apporte généralement davantage de fibres ?", "Pain complet", "Pain blanc très raffiné", "Sucre blanc", "Huile végétale", 0, "Les produits céréaliers complets conservent davantage de fibres que leurs équivalents très raffinés."),
        nutrition(NutritionCategory.DIGESTION, Difficulty.EASY, "Dans quelle partie du tube digestif a lieu l'essentiel de l'absorption des nutriments ?", "L'œsophage", "L'estomac", "L'intestin grêle", "Le côlon uniquement", 2, "L'intestin grêle est le principal site d'absorption de la majorité des nutriments."),
        nutrition(NutritionCategory.DIGESTION, Difficulty.EASY, "Quel est le rôle général des enzymes digestives ?", "Décomposer les nutriments en molécules plus simples", "Produire de l'oxygène", "Créer des vitamines artificielles", "Bloquer toute absorption", 0, "Les enzymes digestives facilitent la décomposition des aliments en molécules pouvant être absorbées."),
        nutrition(NutritionCategory.DIGESTION, Difficulty.MEDIUM, "Quel organe produit la bile ?", "Le pancréas", "Le foie", "L'estomac", "L'intestin grêle", 1, "La bile est produite par le foie et participe notamment à la digestion des lipides.")
    )

    companion object {
        private const val CYBERSECURITY = "CYBERSECURITY"
        private const val NUTRITION = "NUTRITION"
    }
}
