package com.example.cyberquiz.model

enum class Category(val label: String) {
    RESEAUX("Réseaux"), LINUX("Linux"), WINDOWS("Windows"), CRYPTO("Cryptographie"), WEB("Sécurité Web"),
    MALWARE("Malware"), SOCIAL("Ingénierie sociale"), OSINT("OSINT"), FORENSICS("Forensics"),
    PENTEST("Pentest"), AD("Active Directory"), CLOUD("Cloud Security"), MOBILE("Mobile Security"), SYSTEM("Sécurité système")
}

enum class NutritionCategory(val label: String) {
    MACRONUTRIENTS("Macronutriments"),
    MICRONUTRIENTS("Micronutriments"),
    HYDRATION("Hydratation"),
    BALANCE("Équilibre alimentaire"),
    DIGESTION("Digestion")
}

enum class Difficulty { EASY, MEDIUM, HARD }
