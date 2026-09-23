package com.example.data

import com.example.model.*

/**
 * AcademicContentCatalog defines the canonical Chapter and Topic hierarchy for:
 * Class -> Curriculum -> AcademicGroup -> Subject -> Paper -> Chapter -> Topic.
 *
 * Enforces:
 * 1. Class Isolation (e.g. Class 8 never returns Class 10/11 chapters)
 * 2. Group Isolation (Science vs Business vs Humanities)
 * 3. Paper Isolation (Paper 1 vs Paper 2)
 * 4. Curriculum Isolation (NCTB vs Cambridge vs Admission)
 * 5. Religion Isolation (Islam, Hindu, Buddhist, Christian)
 */
object AcademicContentCatalog {

    data class ChapterTemplate(
        val id: String,
        val chapterNumber: Int,
        val title: String,
        val titleBn: String,
        val estimatedHours: String = "3.5 hrs",
        val learningObjectives: List<String> = emptyList(),
        val revisionSummary: String = "",
        val topics: List<TopicTemplate>
    )

    data class TopicTemplate(
        val id: String,
        val title: String,
        val titleBn: String,
        val conceptExplanation: String,
        val conceptExplanationBn: String,
        val exampleProblem: String = "",
        val exampleSolution: String = "",
        val practiceQuestion: PracticeQuestionItem? = null
    )

    fun getChapters(
        educationLevel: String,
        curriculum: String,
        group: AcademicGroup,
        subjectId: String,
        paper: SubjectPaper = SubjectPaper.NONE,
        frameworkTag: String = "Verified Curriculum Framework",
        completedTopicIds: Set<String> = emptySet(),
        topicProgressMap: Map<String, Int> = emptyMap(),
        bookmarkedQuestionIds: Set<String> = emptySet(),
        mistakeQuestionIds: Set<String> = emptySet()
    ): List<SyllabusChapter> {
        val sId = subjectId.lowercase().trim()
        val templates = resolveTemplates(educationLevel, curriculum, group, sId, paper)

        return templates.map { ch ->
            val topics = ch.topics.map { top ->
                val pq = top.practiceQuestion ?: PracticeQuestionItem(
                    id = "pq_${top.id}",
                    topicId = top.id,
                    questionText = "Practice problem for ${top.title}",
                    difficulty = QuestionDifficulty.MEDIUM,
                    hint = "Review key formulas and principles for ${top.title}.",
                    steps = listOf("Step 1: Identify given data", "Step 2: Apply core relationship", "Step 3: Calculate answer"),
                    correctAnswer = "Solved correctly",
                    isBookmarked = bookmarkedQuestionIds.contains("pq_${top.id}"),
                    isAddedToMistakes = mistakeQuestionIds.contains("pq_${top.id}")
                )
                val q1 = QuizQuestion(
                    id = "qq_${top.id}_1",
                    subjectId = sId,
                    topic = top.title,
                    questionText = "Which statement accurately describes ${top.title} (${top.titleBn})?",
                    options = listOf(
                        top.conceptExplanation,
                        "It contradicts the fundamental laws of conservation and balance.",
                        "It is only observed in non-physical hypothetical models without practical relevance.",
                        "None of the standard academic formulations are applicable."
                    ),
                    correctIndex = 0,
                    explanation = "${top.title}: ${top.conceptExplanation}",
                    difficulty = QuestionDifficulty.MEDIUM,
                    chapterId = ch.id,
                    topicId = top.id,
                    curriculumId = CurriculumQuizRepository.mapToCurriculumId(curriculum),
                    className = educationLevel,
                    groupId = group.id,
                    paperId = paper.id
                )
                val q2 = QuizQuestion(
                    id = "qq_${top.id}_2",
                    subjectId = sId,
                    topic = top.title,
                    questionText = if (top.exampleProblem.isNotBlank()) top.exampleProblem else "What is the primary takeaway of ${top.title}?",
                    options = if (top.exampleSolution.isNotBlank()) {
                        listOf(
                            top.exampleSolution,
                            "Zero / Neutral reaction",
                            "Undefined reciprocal relation",
                            "Indeterminate variation under standard conditions"
                        )
                    } else {
                        listOf(
                            top.conceptExplanationBn,
                            "কোনো সুনির্দিষ্ট সূত্র নেই",
                            "তত্ত্বগতভাবে ভুল ধারণা",
                            "কেবলমাত্র ব্যতিক্রমী ক্ষেত্রে প্রযোজ্য"
                        )
                    },
                    correctIndex = 0,
                    explanation = if (top.exampleSolution.isNotBlank()) "Correct solution: ${top.exampleSolution}. ${top.conceptExplanation}" else top.conceptExplanation,
                    difficulty = QuestionDifficulty.EASY,
                    chapterId = ch.id,
                    topicId = top.id,
                    curriculumId = CurriculumQuizRepository.mapToCurriculumId(curriculum),
                    className = educationLevel,
                    groupId = group.id,
                    paperId = paper.id
                )
                SyllabusTopic(
                    id = top.id,
                    chapterId = ch.id,
                    title = top.title,
                    titleBn = top.titleBn,
                    conceptExplanation = top.conceptExplanation,
                    conceptExplanationBn = top.conceptExplanationBn,
                    exampleProblem = top.exampleProblem,
                    exampleSolution = top.exampleSolution,
                    isCompleted = completedTopicIds.contains(top.id),
                    progressPercent = topicProgressMap[top.id] ?: 0,
                    practiceQuestions = listOf(pq),
                    quickQuizQuestions = listOf(q1, q2)
                )
            }
            val progress = if (topics.isNotEmpty()) topics.map { it.progressPercent }.average().toInt() else 0
            val status = when {
                progress == 100 -> ChapterStatus.COMPLETED
                progress > 0 -> ChapterStatus.IN_PROGRESS
                else -> ChapterStatus.NOT_STARTED
            }
            SyllabusChapter(
                id = ch.id,
                subjectId = sId,
                chapterNumber = ch.chapterNumber,
                title = ch.title,
                titleBn = ch.titleBn,
                progressPercent = progress,
                status = status,
                estimatedHours = ch.estimatedHours,
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = ch.learningObjectives,
                revisionSummary = ch.revisionSummary,
                topics = topics
            )
        }
    }

    private fun resolveTemplates(
        educationLevel: String,
        curriculum: String,
        group: AcademicGroup,
        subjectId: String,
        paper: SubjectPaper
    ): List<ChapterTemplate> {
        // 1. Cambridge / Edexcel
        if (AcademicCatalog.isCambridgeOrEdexcel(curriculum)) {
            return getCambridgeChapters(subjectId)
        }

        // 2. Admission
        if (AcademicCatalog.isAdmission(educationLevel, curriculum)) {
            return getAdmissionChapters(subjectId)
        }

        // 3. Junior Level (Class 6 - 8)
        if (AcademicCatalog.isJuniorClass(educationLevel)) {
            return getJuniorChapters(subjectId)
        }

        // 4. Secondary Level (Class 9 - 10)
        if (AcademicCatalog.isSecondaryClass(educationLevel)) {
            return getSecondaryChapters(group, subjectId)
        }

        // 5. Higher Secondary Level (Class 11 - 12)
        return getHigherSecondaryChapters(group, subjectId, paper)
    }

