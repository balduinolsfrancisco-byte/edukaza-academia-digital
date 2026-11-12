package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class EduRepository(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("edukaza_prefs", Context.MODE_PRIVATE)

  // Demo users
  val demoUsers = listOf(
    UserProfile(
      id = "usr_student_01",
      name = "Paulo Nzau",
      email = "paulo.nzau@edukaza.ao",
      role = UserRole.STUDENT,
      province = "Luanda",
      bio = "Estudante entusiasta de tecnologia e desenvolvimento de software.",
      joinedDate = "Janeiro de 2026",
      phone = "+244 923 456 789"
    ),
    UserProfile(
      id = "usr_inst_01",
      name = "Eng. Manuel Silva",
      email = "manuel.silva@edukaza.ao",
      role = UserRole.INSTRUCTOR,
      province = "Luanda",
      bio = "Engenheiro de Software com 10 anos de experiência em Luanda e formador digital.",
      joinedDate = "Novembro de 2025",
      phone = "+244 931 112 233"
    ),
    UserProfile(
      id = "usr_admin_01",
      name = "Dra. Esperança Cabral",
      email = "admin@edukaza.ao",
      role = UserRole.ADMIN,
      province = "Luanda",
      bio = "Diretora Pedagógica da EDUKAZA - Academia Digital.",
      joinedDate = "Outubro de 2025",
      phone = "+244 912 998 877"
    )
  )

  private val _currentUser = MutableStateFlow(demoUsers[0])
  val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

  private val _courses = MutableStateFlow<List<Course>>(createInitialCourses())
  val courses: StateFlow<List<Course>> = _courses.asStateFlow()

  private val _enrollments = MutableStateFlow<List<Enrollment>>(createInitialEnrollments())
  val enrollments: StateFlow<List<Enrollment>> = _enrollments.asStateFlow()

  private val _certificates = MutableStateFlow<List<Certificate>>(createInitialCertificates())
  val certificates: StateFlow<List<Certificate>> = _certificates.asStateFlow()

  private val _allUsers = MutableStateFlow<List<UserProfile>>(demoUsers)
  val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

  fun switchUserRole(role: UserRole) {
    val targetUser = _allUsers.value.find { it.role == role } ?: demoUsers.find { it.role == role } ?: demoUsers[0]
    _currentUser.value = targetUser
  }

  fun login(email: String, role: UserRole): Boolean {
    val found = _allUsers.value.find { it.email.equals(email.trim(), ignoreCase = true) }
    if (found != null) {
      _currentUser.value = found
      return true
    }
    // Create new profile if not found
    val newUser = UserProfile(
      id = "usr_" + UUID.randomUUID().toString().take(6),
      name = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
      email = email,
      role = role,
      province = "Luanda",
      bio = "Membro da comunidade EDUKAZA",
      joinedDate = SimpleDateFormat("MMMM yyyy", Locale("pt", "AO")).format(Date())
    )
    _allUsers.value = _allUsers.value + newUser
    _currentUser.value = newUser
    return true
  }

  fun enrollInCourse(courseId: String): Boolean {
    val user = _currentUser.value
    val currentList = _enrollments.value
    if (currentList.any { it.userId == user.id && it.courseId == courseId }) {
      return false // already enrolled
    }

    val newEnrollment = Enrollment(
      id = "enr_" + UUID.randomUUID().toString().take(8),
      userId = user.id,
      courseId = courseId,
      enrolledAt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
      completedLessonIds = emptySet(),
      progressPercent = 0f,
      isCompleted = false
    )
    _enrollments.value = currentList + newEnrollment

    // Update course enrollment count
    _courses.value = _courses.value.map {
      if (it.id == courseId) it.copy(enrolledCount = it.enrolledCount + 1) else it
    }
    return true
  }

  fun completeLesson(courseId: String, lessonId: String): Enrollment? {
    val user = _currentUser.value
    val currentList = _enrollments.value
    val enrollment = currentList.find { it.userId == user.id && it.courseId == courseId } ?: return null
    val course = _courses.value.find { it.id == courseId } ?: return null

    val updatedLessons = enrollment.completedLessonIds + lessonId
    val totalLessons = course.lessons.size.coerceAtLeast(1)
    val progress = (updatedLessons.size.toFloat() / totalLessons.toFloat()).coerceIn(0f, 1f)
    val isCompleted = updatedLessons.size >= totalLessons

    val updatedEnrollment = enrollment.copy(
      completedLessonIds = updatedLessons,
      progressPercent = progress,
      isCompleted = isCompleted,
      completedAt = if (isCompleted && enrollment.completedAt == null) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
      } else enrollment.completedAt
    )

    _enrollments.value = currentList.map {
      if (it.id == enrollment.id) updatedEnrollment else it
    }

    // Auto issue certificate if 100% completed and no certificate exists yet
    if (isCompleted) {
      issueCertificateIfEligible(user, course)
    }

    return updatedEnrollment
  }

  fun submitQuizScore(courseId: String, lessonId: String, scorePercent: Int): Enrollment? {
    val user = _currentUser.value
    val currentList = _enrollments.value
    val enrollment = currentList.find { it.userId == user.id && it.courseId == courseId } ?: return null
    val course = _courses.value.find { it.id == courseId } ?: return null

    val updatedLessons = enrollment.completedLessonIds + lessonId
    val totalLessons = course.lessons.size.coerceAtLeast(1)
    val progress = (updatedLessons.size.toFloat() / totalLessons.toFloat()).coerceIn(0f, 1f)
    val isCompleted = updatedLessons.size >= totalLessons

    val updatedEnrollment = enrollment.copy(
      completedLessonIds = updatedLessons,
      progressPercent = progress,
      isCompleted = isCompleted,
      finalAssessmentScore = scorePercent,
      completedAt = if (isCompleted && enrollment.completedAt == null) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
      } else enrollment.completedAt
    )

    _enrollments.value = currentList.map {
      if (it.id == enrollment.id) updatedEnrollment else it
    }

    if (isCompleted && scorePercent >= 70) {
      issueCertificateIfEligible(user, course, scorePercent)
    }

    return updatedEnrollment
  }

  private fun issueCertificateIfEligible(user: UserProfile, course: Course, score: Int = 95) {
    val currentCerts = _certificates.value
    if (currentCerts.any { it.userId == user.id && it.courseId == course.id }) {
      return // already has certificate
    }

    val code = "EDK-AO-" + SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()) + "-" +
        UUID.randomUUID().toString().take(4).uppercase(Locale.ROOT)

    val newCert = Certificate(
      id = "cert_" + UUID.randomUUID().toString().take(8),
      verificationCode = code,
      userId = user.id,
      userName = user.name,
      courseId = course.id,
      courseTitle = course.title,
      category = course.category,
      workloadHours = course.workloadHours,
      gradeScore = score,
      issueDate = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "AO")).format(Date()),
      instructorName = course.instructorName,
      directorName = "Dra. Esperança Cabral"
    )

    _certificates.value = currentCerts + newCert
  }

  fun verifyCertificateCode(code: String): Certificate? {
    val cleanCode = code.trim().uppercase()
    return _certificates.value.find { it.verificationCode.uppercase() == cleanCode }
  }

  fun createCourse(
    code: String,
    title: String,
    category: CourseCategory,
    level: CourseLevel,
    workloadHours: Int,
    description: String,
    instructorName: String
  ): Course {
    val newCourse = Course(
      id = "EDK" + String.format(Locale.ROOT, "%03d", _courses.value.size + 1),
      code = code.ifBlank { "EDK" + String.format(Locale.ROOT, "%03d", _courses.value.size + 1) },
      title = title,
      category = category,
      level = level,
      workloadHours = workloadHours,
      instructorName = instructorName.ifBlank { _currentUser.value.name },
      instructorTitle = "Instrutor Certificado EDUKAZA",
      description = description,
      formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.QUIZ),
      rating = 5.0f,
      enrolledCount = 0,
      isPublished = true,
      lessons = listOf(
        Lesson(
          id = "les_new_1",
          courseId = code,
          order = 1,
          title = "Módulo 1: Introdução e Fundamentos",
          durationMinutes = 25,
          type = ContentType.VIDEO,
          videoTranscript = "Bem-vindos ao módulo introdutório da EDUKAZA. Vamos explorar os conceitos fundamentais desta disciplina."
        ),
        Lesson(
          id = "les_new_2",
          courseId = code,
          order = 2,
          title = "Módulo 2: Guia de Estudo e Casos Práticos",
          durationMinutes = 35,
          type = ContentType.PDF,
          pdfSummary = "Documento detalhado com exercícios e estudos de caso adaptados ao contexto de Angola."
        ),
        Lesson(
          id = "les_new_3",
          courseId = code,
          order = 3,
          title = "Módulo 3: Avaliação de Competências",
          durationMinutes = 20,
          type = ContentType.QUIZ,
          quizQuestions = listOf(
            QuizQuestion(
              id = 1,
              question = "Qual o principal objetivo deste módulo?",
              options = listOf("Fixar conceitos chave", "Decorar termos", "Nenhum", "Apenas leitura"),
              correctIndex = 0,
              explanation = "A fixação prática dos conceitos é o pilar da formação profissional na EDUKAZA."
            )
          )
        )
      )
    )
    _courses.value = _courses.value + newCourse
    return newCourse
  }

  fun toggleCoursePublish(courseId: String) {
    _courses.value = _courses.value.map {
      if (it.id == courseId) it.copy(isPublished = !it.isPublished) else it
    }
  }

  fun deleteCourse(courseId: String) {
    _courses.value = _courses.value.filterNot { it.id == courseId }
  }

  fun updateUserRole(userId: String, newRole: UserRole) {
    _allUsers.value = _allUsers.value.map {
      if (it.id == userId) it.copy(role = newRole) else it
    }
    if (_currentUser.value.id == userId) {
      _currentUser.value = _currentUser.value.copy(role = newRole)
    }
  }

  fun getPlatformStats(): PlatformStats {
    val totalC = _courses.value.size
    val totalE = _enrollments.value.size
    val completedCount = _enrollments.value.count { it.isCompleted }
    val rate = if (totalE > 0) (completedCount * 100) / totalE else 78

    return PlatformStats(
      totalStudents = 1420 + totalE,
      totalCourses = totalC,
      totalCertificates = 340 + _certificates.value.size,
      completionRatePercent = rate,
      inefopStatus = "Acreditação INEFOP/INQ em Tramitação Legal",
      provinceDistribution = mapOf(
        "Luanda" to 710,
        "Benguela" to 220,
        "Huambo" to 180,
        "Huíla (Lubango)" to 140,
        "Cabinda" to 85,
        "Uíge" to 55,
        "Outras Províncias" to 30
      )
    )
  }

  // --- Seed Data Generators ---

  private fun createInitialCourses(): List<Course> {
    return listOf(
      // EDK001
      Course(
        id = "EDK001",
        code = "EDK001",
        title = "Informática Básica",
        category = CourseCategory.INFORMATICA,
        level = CourseLevel.BEGINNER,
        workloadHours = 30,
        instructorName = "Eng. Manuel Silva",
        instructorTitle = "Especialista em Redes e Sistemas, Luanda",
        description = "Aprenda a operar computadores com segurança, dominar o sistema operativo, atalhos do teclado, navegação na internet e gestão eficaz de ficheiros no dia a dia profissional.",
        formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.QUIZ),
        rating = 4.9f,
        enrolledCount = 428,
        isPublished = true,
        lessons = listOf(
          Lesson(
            id = "les_001_1",
            courseId = "EDK001",
            order = 1,
            title = "Hardware, Software e Sistemas Operativos",
            durationMinutes = 25,
            type = ContentType.VIDEO,
            videoTranscript = "Bem-vindo à EDUKAZA! Nesta aula fundamental, vamos desmistificar o que é o Hardware (a parte física do computador como monitor, teclado, processador e memória RAM) e o Software (programas e aplicações). Também vamos entender como o sistema operativo gere todos os recursos da sua máquina."
          ),
          Lesson(
            id = "les_001_2",
            courseId = "EDK001",
            order = 2,
            title = "Gestão de Ficheiros, Pastas e Atalhos Essenciais",
            durationMinutes = 30,
            type = ContentType.PDF,
            pdfSummary = """
              GUIA DO ALUNO: ORGANIZAÇÃO DE FICHEIROS
              1. Estrutura de Pastas Profissional: Crie categorias lógicas (Trabalho, Finanças, Estudos).
              2. Extensões Comuns: .docx (documentos), .xlsx (folhas de cálculo), .pdf (documento estático), .png/.jpg (imagens).
              3. Atalhos que salvam tempo:
                 - Ctrl + C (Copiar) | Ctrl + V (Colar) | Ctrl + X (Recortar)
                 - Ctrl + Z (Desfazer) | Ctrl + S (Guardar ficheiro)
                 - Alt + Tab (Alternar entre janelas) | Windows + D (Mostrar Ambiente de Trabalho)
              4. Cópia de Segurança (Backup): Guarde sempre cópias dos seus ficheiros importantes numa pen drive ou na nuvem.
            """.trimIndent()
          ),
          Lesson(
            id = "les_001_3",
            courseId = "EDK001",
            order = 3,
            title = "Teste de Avaliação: Informática Essencial",
            durationMinutes = 15,
            type = ContentType.QUIZ,
            quizQuestions = listOf(
              QuizQuestion(
                id = 1,
                question = "Qual dos seguintes componentes é considerado Hardware?",
                options = listOf("Memória RAM", "Google Chrome", "Windows 11", "Microsoft Word"),
                correctIndex = 0,
                explanation = "A Memória RAM é a peça física que armazena temporariamente os dados dos programas em execução."
              ),
              QuizQuestion(
                id = 2,
                question = "Qual é o atalho universal de teclado para guardar um ficheiro rapidamente?",
                options = listOf("Ctrl + S", "Ctrl + P", "Ctrl + Z", "Ctrl + F"),
                correctIndex = 0,
                explanation = "Ctrl + S (ou Ctrl + B em algumas versões portuguesas) guarda o progresso do ficheiro."
              ),
              QuizQuestion(
                id = 3,
                question = "O que significa a sigla PDF?",
                options = listOf(
                  "Portable Document Format",
                  "Personal Data File",
                  "Power Digital Format",
                  "Program Desktop File"
                ),
                correctIndex = 0,
                explanation = "PDF significa Portable Document Format, garantindo que o documento seja lido igual em qualquer dispositivo."
              )
            )
          )
        )
      ),

      // EDK002
      Course(
        id = "EDK002",
        code = "EDK002",
        title = "Microsoft Word e Excel",
        category = CourseCategory.INFORMATICA,
        level = CourseLevel.BEGINNER,
        workloadHours = 45,
        instructorName = "Prof.ª Teresa Kiala",
        instructorTitle = "Consultora de Produtividade Empresarial",
        description = "Domine a redação de relatórios e cartas formais no Word e construa tabelas financeiras, gráficos e folhas de cálculo orçamentais em Kwanzas (AOA) no Excel.",
        formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.EXERCISE),
        rating = 4.8f,
        enrolledCount = 385,
        isPublished = true,
        lessons = listOf(
          Lesson(
            id = "les_002_1",
            courseId = "EDK002",
            order = 1,
            title = "Formatação de Documentos Profissionais no Word",
            durationMinutes = 35,
            type = ContentType.VIDEO,
            videoTranscript = "Nesta aula aprendemos a estruturar cabeçalhos, rodapés, paginação, margens A4 segundo normas institucionais angolanas e a criar índices automáticos com estilos de título."
          ),
          Lesson(
            id = "les_002_2",
            courseId = "EDK002",
            order = 2,
            title = "Fórmulas Essenciais no Excel: SOMA, MÉDIA e SE",
            durationMinutes = 40,
            type = ContentType.PDF,
            pdfSummary = """
              GUIA DE FÓRMULAS DO EXCEL:
              1. =SOMA(A1:A10) -> Soma todos os valores no intervalo.
              2. =MÉDIA(B2:B20) -> Calcula a média aritmética.
              3. =SE(C2>=50; "Aprovado"; "Reprovado") -> Condição lógica dinâmica.
              4. Formatação de Moeda: Selecione as células -> Formatar Células -> Moeda -> Kz (AOA).
              5. Gráficos Rápidos: Selecione os dados e pressione Alt + F1 para gerar um gráfico de barras instantâneo.
            """.trimIndent()
          ),
          Lesson(
            id = "les_002_3",
            courseId = "EDK002",
            order = 3,
            title = "Exercício Prático: Orçamento Familiar em Kwanzas (Kz)",
            durationMinutes = 30,
            type = ContentType.EXERCISE,
            exercisePrompt = "Crie uma folha de cálculo com colunas 'Despesa', 'Valor Previsto (Kz)', 'Valor Real (Kz)' e 'Diferença'. Calcule a soma total e determine se houve economia usando a fórmula SE.",
            exerciseStarterCode = "=SOMA(B2:B10)\n=C2-B2\n=SE(C11<=B11; \"Dentro do Orçamento\"; \"Ultrapassou\")",
            exerciseSolution = "Parabéns! O uso da função =SOMA() e a comparação lógica =SE() garantem o controlo financeiro exato em Kwanzas."
          )
        )
      ),

      // EDK003
      Course(
        id = "EDK003",
        code = "EDK003",
        title = "Introdução à Programação",
        category = CourseCategory.PROGRAMACAO,
        level = CourseLevel.BEGINNER,
        workloadHours = 60,
        instructorName = "Eng. Manuel Silva",
        instructorTitle = "Lead Software Architect",
        description = "Entre no mundo do desenvolvimento de software! Desenvolva o raciocínio lógico, aprenda a criar algoritmos e escreva seus primeiros códigos reais em Python.",
        formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.QUIZ),
        rating = 4.95f,
        enrolledCount = 512,
        isPublished = true,
        lessons = listOf(
          Lesson(
            id = "les_003_1",
            courseId = "EDK003",
            order = 1,
            title = "Lógica de Programação e Algoritmos",
            durationMinutes = 40,
            type = ContentType.VIDEO,
            videoTranscript = "Programar é ensinar o computador a resolver problemas passo a passo. Nesta aula vamos criar fluxogramas lógicos, entender entradas de dados, processamento e saídas no ecrã."
          ),
          Lesson(
            id = "les_003_2",
            courseId = "EDK003",
            order = 2,
            title = "Fundamentos de Python: Variáveis e Tipos de Dados",
            durationMinutes = 35,
            type = ContentType.PDF,
            pdfSummary = """
              SINTAXE BÁSICA EM PYTHON:
              # Variáveis
              nome = "Paulo"
              idade = 22
              salario = 150000.50
              estudante = True

              # Condicionais
              if idade >= 18:
                  print("Maior de idade em Angola")
              else:
                  print("Menor de idade")

              # Loops
              for i in range(5):
                  print(f"Contador da EDUKAZA: {i}")
            """.trimIndent()
          ),
          Lesson(
            id = "les_003_3",
            courseId = "EDK003",
            order = 3,
            title = "Quiz de Lógica e Python Básico",
            durationMinutes = 20,
            type = ContentType.QUIZ,
            quizQuestions = listOf(
              QuizQuestion(
                id = 1,
                question = "O que imprime o comando: print(10 + 5 * 2) em Python?",
                options = listOf("20", "30", "25", "1052"),
                correctIndex = 0,
                explanation = "Pela ordem de precedência matemática, a multiplicação (5 * 2 = 10) é feita antes da adição (10 + 10 = 20)."
              ),
              QuizQuestion(
                id = 2,
                question = "Qual palavra-chave é usada para definir uma função em Python?",
                options = listOf("def", "function", "fun", "define"),
                correctIndex = 0,
                explanation = "Em Python, utilizamos 'def nome_da_funcao():' para declarar blocos de funções reutilizáveis."
              ),
              QuizQuestion(
                id = 3,
                question = "Qual é o tipo de dado do valor: True em programação?",
                options = listOf("Booleano (bool)", "Texto (str)", "Inteiro (int)", "Lista (list)"),
                correctIndex = 0,
                explanation = "True e False são valores Booleanos fundamentais para tomada de decisões nos programas."
              )
            )
          )
        )
      ),

      // EDK004
      Course(
        id = "EDK004",
        code = "EDK004",
        title = "Inteligência Artificial",
        category = CourseCategory.INTELIGENCIA_ARTIFICIAL,
        level = CourseLevel.BEGINNER,
        workloadHours = 40,
        instructorName = "Dra. Esperança Cabral",
        instructorTitle = "Pesquisadora em Tecnologias Emergentes",
        description = "Compreenda como funciona a Inteligência Artificial, aprenda engenharia de prompts moderna e use IA generativa para acelerar seus estudos, relatórios e projetos profissionais.",
        formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.EXERCISE),
        rating = 4.92f,
        enrolledCount = 320,
        isPublished = true,
        lessons = listOf(
          Lesson(
            id = "les_004_1",
            courseId = "EDK004",
            order = 1,
            title = "O que é IA e Modelos de Linguagem (LLMs)",
            durationMinutes = 30,
            type = ContentType.VIDEO,
            videoTranscript = "Nesta aula explicamos como redes neuronais artificiais aprendem com grandes volumes de dados de texto e visão para apoiar seres humanos na resolução de problemas complexos."
          ),
          Lesson(
            id = "les_004_2",
            courseId = "EDK004",
            order = 2,
            title = "Engenharia de Prompts para Produtividade Diária",
            durationMinutes = 35,
            type = ContentType.PDF,
            pdfSummary = """
              FÓRMULA DO PROMPT EFICAZ:
              1. Papel: 'Aja como um especialista em recursos humanos em Luanda...'
              2. Contexto: 'Tenho uma pequena empresa de prestação de serviços...'
              3. Tarefa: 'Elabore um modelo de contrato de estágio profissional de 3 meses...'
              4. Formato: 'Retorne em formato de tabela com cláusulas numeradas...'
              5. Restrições: 'Conforme a Lei Geral do Trabalho de Angola.'
            """.trimIndent()
          ),
          Lesson(
            id = "les_004_3",
            courseId = "EDK004",
            order = 3,
            title = "Exercício Prático: Criação de Assistente de Conteúdo",
            durationMinutes = 25,
            type = ContentType.EXERCISE,
            exercisePrompt = "Escreva um prompt estruturado para gerar uma campanha de lançamento de um curso online em Angola, com tom motivacional e foco em jovens empreendedores.",
            exerciseStarterCode = "Papel: Consultor de Marketing Digital em Angola\nObjetivo: Campanha de lançamento\nPúblico-alvo: Jovens de 18 a 30 anos",
            exerciseSolution = "Excelente! O prompt estruturado garante respostas contextuais ricas e diretamente aplicáveis ao mercado local."
          )
        )
      ),

      // EDK005
      Course(
        id = "EDK005",
        code = "EDK005",
        title = "Empreendedorismo",
        category = CourseCategory.EMPREENDEDORISMO,
        level = CourseLevel.BEGINNER,
        workloadHours = 50,
        instructorName = "Dr. Afonso Ndala",
        instructorTitle = "Especialista em Startups & Finanças",
        description = "Da ideia ao negócio real: aprenda a modelar propostas de valor, analisar custos, formalizar empresas em Angola e conquistar seus primeiros clientes com sucesso.",
        formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.FINAL_ASSESSMENT),
        rating = 4.88f,
        enrolledCount = 295,
        isPublished = true,
        lessons = listOf(
          Lesson(
            id = "les_005_1",
            courseId = "EDK005",
            order = 1,
            title = "Oportunidades de Negócio no Contexto Angolano",
            durationMinutes = 30,
            type = ContentType.VIDEO,
            videoTranscript = "Analisamos as dores reais do mercado em setores estratégicos como alimentação, logística, serviços digitais e educação em Luanda e províncias do interior."
          ),
          Lesson(
            id = "les_005_2",
            courseId = "EDK005",
            order = 2,
            title = "Business Model Canvas e Gestão de Fluxo de Caixa",
            durationMinutes = 45,
            type = ContentType.PDF,
            pdfSummary = """
              OS 9 BLOCOS DO CANVAS EMPREENDEDOR:
              1. Proposta de Valor: Que problema resolvemos?
              2. Segmento de Clientes: Quem são os compradores?
              3. Canais: Como o produto chega até eles?
              4. Relacionamento: Como atraímos e retemos clientes?
              5. Fontes de Receita: Como a empresa ganha dinheiro em Kwanzas?
              6. Recursos Chave: Pessoas, tecnologia, capital.
              7. Atividades Chave: O que precisamos executar todos os dias?
              8. Parcerias Estratégicas: Fornecedores e alianças.
              9. Estrutura de Custos: Custos fixos e variáveis.
            """.trimIndent()
          ),
          Lesson(
            id = "les_005_3",
            courseId = "EDK005",
            order = 3,
            title = "Avaliação Final: Validação de Ideia Empreendedora",
            durationMinutes = 30,
            type = ContentType.FINAL_ASSESSMENT,
            quizQuestions = listOf(
              QuizQuestion(
                id = 1,
                question = "O que caracteriza uma Proposta de Valor forte?",
                options = listOf(
                  "Resolver uma dor específica com benefício claro percebido pelo cliente",
                  "Ter apenas o preço mais barato do mercado",
                  "Copiar exatamente o concorrente",
                  "Não ter foco em nenhum cliente específico"
                ),
                correctIndex = 0,
                explanation = "A proposta de valor comunica exatamente como o seu produto ou serviço melhora a vida do cliente."
              ),
              QuizQuestion(
                id = 2,
                question = "O que é o Ponto de Equilíbrio (Break-even) de um negócio?",
                options = listOf(
                  "O momento em que as receitas cobrem exatamente todos os custos operacionais",
                  "Quando a empresa tem 1 milhão de Kwanzas no banco",
                  "Quando a empresa não tem nenhum funcionário",
                  "O dia da fundação da empresa"
                ),
                correctIndex = 0,
                explanation = "O ponto de equilíbrio indica que a empresa não está a ter prejuízo, cobrindo integralmente as suas despesas."
              ),
              QuizQuestion(
                id = 3,
                question = "Qual o primeiro passo antes de investir capital num novo produto?",
                options = listOf(
                  "Validar a ideia com potenciais clientes através de entrevistas ou protótipo",
                  "Alugar um escritório de luxo imediatamente",
                  "Contratar dezenas de colaboradores",
                  "Fazer empréstimo bancário sem plano"
                ),
                correctIndex = 0,
                explanation = "A validação rápida com o público-alvo reduz riscos de investimento e acelera o sucesso do negócio."
              )
            )
          )
        )
      ),

      // EDK006
      Course(
        id = "EDK006",
        code = "EDK006",
        title = "Design Gráfico no Canva e Figma",
        category = CourseCategory.DESIGN_GRAFICO,
        level = CourseLevel.BEGINNER,
        workloadHours = 35,
        instructorName = "Cláudia Domingos",
        instructorTitle = "Diretora de Arte & Brand Designer",
        description = "Crie identidades visuais impactantes, cartazes, panfletos e peças gráficas para redes sociais e empresas utilizando ferramentas modernas e gratuitas.",
        formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.EXERCISE),
        rating = 4.85f,
        enrolledCount = 240,
        isPublished = true,
        lessons = listOf(
          Lesson(
            id = "les_006_1",
            courseId = "EDK006",
            order = 1,
            title = "Teoria das Cores, Tipografia e Hierarquia Visual",
            durationMinutes = 25,
            type = ContentType.VIDEO,
            videoTranscript = "Compreenda o contraste visual, alinhamento, espaçamento e a psicologia das cores em anúncios digitais."
          ),
          Lesson(
            id = "les_006_2",
            courseId = "EDK006",
            order = 2,
            title = "Criação de Posts Profissionais para Redes Sociais",
            durationMinutes = 30,
            type = ContentType.PDF,
            pdfSummary = "Regras práticas para artes digitais: grid 1080x1080, margens de segurança, contraste de texto e exportação em alta qualidade."
          )
        )
      ),

      // EDK007
      Course(
        id = "EDK007",
        code = "EDK007",
        title = "Marketing Digital para o Mercado Angolano",
        category = CourseCategory.MARKETING_DIGITAL,
        level = CourseLevel.BEGINNER,
        workloadHours = 40,
        instructorName = "Mateus Benguela",
        instructorTitle = "Growth Marketer & Estrategista Digital",
        description = "Estratégias comprovadas para vender mais no WhatsApp Business, Instagram e Facebook em Angola com gestão de anúncios e copywriting persuasivo.",
        formats = listOf(ContentType.VIDEO, ContentType.PDF, ContentType.QUIZ),
        rating = 4.9f,
        enrolledCount = 310,
        isPublished = true,
        lessons = listOf(
          Lesson(
            id = "les_007_1",
            courseId = "EDK007",
            order = 1,
            title = "Vendas Rápidas com WhatsApp Business em Angola",
            durationMinutes = 30,
            type = ContentType.VIDEO,
            videoTranscript = "Como configurar catálogos, mensagens automáticas de boas-vindas, etiquetas de pagamento e fecho de negócios com confiança."
          ),
          Lesson(
            id = "les_007_2",
            courseId = "EDK007",
            order = 2,
            title = "Estratégias de Tráfego e Conteúdo Orgânico",
            durationMinutes = 35,
            type = ContentType.PDF,
            pdfSummary = "Calendário editorial semanal, formatos de vídeo curto (Reels/TikTok) e storytelling focado na cultura angolana."
          )
        )
      )
    )
  }

  private fun createInitialEnrollments(): List<Enrollment> {
    return listOf(
      // Paulo Nzau has finished EDK001 (Informática Básica)
      Enrollment(
        id = "enr_001",
        userId = "usr_student_01",
        courseId = "EDK001",
        enrolledAt = "15/01/2026",
        completedLessonIds = setOf("les_001_1", "les_001_2", "les_001_3"),
        progressPercent = 1.0f,
        isCompleted = true,
        completedAt = "28/01/2026",
        finalAssessmentScore = 96
      ),
      // In progress with EDK003 (Introdução à Programação)
      Enrollment(
        id = "enr_002",
        userId = "usr_student_01",
        courseId = "EDK003",
        enrolledAt = "02/02/2026",
        completedLessonIds = setOf("les_003_1"),
        progressPercent = 0.33f,
        isCompleted = false
      )
    )
  }

  private fun createInitialCertificates(): List<Certificate> {
    return listOf(
      Certificate(
        id = "cert_edk_001",
        verificationCode = "EDK-AO-2026-X8B9",
        userId = "usr_student_01",
        userName = "Paulo Nzau",
        courseId = "EDK001",
        courseTitle = "Informática Básica",
        category = CourseCategory.INFORMATICA,
        workloadHours = 30,
        gradeScore = 96,
        issueDate = "28 de Janeiro de 2026",
        instructorName = "Eng. Manuel Silva",
        directorName = "Dra. Esperança Cabral"
      )
    )
  }
}
