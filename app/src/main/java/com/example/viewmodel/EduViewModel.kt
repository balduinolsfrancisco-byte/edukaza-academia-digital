package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EduRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
  object Home : Screen()
  object MyCourses : Screen()
  object Certificates : Screen()
  object Profile : Screen()
  data class CourseDetail(val courseId: String) : Screen()
  data class LessonPlayer(val courseId: String, val lessonId: String) : Screen()
  data class CertificateDetail(val certificateId: String) : Screen()
  object AdminDashboard : Screen()
  object InstructorDashboard : Screen()
}

class EduViewModel(application: Application) : AndroidViewModel(application) {

  val repository = EduRepository(application)

  val currentUser: StateFlow<UserProfile> = repository.currentUser
  val courses: StateFlow<List<Course>> = repository.courses
  val enrollments: StateFlow<List<Enrollment>> = repository.enrollments
  val certificates: StateFlow<List<Certificate>> = repository.certificates
  val allUsers: StateFlow<List<UserProfile>> = repository.allUsers

  // Navigation state & history stack
  private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
  val currentScreen: StateFlow<Screen> = _screenStack.map { it.lastOrNull() ?: Screen.Home }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Home)

  // Filters & Search
  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  private val _selectedCategory = MutableStateFlow<CourseCategory?>(null)
  val selectedCategory = _selectedCategory.asStateFlow()

  private val _selectedLevel = MutableStateFlow<CourseLevel?>(null)
  val selectedLevel = _selectedLevel.asStateFlow()

  // Filtered courses
  val filteredCourses: StateFlow<List<Course>> = combine(
    courses, _searchQuery, _selectedCategory, _selectedLevel
  ) { list, query, category, level ->
    list.filter { course ->
      val matchesSearch = query.isBlank() ||
          course.title.contains(query, ignoreCase = true) ||
          course.description.contains(query, ignoreCase = true) ||
          course.instructorName.contains(query, ignoreCase = true) ||
          course.code.contains(query, ignoreCase = true)

      val matchesCategory = category == null || course.category == category
      val matchesLevel = level == null || course.level == level

      matchesSearch && matchesCategory && matchesLevel
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Snackbar notifications
  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage = _snackbarMessage.asStateFlow()

  // Certificate Verification
  private val _verificationResult = MutableStateFlow<Certificate?>(null)
  val verificationResult = _verificationResult.asStateFlow()
  private val _isVerifying = MutableStateFlow(false)
  val isVerifying = _isVerifying.asStateFlow()

  fun navigateTo(screen: Screen) {
    _screenStack.value = _screenStack.value + screen
  }

  fun navigateBack(): Boolean {
    val current = _screenStack.value
    return if (current.size > 1) {
      _screenStack.value = current.dropLast(1)
      true
    } else {
      false
    }
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setCategoryFilter(category: CourseCategory?) {
    _selectedCategory.value = category
  }

  fun setLevelFilter(level: CourseLevel?) {
    _selectedLevel.value = level
  }

  fun switchUserRole(role: UserRole) {
    repository.switchUserRole(role)
    val msg = when (role) {
      UserRole.STUDENT -> "Modo Estudante ativado"
      UserRole.INSTRUCTOR -> "Modo Instrutor ativado"
      UserRole.ADMIN -> "Modo Administrador ativado"
    }
    showToast(msg)

    // Route appropriately if needed
    when (role) {
      UserRole.ADMIN -> navigateTo(Screen.AdminDashboard)
      UserRole.INSTRUCTOR -> navigateTo(Screen.InstructorDashboard)
      UserRole.STUDENT -> navigateTo(Screen.Home)
    }
  }

  fun enroll(courseId: String) {
    val success = repository.enrollInCourse(courseId)
    if (success) {
      showToast("Inscrição realizada com sucesso! Bons estudos.")
      // Navigate to first lesson
      val course = courses.value.find { it.id == courseId }
      val firstLesson = course?.lessons?.firstOrNull()
      if (firstLesson != null) {
        navigateTo(Screen.LessonPlayer(courseId, firstLesson.id))
      }
    } else {
      showToast("Já está inscrito neste curso!")
    }
  }

  fun completeLesson(courseId: String, lessonId: String) {
    val updated = repository.completeLesson(courseId, lessonId)
    if (updated != null) {
      if (updated.isCompleted) {
        showToast("Parabéns! Concluiu o curso. O seu Certificado está disponível!")
      } else {
        showToast("Aula concluída com sucesso!")
      }
    }
  }

  fun submitQuiz(courseId: String, lessonId: String, scorePercent: Int) {
    val updated = repository.submitQuizScore(courseId, lessonId, scorePercent)
    if (updated != null) {
      if (scorePercent >= 70) {
        showToast("Aprovado com $scorePercent%! Certificado gerado.")
      } else {
        showToast("Nota: $scorePercent%. Tente novamente para atingir 70%.")
      }
    }
  }

  fun verifyCertificate(code: String) {
    viewModelScope.launch {
      _isVerifying.value = true
      delay(400) // realistic feedback
      val result = repository.verifyCertificateCode(code)
      _verificationResult.value = result
      _isVerifying.value = false
      if (result != null) {
        showToast("Certificado autêntico verificado com sucesso!")
      } else {
        showToast("Código de certificado não encontrado ou inválido.")
      }
    }
  }

  fun clearVerificationResult() {
    _verificationResult.value = null
  }

  fun createNewCourse(
    code: String,
    title: String,
    category: CourseCategory,
    level: CourseLevel,
    hours: Int,
    description: String,
    instructorName: String
  ) {
    repository.createCourse(
      code = code,
      title = title,
      category = category,
      level = level,
      workloadHours = hours,
      description = description,
      instructorName = instructorName
    )
    showToast("Novo curso publicado com sucesso!")
  }

  fun toggleCoursePublish(courseId: String) {
    repository.toggleCoursePublish(courseId)
    showToast("Estado do curso atualizado.")
  }

  fun deleteCourse(courseId: String) {
    repository.deleteCourse(courseId)
    showToast("Curso removido.")
  }

  fun updateUserRole(userId: String, newRole: UserRole) {
    repository.updateUserRole(userId, newRole)
    showToast("Papel de utilizador atualizado.")
  }

  fun getPlatformStats(): PlatformStats = repository.getPlatformStats()

  fun showToast(msg: String) {
    _snackbarMessage.value = msg
  }

  fun clearToast() {
    _snackbarMessage.value = null
  }
}