    // ==========================================
    // CLASS 6 - 8 (JUNIOR) CHAPTERS
    // ==========================================
    private fun getJuniorChapters(subjectId: String): List<ChapterTemplate> {
        return when (subjectId) {
            "bangla" -> listOf(
                ChapterTemplate("ch_j_b_1", 1, "Bangla Literature & Poetry", "গদ্য ও পদ্য সংকলন", "4.0 hrs",
                    listOf("Understand themes in poems", "Analyze prose"), "Focus on moral values",
                    listOf(TopicTemplate("j_b_top_1", "Prose Reading", "গদ্য পাঠ ও ভাবার্থ", "Prose analysis for juniors", "গদ্যের মূলভাব অনুধাবন", "What is the lesson?", "The story teaches honesty."))
                ),
                ChapterTemplate("ch_j_b_2", 2, "Bangla Grammar", "বাংলা ব্যাকরণ ও ভাষা", "3.5 hrs",
                    listOf("Parts of speech in Bangla", "Sandhi rules"), "Memorize sandhi rules",
                    listOf(TopicTemplate("j_b_top_2", "Parts of Speech", "পদ প্রকরণ", "Nouns, pronouns and verbs in Bangla", "বিশেষ্য, বিশেষণ ও ক্রিয়া পদের ব্যবহার", "Identify verb", "পড়া হলো একটি ক্রিয়াপদ।"))
                )
            )
            "english_for_today" -> listOf(
                ChapterTemplate("ch_j_eft_1", 1, "Stories & Comprehension", "গল্প ও অনুচ্ছেদ পাঠ", "3.5 hrs",
                    listOf("Read for understanding", "Identify key vocabulary"), "Practice daily reading",
                    listOf(TopicTemplate("j_eft_top_1", "Reading Comprehension", "অনুচ্ছেদ অনুধাবন", "Techniques for unseen passages", "অনুচ্ছেদ পড়ে তথ্য নির্ণয়", "Find main character", "The main character is the wise farmer."))
                )
            )
            "english_grammar" -> listOf(
                ChapterTemplate("ch_j_eg_1", 1, "Parts of Speech & Tenses", "পদ ও কাল", "4.0 hrs",
                    listOf("Identify 8 parts of speech", "Use present and past tenses"), "Form verbs correctly",
                    listOf(TopicTemplate("j_eg_top_1", "Tenses Overview", "টেন্স পরিচিতি", "Present, past, and future structures", "বর্তমান, অতীত ও ভবিষ্যৎ কালের গঠন", "Convert: He goes to school", "Past: He went to school."))
                )
            )
            "math" -> listOf(
                ChapterTemplate("ch_j_m_1", 1, "Numbers, Factors & Fractions", "স্বাভাবিক সংখ্যা ও ভগ্নাংশ", "4.5 hrs",
                    listOf("Prime factorization", "HCF and LCM calculation"), "Product = HCF × LCM",
                    listOf(TopicTemplate("j_m_top_1", "HCF & LCM", "গসাগু ও লসাগু", "Finding highest common factor and lowest common multiple", "মৌলিক উৎপাদকের সাহায্যে গসাগু ও লসাগু নির্ণয়", "Find HCF of 12 and 18", "Factors of 12: 1,2,3,4,6,12. Factors of 18: 1,2,3,6,9,18. HCF = 6."))
                ),
                ChapterTemplate("ch_j_m_2", 2, "Algebraic Expressions & Equations", "বীজগণিতীয় রাশি ও সরল সমীকরণ", "4.0 hrs",
                    listOf("Form simple equations", "Solve single variable linear equations"), "Balance equations",
                    listOf(TopicTemplate("j_m_top_2", "Linear Equations", "সরল সমীকরণ গঠন ও সমাধান", "Solving ax + b = c", "এক চলকবিশিষ্ট সরল সমীকরণ সমাধান", "Solve 2x + 4 = 10", "2x = 6 => x = 3."))
                )
            )
            "science" -> listOf(
                ChapterTemplate("ch_j_sci_1", 1, "Scientific Measurement", "বৈজ্ঞানিক পরিমাপ", "3.0 hrs",
                    listOf("Standard units of measurement", "Measuring mass, length, time"), "SI unit system",
                    listOf(TopicTemplate("j_sci_top_1", "SI Units", "পরিমাপের এসআই একক", "Meter, Kilogram, Second standards", "দৈর্ঘ্য, ভর ও সময়ের এসআই একক", "Unit of mass?", "Kilogram (kg)."))
                ),
                ChapterTemplate("ch_j_sci_2", 2, "Living World & Plant Life", "জীবজগৎ ও উদ্ভিদের অঙ্গসংস্থান", "4.0 hrs",
                    listOf("Five kingdom classification", "Root and stem functions"), "Plant morphology",
                    listOf(TopicTemplate("j_sci_top_2", "Plant Organs", "উদ্ভিদের অঙ্গসমূহ", "Roots absorb water, leaves photosynthesize", "মূল ও পাতার কাজ", "Function of leaf?", "Photosynthesis and food production."))
                )
            )
            "bgs" -> listOf(
                ChapterTemplate("ch_j_bgs_1", 1, "History & Geography of Bangladesh", "বাংলাদেশের ইতিহাস ও ভূগোল", "3.5 hrs",
                    listOf("Ancient Bengal history", "River systems and landforms"), "Key rivers and historical sites",
                    listOf(TopicTemplate("j_bgs_top_1", "Major Rivers", "বাংলাদেশের প্রধান নদনদী", "Padma, Meghna, Jamuna rivers", "পদ্মা, মেঘনা ও যমুনা নদীর ভূমিকা", "Longest river?", "Meghna is one of the widest rivers."))
                )
            )
            "ict" -> listOf(
                ChapterTemplate("ch_j_ict_1", 1, "Introduction to ICT", "তথ্য ও যোগাযোগ প্রযুক্তির ধারণা", "3.0 hrs",
                    listOf("Input and output devices", "Internet basics"), "Safe device usage",
                    listOf(TopicTemplate("j_ict_top_1", "Hardware Basics", "কম্পিউটার হার্ডওয়্যার", "CPU, RAM, Storage overview", "সিপিইউ ও মেমরির কার্যপদ্ধতি", "What is RAM?", "Random Access Memory for active processing."))
                )
            )
            "religion_islam" -> listOf(
                ChapterTemplate("ch_j_rel_i_1", 1, "Aqidah & Iman", "আকাইদ ও ঈমান", "3.0 hrs",
                    listOf("Pillars of Islam and Iman", "Attributes of Allah"), "Seven articles of faith",
                    listOf(TopicTemplate("j_rel_i_top_1", "Tawheed & Risalat", "তাওহিদ ও রিসালাত", "Belief in oneness of Allah and prophethood", "একত্ববাদ ও নবুয়তের ওপর বিশ্বাস", "First pillar of Islam?", "Shahadah (Declaration of faith)."))
                ),
                ChapterTemplate("ch_j_rel_i_2", 2, "Ibadah & Daily Life", "ইবাদাত ও চরিত্র", "3.5 hrs",
                    listOf("Rules of Salat", "Truthfulness and moral conduct"), "Akhlaq-e-Hamidah",
                    listOf(TopicTemplate("j_rel_i_top_2", "Daily Salat", "দৈনন্দিন সালাত", "Five daily prayers and their significance", "পাঁচ ওয়াক্ত সালাতের গুরুত্ব ও নিয়ম", "How many daily obligatory prayers?", "Five prayers."))
                )
            )
            "religion_hindu" -> listOf(
                ChapterTemplate("ch_j_rel_h_1", 1, "Concept of God & Deities", "ঈশ্বর ও দেবদেবী", "3.0 hrs",
                    listOf("Brahman concept", "Worship of deities"), "Devotion and rituals",
                    listOf(TopicTemplate("j_rel_h_top_1", "Avatars & Deities", "ঈশ্বর ও অবতার", "Manifestations and moral guidance", "ঈশ্বরের স্বরূপ ও বিভিন্ন দেবদেবী", "Meaning of Dharma?", "Duty and moral righteousness."))
                )
            )
            "religion_buddhist" -> listOf(
                ChapterTemplate("ch_j_rel_b_1", 1, "Life & Teachings of Buddha", "বুদ্ধের জীবন ও শিক্ষা", "3.0 hrs",
                    listOf("Four Noble Truths", "Noble Eightfold Path"), "Non-violence and compassion",
                    listOf(TopicTemplate("j_rel_b_top_1", "Four Noble Truths", "চতুরার্য সত্য", "Dukkha and its cessation", "দুঃখ ও দুঃখ নিরোধের পথ", "First Noble Truth?", "Life contains suffering."))
                )
            )
            "religion_christian" -> listOf(
                ChapterTemplate("ch_j_rel_c_1", 1, "Creation & Teachings of Jesus Christ", "সৃষ্টি ও যিশুখ্রিষ্টের শিক্ষা", "3.0 hrs",
                    listOf("Creation story", "Gospels and love for humanity"), "The Golden Rule",
                    listOf(TopicTemplate("j_rel_c_top_1", "The Golden Rule", "খ্রিষ্টীয় নৈতিক শিক্ষা", "Treat others as you wish to be treated", "ভালোবাসা ও মানবতার শিক্ষা", "Core message?", "Love and forgiveness."))
                )
            )
            else -> emptyList()
        }
    }

