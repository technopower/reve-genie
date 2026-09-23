package com.example.data

/**
 * Data model for translation languages.
 */
data class TranslationLanguage(
    val code: String,
    val name: String,
    val nativeName: String
)

/**
 * Centralized repository for the 131 supported translation languages.
 */
object TranslationLanguages {
    val BANGLA = TranslationLanguage("bn", "Bangla", "বাংলা")
    val ENGLISH = TranslationLanguage("en", "English", "English")

    val allLanguages: List<TranslationLanguage> = listOf(
        BANGLA,
        ENGLISH,
        TranslationLanguage("hi", "Hindi", "हिन्दी"),
        TranslationLanguage("ar", "Arabic", "العربية"),
        TranslationLanguage("es", "Spanish", "Español"),
        TranslationLanguage("fr", "French", "Français"),
        TranslationLanguage("de", "German", "Deutsch"),
        TranslationLanguage("zh-CN", "Chinese (Simplified)", "简体中文"),
        TranslationLanguage("zh-TW", "Chinese (Traditional)", "繁體中文"),
        TranslationLanguage("ja", "Japanese", "日本語"),
        TranslationLanguage("ko", "Korean", "한국어"),
        TranslationLanguage("ru", "Russian", "Русский"),
        TranslationLanguage("pt", "Portuguese", "Português"),
        TranslationLanguage("it", "Italian", "Italiano"),
        TranslationLanguage("tr", "Turkish", "Türkçe"),
        TranslationLanguage("ur", "Urdu", "اردو"),
        TranslationLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        TranslationLanguage("ta", "Tamil", "தமிழ்"),
        TranslationLanguage("te", "Telugu", "తెలుగు"),
        TranslationLanguage("mr", "Marathi", "मराठी"),
        TranslationLanguage("gu", "Gujarati", "ગુજરાતી"),
        TranslationLanguage("kn", "Kannada", "ಕನ್ನಡ"),
        TranslationLanguage("ml", "Malayalam", "മലയാളം"),
        TranslationLanguage("or", "Odia", "ওড়িয়া"),
        TranslationLanguage("as", "Assamese", "অসমীয়া"),
        TranslationLanguage("ne", "Nepali", "নেপালী"),
        TranslationLanguage("si", "Sinhala", "සිංহල"),
        TranslationLanguage("id", "Indonesian", "Bahasa Indonesia"),
        TranslationLanguage("ms", "Malay", "Bahasa Melayu"),
        TranslationLanguage("vi", "Vietnamese", "Tiếng Việt"),
        TranslationLanguage("th", "Thai", "ไทย"),
        TranslationLanguage("my", "Burmese", "မြန်မာ"),
        TranslationLanguage("km", "Khmer", "ខ្មែរ"),
        TranslationLanguage("lo", "Lao", "ລາວ"),
        TranslationLanguage("fa", "Persian", "فارسی"),
        TranslationLanguage("ps", "Pashto", "پښتو"),
        TranslationLanguage("sd", "Sindhi", "سنڌي"),
        TranslationLanguage("am", "Amharic", "አማርኛ"),
        TranslationLanguage("sw", "Swahili", "Kiswahili"),
        TranslationLanguage("ha", "Hausa", "Hausa"),
        TranslationLanguage("yo", "Yoruba", "Yorùbá"),
        TranslationLanguage("ig", "Igbo", "Asụsụ Igbo"),
        TranslationLanguage("zu", "Zulu", "isiZulu"),
        TranslationLanguage("xh", "Xhosa", "isiXhosa"),
        TranslationLanguage("af", "Afrikaans", "Afrikaans"),
        TranslationLanguage("nl", "Dutch", "Nederlands"),
        TranslationLanguage("pl", "Polish", "Polski"),
        TranslationLanguage("uk", "Ukrainian", "Українська"),
        TranslationLanguage("ro", "Romanian", "Română"),
        TranslationLanguage("el", "Greek", "Ελληνικά"),
        TranslationLanguage("hu", "Hungarian", "Magyar"),
        TranslationLanguage("cs", "Czech", "Čeština"),
        TranslationLanguage("sv", "Swedish", "Svenska"),
        TranslationLanguage("no", "Norwegian", "Norsk"),
        TranslationLanguage("da", "Danish", "Dansk"),
        TranslationLanguage("fi", "Finnish", "Suomi"),
        TranslationLanguage("sk", "Slovak", "Slovenčina"),
        TranslationLanguage("bg", "Bulgarian", "Български"),
        TranslationLanguage("hr", "Croatian", "Hrvatski"),
        TranslationLanguage("sr", "Serbian", "Српски"),
        TranslationLanguage("sl", "Slovenian", "Slovenščina"),
        TranslationLanguage("lt", "Lithuanian", "Lietuvių"),
        TranslationLanguage("lv", "Latvian", "Latviešu"),
        TranslationLanguage("et", "Estonian", "Eesti"),
        TranslationLanguage("he", "Hebrew", "עברית"),
        TranslationLanguage("az", "Azerbaijani", "Azərbaycan"),
        TranslationLanguage("ka", "Georgian", "ქართული"),
        TranslationLanguage("hy", "Armenian", "Հայերেন"),
        TranslationLanguage("kk", "Kazakh", "Қазақ тілі"),
        TranslationLanguage("uz", "Uzbek", "Oʻzbekcha"),
        TranslationLanguage("ky", "Kyrgyz", "Кыргызча"),
        TranslationLanguage("tg", "Tajik", "Тоҷикӣ"),
        TranslationLanguage("tk", "Turkmen", "Türkmen dili"),
        TranslationLanguage("mn", "Mongolian", "Монгол"),
        TranslationLanguage("sq", "Albanian", "Shqip"),
        TranslationLanguage("mk", "Macedonian", "Македонски"),
        TranslationLanguage("bs", "Bosnian", "Bosanski"),
        TranslationLanguage("mt", "Maltese", "Malti"),
        TranslationLanguage("is", "Icelandic", "Íslenska"),
        TranslationLanguage("ga", "Irish", "Gaeilge"),
        TranslationLanguage("cy", "Welsh", "Cymraeg"),
        TranslationLanguage("eu", "Basque", "Euskara"),
        TranslationLanguage("ca", "Catalan", "Català"),
        TranslationLanguage("gl", "Galician", "Galego"),
        TranslationLanguage("be", "Belarusian", "Беларуская"),
        TranslationLanguage("eo", "Esperanto", "Esperanto"),
        TranslationLanguage("la", "Latin", "Lingua Latina"),
        TranslationLanguage("fil", "Filipino", "Wikang Filipino"),
        TranslationLanguage("jv", "Javanese", "Basa Jawa"),
        TranslationLanguage("su", "Sundanese", "Basa Sunda"),
        TranslationLanguage("ceb", "Cebuano", "Cebuano"),
        TranslationLanguage("mg", "Malagasy", "Malagasy"),
        TranslationLanguage("st", "Sesotho", "Sesotho"),
        TranslationLanguage("sn", "Shona", "chiShona"),
        TranslationLanguage("som", "Somali", "Soomaali"),
        TranslationLanguage("om", "Oromo", "Afaan Oromoo"),
        TranslationLanguage("ti", "Tigrinya", "ትግርኛ"),
        TranslationLanguage("rw", "Kinyarwanda", "Ikinyarwanda"),
        TranslationLanguage("ny", "Nyanja", "Chichewa"),
        TranslationLanguage("ln", "Lingala", "Lingála"),
        TranslationLanguage("lg", "Luganda", "Luganda"),
        TranslationLanguage("kg", "Kikongo", "Kikongo"),
        TranslationLanguage("haw", "Hawaiian", "ʻŌlelo Hawaiʻi"),
        TranslationLanguage("sm", "Samoan", "Gagana Samoa"),
        TranslationLanguage("mi", "Maori", "Te Reo Māori"),
        TranslationLanguage("fj", "Fijian", "Na Vosa Vakaviti"),
        TranslationLanguage("to", "Tongan", "Lea Faka-Tonga"),
        TranslationLanguage("ht", "Haitian Creole", "Kreyòl Ayisyen"),
        TranslationLanguage("lb", "Luxembourgish", "Lëtzebuergesch"),
        TranslationLanguage("fy", "Frisian", "Frysk"),
        TranslationLanguage("gd", "Scots Gaelic", "Gàidhlig"),
        TranslationLanguage("yi", "Yiddish", "ייִדיש"),
        TranslationLanguage("ug", "Uyghur", "ئۇيغۇرчә"),
        TranslationLanguage("tt", "Tatar", "Татар"),
        TranslationLanguage("ba", "Bashkir", "Башҡорт"),
        TranslationLanguage("cv", "Chuvash", "Чӑвашла"),
        TranslationLanguage("sah", "Yakut", "Саха тыла"),
        TranslationLanguage("kv", "Komi", "Коми"),
        TranslationLanguage("udm", "Udmurt", "Удмурт"),
        TranslationLanguage("os", "Ossetian", "Ирон"),
        TranslationLanguage("ab", "Abkhazian", "Аҧсшəа"),
        TranslationLanguage("gn", "Guarani", "Avañe'ẽ"),
        TranslationLanguage("qu", "Quechua", "Runa Simi"),
        TranslationLanguage("ay", "Aymara", "Aymar aru"),
        TranslationLanguage("bm", "Bambara", "Bamanankan"),
        TranslationLanguage("ee", "Ewe", "Eʋegbe"),
        TranslationLanguage("ff", "Fulani", "Fulfulde"),
        TranslationLanguage("wo", "Wolof", "Wolof"),
        TranslationLanguage("dv", "Dhivehi", "ދިވެހި"),
        TranslationLanguage("co", "Corsican", "Corsu"),
        TranslationLanguage("sa", "Sanskrit", "संस्कृतम्")
    )

    fun findByCode(code: String): TranslationLanguage {
        return allLanguages.find { it.code.equals(code, ignoreCase = true) }
            ?: allLanguages.find { it.name.equals(code, ignoreCase = true) }
            ?: BANGLA
    }

    fun findByName(name: String): TranslationLanguage {
        return allLanguages.find { it.name.equals(name, ignoreCase = true) }
            ?: allLanguages.find { it.nativeName.equals(name, ignoreCase = true) }
            ?: BANGLA
    }
}
