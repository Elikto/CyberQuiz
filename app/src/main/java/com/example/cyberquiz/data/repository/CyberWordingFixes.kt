package com.example.cyberquiz.data.repository

import com.example.cyberquiz.data.database.QuizDao

private const val CYBER = "CYBERSECURITY"

/**
 * Corrige aussi les questions déjà enregistrées sur le téléphone.
 * Modifier uniquement les listes de questions ne suffit pas, car Room conserve
 * les lignes déjà insérées lors des versions précédentes de l'application.
 */
internal suspend fun applyCyberWordingFixes(dao: QuizDao) {
    rewrite(
        dao = dao,
        oldQuestion = "Pourquoi ajoute-t-on un sel aléatoire avant de hacher un mot de passe ?",
        newQuestion = "Lorsqu'un site protège un mot de passe, il calcule une empreinte cryptographique. Pourquoi ajoute-t-on d'abord une petite valeur aléatoire appelée « sel cryptographique » ?",
        a = "Pour accélérer le réseau",
        b = "Pour que deux mots de passe identiques n'aient pas forcément la même empreinte",
        c = "Pour rendre le mot de passe lisible",
        d = "Pour remplacer la protection HTTPS",
        correctAnswer = "Pour que deux mots de passe identiques n'aient pas forcément la même empreinte",
        explanation = "Le sel cryptographique est simplement une petite valeur aléatoire, différente pour chaque mot de passe. Elle est ajoutée avant le calcul de l'empreinte. Ainsi, deux personnes utilisant le même mot de passe obtiennent des empreintes différentes, ce qui rend certaines attaques basées sur des empreintes déjà calculées beaucoup moins efficaces."
    )

    rewrite(
        dao = dao,
        oldQuestion = "Dans un chiffrement asymétrique, quelle clé peut être partagée publiquement ?",
        newQuestion = "Dans un chiffrement asymétrique, quelle clé peut être partagée publiquement ?",
        a = "La clé publique",
        b = "La clé privée",
        c = "Le mot de passe maître",
        d = "Un code PIN",
        correctAnswer = "La clé publique",
        explanation = "La clé publique est faite pour pouvoir être partagée. La clé privée, elle, doit rester secrète."
    )

    rewrite(
        dao = dao,
        oldQuestion = "Quel cmdlet PowerShell affiche la liste des processus en cours ?",
        newQuestion = "Quelle commande PowerShell affiche la liste des processus en cours ?",
        a = "Get-Process",
        b = "Get-DnsClient",
        c = "New-Item",
        d = "Write-Host",
        correctAnswer = "Get-Process",
        explanation = "Get-Process affiche les processus actuellement visibles sur la machine. Un processus est simplement un programme en cours d'exécution."
    )

    rewrite(
        dao = dao,
        oldQuestion = "Pourquoi préserver une image disque originale en forensic ?",
        newQuestion = "Pourquoi faut-il préserver l'image disque originale lors d'une investigation numérique ?",
        a = "Pour accélérer Internet",
        b = "Pour conserver l'intégrité de la preuve",
        c = "Pour supprimer les métadonnées",
        d = "Pour chiffrer le disque",
        correctAnswer = "Pour conserver l'intégrité de la preuve",
        explanation = "On conserve l'original intact et on travaille sur une copie. Cela permet de montrer que la preuve d'origine n'a pas été modifiée pendant l'analyse."
    )

    rewrite(
        dao = dao,
        oldQuestion = "À quoi sert un bloqueur d'écriture lors de l'acquisition d'un support ?",
        newQuestion = "À quoi sert un bloqueur d'écriture lors de la copie d'un disque à analyser ?",
        a = "À empêcher la modification du disque d'origine",
        b = "À accélérer la connexion Internet",
        c = "À supprimer les fichiers cachés",
        d = "À contourner un mot de passe",
        correctAnswer = "À empêcher la modification du disque d'origine",
        explanation = "Un bloqueur d'écriture empêche l'ordinateur d'écrire sur le disque d'origine. L'enquêteur peut ainsi le copier sans risquer de modifier la preuve."
    )

    rewrite(
        dao = dao,
        oldQuestion = "Qu'est-ce que le pretexting en ingénierie sociale ?",
        newQuestion = "Qu'appelle-t-on le prétextage (« pretexting ») en ingénierie sociale ?",
        a = "Inventer un scénario crédible pour obtenir une information ou pousser quelqu'un à agir",
        b = "Mettre à jour un système d'exploitation",
        c = "Chiffrer une base de données",
        d = "Défragmenter un disque",
        correctAnswer = "Inventer un scénario crédible pour obtenir une information ou pousser quelqu'un à agir",
        explanation = "Le prétextage consiste à inventer une histoire, un rôle ou une situation crédible pour gagner la confiance d'une personne et l'amener à donner une information ou à effectuer une action."
    )

    rewrite(
        dao = dao,
        oldQuestion = "Qu'est-ce qu'une attaque de MFA fatigue ?",
        newQuestion = "Qu'appelle-t-on une attaque par fatigue MFA ?",
        a = "Envoyer de nombreuses demandes de validation MFA pour pousser la victime à en accepter une",
        b = "Supprimer automatiquement le second facteur",
        c = "Changer la fréquence Wi-Fi",
        d = "Renouveler un certificat expiré",
        correctAnswer = "Envoyer de nombreuses demandes de validation MFA pour pousser la victime à en accepter une",
        explanation = "Une attaque par fatigue MFA envoie de nombreuses demandes de validation. L'objectif est que la victime finisse par accepter l'une d'elles par erreur, par lassitude ou pour faire disparaître les notifications."
    )
}

private suspend fun rewrite(
    dao: QuizDao,
    oldQuestion: String,
    newQuestion: String,
    a: String,
    b: String,
    c: String,
    d: String,
    correctAnswer: String,
    explanation: String
) {
    dao.rewriteQuestion(
        quizType = CYBER,
        oldQuestion = oldQuestion,
        newQuestion = newQuestion,
        answerA = a,
        answerB = b,
        answerC = c,
        answerD = d,
        explanation = explanation
    )
    dao.rewriteReviewItem(
        quizType = CYBER,
        oldQuestion = oldQuestion,
        newQuestion = newQuestion,
        newCorrectAnswer = correctAnswer
    )
}
