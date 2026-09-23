package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SyllabusRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyllabusViewModel : ViewModel() {

    private val repository = SyllabusRepository
    val syllabus: StateFlow<PersonalizedSyllabus> = repository.syllabusFlow

    private val _selectedSubjectId = MutableStateFlow("math")
    val selectedSubjectId: StateFlow<String> = _selectedSubjectId.asStateFlow()

    private val _activeChapter = MutableStateFlow<SyllabusChapter?>(null)
    val activeChapter: StateFlow<SyllabusChapter?> = _activeChapter.asStateFlow()

    private val _activeTopic = MutableStateFlow<SyllabusTopic?>(null)
    val activeTopic: StateFlow<SyllabusTopic?> = _activeTopic.asStateFlow()

    init {
        viewModelScope.launch {
            syllabus.collect { currentSyllabus ->
                if (currentSyllabus.subjects.isNotEmpty() && currentSyllabus.subjects.none { it.id == _selectedSubjectId.value }) {
                    _selectedSubjectId.value = currentSyllabus.subjects.first().id
                }
            }
        }
    }

    fun selectSubject(subjectId: String) {
        _selectedSubjectId.value = subjectId
    }

    fun openChapter(chapter: SyllabusChapter) {
        _activeChapter.value = chapter
    }

    fun openTopic(topic: SyllabusTopic) {
        _activeTopic.value = topic
    }

    fun completeTopic(subjectId: String, chapterId: String, topicId: String) {
        viewModelScope.launch {
            repository.markTopicCompleted(subjectId, chapterId, topicId)
            // Refresh active chapter and topic references
            refreshActiveSelection(chapterId, topicId)
        }
    }

    fun updateProgress(subjectId: String, chapterId: String, topicId: String, percent: Int) {
        viewModelScope.launch {
            repository.updateTopicProgress(subjectId, chapterId, topicId, percent)
            refreshActiveSelection(chapterId, topicId)
        }
    }

    fun submitChapterQuiz(subjectId: String, chapterId: String, scorePercent: Int, wrongQuestions: List<QuizQuestion>) {
        viewModelScope.launch {
            repository.recordQuizResult(subjectId, chapterId, scorePercent, wrongQuestions)
            val updatedSubject = syllabus.value.subjects.find { it.id == subjectId }
            val updatedChapter = updatedSubject?.chapters?.find { it.id == chapterId }
            if (updatedChapter != null) {
                _activeChapter.value = updatedChapter
            }
        }
    }

    fun toggleBookmark(topicId: String, questionId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(topicId, questionId)
            val currentTopic = _activeTopic.value
            if (currentTopic?.id == topicId) {
                val updatedQuestions = currentTopic.practiceQuestions.map {
                    if (it.id == questionId) it.copy(isBookmarked = !it.isBookmarked) else it
                }
                _activeTopic.value = currentTopic.copy(practiceQuestions = updatedQuestions)
            }
        }
    }

    fun addQuestionToMistakes(topicId: String, questionId: String) {
        viewModelScope.launch {
            repository.addQuestionToMistakes(topicId, questionId)
            val currentTopic = _activeTopic.value
            if (currentTopic?.id == topicId) {
                val updatedQuestions = currentTopic.practiceQuestions.map {
                    if (it.id == questionId) it.copy(isAddedToMistakes = true) else it
                }
                _activeTopic.value = currentTopic.copy(practiceQuestions = updatedQuestions)
            }
        }
    }

    fun createAiContext(topic: SyllabusTopic, chapter: SyllabusChapter, subject: SyllabusSubject): TopicAiContext {
        val s = syllabus.value
        return TopicAiContext(
            educationLevel = s.educationLevel,
            curriculum = s.curriculum,
            subjectId = subject.id,
            subjectName = subject.name,
            chapterId = chapter.id,
            chapterTitle = chapter.title,
            topicId = topic.id,
            topicTitle = topic.title,
            topicTitleBn = topic.titleBn,
            language = s.language,
            groupId = subject.groupId.ifEmpty { s.groupId },
            group = if (subject.groupId.isNotEmpty()) subject.group else s.group,
            paperId = subject.paperId,
            paper = subject.paper,
            curriculumId = subject.curriculumId.ifEmpty { s.curriculumId }
        )
    }

    private fun refreshActiveSelection(chapterId: String, topicId: String) {
        val s = syllabus.value
        val ch = s.subjects.flatMap { it.chapters }.find { it.id == chapterId }
        if (ch != null) {
            _activeChapter.value = ch
            val tp = ch.topics.find { it.id == topicId }
            if (tp != null) {
                _activeTopic.value = tp
            }
        }
    }
}