    // ==========================================
    // CLASS 9 - 10 (SECONDARY) CHAPTERS
    // ==========================================
    private fun getSecondaryChapters(group: AcademicGroup, subjectId: String): List<ChapterTemplate> {
        return when (subjectId) {
            "math" -> listOf(
                ChapterTemplate("ch_m_1", 4, "Quadratic Equations", "দ্বিঘাত সমীকরণ", "4.5 hrs",
                    listOf("Solve by factoring and quadratic formula", "Discriminant analysis"),
                    "D = b² - 4ac determines nature of roots",
                    listOf(
                        TopicTemplate("m_quad_1", "Standard Form & Factorization", "দ্বিঘাত সমীকরণের আদর্শ রূপ",
                            "ax² + bx + c = 0 factorization method", "উৎপাদকে বিশ্লেষণের মাধ্যমে সমাধান",
                            "Solve x² - 5x + 6 = 0", "x = 2, 3"),
                        TopicTemplate("m_quad_2", "Discriminant Analysis", "নিশ্চায়ক বিশ্লেষণ",
                            "D = b² - 4ac determines roots nature", "মূলের প্রকৃতি নির্ধারণ",
                            "Find D of x² - 4x + 4 = 0", "D = 0, real and equal roots")
                    )
                ),
                ChapterTemplate("ch_m_2", 11, "Coordinate Geometry", "স্থানাঙ্ক জ্যামিতি", "3.8 hrs",
                    listOf("Distance formula", "Slope of a straight line"),
                    "Distance d = √((x₂-x₁)² + (y₂-y₁)²)",
                    listOf(TopicTemplate("m_coord_1", "Distance Formula", "দূরত্ব নির্ণয়",
                        "Cartesian distance calculation", "কার্তেসীয় স্থানাঙ্কে দূরত্ব",
                        "Find distance between (0,0) and (3,4)", "Distance = 5 units"))
                ),
                ChapterTemplate("ch_m_3", 9, "Trigonometric Ratios", "ত্রিকোণমিতিক অনুপাত", "5.0 hrs",
                    listOf("sin, cos, tan definitions", "Trigonometric identities"),
                    "sin²θ + cos²θ = 1",
                    listOf(TopicTemplate("m_trig_1", "Fundamental Identities", "মৌলিক অভেদাবলী",
                        "sin²θ + cos²θ = 1 and related ratios", "মৌলিক ত্রিকোণমিতিক সূত্রাবলী",
                        "Value of sin²30° + cos²30°?", "1"))
                ),
                ChapterTemplate("ch_m_4", 17, "Statistics", "পরিসংখ্যান", "3.2 hrs",
                    listOf("Mean, median and mode of grouped data", "Histogram and ogive"),
                    "Mean = ∑(fi·xi) / N",
                    listOf(TopicTemplate("m_stat_1", "Mean & Median", "গড় ও মধ্যক",
                        "Calculation of grouped data mean and median", "শ্রেণিবিন্যাসকৃত উপাত্তের গড় ও মধ্যক",
                        "Find mid-value of 10-20", "15"))
                )
            )
            "physics" -> listOf(
                ChapterTemplate("ch_p_1", 2, "Motion & Force", "গতি ও বল", "4.0 hrs",
                    listOf("Kinematics equations", "Newton's laws of motion"),
                    "F = ma and v = u + at",
                    listOf(
                        TopicTemplate("p_motion_1", "Distance & Velocity", "দূরত্ব ও বেগ",
                            "Scalar vs Vector concepts", "স্কেলার ও ভেক্টর রাশির পার্থক্য",
                            "Speed vs Velocity difference?", "Velocity has direction"),
                        TopicTemplate("p_motion_2", "Newton's Laws & Force", "নিউটনের সূত্র ও বল",
                            "F = ma application", "বল ও ত্বরণের সম্পর্ক",
                            "Force on 2kg accelerating at 3 m/s²?", "6 N")
                    )
                ),
                ChapterTemplate("ch_p_2", 4, "Work, Power & Energy", "কাজ, ক্ষমতা ও শক্তি", "3.5 hrs",
                    listOf("Work definition and kinetic energy", "Conservation of energy"),
                    "Ek = 0.5mv², Ep = mgh",
                    listOf(TopicTemplate("p_energy_1", "Kinetic & Potential Energy", "গতিশক্তি ও বিভব শক্তি",
                        "Work W = Fs, Energy transformations", "কাজ ও শক্তির নিত্যতা",
                        "Kinetic energy formula?", "Ek = 0.5mv²"))
                )
            )
            "chemistry" -> listOf(
                ChapterTemplate("ch_c_1", 3, "Structure of Matter", "পদার্থের গঠন", "3.5 hrs",
                    listOf("Subatomic particles and atomic models", "Electronic configuration"),
                    "Aufbau principle for orbital filling",
                    listOf(TopicTemplate("c_matter_1", "Electronic Configuration", "ইলেকট্রন বিন্যাস",
                        "s, p, d subshells and Bohr model", "শক্তিস্তর ও অরবিটালে ইলেকট্রন বণ্টন",
                        "Electronic configuration of Sodium (11)?", "1s² 2s² 2p⁶ 3s¹"))
                ),
                ChapterTemplate("ch_c_2", 5, "Chemical Bonds", "রাসায়নিক বন্ধন", "4.2 hrs",
                    listOf("Octet rule", "Ionic and covalent bonding"),
                    "Ionic = transfer, Covalent = sharing",
                    listOf(TopicTemplate("c_bond_1", "Ionic & Covalent Bonding", "আয়নিক ও সমযোজী বন্ধন",
                        "Electron transfer vs electron sharing", "আয়নিক ও সমযোজী যৌগের বৈশিষ্ট্য",
                        "Type of bond in NaCl?", "Ionic bond"))
                )
            )
            "biology" -> listOf(
                ChapterTemplate("ch_b_1", 2, "Cells and Tissues", "জীবকোষ ও টিস্যু", "3.2 hrs",
                    listOf("Plant and animal cell organelles", "Cellular functions"),
                    "Mitochondria is the powerhouse of the cell",
                    listOf(TopicTemplate("b_cell_1", "Cell Organelles", "কোষ অঙ্গাণুসমূহ",
                        "Functions of mitochondria, chloroplast, ribosomes", "মাইটোকন্ড্রিয়া ও ক্লোরোপ্লাস্টের কাজ",
                        "Powerhouse of the cell?", "Mitochondria"))
                )
            )
            "english" -> listOf(
                ChapterTemplate("ch_e_1", 1, "Grammar & Structure", "ইংরেজি ব্যাকরণ ও বাক্য গঠন", "3.0 hrs",
                    listOf("Subject-verb agreement", "Tenses and voice"),
                    "Singular subject takes singular verb",
                    listOf(TopicTemplate("e_verbs_1", "Right Form of Verbs", "ক্রিয়ার সঠিক রূপ",
                        "Subject-verb agreement rules", "কর্তা অনুযায়ী ক্রিয়ার রূপ",
                        "Neither of the boys (is/are) present?", "is"))
                )
            )
            "ict" -> listOf(
                ChapterTemplate("ch_ict_1", 3, "Digital Security & World", "আমার শিক্ষায় ইন্টারনেট ও নিরাপত্তা", "2.5 hrs",
                    listOf("Two-factor authentication", "Malware prevention"),
                    "Use strong passwords and firewalls",
                    listOf(TopicTemplate("ict_sec_1", "Cyber Security", "সাইবার নিরাপত্তা",
                        "2FA and password hygiene", "দ্বিমুখী যাচাইকরণ ও সাইবার নীতি",
                        "What is 2FA?", "Two-factor authentication"))
                )
            )
            "bangla" -> listOf(
                ChapterTemplate("ch_sec_b_1", 1, "Bangla Literature & Poetry", "বাংলা সাহিত্য ও কবিতা", "4.0 hrs",
                    listOf("Selected prose and poems", "Thematic analysis"), "Literary appreciation",
                    listOf(TopicTemplate("sec_b_top_1", "Prose Analysis", "গদ্য বিশ্লেষণ", "Analysis of classic Bangla prose", "বাংলা গদ্যের বিশ্লেষণ", "Identify central theme", "Humanity and brotherhood."))
                )
            )
            "bgs" -> listOf(
                ChapterTemplate("ch_sec_bgs_1", 1, "History of Independent Bangladesh", "স্বাধীন বাংলাদেশের অভ্যুদয়", "4.0 hrs",
                    listOf("1952 Language Movement to 1971 Liberation War", "Role of Bangabandhu"), "March 7 speech and Declaration of Independence",
                    listOf(TopicTemplate("sec_bgs_top_1", "1971 Liberation War", "১৯৭১ সালের মুক্তিযুদ্ধ", "Historic struggle for freedom", "মুক্তিযুদ্ধের ইতিহাস ও বিজয়", "Independence Day of Bangladesh?", "26th March"))
                )
            )
            "religion", "religion_islam" -> listOf(
                ChapterTemplate("ch_sec_rel_1", 1, "Aqidah & Shariah", "আকাইদ ও শরিয়ত", "3.5 hrs",
                    listOf("Articles of Faith", "Islamic jurisprudence principles"), "Halal and Haram guidelines",
                    listOf(TopicTemplate("sec_rel_top_1", "Aqidah Foundations", "আকাইদের মূলনীতি", "Core beliefs and their impact on daily life", "আকাইদের প্রভাব", "Meaning of Islam?", "Peace and submission to Allah"))
                )
            )
            "physical_education" -> listOf(
                ChapterTemplate("ch_sec_pe_1", 1, "Physical Fitness & Health", "শারীরিক সুস্থতা ও স্বাস্থ্য", "2.5 hrs",
                    listOf("Benefits of regular exercise", "First aid for common injuries"), "RICE method for sprains",
                    listOf(TopicTemplate("sec_pe_top_1", "First Aid Basics", "প্রাথমিক চিকিৎসা", "Treating sprains, cuts, and burns", "প্রাথমিক চিকিৎসার সাধারণ নিয়ম", "What is RICE in sports injury?", "Rest, Ice, Compression, Elevation"))
                )
            )
            "accounting" -> listOf(
                ChapterTemplate("ch_acc_1", 2, "Double Entry System & Accounts", "দুতরফা দাখিলা পদ্ধতি ও হিসাব", "4.5 hrs",
                    listOf("Debit and Credit rules", "Accounting equation A = L + OE"),
                    "Assets = Liabilities + Owner's Equity",
                    listOf(
                        TopicTemplate("acc_top_1", "Accounting Equation", "হিসাব সমীকরণ",
                            "A = L + OE fundamentals", "হিসাব সমীকরণের উপাদান ও প্রভাব",
                            "If assets increase by 500 and liabilities stay same, equity?", "Increases by 500"),
                        TopicTemplate("acc_top_2", "Debit Credit Principles", "ডেবিট ও ক্রেডিট নির্ণয়",
                            "Rules for real, personal, and nominal accounts", "হিসাবের ডেবিট-ক্রেডিট নিয়ম",
                            "Cash received from debtor: debit what?", "Cash account")
                    )
                ),
                ChapterTemplate("ch_acc_2", 6, "Financial Statements", "আর্থিক বিবরণী", "5.0 hrs",
                    listOf("Income statement", "Balance sheet preparation"), "Profit and loss calculation",
                    listOf(TopicTemplate("acc_top_3", "Income Statement", "বিশদ আয় বিবরণী",
                        "Net sales minus cost of goods sold", "মোট ও নিট লাভ নির্ণয়",
                        "Gross profit formula?", "Net Revenue - Cost of Goods Sold"))
                )
            )
            "business_entrepreneurship" -> listOf(
                ChapterTemplate("ch_be_1", 1, "Introduction to Entrepreneurship", "ব্যবসায় উদ্যোগের ধারণা", "3.5 hrs",
                    listOf("Characteristics of an entrepreneur", "Forms of business ownership"),
                    "Innovation and risk-taking",
                    listOf(TopicTemplate("be_top_1", "Entrepreneur Qualities", "উদ্যোক্তার গুণাবলী",
                        "Leadership, vision, persistence", "উদ্যোক্তার গুণাবলি ও ভূমিকা",
                        "Key traits of entrepreneur?", "Vision, persistence, risk management"))
                )
            )
            "finance_banking" -> listOf(
                ChapterTemplate("ch_fb_1", 3, "Time Value of Money", "অর্থের সময়মূল্য", "4.0 hrs",
                    listOf("Present value and future value", "Opportunity cost"),
                    "FV = PV × (1 + r)^n",
                    listOf(TopicTemplate("fb_top_1", "Future Value Calculation", "ভবিষ্যৎ মূল্য নির্ণয়",
                        "Compounding interest formulas", "চক্রবৃদ্ধি সুদের হিসাব",
                        "FV formula?", "FV = PV × (1 + r)^n"))
                )
            )
            "history" -> listOf(
                ChapterTemplate("ch_hist_1", 1, "Ancient Bengal & Cultural Heritage", "প্রাচীন বাংলার ইতিহাস ও ঐতিহ্য", "3.5 hrs",
                    listOf("Maurya and Gupta eras", "Pala and Sena dynasties"), "Cultural monuments of Bengal",
                    listOf(TopicTemplate("hist_top_1", "Pala Dynasty", "পাল বংশ ও শিল্পকলা", "Establishment and Buddhist patronage", "পাল রাজাদের শাসনকাল", "Founder of Pala dynasty?", "Gopala"))
                )
            )
            "geography" -> listOf(
                ChapterTemplate("ch_geog_1", 2, "Earth's Structure & Universe", "মহাবিশ্ব ও পৃথিবী", "3.5 hrs",
                    listOf("Solar system planets", "Earth's internal layers (crust, mantle, core)"), "Lithosphere and tectonic plates",
                    listOf(TopicTemplate("geog_top_1", "Earth Layers", "পৃথিবীর অভ্যন্তরীণ গঠন", "Crust, Mantle, Outer Core, Inner Core", "ভূত্বক, গুরুমণ্ডল ও কেন্দ্রমণ্ডলের বৈশিষ্ট্য", "Innermost layer?", "Core"))
                )
            )
            "economics" -> listOf(
                ChapterTemplate("ch_econ_1", 1, "Basic Economic Problems", "অর্থনীতির মৌলিক সমস্যা", "3.5 hrs",
                    listOf("Scarcity and choice", "Opportunity cost principle"), "Resources are scarce while wants are unlimited",
                    listOf(TopicTemplate("econ_top_1", "Scarcity & Choice", "দুষ্প্রাপ্যতা ও নির্বাচন", "Robbins definition of economics", "দুষ্প্রাপ্যতা ও সুযোগ ব্যয়", "Definition of scarcity?", "Unlimited wants with limited resources"))
                )
            )
            "civics" -> listOf(
                ChapterTemplate("ch_civ_1", 1, "Citizenship & State", "পৌরনীতি, রাষ্ট্র ও নাগরিকতা", "3.5 hrs",
                    listOf("Elements of a state", "Rights and duties of citizens"), "State elements: Population, Territory, Government, Sovereignty",
                    listOf(TopicTemplate("civ_top_1", "Elements of State", "রাষ্ট্রের উপাদানসমূহ", "Four fundamental elements", "জনসমষ্টি, নির্দিষ্ট ভূখণ্ড, সরকার ও সার্বভৌমত্ব", "Supreme power of state?", "Sovereignty"))
                )
            )
            "higher_math" -> listOf(
                ChapterTemplate("ch_hm_1", 1, "Sets, Functions & Binomial Expansion", "সেট, ফাংশন ও দ্বিপদী বিস্তৃতি", "4.5 hrs",
                    listOf("Domain and range", "Pascal's triangle and binomial theorem"), "Binomial expansion formula",
                    listOf(TopicTemplate("hm_top_1", "Binomial Theorem", "দ্বিপদী বিস্তৃতি", "Expanding (a + x)^n using combinations", "দ্বিপদী উপপাদ্যের প্রয়োগ", "Expansion of (1 + x)²?", "1 + 2x + x²"))
                )
            )
            "home_science" -> listOf(
                ChapterTemplate("ch_hs_1", 1, "Home Management & Family Living", "গৃহ ব্যবস্থাপনা ও সম্পদ", "3.0 hrs",
                    listOf("Planning, organizing, controlling, evaluating", "Nutritional balance"), "Family resource management",
                    listOf(TopicTemplate("hs_top_1", "Management Process", "গৃহ ব্যবস্থাপনা পদ্ধতি", "Steps in achieving family goals", "পারিবারিক লক্ষ্য অর্জনের ধাপ", "First step?", "Planning"))
                )
            )
            "agriculture" -> listOf(
                ChapterTemplate("ch_agri_1", 1, "Agricultural Technology & Crops", "কৃষি প্রযুক্তি ও ফসল উৎপাদন", "3.0 hrs",
                    listOf("Crop diversification", "Soil preparation and seed germination"), "Modern irrigation practices",
                    listOf(TopicTemplate("agri_top_1", "Seed Germination", "বীজ ও অঙ্কুরোদগম", "Conditions for healthy germination", "বীজের মান ও অঙ্কুরোদগম", "Key conditions for germination?", "Moisture, air, optimal temperature"))
                )
            )
            else -> emptyList()
        }
    }

