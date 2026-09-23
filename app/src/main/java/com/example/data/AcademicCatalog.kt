package com.example.data

import com.example.model.AcademicGroup
import com.example.model.SubjectPaper

data class AcademicCatalogSubject(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val icon: String,
    val group: AcademicGroup,
    val paper: SubjectPaper = SubjectPaper.NONE,
    val colorHex: Long = 0xFF3D5AFE,
    val isCore: Boolean = false
) {
    fun getDisplayName(isBangla: Boolean): String {
        return if (isBangla && nameBn.isNotBlank()) nameBn else nameEn
    }
}

object AcademicCatalog {

    fun isJuniorClass(level: String): Boolean {
        val normalized = level.lowercase().trim()
        return normalized.contains("class 6") ||
                normalized.contains("class 7") ||
                normalized.contains("class 8") ||
                normalized == "class 6" ||
                normalized == "class 7" ||
                normalized == "class 8"
    }

    fun isSecondaryClass(level: String): Boolean {
        val normalized = level.lowercase().trim()
        return normalized.contains("class 9") ||
                normalized.contains("class 10") ||
                normalized.contains("ssc")
    }

    fun isHigherSecondaryClass(level: String): Boolean {
        val normalized = level.lowercase().trim()
        return normalized.contains("class 11") ||
                normalized.contains("class 12") ||
                normalized.contains("hsc")
    }

    fun isAdmission(level: String, curriculum: String): Boolean {
        val normLevel = level.lowercase()
        val normCurriculum = curriculum.lowercase()
        return normLevel.contains("admission") || normCurriculum.contains("admission")
    }

    fun isCambridgeOrEdexcel(curriculum: String): Boolean {
        val norm = curriculum.lowercase()
        return norm.contains("cambridge") || norm.contains("edexcel")
    }

    fun getAvailableGroups(educationLevel: String, curriculum: String = ""): List<AcademicGroup> {
        if (isJuniorClass(educationLevel)) {
            return listOf(AcademicGroup.GENERAL_JUNIOR)
        }
        return listOf(
            AcademicGroup.SCIENCE,
            AcademicGroup.BUSINESS_STUDIES,
            AcademicGroup.HUMANITIES
        )
    }

    // Class 6-8 Subjects (General Stream)
    private val juniorSubjects = listOf(
        AcademicCatalogSubject("bangla", "Bangla", "বাংলা", "🇧🇩", AcademicGroup.GENERAL_JUNIOR, isCore = true),
        AcademicCatalogSubject("english_for_today", "English For Today", "ইংলিশ ফর টুডে", "🇬🇧", AcademicGroup.GENERAL_JUNIOR, isCore = true),
        AcademicCatalogSubject("english_grammar", "English Grammar and Composition", "ইংরেজি ব্যাকরণ ও নির্মিতি", "📖", AcademicGroup.GENERAL_JUNIOR, isCore = true),
        AcademicCatalogSubject("math", "Mathematics", "গণিত", "🧮", AcademicGroup.GENERAL_JUNIOR, isCore = true),
        AcademicCatalogSubject("science", "Science", "বিজ্ঞান", "🔬", AcademicGroup.GENERAL_JUNIOR, isCore = true),
        AcademicCatalogSubject("bgs", "Bangladesh and Global Studies", "বাংলাদেশ ও বিশ্বপরিচয়", "🌏", AcademicGroup.GENERAL_JUNIOR),
        AcademicCatalogSubject("ict", "ICT", "তথ্য ও যোগাযোগ প্রযুক্তি", "💻", AcademicGroup.GENERAL_JUNIOR),
        AcademicCatalogSubject("religion_islam", "Islam and Moral Education", "ইসলাম ও নৈতিক শিক্ষা", "🕌", AcademicGroup.GENERAL_JUNIOR),
        AcademicCatalogSubject("religion_hindu", "Hinduism and Moral Education", "হিন্দুধর্ম ও নৈতিক শিক্ষা", "🕉️", AcademicGroup.GENERAL_JUNIOR),
        AcademicCatalogSubject("religion_buddhist", "Buddhism and Moral Education", "বৌদ্ধধর্ম ও নৈতিক শিক্ষা", "☸️", AcademicGroup.GENERAL_JUNIOR),
        AcademicCatalogSubject("religion_christian", "Christianity and Moral Education", "খ্রিষ্টধর্ম ও নৈতিক শিক্ষা", "✝️", AcademicGroup.GENERAL_JUNIOR)
    )

