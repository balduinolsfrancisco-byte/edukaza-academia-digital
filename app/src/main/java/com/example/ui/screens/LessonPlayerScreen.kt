package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun LessonPlayerScreen(
  courseId: String,
  lessonId: String,
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val courses by viewModel.courses.collectAsState()
  val enrollments by viewModel.enrollments.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val certificates by viewModel.certificates.collectAsState()

  val course = courses.find { it.id == courseId } ?: return
  val lesson = course.lessons.find { it.id == lessonId } ?: course.lessons.firstOrNull() ?: return
  val enrollment = enrollments.find { it.userId == currentUser.id && it.courseId == course.id }
  val isCompleted = enrollment?.completedLessonIds?.contains(lesson.id) == true

  // Video state
  var isPlaying by remember { mutableStateOf(false) }
  var playbackPosition by remember { mutableFloatStateOf(0.2f) }
  var playbackSpeed by remember { mutableStateOf("1.0x") }

  // Exercise state
  var exerciseInput by remember { mutableStateOf(lesson.exerciseStarterCode) }
  var exerciseFeedback by remember { mutableStateOf<String?>(null) }
  var isExerciseSuccess by remember { mutableStateOf(false) }

  // Quiz state
  var selectedAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }
  var quizSubmitted by remember { mutableStateOf(false) }
  var quizScore by remember { mutableIntStateOf(0) }

  // Certificate claimed dialog
  var showCertificateCelebration by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      Surface(color = EduNavy, shadowElevation = 4.dp) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.testTag("lesson_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = course.title,
              color = EduGoldLight,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = lesson.title,
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
          }
        }
      }
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Status indicator
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
              contentDescription = null,
              tint = if (isCompleted) EduSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isCompleted) "Módulo Concluído" else "Em andamento",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isCompleted) EduSuccess else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Complete or Next button
          val currentIndex = course.lessons.indexOfFirst { it.id == lesson.id }
          val nextLesson = if (currentIndex in 0 until course.lessons.lastIndex) {
            course.lessons[currentIndex + 1]
          } else null

          Button(
            onClick = {
              viewModel.completeLesson(course.id, lesson.id)
              if (nextLesson != null) {
                viewModel.navigateTo(Screen.LessonPlayer(course.id, nextLesson.id))
              } else {
                // Course fully completed! Check certificate
                showCertificateCelebration = true
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (nextLesson == null) EduGoldDark else EduNavy
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("action_complete_or_next_button")
          ) {
            Icon(
              imageVector = if (nextLesson != null) Icons.Default.ArrowForward else Icons.Default.WorkspacePremium,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (nextLesson != null) "Avançar Próxima" else "Finalizar Curso",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
    ) {
      // CONTENT PLAYER SECTION BY TYPE
      when (lesson.type) {
        ContentType.VIDEO -> {
          // Video Player Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(220.dp)
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            // Dark gradient backdrop
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color.Black)
                  )
                )
            )

            // Play/Pause center overlay
            IconButton(
              onClick = { isPlaying = !isPlaying },
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(EduGold.copy(alpha = 0.9f))
                .testTag("video_play_pause_button")
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                tint = Color(0xFF2E1700),
                modifier = Modifier.size(36.dp)
              )
            }

            // Bottom controls overlay
            Column(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                  )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Slider(
                value = playbackPosition,
                onValueChange = { playbackPosition = it },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                  thumbColor = EduGold,
                  activeTrackColor = EduGold,
                  inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "${(playbackPosition * lesson.durationMinutes).toInt()}:15 / ${lesson.durationMinutes}:00",
                  color = Color.White,
                  fontSize = 11.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                  TextButton(
                    onClick = {
                      playbackSpeed = when (playbackSpeed) {
                        "1.0x" -> "1.25x"
                        "1.25x" -> "1.5x"
                        else -> "1.0x"
                      }
                    }
                  ) {
                    Text(text = playbackSpeed, color = EduGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  IconButton(onClick = { viewModel.showToast("Modo de ecrã inteiro") }) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Ecrã inteiro", tint = Color.White)
                  }
                }
              }
            }
          }

          // Transcript & Video Notes
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Transcrição da Aula e Resumo",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = lesson.videoTranscript.ifBlank {
                  "Nesta aula com a EDUKAZA, abordamos os conceitos mais importantes de ${course.title}. Acompanhe o vídeo com atenção para os testes e exercícios seguintes."
                },
                modifier = Modifier.padding(14.dp),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        ContentType.PDF -> {
          // PDF Reader Container
          Column(modifier = Modifier.padding(16.dp)) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = EduNavyDark),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = EduGold, modifier = Modifier.size(32.dp))
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(text = "Manual Digital EDUKAZA", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "PDF • 14 Páginas • Formato A4", color = EduCyanLight, fontSize = 11.sp)
                  }
                }

                FilledTonalButton(
                  onClick = { viewModel.showToast("Material guardado no dispositivo para estudo offline!") },
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = EduNavyLight)
                ) {
                  Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Baixar", color = Color.White, fontSize = 12.sp)
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "Guia Teórico & Prático",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              shape = RoundedCornerShape(12.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Text(
                text = lesson.pdfSummary,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        ContentType.EXERCISE -> {
          // Coding or Practical Exercise Editor
          Column(modifier = Modifier.padding(16.dp)) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              shape = RoundedCornerShape(12.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Code, contentDescription = null, tint = EduBlueAccent)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = "Instruções do Exercício", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = lesson.exercisePrompt,
                  fontSize = 13.sp,
                  lineHeight = 20.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Espaço de Prática / Resposta:",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
              value = exerciseInput,
              onValueChange = { exerciseInput = it },
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .testTag("exercise_input_field"),
              placeholder = { Text("Escreva aqui o seu código ou resposta prática...") },
              textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              OutlinedButton(
                onClick = {
                  exerciseInput = lesson.exerciseStarterCode
                  exerciseFeedback = null
                }
              ) {
                Text("Repor")
              }

              Button(
                onClick = {
                  isExerciseSuccess = true
                  exerciseFeedback = lesson.exerciseSolution
                  viewModel.completeLesson(course.id, lesson.id)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
                modifier = Modifier.testTag("submit_exercise_button")
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Validar Solução")
              }
            }

            // Exercise Feedback
            if (exerciseFeedback != null) {
              Spacer(modifier = Modifier.height(12.dp))
              Card(
                colors = CardDefaults.cardColors(containerColor = EduSuccessContainer),
                shape = RoundedCornerShape(10.dp)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EduSuccess)
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = exerciseFeedback!!,
                    fontSize = 13.sp,
                    color = Color(0xFF065F46),
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }

        ContentType.QUIZ, ContentType.FINAL_ASSESSMENT -> {
          // Interactive Quiz Questions
          Column(modifier = Modifier.padding(16.dp)) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(
                containerColor = if (lesson.type == ContentType.FINAL_ASSESSMENT) EduGoldLight else MaterialTheme.colorScheme.surfaceVariant
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (lesson.type == ContentType.FINAL_ASSESSMENT) Icons.Default.FactCheck else Icons.Default.Quiz,
                  contentDescription = null,
                  tint = if (lesson.type == ContentType.FINAL_ASSESSMENT) EduGoldDark else EduNavy
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (lesson.type == ContentType.FINAL_ASSESSMENT) "Avaliação Final de Curso" else "Teste de Conhecimento",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(
                    text = "Nota mínima para aprovação: 70%. Responda a todas as questões.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Questions
            lesson.quizQuestions.forEachIndexed { qIdx, question ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = "Questão ${qIdx + 1} de ${lesson.quizQuestions.size}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = question.question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Spacer(modifier = Modifier.height(10.dp))

                  question.options.forEachIndexed { optIdx, optionText ->
                    val isSelected = selectedAnswers[question.id] == optIdx
                    val isCorrect = optIdx == question.correctIndex

                    val borderColor = when {
                      quizSubmitted && isCorrect -> EduSuccess
                      quizSubmitted && isSelected && !isCorrect -> EduError
                      isSelected -> EduNavy
                      else -> MaterialTheme.colorScheme.outline
                    }

                    val bgColor = when {
                      quizSubmitted && isCorrect -> EduSuccessContainer
                      quizSubmitted && isSelected && !isCorrect -> EduErrorContainer
                      isSelected -> EduCyanLight.copy(alpha = 0.5f)
                      else -> MaterialTheme.colorScheme.surface
                    }

                    Surface(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = !quizSubmitted) {
                          selectedAnswers = selectedAnswers + (question.id to optIdx)
                        }
                        .testTag("question_${question.id}_option_$optIdx"),
                      color = bgColor
                    ) {
                      Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        RadioButton(
                          selected = isSelected,
                          onClick = {
                            if (!quizSubmitted) {
                              selectedAnswers = selectedAnswers + (question.id to optIdx)
                            }
                          },
                          enabled = !quizSubmitted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = optionText,
                          fontSize = 13.sp,
                          color = MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier.weight(1f)
                        )
                        if (quizSubmitted && isCorrect) {
                          Icon(Icons.Default.Check, contentDescription = null, tint = EduSuccess, modifier = Modifier.size(18.dp))
                        }
                      }
                    }
                  }

                  if (quizSubmitted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Explicação: ${question.explanation}",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      lineHeight = 16.sp
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Submit Quiz Button
            if (!quizSubmitted) {
              Button(
                onClick = {
                  var correctCount = 0
                  lesson.quizQuestions.forEach { q ->
                    if (selectedAnswers[q.id] == q.correctIndex) {
                      correctCount++
                    }
                  }
                  val total = lesson.quizQuestions.size.coerceAtLeast(1)
                  val score = (correctCount * 100) / total
                  quizScore = score
                  quizSubmitted = true
                  viewModel.submitQuiz(course.id, lesson.id, score)

                  if (score >= 70 && course.lessons.lastOrNull()?.id == lesson.id) {
                    showCertificateCelebration = true
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("submit_quiz_button"),
                enabled = selectedAnswers.size == lesson.quizQuestions.size,
                colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("Submeter Avaliação", fontWeight = FontWeight.Bold)
              }
            } else {
              // Results card
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                  containerColor = if (quizScore >= 70) EduSuccessContainer else EduErrorContainer
                ),
                shape = RoundedCornerShape(12.dp)
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = if (quizScore >= 70) "Aprovado com Sucesso!" else "Abaixo da Média",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (quizScore >= 70) Color(0xFF065F46) else Color(0xFF991B1B)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "A sua pontuação: $quizScore%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (quizScore >= 70) Color(0xFF065F46) else Color(0xFF991B1B)
                  )

                  Spacer(modifier = Modifier.height(8.dp))

                  if (quizScore < 70) {
                    OutlinedButton(
                      onClick = {
                        quizSubmitted = false
                        selectedAnswers = emptyMap()
                      }
                    ) {
                      Text("Tentar Novamente")
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Certificate Celebration Dialog
  if (showCertificateCelebration) {
    val certificate = certificates.find { it.userId == currentUser.id && it.courseId == course.id }
    AlertDialog(
      onDismissRequest = { showCertificateCelebration = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = EduGold, modifier = Modifier.size(28.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Parabéns!", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = "Concluiu com êxito o curso \"${course.title}\" com carga de ${course.workloadHours} horas na EDUKAZA!",
            fontSize = 14.sp,
            lineHeight = 20.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "O seu Certificado de Conclusão já foi emitido e associado ao seu perfil com código de verificação único.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showCertificateCelebration = false
            if (certificate != null) {
              viewModel.navigateTo(Screen.CertificateDetail(certificate.id))
            } else {
              viewModel.navigateTo(Screen.Certificates)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EduNavy)
        ) {
          Text("Ver Meu Certificado")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCertificateCelebration = false }) {
          Text("Fechar")
        }
      }
    )
  }
}
