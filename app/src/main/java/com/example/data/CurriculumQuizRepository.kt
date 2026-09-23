package com.example.data

import com.example.model.AcademicGroup
import com.example.model.QuestionDifficulty
import com.example.model.QuizQuestion
import com.example.model.SubjectPaper
import com.example.model.SyllabusChapter

object CurriculumQuizRepository {

    const val CURRICULUM_NCTB_BN = "nctb_bn"
    const val CURRICULUM_NCTB_EN = "nctb_en"
    const val CURRICULUM_CAMBRIDGE_EDEXCEL = "cambridge_edexcel"
    const val CURRICULUM_ADMISSION = "admission"

    val supportedCurricula = listOf(
        "NCTB (Bangla Version)" to CURRICULUM_NCTB_BN,
        "NCTB (English Version)" to CURRICULUM_NCTB_EN,
        "English Medium (Cambridge / Edexcel)" to CURRICULUM_CAMBRIDGE_EDEXCEL,
        "Admission (Engineering / Medical / Varsity)" to CURRICULUM_ADMISSION
    )

    fun mapToCurriculumId(input: String): String {
        val lower = input.lowercase().trim()
        return when {
            lower.contains("bangla") || lower == CURRICULUM_NCTB_BN -> CURRICULUM_NCTB_BN
            lower.contains("english version") || lower == CURRICULUM_NCTB_EN -> CURRICULUM_NCTB_EN
            lower.contains("cambridge") || lower.contains("edexcel") || lower.contains("medium") || lower == CURRICULUM_CAMBRIDGE_EDEXCEL -> CURRICULUM_CAMBRIDGE_EDEXCEL
            lower.contains("admission") || lower == CURRICULUM_ADMISSION -> CURRICULUM_ADMISSION
            else -> CURRICULUM_NCTB_BN
        }
    }