    // ==========================================
    // CLASS 11 - 12 (HIGHER SECONDARY) CHAPTERS
    // ==========================================
    private fun getHigherSecondaryChapters(group: AcademicGroup, subjectId: String, paper: SubjectPaper): List<ChapterTemplate> {
        val sId = subjectId.lowercase()
        return when {
            // --- PHYSICS ---
            sId == "physics_1" || (sId == "physics" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_p1_1", 2, "Vectors", "ভেক্টর", "4.5 hrs",
                    listOf("Vector dot and cross product", "Vector calculus: gradient, divergence, curl"),
                    "A · B = AB cosθ, |A × B| = AB sinθ",
                    listOf(
                        TopicTemplate("hsc_p1_top_1", "Vector Operations", "ভেক্টর গুণন ও ডট-ক্রস গুণফল",
                            "Dot product yields scalar; cross product yields orthogonal vector", "স্কেলার গুণন ও ভেক্টর গুণন",
                            "Dot product of perpendicular vectors?", "Zero"),
                        TopicTemplate("hsc_p1_top_2", "Calculus with Vectors", "ভেক্টর ক্যালকুলাস",
                            "Gradient, divergence and solenoidal fields", "গ্রেডিয়েন্ট, ডাইভারজেন্স ও কার্ল",
                            "When is a field solenoidal?", "div F = 0")
                    )
                ),
                ChapterTemplate("ch_hsc_p1_2", 4, "Newtonian Mechanics", "নিউটনিয়ান বলবিদ্যা", "5.0 hrs",
                    listOf("Law of conservation of linear and angular momentum", "Centripetal force and road banking"),
                    "Banking angle tanθ = v² / (rg)",
                    listOf(TopicTemplate("hsc_p1_top_3", "Road Banking & Torque", "রাস্তার ব্যাংকিং ও টর্ক",
                        "tanθ = v²/(rg) calculations", "বাঁকে গাড়ির গতি ও ব্যাংকিং কোণ",
                        "Banking formula?", "tanθ = v² / (rg)"))
                ),
                ChapterTemplate("ch_hsc_p1_3", 10, "Ideal Gas & Kinetic Theory", "আদর্শ গ্যাস ও গ্যাসের গতিতত্ত্ব", "4.0 hrs",
                    listOf("Ideal gas law PV = nRT", "RMS velocity of gas molecules"),
                    "c_rms = √(3RT / M)",
                    listOf(TopicTemplate("hsc_p1_top_4", "RMS Velocity", "মূল গড় বর্গবেগ",
                        "Calculation of c_rms = √(3RT/M)", "গ্যাসের অণুর গড় বর্গবেগ নির্ণয়",
                        "RMS velocity formula?", "√(3RT/M)"))
                )
            )
            sId == "physics_2" || (sId == "physics" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_p2_1", 1, "Thermodynamics", "তাপগতিবিদ্যা", "5.0 hrs",
                    listOf("First and Second laws of thermodynamics", "Carnot engine efficiency and entropy"),
                    "Efficiency η = 1 - (T₂ / T₁)",
                    listOf(
                        TopicTemplate("hsc_p2_top_1", "First Law & Work Done", "তাপগতিবিদ্যার ১ম সূত্র ও কাজ",
                            "dQ = dU + dW for isobaric and adiabatic processes", "তাপগতিবিদ্যার প্রথম সূত্র ও অভ্যন্তরীণ শক্তি",
                            "First law equation?", "dQ = dU + dW"),
                        TopicTemplate("hsc_p2_top_2", "Carnot Engine & Entropy", "কার্নো ইঞ্জিন ও এন্ট্রপি",
                            "η = 1 - T₂/T₁, dS = dQ/T", "কার্নো ইঞ্জিনের কর্মদক্ষতা ও এন্ট্রপি",
                            "Can efficiency reach 100%?", "No, second law prohibits it")
                    )
                ),
                ChapterTemplate("ch_hsc_p2_2", 3, "Current Electricity", "চলতড়িৎ", "4.5 hrs",
                    listOf("Kirchhoff's laws", "Wheatstone bridge principle"),
                    "Wheatstone bridge balance: P/Q = R/S",
                    listOf(TopicTemplate("hsc_p2_top_3", "Kirchhoff's Laws & Bridge", "কার্শফের সূত্র ও হুইটস্টোন ব্রিজ",
                        "Loop rule and junction rule", "হুইটস্টোন ব্রিজ নীতি",
                        "Bridge balance condition?", "P / Q = R / S"))
                ),
                ChapterTemplate("ch_hsc_p2_3", 8, "Introduction to Modern Physics", "আধুনিক পদার্থবিজ্ঞানের সূচনা", "4.5 hrs",
                    listOf("Special relativity: time dilation, length contraction", "Photoelectric effect and photon energy E = hf"),
                    "E = mc², E = hf",
                    listOf(TopicTemplate("hsc_p2_top_4", "Special Relativity", "আপেক্ষিকতার বিশেষ তত্ত্ব",
                        "Lorentz transformation and mass-energy equivalence", "দৈর্ঘ্য সংকোচন ও সময় প্রসারণ",
                        "Mass energy relation?", "E = mc²"))
                )
            )

