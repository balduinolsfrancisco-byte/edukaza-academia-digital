package com.example.model

enum class UserRole(val label: String, val badgeColorHex: Long) {
  STUDENT("Estudante", 0xFF0284C7),
  INSTRUCTOR("Instrutor", 0xFFF59E0B),
  ADMIN("Administrador", 0xFF10B981)
}

enum class CourseCategory(val displayName: String, val iconName: String) {
  INFORMATICA("Informática", "Computer"),
  PROGRAMACAO("Programação", "Code"),
  INTELIGENCIA_ARTIFICIAL("Inteligência Artificial", "Psychology"),
  EMPREENDEDORISMO("Empreendedorismo", "TrendingUp"),
  DESIGN_GRAFICO("Design Gráfico", "Palette"),
  MARKETING_DIGITAL("Marketing Digital", "Campaign")
}

enum class CourseLevel(val label: String) {
  BEGINNER("Iniciante"),
  INTERMEDIATE("Intermédio"),
  ADVANCED("Avançado")
}

enum class ContentType(val label: String, val icon: String) {
  VIDEO("Vídeo Aula", "PlayCircle"),
  PDF("Material PDF", "Description"),
  EXERCISE("Exercício Prático", "Terminal"),
  QUIZ("Teste Rápido", "Quiz"),
  FINAL_ASSESSMENT("Avaliação Final", "FactCheck")
}

data class QuizQuestion(
  val id: Int,
  val question: String,
  val options: List<String>,
  val correctIndex: Int,
  val explanation: String
)

data class Lesson(
  val id: String,
  val courseId: String,
  val order: Int,
  val title: String,
  val durationMinutes: Int,
  val type: ContentType,
  val videoTranscript: String = "",
  val pdfSummary: String = "",
  val exercisePrompt: String = "",
  val exerciseStarterCode: String = "",
  val exerciseSolution: String = "",
  val quizQuestions: List<QuizQuestion> = emptyList()
)

data class Course(
  val id: String,
  val code: String, // e.g. EDK001
  val title: String,
  val category: CourseCategory,
  val level: CourseLevel,
  val workloadHours: Int,
  val instructorName: String,
  val instructorTitle: String,
  val description: String,
  val formats: List<ContentType>,
  val rating: Float,
  val enrolledCount: Int,
  val isPublished: Boolean = true,
  val lessons: List<Lesson> = emptyList()
)

data class Enrollment(
  val id: String,
  val userId: String,
  val courseId: String,
  val enrolledAt: String,
  val completedLessonIds: Set<String> = emptySet(),
  val progressPercent: Float = 0f,
  val isCompleted: Boolean = false,
  val completedAt: String? = null,
  val finalAssessmentScore: Int? = null
)

data class Certificate(
  val id: String,
  val verificationCode: String, // e.g. EDK-AO-2026-X8B9
  val userId: String,
  val userName: String,
  val courseId: String,
  val courseTitle: String,
  val category: CourseCategory,
  val workloadHours: Int,
  val gradeScore: Int,
  val issueDate: String,
  val instructorName: String,
  val directorName: String = "Direção Geral EDUKAZA",
  val legalNotice: String = "Certificado de formação profissional livre emitido nos termos da legislação angolana. Reconhecimento oficial condicionado aos processos de autorização/acreditação legalmente aplicáveis junto ao INEFOP / INQ."
)

data class UserProfile(
  val id: String,
  val name: String,
  val email: String,
  val role: UserRole,
  val province: String,
  val bio: String,
  val joinedDate: String,
  val phone: String = "+244 923 000 000"
)

data class PlatformStats(
  val totalStudents: Int,
  val totalCourses: Int,
  val totalCertificates: Int,
  val completionRatePercent: Int,
  val inefopStatus: String = "Processo de Acreditação em Tramitação",
  val provinceDistribution: Map<String, Int>
)