    // Class 9-10 Common compulsory subjects across groups
    private fun getSecondaryCommon(includeBgs: Boolean = true): List<AcademicCatalogSubject> {
        val list = mutableListOf(
            AcademicCatalogSubject("bangla", "Bangla", "বাংলা", "🇧🇩", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("english", "English", "ইংরেজি", "🇬🇧", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("math", "Mathematics", "গণিত", "🧮", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("ict", "ICT", "তথ্য ও যোগাযোগ প্রযুক্তি", "💻", AcademicGroup.SCIENCE),
            AcademicCatalogSubject("religion", "Religion and Moral Education", "ধর্ম ও নৈতিক শিক্ষা", "🕌", AcademicGroup.SCIENCE),
            AcademicCatalogSubject("physical_education", "Physical Education & Sports", "শারীরিক শিক্ষা ও স্বাস্থ্য", "🏃", AcademicGroup.SCIENCE)
        )
        if (includeBgs) {
            list.add(4, AcademicCatalogSubject("bgs", "Bangladesh and Global Studies", "বাংলাদেশ ও বিশ্বপরিচয়", "🌏", AcademicGroup.SCIENCE))
        }
        return list
    }

    // Class 9-10 Science
    private val secondaryScienceSubjects: List<AcademicCatalogSubject> by lazy {
        getSecondaryCommon(includeBgs = true).map { it.copy(group = AcademicGroup.SCIENCE) } + listOf(
            AcademicCatalogSubject("physics", "Physics", "পদার্থবিজ্ঞান", "⚛️", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("chemistry", "Chemistry", "রসায়ন", "🧪", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("biology", "Biology", "জীববিজ্ঞান", "🧬", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("higher_math", "Higher Mathematics", "উচ্চতর গণিত", "📐", AcademicGroup.SCIENCE)
        )
    }

    // Class 9-10 Business Studies
    private val secondaryBusinessSubjects: List<AcademicCatalogSubject> by lazy {
        getSecondaryCommon(includeBgs = true).map { it.copy(group = AcademicGroup.BUSINESS_STUDIES) } + listOf(
            AcademicCatalogSubject("accounting", "Accounting", "হিসাববিজ্ঞান", "📊", AcademicGroup.BUSINESS_STUDIES, isCore = true),
            AcademicCatalogSubject("business_entrepreneurship", "Business Entrepreneurship", "ব্যবসায় উদ্যোগ", "💼", AcademicGroup.BUSINESS_STUDIES, isCore = true),
            AcademicCatalogSubject("finance_banking", "Finance and Banking", "ফিন্যান্স ও ব্যাংকিং", "🏦", AcademicGroup.BUSINESS_STUDIES)
        )
    }

    // Class 9-10 Humanities
    private val secondaryHumanitiesSubjects: List<AcademicCatalogSubject> by lazy {
        getSecondaryCommon(includeBgs = false).map { it.copy(group = AcademicGroup.HUMANITIES) } + listOf(
            AcademicCatalogSubject("bgs", "Bangladesh and Global Studies", "বাংলাদেশ ও বিশ্বপরিচয়", "🌏", AcademicGroup.HUMANITIES),
            AcademicCatalogSubject("history", "History and World Civilization", "ইতিহাস ও বিশ্বসভ্যতা", "🏛️", AcademicGroup.HUMANITIES, isCore = true),
            AcademicCatalogSubject("geography", "Geography and Environment", "ভূগোল ও পরিবেশ", "🗺️", AcademicGroup.HUMANITIES, isCore = true),
            AcademicCatalogSubject("economics", "Economics", "অর্থনীতি", "📈", AcademicGroup.HUMANITIES),
            AcademicCatalogSubject("civics", "Civics and Citizenship", "পৌরনীতি ও নাগরিকতা", "⚖️", AcademicGroup.HUMANITIES),
            AcademicCatalogSubject("home_science", "Home Science", "গার্হস্থ্য বিজ্ঞান", "🏡", AcademicGroup.HUMANITIES),
            AcademicCatalogSubject("agriculture", "Agriculture Studies", "কৃষিশিক্ষা", "🌾", AcademicGroup.HUMANITIES),
            AcademicCatalogSubject("higher_math", "Higher Mathematics", "উচ্চতর গণিত", "📐", AcademicGroup.HUMANITIES)
        )
    }

    // Class 11-12 Common compulsory subjects
    private fun getHigherSecondaryCommon(group: AcademicGroup): List<AcademicCatalogSubject> {
        return listOf(
            AcademicCatalogSubject("bangla_1", "Bangla 1st Paper", "বাংলা ১ম পত্র", "🇧🇩", group, SubjectPaper.PAPER_1, isCore = true),
            AcademicCatalogSubject("bangla_2", "Bangla 2nd Paper", "বাংলা ২য় পত্র", "🇧🇩", group, SubjectPaper.PAPER_2, isCore = true),
            AcademicCatalogSubject("english_1", "English 1st Paper", "ইংরেজি ১ম পত্র", "🇬🇧", group, SubjectPaper.PAPER_1, isCore = true),
            AcademicCatalogSubject("english_2", "English 2nd Paper", "ইংরেজি ২য় পত্র", "🇬🇧", group, SubjectPaper.PAPER_2, isCore = true),
            AcademicCatalogSubject("ict", "ICT", "তথ্য ও যোগাযোগ প্রযুক্তি", "💻", group, SubjectPaper.NONE)
        )
    }

    // Class 11-12 Science
    private val higherSecondaryScienceSubjects: List<AcademicCatalogSubject> by lazy {
        getHigherSecondaryCommon(AcademicGroup.SCIENCE) + listOf(
            AcademicCatalogSubject("physics_1", "Physics 1st Paper", "পদার্থবিজ্ঞান ১ম পত্র", "⚛️", AcademicGroup.SCIENCE, SubjectPaper.PAPER_1, isCore = true),
            AcademicCatalogSubject("physics_2", "Physics 2nd Paper", "পদার্থবিজ্ঞান ২য় পত্র", "⚛️", AcademicGroup.SCIENCE, SubjectPaper.PAPER_2, isCore = true),
            AcademicCatalogSubject("chemistry_1", "Chemistry 1st Paper", "রসায়ন ১ম পত্র", "🧪", AcademicGroup.SCIENCE, SubjectPaper.PAPER_1, isCore = true),
            AcademicCatalogSubject("chemistry_2", "Chemistry 2nd Paper", "রসায়ন ২য় পত্র", "🧪", AcademicGroup.SCIENCE, SubjectPaper.PAPER_2, isCore = true),
            AcademicCatalogSubject("higher_math_1", "Higher Mathematics 1st Paper", "উচ্চতর গণিত ১ম পত্র", "📐", AcademicGroup.SCIENCE, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("higher_math_2", "Higher Mathematics 2nd Paper", "উচ্চতর গণিত ২য় পত্র", "📐", AcademicGroup.SCIENCE, SubjectPaper.PAPER_2),
            AcademicCatalogSubject("biology_1", "Biology 1st Paper", "জীববিজ্ঞান ১ম পত্র", "🧬", AcademicGroup.SCIENCE, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("biology_2", "Biology 2nd Paper", "জীববিজ্ঞান ২য় পত্র", "🧬", AcademicGroup.SCIENCE, SubjectPaper.PAPER_2)
        )
    }

    // Class 11-12 Business Studies
    private val higherSecondaryBusinessSubjects: List<AcademicCatalogSubject> by lazy {
        getHigherSecondaryCommon(AcademicGroup.BUSINESS_STUDIES) + listOf(
            AcademicCatalogSubject("accounting_1", "Accounting 1st Paper", "হিসাববিজ্ঞান ১ম পত্র", "📊", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_1, isCore = true),
            AcademicCatalogSubject("accounting_2", "Accounting 2nd Paper", "হিসাববিজ্ঞান ২য় পত্র", "📊", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_2, isCore = true),
            AcademicCatalogSubject("bom_1", "Business Organization & Management 1st Paper", "ব্যবসায় সংগঠন ও ব্যবস্থাপনা ১ম পত্র", "🏢", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("bom_2", "Business Organization & Management 2nd Paper", "ব্যবসায় সংগঠন ও ব্যবস্থাপনা ২য় পত্র", "🏢", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_2),
            AcademicCatalogSubject("fbi_1", "Finance, Banking & Insurance 1st Paper", "ফিন্যান্স, ব্যাংকিং ও বিমা ১ম পত্র", "🏦", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("fbi_2", "Finance, Banking & Insurance 2nd Paper", "ফিন্যান্স, ব্যাংকিং ও বিমা ২য় পত্র", "🏦", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_2),
            AcademicCatalogSubject("pmm_1", "Production Management & Marketing 1st Paper", "উৎপাদন ব্যবস্থাপনা ও বিপণন ১ম পত্র", "📦", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("pmm_2", "Production Management & Marketing 2nd Paper", "উৎপাদন ব্যবস্থাপনা ও বিপণন ২য় পত্র", "📦", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_2),
            AcademicCatalogSubject("economics_1", "Economics 1st Paper", "অর্থনীতি ১ম পত্র", "📈", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("economics_2", "Economics 2nd Paper", "অর্থনীতি ২য় পত্র", "📈", AcademicGroup.BUSINESS_STUDIES, SubjectPaper.PAPER_2)
        )
    }

    // Class 11-12 Humanities
    private val higherSecondaryHumanitiesSubjects: List<AcademicCatalogSubject> by lazy {
        getHigherSecondaryCommon(AcademicGroup.HUMANITIES) + listOf(
            AcademicCatalogSubject("economics_1", "Economics 1st Paper", "অর্থনীতি ১ম পত্র", "📈", AcademicGroup.HUMANITIES, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("economics_2", "Economics 2nd Paper", "অর্থনীতি ২য় পত্র", "📈", AcademicGroup.HUMANITIES, SubjectPaper.PAPER_2),
            AcademicCatalogSubject("civics_1", "Civics and Good Governance 1st Paper", "পৌরনীতি ও সুশাসন ১ম পত্র", "⚖️", AcademicGroup.HUMANITIES, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("civics_2", "Civics and Good Governance 2nd Paper", "পৌরনীতি ও সুশাসন ২য় পত্র", "⚖️", AcademicGroup.HUMANITIES, SubjectPaper.PAPER_2),
            AcademicCatalogSubject("history_1", "History / Islamic History 1st Paper", "ইতিহাস / ইসলামের ইতিহাস ১ম পত্র", "🏛️", AcademicGroup.HUMANITIES, SubjectPaper.PAPER_1),
            AcademicCatalogSubject("history_2", "History / Islamic History 2nd Paper", "ইতিহাস / ইসলামের ইতিহাস ২য় পত্র", "🏛️", AcademicGroup.HUMANITIES, SubjectPaper.PAPER_2),
            AcademicCatalogSubject("sociology", "Sociology", "সমাজবিজ্ঞান", "👥", AcademicGroup.HUMANITIES, SubjectPaper.NONE),
            AcademicCatalogSubject("social_work", "Social Work", "সমাজকর্ম", "🤝", AcademicGroup.HUMANITIES, SubjectPaper.NONE),
            AcademicCatalogSubject("logic", "Logic", "যুক্তিবিদ্যা", "🧠", AcademicGroup.HUMANITIES, SubjectPaper.NONE),
            AcademicCatalogSubject("geography", "Geography", "ভূগোল", "🗺️", AcademicGroup.HUMANITIES, SubjectPaper.NONE)
        )
    }

    // Cambridge / Edexcel (O-Level / A-Level)
    private val cambridgeSubjects: List<AcademicCatalogSubject> by lazy {
        listOf(
            AcademicCatalogSubject("math", "Mathematics (Pure / Statistics / Mechanics)", "গণিত", "🧮", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("physics", "Physics", "পদার্থবিজ্ঞান", "⚛️", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("chemistry", "Chemistry", "রসায়ন", "🧪", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("biology", "Biology", "জীববিজ্ঞান", "🧬", AcademicGroup.SCIENCE),
            AcademicCatalogSubject("english", "English Language & Literature", "ইংরেজি", "🇬🇧", AcademicGroup.SCIENCE, isCore = true),
            AcademicCatalogSubject("ict", "Computer Science & ICT", "কম্পিউটার সায়েন্স ও আইসিটি", "💻", AcademicGroup.SCIENCE),
            AcademicCatalogSubject("accounting", "Accounting", "হিসাববিজ্ঞান", "📊", AcademicGroup.BUSINESS_STUDIES),
            AcademicCatalogSubject("economics", "Economics", "অর্থনীতি", "📈", AcademicGroup.BUSINESS_STUDIES),
            AcademicCatalogSubject("business_studies", "Business Studies", "ব্যবসায় শিক্ষা", "💼", AcademicGroup.BUSINESS_STUDIES)
        )
    }

    // Admission Candidate Subjects
    private fun getAdmissionSubjects(group: AcademicGroup): List<AcademicCatalogSubject> {
        return when (group) {
            AcademicGroup.SCIENCE -> listOf(
                AcademicCatalogSubject("physics", "Physics (Engineering / Medical / Varsity)", "পদার্থবিজ্ঞান", "⚛️", AcademicGroup.SCIENCE, isCore = true),
                AcademicCatalogSubject("chemistry", "Chemistry (Engineering / Medical / Varsity)", "রসায়ন", "🧪", AcademicGroup.SCIENCE, isCore = true),
                AcademicCatalogSubject("math", "Mathematics (Engineering & Varsity)", "উচ্চতর গণিত", "🧮", AcademicGroup.SCIENCE, isCore = true),
                AcademicCatalogSubject("biology", "Biology (Medical & Varsity)", "জীববিজ্ঞান", "🧬", AcademicGroup.SCIENCE, isCore = true),
                AcademicCatalogSubject("english", "English (Admission General)", "ইংরেজি", "🇬🇧", AcademicGroup.SCIENCE),
                AcademicCatalogSubject("ict", "ICT & General Knowledge", "আইসিটি ও সাধারণ জ্ঞান", "💻", AcademicGroup.SCIENCE)
            )
            AcademicGroup.BUSINESS_STUDIES -> listOf(
                AcademicCatalogSubject("accounting", "Accounting (Varsity C-Unit)", "হিসাববিজ্ঞান", "📊", AcademicGroup.BUSINESS_STUDIES, isCore = true),
                AcademicCatalogSubject("business_management", "Business Organization & Management", "ব্যবসায় সংগঠন", "🏢", AcademicGroup.BUSINESS_STUDIES, isCore = true),
                AcademicCatalogSubject("finance_banking", "Finance & Banking", "ফিন্যান্স ও ব্যাংকিং", "🏦", AcademicGroup.BUSINESS_STUDIES),
                AcademicCatalogSubject("english", "English (Admission General)", "ইংরেজি", "🇬🇧", AcademicGroup.BUSINESS_STUDIES, isCore = true),
                AcademicCatalogSubject("ict", "ICT & General Knowledge", "আইসিটি ও সাধারণ জ্ঞান", "💻", AcademicGroup.BUSINESS_STUDIES)
            )
            AcademicGroup.HUMANITIES, AcademicGroup.GENERAL_JUNIOR -> listOf(
                AcademicCatalogSubject("bangla", "Bangla (Varsity B-Unit)", "বাংলা", "🇧🇩", AcademicGroup.HUMANITIES, isCore = true),
                AcademicCatalogSubject("english", "English (Admission General)", "ইংরেজি", "🇬🇧", AcademicGroup.HUMANITIES, isCore = true),
                AcademicCatalogSubject("general_knowledge", "General Knowledge (Bangladesh & International)", "সাধারণ জ্ঞান", "🌏", AcademicGroup.HUMANITIES, isCore = true),
                AcademicCatalogSubject("economics", "Economics & Social Studies", "অর্থনীতি ও সামাজিক বিজ্ঞান", "📈", AcademicGroup.HUMANITIES)
            )
        }
    }

    /**
     * Primary catalog query function returning dynamic subjects based on:
     * Class -> Curriculum -> Academic Group.
     */
    fun getSubjects(
        educationLevel: String,
        curriculum: String,
        group: AcademicGroup
    ): List<AcademicCatalogSubject> {
        // 1. Check if Cambridge / Edexcel
        if (isCambridgeOrEdexcel(curriculum)) {
            return if (group == AcademicGroup.BUSINESS_STUDIES) {
                cambridgeSubjects.filter { it.group == AcademicGroup.BUSINESS_STUDIES || it.id in listOf("math", "english", "ict") }
            } else {
                cambridgeSubjects.filter { it.group == AcademicGroup.SCIENCE || it.id in listOf("math", "english", "ict") }
            }
        }

        // 2. Check if Admission
        if (isAdmission(educationLevel, curriculum)) {
            return getAdmissionSubjects(group)
        }

        // 3. Junior Level (Class 6 - 8)
        if (isJuniorClass(educationLevel)) {
            return juniorSubjects
        }

        // 4. Secondary Level (Class 9 - 10)
        if (isSecondaryClass(educationLevel)) {
            return when (group) {
                AcademicGroup.SCIENCE -> secondaryScienceSubjects
                AcademicGroup.BUSINESS_STUDIES -> secondaryBusinessSubjects
                AcademicGroup.HUMANITIES -> secondaryHumanitiesSubjects
                AcademicGroup.GENERAL_JUNIOR -> secondaryScienceSubjects
            }
        }

        // 5. Higher Secondary Level (Class 11 - 12) or Higher
        return when (group) {
            AcademicGroup.SCIENCE -> higherSecondaryScienceSubjects
            AcademicGroup.BUSINESS_STUDIES -> higherSecondaryBusinessSubjects
            AcademicGroup.HUMANITIES -> higherSecondaryHumanitiesSubjects
            AcademicGroup.GENERAL_JUNIOR -> higherSecondaryScienceSubjects
        }
    }

    /**
     * Filters previously selected subject IDs/names against the available subjects in the new context.
     * Retains any selection that is still valid, while stripping invalid/stale ones.
     */
    fun filterValidSelectedSubjects(
        previouslySelected: List<String>,
        availableSubjects: List<AcademicCatalogSubject>
    ): List<String> {
        val validIds = mutableSetOf<String>()
        val availableIds = availableSubjects.map { it.id.lowercase() }.toSet()

        previouslySelected.forEach { sel ->
            val selLower = sel.lowercase().trim()
            // Direct ID match
            if (selLower in availableIds) {
                validIds.add(availableSubjects.first { it.id.equals(selLower, ignoreCase = true) }.id)
            } else {
                // Name match (English or Bangla)
                val matched = availableSubjects.find {
                    it.nameEn.equals(sel, ignoreCase = true) ||
                    it.nameBn.equals(sel, ignoreCase = true) ||
                    it.nameEn.contains(sel, ignoreCase = true) ||
                    sel.contains(it.nameEn, ignoreCase = true)
                }
                if (matched != null) {
                    validIds.add(matched.id)
                }
            }
        }
        return validIds.toList()
    }

    /**
     * Checks if a subject ID is allowed for a given Class, Curriculum, and Group.
     */
    fun isSubjectAllowed(
        educationLevel: String,
        curriculum: String,
        group: AcademicGroup,
        subjectId: String
    ): Boolean {
        val available = getSubjects(educationLevel, curriculum, group)
        val normalizedId = subjectId.lowercase().trim()
        return available.any { it.id.equals(normalizedId, ignoreCase = true) }
    }

    /**
     * Returns default core subject IDs for the given Class, Curriculum, and Group.
     */
    fun getDefaultCoreSubjectIds(
        educationLevel: String,
        curriculum: String,
        group: AcademicGroup
    ): List<String> {
        val available = getSubjects(educationLevel, curriculum, group)
        val core = available.filter { it.isCore }.map { it.id }
        return if (core.isNotEmpty()) core else available.take(4).map { it.id }
    }

    /**
     * Dynamic Quiz subjects for:
     * Class -> Curriculum -> AcademicGroup.
     * Class 6-8: Full junior subjects (Bangla, English For Today, Grammar, Math, Science, BGS, ICT, 4 Religions).
     * Class 9-10: Complete common + stream-specific subjects.
     * Class 11-12: Distinct base subjects with explicit supportedPapers (Paper 1 / Paper 2 / None).
     */
    fun getQuizSubjects(
        educationLevel: String,
        curriculum: String,
        group: AcademicGroup
    ): List<AcademicQuizSubject> {
        if (isJuniorClass(educationLevel)) {
            return juniorSubjects.map {
                AcademicQuizSubject(
                    id = it.id,
                    nameEn = it.nameEn,
                    nameBn = it.nameBn,
                    icon = it.icon,
                    group = AcademicGroup.GENERAL_JUNIOR,
                    availablePapers = listOf(SubjectPaper.NONE)
                )
            }
        }
        if (isCambridgeOrEdexcel(curriculum)) {
            val list = if (group == AcademicGroup.BUSINESS_STUDIES) {
                cambridgeSubjects.filter { it.group == AcademicGroup.BUSINESS_STUDIES || it.id in listOf("math", "english", "ict") }
            } else {
                cambridgeSubjects.filter { it.group == AcademicGroup.SCIENCE || it.id in listOf("math", "english", "ict") }
            }
            return list.map {
                AcademicQuizSubject(
                    id = it.id,
                    nameEn = it.nameEn,
                    nameBn = it.nameBn,
                    icon = it.icon,
                    group = it.group,
                    availablePapers = listOf(SubjectPaper.NONE)
                )
            }
        }
        if (isAdmission(educationLevel, curriculum)) {
            return getAdmissionSubjects(group).map {
                AcademicQuizSubject(
                    id = it.id,
                    nameEn = it.nameEn,
                    nameBn = it.nameBn,
                    icon = it.icon,
                    group = it.group,
                    availablePapers = listOf(SubjectPaper.NONE)
                )
            }
        }
        if (isSecondaryClass(educationLevel)) {
            val list = when (group) {
                AcademicGroup.SCIENCE -> secondaryScienceSubjects
                AcademicGroup.BUSINESS_STUDIES -> secondaryBusinessSubjects
                AcademicGroup.HUMANITIES -> secondaryHumanitiesSubjects
                AcademicGroup.GENERAL_JUNIOR -> secondaryScienceSubjects
            }
            return list.map {
                AcademicQuizSubject(
                    id = it.id,
                    nameEn = it.nameEn,
                    nameBn = it.nameBn,
                    icon = it.icon,
                    group = it.group,
                    availablePapers = listOf(SubjectPaper.NONE)
                )
            }
        }
        // Class 11 - 12 (Higher Secondary)
        return when (group) {
            AcademicGroup.SCIENCE -> listOf(
                AcademicQuizSubject("physics", "Physics", "পদার্থবিজ্ঞান", "⚛️", AcademicGroup.SCIENCE, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("chemistry", "Chemistry", "রসায়ন", "🧪", AcademicGroup.SCIENCE, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("higher_math", "Higher Mathematics", "উচ্চতর গণিত", "📐", AcademicGroup.SCIENCE, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("biology", "Biology", "জীববিজ্ঞান", "🧬", AcademicGroup.SCIENCE, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("bangla", "Bangla", "বাংলা", "🇧🇩", AcademicGroup.SCIENCE, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("english", "English", "ইংরেজি", "🇬🇧", AcademicGroup.SCIENCE, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("ict", "ICT", "তথ্য ও যোগাযোগ প্রযুক্তি", "💻", AcademicGroup.SCIENCE, listOf(SubjectPaper.NONE))
            )
            AcademicGroup.BUSINESS_STUDIES -> listOf(
                AcademicQuizSubject("accounting", "Accounting", "হিসাববিজ্ঞান", "📊", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("bom", "Business Organization & Management", "ব্যবসায় সংগঠন ও ব্যবস্থাপনা", "🏢", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("fbi", "Finance, Banking & Insurance", "ফিন্যান্স, ব্যাংকিং ও বিমা", "🏦", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("pmm", "Production Management & Marketing", "উৎপাদন ব্যবস্থাপনা ও বিপণন", "📦", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("economics", "Economics", "অর্থনীতি", "📈", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("bangla", "Bangla", "বাংলা", "🇧🇩", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("english", "English", "ইংরেজি", "🇬🇧", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("ict", "ICT", "তথ্য ও যোগাযোগ প্রযুক্তি", "💻", AcademicGroup.BUSINESS_STUDIES, listOf(SubjectPaper.NONE))
            )
            AcademicGroup.HUMANITIES, AcademicGroup.GENERAL_JUNIOR -> listOf(
                AcademicQuizSubject("economics", "Economics", "অর্থনীতি", "📈", AcademicGroup.HUMANITIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("civics", "Civics and Good Governance", "পৌরনীতি ও সুশাসন", "⚖️", AcademicGroup.HUMANITIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("history", "History / Islamic History", "ইতিহাস / ইসলামের ইতিহাস", "🏛️", AcademicGroup.HUMANITIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("sociology", "Sociology", "সমাজবিজ্ঞান", "👥", AcademicGroup.HUMANITIES, listOf(SubjectPaper.NONE)),
                AcademicQuizSubject("social_work", "Social Work", "সমাজকর্ম", "🤝", AcademicGroup.HUMANITIES, listOf(SubjectPaper.NONE)),
                AcademicQuizSubject("logic", "Logic", "যুক্তিবিদ্যা", "🧠", AcademicGroup.HUMANITIES, listOf(SubjectPaper.NONE)),
                AcademicQuizSubject("geography", "Geography", "ভূগোল", "🗺️", AcademicGroup.HUMANITIES, listOf(SubjectPaper.NONE)),
                AcademicQuizSubject("bangla", "Bangla", "বাংলা", "🇧🇩", AcademicGroup.HUMANITIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("english", "English", "ইংরেজি", "🇬🇧", AcademicGroup.HUMANITIES, listOf(SubjectPaper.PAPER_1, SubjectPaper.PAPER_2)),
                AcademicQuizSubject("ict", "ICT", "তথ্য ও যোগাযোগ প্রযুক্তি", "💻", AcademicGroup.HUMANITIES, listOf(SubjectPaper.NONE))
            )
        }
    }
}

data class AcademicQuizSubject(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val icon: String,
    val group: AcademicGroup,
    val availablePapers: List<SubjectPaper> = listOf(SubjectPaper.NONE)
) {
    val name: String get() = nameEn
}