            // --- CHEMISTRY ---
            sId == "chemistry_1" || (sId == "chemistry" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_c1_1", 2, "Qualitative Chemistry", "গুণগত রসায়ন", "5.0 hrs",
                    listOf("Quantum numbers (n, l, m, s)", "Aufbau, Hund's, Pauli's principles"),
                    "Four quantum numbers define electron state completely",
                    listOf(TopicTemplate("hsc_c1_top_1", "Quantum Numbers & Orbitals", "কোয়ান্টাম সংখ্যা ও অরবিটাল",
                        "n, l, m, s values and orbital shapes", "চারটি কোয়ান্টাম সংখ্যার তাত্পর্য",
                        "Number of orbitals in p subshell?", "3 orbitals (px, py, pz)"))
                ),
                ChapterTemplate("ch_hsc_c1_2", 4, "Chemical Change & Equilibrium", "রাসায়নিক পরিবর্তন ও সাম্যাবস্থা", "4.5 hrs",
                    listOf("Law of Mass Action, Kp and Kc", "Le Chatelier's principle and pH"),
                    "Kp = Kc · (RT)^Δn",
                    listOf(TopicTemplate("hsc_c1_top_2", "Chemical Equilibrium Kp & Kc", "রাসায়নিক সাম্যাবস্থা ও Kp, Kc",
                        "Equilibrium constant derivations", "সাম্যধ্রুবক Kp ও Kc এর সম্পর্ক",
                        "Relation between Kp and Kc?", "Kp = Kc(RT)^Δn"))
                )
            )
            sId == "chemistry_2" || (sId == "chemistry" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_c2_1", 1, "Environmental Chemistry", "পরিবেশ রসায়ন", "4.0 hrs",
                    listOf("Gas laws: Boyle, Charles, Avogadro", "Dalton's law of partial pressures"),
                    "P_total = P₁ + P₂ + ...",
                    listOf(TopicTemplate("hsc_c2_top_1", "Gas Laws & Dalton's Law", "গ্যাস সূত্রাবলী ও ডাল্টনের সূত্র",
                        "Partial pressure Pi = Xi · P_total", "আংশিক চাপ ও মোট চাপের সম্পর্ক",
                        "Formula for partial pressure?", "Mole fraction × Total Pressure"))
                ),
                ChapterTemplate("ch_hsc_c2_2", 2, "Organic Chemistry", "জৈব রসায়ন", "6.0 hrs",
                    listOf("Nomenclature, isomerism, reaction mechanisms", "Hydrocarbons, alcohols, aldehydes, ketones"),
                    "SN1, SN2, electrophilic addition and substitution",
                    listOf(TopicTemplate("hsc_c2_top_2", "Isomerism & Reactions", "সমাণুতা ও বিক্রিয়া কৌশল",
                        "Structural and stereoisomerism in hydrocarbons", "জৈব যৌগের সমাণুতা ও বিক্রিয়া",
                        "Types of optical isomers?", "Enantiomers and diastereomers"))
                )
            )

            // --- HIGHER MATHEMATICS ---
            sId == "higher_math_1" || (sId == "higher_math" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_hm1_1", 1, "Matrices and Determinants", "ম্যাট্রিক্স ও নির্ণায়ক", "4.5 hrs",
                    listOf("Matrix multiplication and inverse", "Cramer's rule for linear systems"),
                    "Inverse A⁻¹ = (1/|A|) · adj(A)",
                    listOf(TopicTemplate("hsc_hm1_top_1", "Matrix Inverse & Determinants", "বিপরীত ম্যাট্রিক্স ও নির্ণায়ক",
                        "Adjoint method and Cramer's rule", "ইনভার্স ম্যাট্রিক্স নির্ণয়",
                        "When does inverse not exist?", "When determinant is zero (singular matrix)"))
                ),
                ChapterTemplate("ch_hsc_hm1_2", 9, "Differentiation & Calculus", "অন্তরীকরণ", "5.5 hrs",
                    listOf("First principle derivative", "Product rule, quotient rule, chain rule"),
                    "d/dx (u·v) = u v' + v u'",
                    listOf(TopicTemplate("hsc_hm1_top_2", "Calculus Derivatives", "অন্তরীকরণ ও চেইন রুল",
                        "Derivative formulas and rate of change", "ক্যালকুলাসের মৌলিক সূত্র",
                        "Derivative of sin(x)?", "cos(x)"))
                )
            )
            sId == "higher_math_2" || (sId == "higher_math" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_hm2_1", 3, "Complex Numbers", "জটিল সংখ্যা", "4.0 hrs",
                    listOf("Modulus and argument", "De Moivre's theorem and roots of unity"),
                    "z = r(cosθ + i sinθ)",
                    listOf(TopicTemplate("hsc_hm2_top_1", "Modulus & Argument", "মডুলাস ও আর্গুমেন্ট",
                        "Polar representation of z = a + bi", "জটিল সংখ্যার পোলার রূপ",
                        "Modulus of 3 + 4i?", "√(3² + 4²) = 5"))
                ),
                ChapterTemplate("ch_hsc_hm2_2", 6, "Conics (Parabola, Ellipse, Hyperbola)", "কণিক", "5.0 hrs",
                    listOf("Standard equations of parabola, ellipse, hyperbola", "Eccentricity e and directrix"),
                    "Parabola e=1, Ellipse e<1, Hyperbola e>1",
                    listOf(TopicTemplate("hsc_hm2_top_2", "Conic Sections", "কণিক ও পরাবৃত্ত",
                        "y² = 4ax and general conic properties", "পরাবৃত্তের সমীকরণ ও বৈশিষ্ট্য",
                        "Eccentricity of parabola?", "e = 1"))
                )
            )

            // --- BIOLOGY ---
            sId == "biology_1" || (sId == "biology" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_b1_1", 1, "Cell Structure & Genetics (Botany)", "কোষ ও এর গঠন", "4.5 hrs",
                    listOf("Plant cell wall, DNA and RNA structure", "Replication, transcription, translation"),
                    "Central dogma: DNA -> RNA -> Protein",
                    listOf(TopicTemplate("hsc_b1_top_1", "DNA Structure & Central Dogma", "ডিএনএ গঠন ও অনুলিপন",
                        "Watson-Crick double helix structure", "ডিএনএ অনুলিপন ও প্রোটিন সংশ্লেষণ",
                        "Purines in DNA?", "Adenine and Guanine"))
                )
            )
            sId == "biology_2" || (sId == "biology" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_b2_1", 4, "Human Physiology: Circulation (Zoology)", "মানব শারীরতত্ত্ব: রক্ত ও সংবহন", "4.5 hrs",
                    listOf("Heart structure and cardiac cycle", "Blood groups and clotting factors"),
                    "Cardiac cycle: Atrial & Ventricular systole and diastole",
                    listOf(TopicTemplate("hsc_b2_top_1", "Heart & Cardiac Cycle", "হৃদযন্ত্র ও রক্ত সংবহন",
                        "Double circulation in humans", "হৃৎপিণ্ডের গঠন ও রক্ত প্রবাহ",
                        "Universal blood donor?", "O negative"))
                )
            )

            // --- ACCOUNTING ---
            sId == "accounting_1" || (sId == "accounting" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_acc1_1", 1, "Books of Accounts & Worksheet", "হিসাবের বই ও কার্যপত্র", "4.5 hrs",
                    listOf("Special journals and subsidiary ledgers", "10-column worksheet preparation"),
                    "Worksheet bridges trial balance and financial statements",
                    listOf(TopicTemplate("hsc_acc1_top_1", "Worksheet & Adjustments", "কার্যপত্র ও সমন্বয় দাখিলা",
                        "Accruals, prepayments and worksheet preparation", "সমন্বয় দাখিলা ও কার্যপত্র",
                        "Prepaid expense is what type of account?", "Asset account"))
                )
            )
            sId == "accounting_2" || (sId == "accounting" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_acc2_1", 2, "Partnership & Company Accounts", "অংশীদারি ও যৌথ মূলধনি কোম্পানি", "5.0 hrs",
                    listOf("Profit and loss appropriation", "Share issue and forfeiture"),
                    "Partnership agreement governs profit sharing",
                    listOf(TopicTemplate("hsc_acc2_top_1", "Partnership Accounts", "অংশীদারি ব্যবসায়ের হিসাব",
                        "Appropriation account and partners capital accounts", "লাভ-লোকসান বণ্টন হিসাব",
                        "If no partnership deed, interest on loan rate?", "6%"))
                )
            )

            // --- BUSINESS ORGANIZATION & MANAGEMENT ---
            sId == "bom_1" || (sId == "bom" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_bom1_1", 1, "Business Organization Fundamentals", "ব্যবসায়ের মৌলিক ধারণা", "4.0 hrs",
                    listOf("Sole proprietorship and partnership structures", "Joint stock company characteristics"),
                    "Limited liability in companies",
                    listOf(TopicTemplate("hsc_bom1_top_1", "Forms of Business", "ব্যবসায়ের বিভিন্ন ধরন",
                        "Comparison of ownership forms", "একমালিকানা ও কোম্পানির পার্থক্য",
                        "Feature of public limited company?", "Transferable shares"))
                )
            )
            sId == "bom_2" || (sId == "bom" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_bom2_1", 1, "Principles of Management", "ব্যবস্থাপনার নীতি ও পরিকল্পনা", "4.0 hrs",
                    listOf("Fayol's 14 principles of management", "Strategic and operational planning"),
                    "Planning is the primary function of management",
                    listOf(TopicTemplate("hsc_bom2_top_1", "Management Functions", "ব্যবস্থাপনার মৌলিক কার্যাবলি",
                        "Planning, Organizing, Leading, Controlling", "ব্যবস্থাপনার কাজ ও গুরুত্ব",
                        "First function of management?", "Planning"))
                )
            )

            // --- FINANCE, BANKING & INSURANCE ---
            sId == "fbi_1" || (sId == "fbi" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_fbi1_1", 3, "Capital Budgeting & Risk", "মূলধন বাজেটিং ও ঝুঁকি", "4.5 hrs",
                    listOf("NPV, IRR and Payback Period", "Risk and return tradeoff"),
                    "Accept project if NPV > 0",
                    listOf(TopicTemplate("hsc_fbi1_top_1", "Capital Budgeting Techniques", "মূলধন বাজেটিং পদ্ধতি",
                        "Calculation of Payback and Net Present Value", "এনপিভি ও পরিশোধকাল নির্ণয়",
                        "Decision rule for NPV?", "Accept if NPV > 0"))
                )
            )
            sId == "fbi_2" || (sId == "fbi" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_fbi2_1", 1, "Central & Commercial Banking", "কেন্দ্রীয় ও বাণিজ্যিক ব্যাংক", "4.0 hrs",
                    listOf("Functions of Bangladesh Bank", "Credit creation by commercial banks"),
                    "Central bank is lender of last resort",
                    listOf(TopicTemplate("hsc_fbi2_top_1", "Central Banking", "কেন্দ্রীয় ব্যাংকের কার্যাবলি",
                        "Monetary policy tools and reserve ratios", "মুদ্রানীতি ও ঋণ নিয়ন্ত্রণ",
                        "Central bank of Bangladesh?", "Bangladesh Bank"))
                )
            )

            // --- PRODUCTION MANAGEMENT & MARKETING ---
            sId == "pmm_1" || (sId == "pmm" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_pmm1_1", 1, "Production & Productivity", "উৎপাদন ও উৎপাদনশীলতা", "4.0 hrs",
                    listOf("Types of utility created by production", "Productivity measurement"),
                    "Productivity = Output / Input",
                    listOf(TopicTemplate("hsc_pmm1_top_1", "Productivity Concepts", "উৎপাদনশীলতার ধারণা",
                        "Labor, capital and total factor productivity", "উৎপাদনশীলতার হিসাব",
                        "Formula for productivity?", "Total Output / Total Input"))
                )
            )
            sId == "pmm_2" || (sId == "pmm" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_pmm2_1", 1, "Marketing Concepts & Environment", "বিপণন পরিচিতি ও পরিবেশ", "4.0 hrs",
                    listOf("4 Ps of marketing mix (Product, Price, Place, Promotion)", "Micro and macro environment"),
                    "Customer value and satisfaction",
                    listOf(TopicTemplate("hsc_pmm2_top_1", "Marketing Mix 4Ps", "বিপণন মিশ্রণ (৪পি)",
                        "Product, Price, Place, Promotion strategy", "মার্কেটিং মিক্সের উপাদানসমূহ",
                        "What are the 4 Ps?", "Product, Price, Place, Promotion"))
                )
            )

            // --- ECONOMICS ---
            sId == "economics_1" || (sId == "economics" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_econ1_1", 2, "Consumer Behavior & Demand", "ভোক্তার আচরণ ও চাহিদা", "4.5 hrs",
                    listOf("Law of diminishing marginal utility", "Price elasticity of demand"),
                    "Elasticity Ed = (% ΔQ) / (% ΔP)",
                    listOf(TopicTemplate("hsc_econ1_top_1", "Demand & Elasticity", "চাহিদা ও স্থিতিস্থাপকতা",
                        "Calculating price elasticity of demand", "চাহিদার স্থিতিস্থাপকতা নির্ণয়",
                        "Law of demand states?", "Price and quantity demanded are inversely related"))
                )
            )
            sId == "economics_2" || (sId == "economics" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_econ2_1", 4, "Inflation & Fiscal Policy in Bangladesh", "মুদ্রাস্ফীতি ও রাজস্ব নীতি", "4.5 hrs",
                    listOf("Causes and types of inflation", "Government budget, tax structures and development"),
                    "Demand-pull vs Cost-push inflation",
                    listOf(TopicTemplate("hsc_econ2_top_1", "Inflation & Monetary Measures", "মুদ্রাস্ফীতি ও প্রতিকার",
                        "CPI measurement and inflation control", "মুদ্রাস্ফীতির কারণ ও নিয়ন্ত্রণ",
                        "What causes demand-pull inflation?", "Excess demand over supply"))
                )
            )

            // --- CIVICS & GOOD GOVERNANCE ---
            sId == "civics_1" || (sId == "civics" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_civ1_1", 1, "Good Governance & Rule of Law", "পৌরনীতি ও সুশাসন পরিচিতি", "4.0 hrs",
                    listOf("Pillars of good governance: accountability, transparency, rule of law", "Rights and civil liberties"),
                    "Good governance ensures public accountability",
                    listOf(TopicTemplate("hsc_civ1_top_1", "Pillars of Good Governance", "সুশাসনের স্তম্ভসমূহ",
                        "Transparency, accountability, participation", "সুশাসনের মৌলিক উপাদান",
                        "Core pillars of good governance?", "Accountability, transparency, rule of law"))
                )
            )
            sId == "civics_2" || (sId == "civics" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_civ2_1", 1, "British India & Emergence of Bangladesh", "স্বাধীন বাংলাদেশের অভ্যুদয়ের ইতিহাস", "4.5 hrs",
                    listOf("1947 partition to 1970 elections", "1971 war of independence and constitution of 1972"),
                    "Four state principles of 1972 constitution",
                    listOf(TopicTemplate("hsc_civ2_top_1", "1972 Constitution", "১৯৭২ সালের সংবিধান",
                        "Fundamental rights and state principles", "বাংলাদেশের সংবিধানের মূলনীতি",
                        "Fundamental principles of 1972 constitution?", "Nationalism, Socialism, Democracy, Secularism"))
                )
            )

            // --- HISTORY ---
            sId == "history_1" || (sId == "history" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_hist1_1", 1, "Colonial Rule in Indian Subcontinent", "ভারত উপমহাদেশে ব্রিটিশ শাসন", "4.0 hrs",
                    listOf("Battle of Plassey 1757", "Permanent settlement and 1857 rebellion"),
                    "Impact of British colonial policies",
                    listOf(TopicTemplate("hsc_hist1_top_1", "Battle of Plassey", "পলাশীর যুদ্ধ ও ফলাফল",
                        "Fall of Nawab Siraj-ud-Daulah", "১৭৫৭ সালের পলাশীর যুদ্ধ",
                        "Year of Battle of Plassey?", "1757"))
                )
            )
            sId == "history_2" || (sId == "history" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_hist2_1", 1, "World History & World Wars", "শিল্প বিপ্লব ও বিশ্বযুদ্ধ", "4.5 hrs",
                    listOf("Industrial Revolution in Europe", "First and Second World Wars and League of Nations"),
                    "Global geopolitical transformations",
                    listOf(TopicTemplate("hsc_hist2_top_1", "Industrial Revolution", "শিল্প বিপ্লবের প্রভাব",
                        "Technological leaps and socioeconomic shifts", "শিল্প বিপ্লবের সূচনা ও বিস্তৃতি",
                        "Country where Industrial Revolution began?", "Great Britain"))
                )
            )

            // --- COMMON HIGHER SECONDARY SUBJECTS ---
            sId == "bangla_1" || (sId == "bangla" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_b1", 1, "HSC Literature & Poetry", "উচ্চমাধ্যমিক সাহিত্য পাঠ ও কবিতা", "4.0 hrs",
                    listOf("Analysis of classic prose and poetry", "Stylistic devices"), "Literary appreciation",
                    listOf(TopicTemplate("hsc_b1_top", "Classic Prose Analysis", "উচ্চমাধ্যমিক গদ্য বিশ্লেষণ",
                        "Themes and characters in prescribed texts", "গদ্যের মূলভাব ও বিশ্লেষণ",
                        "Analysis objective?", "Comprehend historical and social context"))
                )
            )
            sId == "bangla_2" || (sId == "bangla" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_b2", 1, "Bangla Grammar & Composition", "বাংলা ব্যাকরণিক শব্দশ্রেণি ও নির্মিতি", "4.0 hrs",
                    listOf("Parts of speech classification", "Report writing and formal letters"), "Grammar precision",
                    listOf(TopicTemplate("hsc_b2_top", "Grammar Categories", "ব্যাকরণিক শব্দশ্রেণি",
                        "Noun, verb, and modifier rules", "শব্দশ্রেণির শ্রেণিবিভাগ",
                        "Purpose?", "Master applied Bangla grammar"))
                )
            )
            sId == "english_1" || (sId == "english" && paper == SubjectPaper.PAPER_1) -> listOf(
                ChapterTemplate("ch_hsc_e1", 1, "HSC English For Today", "ইংলিশ ফর টুডে পাঠ্যবই", "4.0 hrs",
                    listOf("Reading comprehension of units", "Vocabulary in context"), "Contextual analysis",
                    listOf(TopicTemplate("hsc_e1_top", "Theme & Passage Analysis", "থিম ও অনুচ্ছেদ বিশ্লেষণ",
                        "Textual appreciation and answering questions", "প্যাসেজ বিশ্লেষণ",
                        "Key skill?", "Critical reading comprehension"))
                )
            )
            sId == "english_2" || (sId == "english" && paper == SubjectPaper.PAPER_2) -> listOf(
                ChapterTemplate("ch_hsc_e2", 1, "Advanced Grammar & Writing", "উন্নত ব্যাকরণ ও রচনা", "4.0 hrs",
                    listOf("Sentence transformation, modifiers, connectives", "Formal letter, report and essay writing"), "Advanced composition",
                    listOf(TopicTemplate("hsc_e2_top", "Modifiers & Connectors", "মডিফায়ার ও কানেক্টরস",
                        "Pre-modifiers, post-modifiers, logical connectors", "মডিফায়ারের সঠিক প্রয়োগ",
                        "What is a modifier?", "A word that qualifies or describes another word"))
                )
            )
            sId == "ict" -> listOf(
                ChapterTemplate("ch_hsc_ict_1", 1, "Communication & Networking", "কমিউনিকেশন সিস্টেমস ও নেটওয়ার্কিং", "4.5 hrs",
                    listOf("Transmission modes, topologies, OSI model", "Wireless technologies (WiFi, Bluetooth, 5G)"),
                    "Network topologies: Star, Mesh, Ring, Bus",
                    listOf(TopicTemplate("hsc_ict_top_1", "Network Topologies", "নেটওয়ার্ক টপোলজি",
                        "Star, Mesh, Bus topology comparisons", "টপোলজির বৈশিষ্ট্য ও সুবিধা",
                        "Topology with central hub?", "Star topology"))
                ),
                ChapterTemplate("ch_hsc_ict_2", 3, "Number Systems & Digital Logic", "সংখ্যা পদ্ধতি ও ডিজিটাল লজিক", "5.0 hrs",
                    listOf("Binary, Octal, Decimal, Hex conversions", "Logic gates (AND, OR, NOT, NAND, NOR, XOR)"),
                    "Universal gates: NAND and NOR",
                    listOf(TopicTemplate("hsc_ict_top_2", "Logic Gates & Boolean Algebra", "লজিক গেট ও বুলিয়ান অ্যালজেবরা",
                        "Truth tables and De Morgan's laws", "মৌলিক ও সার্বজনীন গেট",
                        "Universal gates?", "NAND and NOR"))
                ),
                ChapterTemplate("ch_hsc_ict_3", 5, "Programming in C", "সি প্রোগ্রামিং ভাষা", "5.5 hrs",
                    listOf("Variables, data types, loops, arrays", "Functions and pointers"),
                    "C language structured programming principles",
                    listOf(TopicTemplate("hsc_ict_top_3", "C Loops & Control Statements", "সি ভাষায় লুপ ও শর্তযুক্ত স্টেটমেন্ট",
                        "for, while, if-else structures", "লুপ ও কন্ট্রোল স্টেটমেন্ট",
                        "Loop construct in C?", "for, while, do-while"))
                )
            )

            // Other Humanities subjects
            sId == "sociology" -> listOf(
                ChapterTemplate("ch_hsc_soc_1", 1, "Introduction to Sociology", "সমাজবিজ্ঞানের উৎপত্তি ও বিকাশ", "3.5 hrs",
                    listOf("Auguste Comte and sociological imagination", "Social institutions"), "Sociology as a scientific discipline",
                    listOf(TopicTemplate("hsc_soc_top_1", "Origins of Sociology", "সমাজবিজ্ঞানের ধারণা", "Definition and scope", "সমাজবিজ্ঞানের বিষয়বস্তু", "Father of Sociology?", "Auguste Comte"))
                )
            )
            sId == "social_work" -> listOf(
                ChapterTemplate("ch_hsc_sw_1", 1, "Philosophy of Social Work", "সমাজকর্মের প্রকৃতি ও দর্শন", "3.5 hrs",
                    listOf("Values and principles of professional social work", "Case work and group work methods"), "Empowering communities",
                    listOf(TopicTemplate("hsc_sw_top_1", "Social Work Methods", "সমাজকর্মের পদ্ধতিসমূহ", "Primary and secondary methods", "সমাজকর্মের মৌলিক পদ্ধতি", "Primary methods?", "Social casework, group work, community organization"))
                )
            )
            sId == "logic" -> listOf(
                ChapterTemplate("ch_hsc_log_1", 1, "Nature of Logic & Deductive Reasoning", "যুক্তির স্বরূপ ও সহানুমান", "4.0 hrs",
                    listOf("Propositions, terms, syllogistic reasoning", "Fallacies in deductive logic"), "Validity vs truth",
                    listOf(TopicTemplate("hsc_log_top_1", "Syllogism & Propositions", "সহানুমান ও অবধারণ", "Structure of categorical syllogism", "যুক্তিবাক্য ও সহানুমানের নিয়ম", "What is a syllogism?", "Deductive argument with two premises and a conclusion"))
                )
            )
            sId == "geography" -> listOf(
                ChapterTemplate("ch_hsc_geo_1", 1, "Physical Geography of Earth", "প্রাকৃতিক ভূগোল ও ভূমিরূপ", "4.0 hrs",
                    listOf("Plate tectonics and landform evolution", "Atmospheric pressure belts and global winds"), "Geomorphology",
                    listOf(TopicTemplate("hsc_geo_top_1", "Plate Tectonics", "পাত সংস্থান তত্ত্ব ও ভূমিরূপ", "Convergent and divergent boundaries", "প্লেট টেকটনিক তত্ত্ব", "Boundary forming mountains?", "Convergent boundary"))
                )
            )
            else -> emptyList()
        }
    }

    // ==========================================
    // CAMBRIDGE / EDEXCEL CHAPTERS
    // ==========================================
    private fun getCambridgeChapters(subjectId: String): List<ChapterTemplate> {
        return when (subjectId) {
            "math" -> listOf(
                ChapterTemplate("ch_cam_m_1", 1, "Pure Mathematics: Functions & Calculus", "পিউর ম্যাথমেটিক্স", "5.0 hrs",
                    listOf("Functions, domain and range", "Differentiation and integration techniques"), "Calculus fundamentals",
                    listOf(TopicTemplate("cam_m_top_1", "Calculus Integration", "ক্যালকুলাস ইন্টিগ্রেশন", "Definite and indefinite integrals", "ইন্টিগ্রেশনের নিয়ম", "Integral of x dx?", "x²/2 + C"))
                )
            )
            "physics" -> listOf(
                ChapterTemplate("ch_cam_p_1", 1, "Mechanics & Thermal Physics", "মেকানিক্স ও তাপবিজ্ঞান", "5.0 hrs",
                    listOf("Forces, equilibrium and projectile motion", "Ideal gas laws and thermodynamics"), "Kinematic modeling",
                    listOf(TopicTemplate("cam_p_top_1", "Projectile Motion", "প্রাসের গতি", "2D kinematics under gravity", "প্রাসের দ্বিমাত্রিক গতি", "Horizontal acceleration of projectile?", "Zero (ignoring air resistance)"))
                )
            )
            "chemistry" -> listOf(
                ChapterTemplate("ch_cam_c_1", 1, "Physical & Organic Chemistry", "ভৌত ও জৈব রসায়ন", "5.0 hrs",
                    listOf("Reaction energetics and kinetics", "Functional groups and reaction mechanisms"), "Transition states and enthalpy",
                    listOf(TopicTemplate("cam_c_top_1", "Enthalpy Changes", "এনথালপি পরিবর্তন", "Hess's Law and calorimetry", "হেসের সূত্র", "Hess's Law principle?", "Total enthalpy change is independent of route taken"))
                )
            )
            "biology" -> listOf(
                ChapterTemplate("ch_cam_b_1", 1, "Molecular Biology & Genetics", "আণবিক জীববিজ্ঞান", "4.5 hrs",
                    listOf("Enzymes and metabolic pathways", "Gene expression and inheritance"), "Enzyme kinetics",
                    listOf(TopicTemplate("cam_b_top_1", "Enzyme Kinetics", "এনজাইম বিক্রিয়া", "Lock and key vs induced fit model", "এনজাইমের কর্মপদ্ধতি", "Active site function?", "Binds substrate specifically"))
                )
            )
            "accounting" -> listOf(
                ChapterTemplate("ch_cam_acc_1", 1, "Financial Reporting & Ratios", "ফাইন্যান্সিয়াল রিপোর্টিং", "4.5 hrs",
                    listOf("Profitability and liquidity ratio analysis", "Statement of cash flows"), "Ratio analysis",
                    listOf(TopicTemplate("cam_acc_top_1", "Liquidity Ratios", "তারল্য অনুপাত", "Current ratio and quick ratio calculations", "কারেন্ট ও কুইক রেশিও", "Current ratio formula?", "Current Assets / Current Liabilities"))
                )
            )
            "economics" -> listOf(
                ChapterTemplate("ch_cam_econ_1", 1, "Micro & Macro Economics", "মাইক্রো ও ম্যাক্রো অর্থনীতি", "4.5 hrs",
                    listOf("Market equilibrium and market failure", "Fiscal and monetary policy in open economy"), "Market mechanisms",
                    listOf(TopicTemplate("cam_econ_top_1", "Market Failure", "বাজার ব্যর্থতা ও বাহ্যিকতা", "Public goods, merit goods and externalities", "বাহ্যিকতা ও সরকারি হস্তক্ষেপ", "Example of public good?", "National defence"))
                )
            )
            "english" -> listOf(
                ChapterTemplate("ch_cam_e_1", 1, "Text Analysis & Directed Writing", "টেক্সট অ্যানালাইসিস", "4.0 hrs",
                    listOf("Rhetorical devices and tone analysis", "Directed composition and argumentation"), "Effective argumentation",
                    listOf(TopicTemplate("cam_e_top_1", "Rhetorical Devices", "অলঙ্কার ও প্রকাশভঙ্গি", "Metaphor, irony, tone and audience awareness", "লেখক কৌশল বিশ্লেষণ", "Purpose of persuasive writing?", "To convince the reader with reasoned argument"))
                )
            )
            "ict" -> listOf(
                ChapterTemplate("ch_cam_ict_1", 1, "Data Representation & Algorithms", "ডেটা ও অ্যালগরিদম", "4.5 hrs",
                    listOf("Binary, hex, sound and image representation", "Pseudocode and algorithm efficiency"), "Big-O notation basics",
                    listOf(TopicTemplate("cam_ict_top_1", "Pseudocode Algorithms", "সিউডোকোড ও অ্যালগরিদম", "Writing structured algorithms for searching and sorting", "অ্যালগরিদম ডিজাইন", "Binary search requirement?", "List must be sorted"))
                )
            )
            else -> emptyList()
        }
    }

    // ==========================================
    // ADMISSION CHAPTERS
    // ==========================================
    private fun getAdmissionChapters(subjectId: String): List<ChapterTemplate> {
        return when (subjectId) {
            "physics" -> listOf(
                ChapterTemplate("ch_adm_p_1", 1, "BUET / Engineering Advanced Physics", "ইঞ্জিনিয়ারিং অ্যাডভান্সড পদার্থবিজ্ঞান", "5.0 hrs",
                    listOf("Complex rotational dynamics and moment of inertia", "SHM, wave interference and electromagnetic induction"), "High-yield numerical shortcuts",
                    listOf(TopicTemplate("adm_p_top_1", "Rotational Dynamics & SHM", "ঘূর্ণন গতিবিদ্যা ও স্পন্দন", "Moment of inertia calculations and damping", "জড়তার ভ্রামক ও সরল ছন্দিত স্পন্দন", "Moment of inertia of solid cylinder?", "0.5 M R²"))
                )
            )
            "chemistry" -> listOf(
                ChapterTemplate("ch_adm_c_1", 1, "Medical & Varsity High-Yield Chemistry", "মেডিকেল ও ভার্সিটি রসায়ন", "5.0 hrs",
                    listOf("Organic reaction mechanisms and conversions", "Equilibrium, buffer solutions and electrochemical cells"), "Rapid calculations and memorization techniques",
                    listOf(TopicTemplate("adm_c_top_1", "Buffer Solutions & Electrochemistry", "বাফার দ্রবণ ও তড়িৎ রসায়ন", "Henderson-Hasselbalch equation and Nernst equation", "বাফার পিএইচ ও নার্নস্ট সমীকরণ", "Henderson equation?", "pH = pKa + log([Salt]/[Acid])"))
                )
            )
            "math" -> listOf(
                ChapterTemplate("ch_adm_m_1", 1, "Varsity / Engineering Advanced Mathematics", "উচ্চতর গণিত স্পেশাল", "5.5 hrs",
                    listOf("Complex calculus problems and maxima/minima", "Conics, vectors and 3D coordinate geometry"), "Speed-solving tricks",
                    listOf(TopicTemplate("adm_m_top_1", "Calculus Speed Solving", "ক্যালকুলাস শর্টকাট টেকনিক", "Integration by parts and L'Hopital's rule", "লা হসপিটাল নীতি ও ইন্টিগ্রেশন", "When apply L'Hopital's rule?", "For 0/0 or ∞/∞ indeterminate forms"))
                )
            )
            "biology" -> listOf(
                ChapterTemplate("ch_adm_b_1", 1, "Medical High-Yield Biology", "মেডিকেল ভর্তি জীববিজ্ঞান", "5.0 hrs",
                    listOf("Human organ systems, endocrine glands, genetics", "Plant physiology, cycles (Calvin, Krebs)"), "Medical admission focus areas",
                    listOf(TopicTemplate("adm_b_top_1", "High-Yield Genetics & Physiology", "মেডিকেল জিনতত্ত্ব ও শারীরতত্ত্ব", "Mendelian ratios, hormones and metabolic pathways", "মেডিকেল গুরুত্বপূর্ণ শারীরতাত্ত্বিক তথ্য", "Blood sugar lowering hormone?", "Insulin"))
                )
            )
            "accounting" -> listOf(
                ChapterTemplate("ch_adm_acc_1", 1, "DU C-Unit Advanced Accounting", "ঢাবি গ-ইউনিট হিসাববিজ্ঞান", "4.5 hrs",
                    listOf("Company accounts, cash flows, partnership adjustments", "Cost accounting ratios and standard costing"), "C-Unit exam patterns",
                    listOf(TopicTemplate("adm_acc_top_1", "C-Unit Exam Practice", "গ-ইউনিট প্রশ্নব্যাংক সমাধান", "Previous years DU C-unit solutions", "ঢাবি প্রশ্নব্যাংক বিশ্লেষণ", "Key focus?", "Accuracy and rapid problem solving"))
                )
            )
            "general_knowledge" -> listOf(
                ChapterTemplate("ch_adm_gk_1", 1, "Bangladesh & International Affairs", "বাংলাদেশ ও আন্তর্জাতিক বিষয়াবলি", "4.0 hrs",
                    listOf("Liberation War chronology and constitution", "Global organizations, treaties, geographic trivia"), "Varsity B/D unit preparation",
                    listOf(TopicTemplate("adm_gk_top_1", "Liberation War History", "মুক্তিযুদ্ধের ইতিহাস ও ঘটনাপঞ্জি", "Sector commanders, historic dates, bir shreshtho", "মুক্তিযুদ্ধের সেক্টর ও বীরশ্রেষ্ঠগণ", "How many Bir Shreshtho?", "Seven"))
                )
            )
            "english" -> listOf(
                ChapterTemplate("ch_adm_e_1", 1, "Admission Vocabulary & Grammatical Mastery", "ভর্তি ইংরেজি ও শব্দভাণ্ডার", "4.5 hrs",
                    listOf("Analogy, synonyms, antonyms, idioms", "Pinpoint error identification and sentence correction"), "Speed and precision",
                    listOf(TopicTemplate("adm_e_top_1", "Analogy & Pinpoint Errors", "অ্যানালজি ও পিনপয়েন্ট এরর", "Identifying logical relationships between word pairs", "শব্দজোড় ও ব্যাকরণিক ভুল নির্ণয়", "Example of synonym pair?", "Benevolent : Kind"))
                )
            )
            "bangla" -> listOf(
                ChapterTemplate("ch_adm_b_1", 1, "Varsity B-Unit Bangla", "ভার্সিটি খ-ইউনিট বাংলা", "4.0 hrs",
                    listOf("Textbook prose/poetry in-depth analysis", "Applied grammar, terminology, and literature history"), "DU B-unit focus",
                    listOf(TopicTemplate("adm_b_top_1", "Textbook In-Depth Analysis", "পাঠ্যবইয়ের খুঁটিনাটি ও ব্যাকরণ", "Key characters, citations and grammatical questions", "পাঠ্যবই বিশ্লেষণ", "Focus area?", "Line-by-line understanding of HSC texts"))
                )
            )
            else -> emptyList()
        }
    }
}