    val questions: List<QuizQuestion> = listOf(
        // ==========================================
        // 1. NCTB (BANGLA VERSION) - MATH
        // ==========================================
        QuizQuestion(
            id = "bn_m_1",
            subjectId = "math",
            topic = "দ্বিঘাত সমীকরণ",
            questionText = "2x² - 8 = 0 দ্বিঘাত সমীকরণটির মূলদ্বয় কোনটি?",
            options = listOf("x = 2, -2", "x = 4, -4", "x = 2, 0", "x = 1, -1"),
            correctIndex = 0,
            explanation = "2x² = 8 বা x² = 4, সুতরাং x = ±2। উভয় মূলই বাস্তব ও অসমান।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_m_1",
            topicId = "m_quad_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_m_2",
            subjectId = "math",
            topic = "দ্বিঘাত সমীকরণ",
            questionText = "ax² + bx + c = 0 সমীকরণের নিশ্চায়ক (Discriminant) কোনটি?",
            options = listOf("b² + 4ac", "b² - 4ac", "2b - 4ac", "b - 4a²c"),
            correctIndex = 1,
            explanation = "দ্বিঘাত সমীকরণের নিশ্চায়ক D = b² - 4ac। D > 0 হলে মূলগুলো বাস্তব ও অসমান হয়।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_m_1",
            topicId = "m_quad_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_m_3",
            subjectId = "math",
            topic = "দ্বিঘাত সমীকরণ",
            questionText = "x² - 5x + 6 = 0 সমীকরণের মূলদ্বয়ের গুণফল কত?",
            options = listOf("5", "-5", "6", "-6"),
            correctIndex = 2,
            explanation = "মূলদ্বয়ের গুণফল = c / a = 6 / 1 = 6 এবং যোগফল = -b / a = 5।",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_m_1",
            topicId = "m_quad_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_m_4",
            subjectId = "math",
            topic = "স্থানাঙ্ক জ্যামিতি",
            questionText = "(2, 3) এবং (5, 7) বিন্দু দুইটির মধ্যবর্তী দূরত্ব কত একক?",
            options = listOf("3 একক", "4 একক", "5 একক", "6 একক"),
            correctIndex = 2,
            explanation = "দূরত্ব d = √[(5 - 2)² + (7 - 3)²] = √[3² + 4²] = √25 = 5 একক।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_m_2",
            topicId = "m_coord_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_m_5",
            subjectId = "math",
            topic = "স্থানাঙ্ক জ্যামিতি",
            questionText = "যে সরলরেখার সমীকরণ y = 3x + 5, তার ঢাল (Slope) কত?",
            options = listOf("5", "3", "-3", "1/3"),
            correctIndex = 1,
            explanation = "সরলরেখার আদর্শ সমীকরণ y = mx + c এর সাথে তুলনা করলে ঢাল m = 3।",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_m_2",
            topicId = "m_coord_2",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_m_6",
            subjectId = "math",
            topic = "পাটিগণিত ও বীজগণিত",
            questionText = "একটি সংখ্যার ৪ গুণ থেকে ৫ বিয়োগ করলে ফল ১৯ হয়। সংখ্যাটি কত?",
            options = listOf("4", "5", "6", "7"),
            correctIndex = 2,
            explanation = "ধরি সংখ্যাটি x। প্রশ্নমতে, 4x - 5 = 19 => 4x = 24 => x = 6।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_m_1",
            topicId = "m_quad_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 8"
        ),
        QuizQuestion(
            id = "bn_m_7",
            subjectId = "math",
            topic = "অনুপাত ও শতকরা",
            questionText = "১০০ টাকার ২৫% এর সাথে ৫০ যোগ করলে কত টাকা হবে?",
            options = listOf("২৫ টাকা", "৫০ টাকা", "৭৫ টাকা", "১০০ টাকা"),
            correctIndex = 2,
            explanation = "১০০ টাকার ২৫% = ২৫ টাকা। সুতরাং ২৫ + ৫০ = ৭৫ টাকা।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_m_1",
            topicId = "m_quad_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 6"
        ),

        // ==========================================
        // 1. NCTB (BANGLA VERSION) - PHYSICS
        // ==========================================
        QuizQuestion(
            id = "bn_p_1",
            subjectId = "physics",
            topic = "নিউটনীয় বলবিদ্যা",
            questionText = "নিউটনের গতির কোন সূত্র থেকে বলের পরিমাপ ও সমীকরণ (F = ma) পাওয়া যায়?",
            options = listOf("প্রথম সূত্র", "দ্বিতীয় সূত্র", "তৃতীয় সূত্র", "মহাকর্ষ সূত্র"),
            correctIndex = 1,
            explanation = "নিউটনের গতির দ্বিতীয় সূত্রানুসারে বস্তুর ভরবেগের পরিবর্তনের হার তার উপর প্রযুক্ত বলের সমানুপাতিক (F = ma)।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_p_1",
            topicId = "p_motion_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_p_2",
            subjectId = "physics",
            topic = "নিউটনীয় বলবিদ্যা",
            questionText = "৫ কেজি ভরের একটি বস্তুর উপর ১০ নিউটন বল প্রয়োগ করলে ত্বরণ কত হবে?",
            options = listOf("0.5 m/s²", "2 m/s²", "5 m/s²", "50 m/s²"),
            correctIndex = 1,
            explanation = "ত্বরণ a = F / m = 10 N / 5 kg = 2 m/s²।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_p_1",
            topicId = "p_motion_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_p_3",
            subjectId = "physics",
            topic = "কাজ, শক্তি ও ক্ষমতা",
            questionText = "একটি বস্তুর বেগ দ্বিগুণ করা হলে তার গতিশক্তি পূর্বের কত গুণ হবে?",
            options = listOf("দ্বিগুণ", "তিনগুণ", "চারগুণ", "অর্ধেক"),
            correctIndex = 2,
            explanation = "গতিশক্তি Ek = 1/2 mv²। বেগ v দ্বিগুণ হলে গতিশক্তি (2)² = 4 গুণ হবে।",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_p_2",
            topicId = "p_work_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_p_4",
            subjectId = "physics",
            topic = "কাজ, শক্তি ও ক্ষমতা",
            questionText = "কাজের এস.আই (S.I) একক কোনটি?",
            options = listOf("ওয়াট (Watt)", "জুল (Joule)", "নিউটন (Newton)", "প্যাসকেল (Pascal)"),
            correctIndex = 1,
            explanation = "কাজের একক জুল (J)। বল ও বলের দিকে সরণের গুণফলই হলো কাজ।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_p_2",
            topicId = "p_work_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 1. NCTB (BANGLA VERSION) - CHEMISTRY
        // ==========================================
        QuizQuestion(
            id = "bn_c_1",
            subjectId = "chemistry",
            topic = "পর্যায় সারণি",
            questionText = "আধুনিক পর্যায় সারণিতে মৌলসমূহকে কিসের ভিত্তিতে সাজানো হয়েছে?",
            options = listOf("পারমাণবিক ভর", "পারমাণবিক সংখ্যা", "ভর সংখ্যা", "যোজ্যতা"),
            correctIndex = 1,
            explanation = "মোজলের সূত্র অনুযায়ী আধুনিক পর্যায় সারণিতে মৌলসমূহকে তাদের পারমাণবিক সংখ্যার ক্রমানুসারে সাজানো হয়েছে।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_c_1",
            topicId = "c_matter_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_c_2",
            subjectId = "chemistry",
            topic = "রাসায়নিক বন্ধন",
            questionText = "সোডিয়াম ক্লোরাইড (NaCl) যৌগে কোন ধরনের বন্ধন বিদ্যমান?",
            options = listOf("সমযোজী বন্ধন", "আয়নিক বন্ধন", "ধাতব বন্ধন", "হাইড্রোজেন বন্ধন"),
            correctIndex = 1,
            explanation = "ধাতু Na ইলেকট্রন ত্যাগ করে Na⁺ এবং অধাতু Cl ইলেকট্রন গ্রহণ করে Cl⁻ তৈরি করে স্থির তড়িৎ আকর্ষণে আয়নিক বন্ধন গঠন করে।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_c_2",
            topicId = "c_matter_2",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_c_3",
            subjectId = "chemistry",
            topic = "পর্যায় সারণি",
            questionText = "পর্যায় সারণির গ্রুপ-১ এর মৌলসমূহকে কী বলা হয়?",
            options = listOf("ক্ষার ধাতু", "মৃৎক্ষার ধাতু", "হ্যালোজেন", "নিষ্ক্রিয় গ্যাস"),
            correctIndex = 0,
            explanation = "গ্রুপ-১ এর মৌলসমূহকে (যেমন Li, Na, K) ক্ষার ধাতু বলা হয় কারণ এরা পানির সাথে বিক্রিয়া করে তীব্র ক্ষার তৈরি করে।",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_c_1",
            topicId = "c_matter_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 1. NCTB (BANGLA VERSION) - ENGLISH
        // ==========================================
        QuizQuestion(
            id = "bn_e_1",
            subjectId = "english",
            topic = "ক্রিয়ার সঠিক রূপ",
            questionText = "বাক্যটিতে ক্রিয়ার সঠিক রূপ নির্বাচন করো: 'The train had left before we ___ the station.'",
            options = listOf("reach", "reached", "had reached", "reaching"),
            correctIndex = 1,
            explanation = "Past Perfect Tense-এ 'before' যুক্ত ক্লজের পূর্বের অংশে had + V3 এবং পরের অংশে Past Simple (reached) বসে।",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_e_1",
            topicId = "e_verbs_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_e_2",
            subjectId = "english",
            topic = "বাক্য রূপান্তর",
            questionText = "'He is too weak to walk.' - বাক্যটির সঠিক Complex রূপ কোনটি?",
            options = listOf(
                "He is so weak that he cannot walk.",
                "He is very weak and he cannot walk.",
                "He is weak enough to walk.",
                "He cannot walk because of weakness."
            ),
            correctIndex = 0,
            explanation = "'too...to' যুক্ত Simple বাক্যকে Complex করতে 'so...that + subject + cannot/could not + verb' নিয়ম অনুসরণ করা হয়।",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_e_2",
            topicId = "e_verbs_2",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 2. NCTB (ENGLISH VERSION) - MATH
        // ==========================================
        QuizQuestion(
            id = "en_m_1",
            subjectId = "math",
            topic = "Quadratic Equations",
            questionText = "What are the roots of the equation 3x² - 12 = 0 in NCTB English Version syllabus?",
            options = listOf("x = ±2", "x = ±4", "x = 2 only", "x = 0"),
            correctIndex = 0,
            explanation = "3x² = 12 implies x² = 4, thus x = ±2. Both roots are real and unequal.",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_m_1",
            topicId = "m_quad_1",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "en_m_2",
            subjectId = "math",
            topic = "Coordinate Geometry",
            questionText = "Find the slope of the line passing through (1, 2) and (3, 8).",
            options = listOf("2", "3", "4", "6"),
            correctIndex = 1,
            explanation = "Slope m = (y₂ - y₁) / (x₂ - x₁) = (8 - 2) / (3 - 1) = 6 / 2 = 3.",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_m_2",
            topicId = "m_coord_1",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "en_m_3",
            subjectId = "math",
            topic = "Quadratic Equations",
            questionText = "For what value of k does x² + kx + 9 = 0 have equal real roots?",
            options = listOf("±3", "±6", "±9", "±18"),
            correctIndex = 1,
            explanation = "For equal roots, discriminant D = b² - 4ac = 0. Here k² - 4(1)(9) = 0 => k² = 36 => k = ±6.",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_m_1",
            topicId = "m_quad_2",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 2. NCTB (ENGLISH VERSION) - PHYSICS
        // ==========================================
        QuizQuestion(
            id = "en_p_1",
            subjectId = "physics",
            topic = "Newtonian Mechanics",
            questionText = "According to Newton's Second Law of Motion, what is the equation defining force?",
            options = listOf("F = m/a", "F = ma", "F = mv", "F = 1/2 ma²"),
            correctIndex = 1,
            explanation = "The rate of change of momentum is proportional to applied force, yielding F = ma.",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_p_1",
            topicId = "p_motion_1",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "en_p_2",
            subjectId = "physics",
            topic = "Work, Energy & Power",
            questionText = "A force of 20 N displaces an object by 3 m in its direction. What is the work done?",
            options = listOf("20 J", "40 J", "60 J", "80 J"),
            correctIndex = 2,
            explanation = "Work W = F * s = 20 N * 3 m = 60 Joules.",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_p_2",
            topicId = "p_work_1",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 2. NCTB (ENGLISH VERSION) - CHEMISTRY
        // ==========================================
        QuizQuestion(
            id = "en_c_1",
            subjectId = "chemistry",
            topic = "Periodic Table",
            questionText = "How does atomic radius generally vary across a period from left to right?",
            options = listOf("Increases", "Decreases", "Remains constant", "First decreases then increases"),
            correctIndex = 1,
            explanation = "Effective nuclear charge increases across a period pulling electrons closer, reducing atomic radius.",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_c_1",
            topicId = "c_matter_1",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "en_c_2",
            subjectId = "chemistry",
            topic = "Chemical Bonding",
            questionText = "Which type of chemical bond involves the sharing of electron pairs?",
            options = listOf("Ionic bond", "Covalent bond", "Metallic bond", "Coordinate bond"),
            correctIndex = 1,
            explanation = "Covalent bonding involves the mutual sharing of valence electrons between non-metal atoms.",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_c_2",
            topicId = "c_matter_2",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 2. NCTB (ENGLISH VERSION) - ENGLISH
        // ==========================================
        QuizQuestion(
            id = "en_e_1",
            subjectId = "english",
            topic = "Right Form of Verbs",
            questionText = "Neither of the two students ___ present in the auditorium yesterday.",
            options = listOf("was", "were", "are", "have been"),
            correctIndex = 0,
            explanation = "'Neither of' takes a singular verb. Since it refers to yesterday, the past singular 'was' is correct.",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_e_1",
            topicId = "e_verbs_1",
            curriculumId = CURRICULUM_NCTB_EN,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 3. ENGLISH MEDIUM (CAMBRIDGE / EDEXCEL)
        // ==========================================
        QuizQuestion(
            id = "cam_m_1",
            subjectId = "math",
            topic = "Quadratics & Functions",
            questionText = "Solve the quadratic equation x² - 7x + 10 = 0 using factorisation (Cambridge IGCSE syllabus 0580).",
            options = listOf("x = 2 or x = 5", "x = -2 or x = -5", "x = 1 or x = 10", "x = -1 or x = -10"),
            correctIndex = 0,
            explanation = "(x - 2)(x - 5) = 0, giving linear factors with roots x = 2 and x = 5.",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_m_1",
            topicId = "m_quad_1",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "cam_m_2",
            subjectId = "math",
            topic = "Coordinate Geometry",
            questionText = "Determine the gradient of the line perpendicular to the line y = -2x + 7.",
            options = listOf("-2", "2", "1/2", "-1/2"),
            correctIndex = 2,
            explanation = "Perpendicular gradients satisfy m₁ * m₂ = -1. Since m₁ = -2, m₂ = -1 / (-2) = +1/2.",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_m_2",
            topicId = "m_coord_1",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "cam_p_1",
            subjectId = "physics",
            topic = "Forces & Motion",
            questionText = "An aircraft of mass 40,000 kg accelerates uniformly at 2.5 m/s². Calculate the resultant forward thrust required (Cambridge 0625).",
            options = listOf("16,000 N", "100,000 N", "160,000 N", "1,000,000 N"),
            correctIndex = 1,
            explanation = "Resultant Force F = m * a = 40,000 kg * 2.5 m/s² = 100,000 N (or 100 kN).",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_p_1",
            topicId = "p_motion_1",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "cam_p_2",
            subjectId = "physics",
            topic = "Work & Power",
            questionText = "A crane lifts a 500 kg crate through a vertical height of 12 m in 10 s. Taking g = 9.8 m/s², what is the useful output power?",
            options = listOf("5,880 W", "6,000 W", "58,800 W", "588 W"),
            correctIndex = 0,
            explanation = "Work = mgh = 500 * 9.8 * 12 = 58,800 J. Power = Work / time = 58,800 / 10 = 5,880 W.",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_p_2",
            topicId = "p_work_1",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "cam_c_1",
            subjectId = "chemistry",
            topic = "Periodic Trends",
            questionText = "Which Group VII halogen has the highest first electronegativity value in the Cambridge IGCSE periodic table?",
            options = listOf("Chlorine", "Fluorine", "Bromine", "Iodine"),
            correctIndex = 1,
            explanation = "Fluorine has the smallest atomic radius and highest effective nuclear attraction for bonding electrons (Paulings 4.0).",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_c_1",
            topicId = "c_matter_1",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "cam_e_1",
            subjectId = "english",
            topic = "Grammar & Syntax",
            questionText = "Identify the correct conditional structure: 'If the catalyst had been added sooner, the reaction ___ faster.'",
            options = listOf("would occur", "would have occurred", "will have occurred", "had occurred"),
            correctIndex = 1,
            explanation = "Third conditional sentences require: If + past perfect (had been added), main clause with would have + past participle (would have occurred).",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_e_1",
            topicId = "e_verbs_1",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),

        // ==========================================
        // 4. ADMISSION (ENGINEERING / MEDICAL / VARSITY)
        // ==========================================
        QuizQuestion(
            id = "adm_m_1",
            subjectId = "math",
            topic = "Quadratic Equations & Roots",
            questionText = "If α and β are the roots of 2x² - 3x + 1 = 0, evaluate (α/β + β/α) [BUET Admission Pattern].",
            options = listOf("5/2", "9/4", "5/4", "13/4"),
            correctIndex = 0,
            explanation = "α + β = 3/2 and αβ = 1/2. α/β + β/α = (α² + β²) / (αβ) = [(α + β)² - 2αβ] / (αβ) = [(9/4) - 1] / (1/2) = (5/4) / (1/2) = 5/2.",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_m_1",
            topicId = "m_quad_2",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "adm_m_2",
            subjectId = "math",
            topic = "Coordinate Geometry",
            questionText = "Find the perpendicular distance from the point (3, -2) to the straight line 4x - 3y + 2 = 0 [DU 'Ka' Admission].",
            options = listOf("2 units", "3 units", "4 units", "5 units"),
            correctIndex = 2,
            explanation = "d = |4(3) - 3(-2) + 2| / √(4² + (-3)²) = |12 + 6 + 2| / 5 = 20 / 5 = 4 units.",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_m_2",
            topicId = "m_coord_1",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "adm_p_1",
            subjectId = "physics",
            topic = "Newtonian Mechanics",
            questionText = "A bullet of mass 10 g is fired into a ballistic pendulum of mass 990 g at rest. If the system rises to height 5 cm, what was the muzzle velocity? (g = 9.8 m/s²) [Engineering Admission].",
            options = listOf("99 m/s", "100 m/s", "98 m/s", "105 m/s"),
            correctIndex = 0,
            explanation = "Combined velocity V = √(2gh) = √(2 * 9.8 * 0.05) = √0.98 ≈ 0.99 m/s. By momentum conservation: m*v = (m+M)*V => 0.01*v = 1.0 * 0.99 => v = 99 m/s.",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_p_1",
            topicId = "p_motion_1",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "adm_p_2",
            subjectId = "physics",
            topic = "Work, Energy & Power",
            questionText = "Under a variable force F(x) = (3x² + 2x) N, an object moves from x = 1 m to x = 3 m. Calculate the total work done [Admission Test MCQ].",
            options = listOf("26 J", "32 J", "34 J", "40 J"),
            correctIndex = 2,
            explanation = "W = ∫[1 to 3] (3x² + 2x) dx = [x³ + x²][1 to 3] = (27 + 9) - (1 + 1) = 36 - 2 = 34 Joules.",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_p_2",
            topicId = "p_work_1",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "adm_c_1",
            subjectId = "chemistry",
            topic = "Chemical Bonding & Hybridization",
            questionText = "What is the hybridization and geometric shape of the central atom in SF₆? [Medical / Varsity Admission].",
            options = listOf("sp³d, Trigonal bipyramidal", "sp³d², Octahedral", "sp³d², Square planar", "sp³d³, Pentagonal"),
            correctIndex = 1,
            explanation = "Sulfur in SF₆ has 6 bonding pairs and 0 lone pairs. Hybridization is sp³d² with an octahedral regular geometry.",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_c_2",
            topicId = "c_matter_2",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "adm_e_1",
            subjectId = "english",
            topic = "Advanced Vocabulary & Grammar",
            questionText = "Choose the synonymous pair representing 'ephemeral' in varsity admission English comprehension:",
            options = listOf("Permanent : Enduring", "Transient : Fleeting", "Spurious : Authentic", "Lucid : Obscure"),
            correctIndex = 1,
            explanation = "'Ephemeral' means lasting for a very short time. 'Transient' and 'Fleeting' are exact synonyms.",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_e_1",
            topicId = "e_verbs_1",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "adm_c_2",
            subjectId = "chemistry",
            topic = "Periodic Trends & Acid-Base",
            questionText = "Which of the following oxoacids of chlorine exhibits the highest acidic strength? [BUET / Medical MCQ]",
            options = listOf("HClO", "HClO₂", "HClO₃", "HClO₄"),
            correctIndex = 3,
            explanation = "In HClO₄, chlorine has its highest oxidation state (+7). The conjugate base ClO₄⁻ is stabilized by resonance across four oxygen atoms.",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_c_1",
            topicId = "c_matter_1",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "adm_e_2",
            subjectId = "english",
            topic = "Inversion & Sentence Structure",
            questionText = "Complete the inverted sentence: 'Rarely ___ such exquisite artistic precision.' [DU 'Kha' / 'Ga' Admission]",
            options = listOf("we saw", "did we see", "have we seen", "we have seen"),
            correctIndex = 2,
            explanation = "Negative adverbs like 'Rarely' at the beginning of a clause trigger subject-auxiliary inversion (Rarely + have + subject + V3).",
            difficulty = QuestionDifficulty.HARD,
            chapterId = "ch_e_2",
            topicId = "e_verbs_2",
            curriculumId = CURRICULUM_ADMISSION,
            className = "Class 12 (HSC)"
        ),
        QuizQuestion(
            id = "cam_c_2",
            subjectId = "chemistry",
            topic = "Covalent Bonding & Structure",
            questionText = "How many shared bonding pairs and non-bonding lone pairs surround the nitrogen atom in ammonia (NH₃) in Cambridge 0620 syllabus?",
            options = listOf("3 bonding pairs, 1 lone pair", "2 bonding pairs, 2 lone pairs", "4 bonding pairs, 0 lone pairs", "3 bonding pairs, 2 lone pairs"),
            correctIndex = 0,
            explanation = "Nitrogen has 5 valence electrons. It shares 3 electrons with 3 hydrogen atoms (3 bonding pairs) and retains 1 lone pair.",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_c_2",
            topicId = "c_matter_2",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "cam_e_2",
            subjectId = "english",
            topic = "Reported Speech & Syntax",
            questionText = "Select the correct indirect speech conversion: 'I will finish the Cambridge physics paper tomorrow,' said Tahsin.",
            options = listOf(
                "Tahsin said that he would finish the Cambridge physics paper the following day.",
                "Tahsin said that he will finish the Cambridge physics paper tomorrow.",
                "Tahsin told that he would finish the Cambridge physics paper tomorrow.",
                "Tahsin said he finishes the Cambridge physics paper the following day."
            ),
            correctIndex = 0,
            explanation = "In indirect speech with past reporting verb: 'will' changes to 'would', and 'tomorrow' shifts to 'the following day'.",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_e_2",
            topicId = "e_verbs_2",
            curriculumId = CURRICULUM_CAMBRIDGE_EDEXCEL,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_c_4",
            subjectId = "chemistry",
            topic = "রাসায়নিক বন্ধন ও আন্তঃআণবিক বল",
            questionText = "পানিতে (H₂O) অণুসমূহের মধ্যে সমযোজী বন্ধন ছাড়াও কোন বিশেষ আন্তঃআণবিক আকর্ষণ বল বিদ্যমান?",
            options = listOf("ভ্যান্ডার ওয়ালস বল", "হাইড্রোজেন বন্ধন", "আয়নিক আকর্ষণ", "ধাতব বন্ধন"),
            correctIndex = 1,
            explanation = "অক্সিজেন অধিক তড়িৎঋণাত্মক হওয়ায় পানির অণুগুলোর মধ্যে হাইড্রোজেন বন্ধন গঠিত হয়, যার কারণে পানির স্ফুটনাঙ্ক স্বাভাবিকের চেয়ে বেশি (১০০° সেলসিয়াস)।",
            difficulty = QuestionDifficulty.MEDIUM,
            chapterId = "ch_c_2",
            topicId = "c_matter_2",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        ),
        QuizQuestion(
            id = "bn_p_5",
            subjectId = "physics",
            topic = "কাজ, শক্তি ও ক্ষমতা",
            questionText = "৫০ কেজি ভরের একটি বস্তুকে মাটি থেকে ১০ মিটার উচ্চতায় উঠালে এর মধ্যে কত স্থিতি শক্তি সঞ্চিত হবে? (g = 9.8 m/s²)",
            options = listOf("৪৯০ জুল", "৪৯০০ জুল", "৫০০ জুল", "৯৮০ জুল"),
            correctIndex = 1,
            explanation = "স্থিতিশক্তি Ep = mgh = 50 kg * 9.8 m/s² * 10 m = 4900 জুল (J)।",
            difficulty = QuestionDifficulty.EASY,
            chapterId = "ch_p_2",
            topicId = "p_work_1",
            curriculumId = CURRICULUM_NCTB_BN,
            className = "Class 10 (SSC)"
        )
    )

    fun resolveQuizQuestions(
        selectedClass: String,
        selectedCurriculum: String,
        targetSubjectId: String,
        chapterId: String = "",
        topicId: String? = null,
        difficulty: QuestionDifficulty? = null,
        maxCount: Int = 10,
        selectedChapter: SyllabusChapter? = null,
        selectedGroup: AcademicGroup = AcademicGroup.SCIENCE,
        selectedPaper: SubjectPaper = SubjectPaper.NONE
    ): List<QuizQuestion> {
        val sClassLower = selectedClass.lowercase().trim()
        val sSubjLower = targetSubjectId.lowercase().trim()
        val isJunior = AcademicCatalog.isJuniorClass(selectedClass)

        // 1. Cross-stream / Cross-class safety checks:
        // Junior Class (6-8) cannot receive Class 9-12 group subjects
        val secondaryGroupSubjectIds = setOf(
            "physics", "chemistry", "biology", "higher_math", "higher_math_1", "higher_math_2",
            "accounting", "accounting_1", "accounting_2", "business_entrepreneurship",
            "bom_1", "bom_2", "finance_banking", "fbi_1", "fbi_2", "pmm_1", "pmm_2",
            "history", "history_1", "history_2", "geography", "economics", "economics_1", "economics_2",
            "civics", "civics_1", "civics_2", "logic", "sociology", "social_work"
        )
        if (isJunior && sSubjLower in secondaryGroupSubjectIds) {
            return emptyList()
        }

        // Science group cannot receive Business Studies or Humanities subjects
        val businessSubjectIds = setOf(
            "accounting", "accounting_1", "accounting_2", "business_entrepreneurship",
            "bom_1", "bom_2", "finance_banking", "fbi_1", "fbi_2", "pmm_1", "pmm_2",
            "business_management", "business_studies"
        )
        val humanitiesSubjectIds = setOf(
            "history", "history_1", "history_2", "geography", "economics", "economics_1", "economics_2",
            "civics", "civics_1", "civics_2", "logic", "sociology", "social_work", "home_science", "agriculture"
        )
        val scienceSubjectIds = setOf(
            "physics", "physics_1", "physics_2", "chemistry", "chemistry_1", "chemistry_2",
            "biology", "biology_1", "biology_2", "higher_math", "higher_math_1", "higher_math_2"
        )

        if (!isJunior) {
            if (selectedGroup == AcademicGroup.SCIENCE && (sSubjLower in businessSubjectIds || sSubjLower in humanitiesSubjectIds)) {
                return emptyList()
            }
            if (selectedGroup == AcademicGroup.BUSINESS_STUDIES && (sSubjLower in scienceSubjectIds || sSubjLower in humanitiesSubjectIds)) {
                return emptyList()
            }
            if (selectedGroup == AcademicGroup.HUMANITIES && (sSubjLower in scienceSubjectIds || sSubjLower in businessSubjectIds)) {
                return emptyList()
            }
        }

        // 2. Strict Subject Matching (Never mix unrelated subjects)
        val subjectPool = questions.filter { q ->
            q.subjectId.equals(sSubjLower, ignoreCase = true)
        }
        if (subjectPool.isEmpty()) {
            val chapters = if (selectedChapter != null) listOf(selectedChapter) else {
                AcademicContentCatalog.getChapters(
                    educationLevel = selectedClass,
                    curriculum = selectedCurriculum,
                    group = selectedGroup,
                    subjectId = targetSubjectId,
                    paper = selectedPaper
                )
            }
            val targetChapter = if (chapterId.isNotEmpty()) chapters.firstOrNull { it.id.equals(chapterId, ignoreCase = true) } else chapters.firstOrNull()
            val availableChs = if (targetChapter != null) listOf(targetChapter) else chapters
            var chapterMatching = availableChs.flatMap { ch ->
                ch.topics.filter { topicId.isNullOrEmpty() || it.id.equals(topicId, ignoreCase = true) }
                    .flatMap { it.quickQuizQuestions }
            }.filter { it.subjectId.equals(sSubjLower, ignoreCase = true) }
            if (difficulty != null) {
                val diffMatches = chapterMatching.filter { it.difficulty == difficulty }
                if (diffMatches.isNotEmpty()) chapterMatching = diffMatches
            }
            return chapterMatching.distinctBy { it.id }.shuffled().take(maxCount)
        }

        // 3. Curriculum Matching
        val targetCurriculumId = mapToCurriculumId(selectedCurriculum)
        val curriculumPool = subjectPool.filter { q ->
            q.curriculumId.isEmpty() || q.curriculumId.equals(targetCurriculumId, ignoreCase = true)
        }
        val poolAfterCurriculum = if (curriculumPool.isNotEmpty()) curriculumPool else subjectPool

        // 4. Class Matching
        val classPool = poolAfterCurriculum.filter { q ->
            if (q.className.isEmpty()) {
                true
            } else {
                val qClass = q.className.lowercase().trim()
                when {
                    isJunior -> {
                        if (sClassLower.contains("class 6")) qClass.contains("class 6")
                        else if (sClassLower.contains("class 7")) qClass.contains("class 7")
                        else if (sClassLower.contains("class 8")) qClass.contains("class 8")
                        else false
                    }
                    AcademicCatalog.isSecondaryClass(sClassLower) -> {
                        (qClass.contains("class 9") || qClass.contains("class 10") || qClass.contains("ssc")) &&
                        !qClass.contains("class 6") && !qClass.contains("class 7") && !qClass.contains("class 8")
                    }
                    AcademicCatalog.isHigherSecondaryClass(sClassLower) -> {
                        (qClass.contains("class 11") || qClass.contains("class 12") || qClass.contains("hsc")) &&
                        !qClass.contains("class 6") && !qClass.contains("class 7") && !qClass.contains("class 8") && !qClass.contains("class 10")
                    }
                    AcademicCatalog.isAdmission(sClassLower, selectedCurriculum) -> {
                        qClass.contains("admission") || qClass.contains("class 12") || qClass.contains("hsc")
                    }
                    else -> sClassLower.contains(qClass) || qClass.contains(sClassLower)
                }
            }
        }
        val poolAfterClass = if (classPool.isNotEmpty()) classPool else {
            poolAfterCurriculum.filter { q ->
                if (isJunior) {
                    !q.className.lowercase().contains("class 10") && !q.className.lowercase().contains("ssc") && !q.className.lowercase().contains("hsc")
                } else {
                    !q.className.lowercase().contains("class 6") && !q.className.lowercase().contains("class 7") && !q.className.lowercase().contains("class 8")
                }
            }
        }

        // 5. Group Matching
        val poolAfterGroup = poolAfterClass.filter { q ->
            q.groupId.isEmpty() ||
            q.groupId == AcademicGroup.GENERAL_JUNIOR.id ||
            q.groupId.equals(selectedGroup.id, ignoreCase = true)
        }
        val poolForGroup = if (poolAfterGroup.isNotEmpty()) poolAfterGroup else poolAfterClass

        // 6. Paper Matching
        val poolAfterPaper = if (selectedPaper != SubjectPaper.NONE) {
            val paperMatches = poolForGroup.filter { q ->
                q.paperId.isEmpty() || q.paperId == SubjectPaper.NONE.id || q.paperId.equals(selectedPaper.id, ignoreCase = true)
            }
            if (paperMatches.isNotEmpty()) paperMatches else poolForGroup
        } else {
            poolForGroup
        }

        // 7. Chapter and Topic Matching
        val poolAfterChapter = if (chapterId.isNotEmpty()) {
            val chMatches = poolAfterPaper.filter { it.chapterId.equals(chapterId, ignoreCase = true) }
            if (chMatches.isNotEmpty()) chMatches else poolAfterPaper
        } else {
            poolAfterPaper
        }

        val poolAfterTopic = if (!topicId.isNullOrEmpty()) {
            val topMatches = poolAfterChapter.filter { it.topicId.equals(topicId, ignoreCase = true) }
            if (topMatches.isNotEmpty()) topMatches else poolAfterChapter
        } else {
            poolAfterChapter
        }

        // 8. Difficulty filtering
        val poolAfterDifficulty = if (difficulty != null) {
            val diffMatches = poolAfterTopic.filter { it.difficulty == difficulty }
            if (diffMatches.isNotEmpty()) diffMatches else poolAfterTopic
        } else {
            poolAfterTopic
        }

        // 9. Chapter Practice/Quick Quiz augmentation
        val combined = poolAfterDifficulty.toMutableList()
        if (selectedChapter != null && combined.size < maxCount) {
            val chapterQuizItems = selectedChapter.topics
                .filter { topicId.isNullOrEmpty() || it.id.equals(topicId, ignoreCase = true) }
                .flatMap { it.quickQuizQuestions }
                .filter { it.subjectId.equals(sSubjLower, ignoreCase = true) }
            combined.addAll(chapterQuizItems)
        }

        return combined
            .distinctBy { it.id }
            .shuffled()
            .take(maxCount)
    }
}
