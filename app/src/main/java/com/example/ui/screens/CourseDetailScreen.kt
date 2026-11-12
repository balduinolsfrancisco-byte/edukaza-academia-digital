package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.RegulatoryNoticeBanner
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen

@Composable
fun CourseDetailScreen(
  courseId: String,
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
  val enrollment = enrollments.find { it.userId == currentUser.id && it.courseId == course.id }
  val certificate = certificates.find { it.userId == currentUser.id && it.courseId == course.id }

  Scaffold(
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        modifier = Modifier.navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Preço:",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "100% Gratuito",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = EduSuccess
            )
          }

          if (enrollment == null) {
            Button(
              onClick = { viewModel.enroll(course.id) },
              colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .height(48.dp)
                .testTag("enroll_button")
            ) {
              Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Inscrever-se Grátis", fontWeight = FontWeight.Bold)
            }
          } else if (enrollment.isCompleted) {
            Button(
              onClick = {
                if (certificate != null) {
                  viewModel.navigateTo(Screen.CertificateDetail(certificate.id))
                } else {
                  val firstLesson = course.lessons.firstOrNull()
                  if (firstLesson != null) {
                    viewModel.navigateTo(Screen.LessonPlayer(course.id, firstLesson.id))
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EduGoldDark),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .height(48.dp)
                .testTag("view_certificate_button")
            ) {
              Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Ver Certificado", fontWeight = FontWeight.Bold, color = Color.White)
            }
          } else {
            val nextLesson = course.lessons.find { !enrollment.completedLessonIds.contains(it.id) }
              ?: course.lessons.firstOrNull()

            Button(
              onClick = {
                if (nextLesson != null) {
                  viewModel.navigateTo(Screen.LessonPlayer(course.id, nextLesson.id))
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .height(48.dp)
                .testTag("continue_course_button")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Continuar (${(enrollment.progressPercent * 100).toInt()}%)", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      // 1. Header Banner
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
              Brush.verticalGradient(
                colors = listOf(EduNavyDark, EduNavyMedium)
              )
            )
            .padding(16.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                color = EduGoldDark,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = course.code,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = course.level.label,
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            Column {
              Text(
                text = course.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = course.category.displayName,
                color = EduCyanLight,
                fontSize = 13.sp
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = EduGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${course.workloadHours} horas", color = Color.White, fontSize = 12.sp)
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = EduGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${course.rating} / 5.0", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${course.enrolledCount} alunos", color = Color.White, fontSize = 12.sp)
              }
            }
          }
        }
      }

      // 2. Instructor Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(EduCyanLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Person, contentDescription = null, tint = EduNavy, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Instrutor do Curso",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = course.instructorName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = course.instructorTitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // 3. Description
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
          Text(
            text = "Sobre este curso",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = course.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )
        }
      }

      // 4. Formats Available
      item {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
          Text(
            text = "Metodologia e Recursos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            course.formats.forEach { format ->
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = when (format) {
                      ContentType.VIDEO -> Icons.Default.PlayCircle
                      ContentType.PDF -> Icons.Default.Description
                      ContentType.EXERCISE -> Icons.Default.Code
                      ContentType.QUIZ -> Icons.Default.Quiz
                      ContentType.FINAL_ASSESSMENT -> Icons.Default.FactCheck
                    },
                    contentDescription = null,
                    tint = EduNavy,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = format.label.substringBefore(" "),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }

      // 5. Syllabus (Modules / Lessons)
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Conteúdo Programático",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${course.lessons.size} módulos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      itemsIndexed(course.lessons) { index, lesson ->
        val isCompleted = enrollment?.completedLessonIds?.contains(lesson.id) == true

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable {
              viewModel.navigateTo(Screen.LessonPlayer(course.id, lesson.id))
            }
            .testTag("lesson_item_${lesson.id}"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(12.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                  if (isCompleted) EduSuccessContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = "Concluído", tint = EduSuccess, modifier = Modifier.size(20.dp))
              } else {
                Text(
                  text = "${index + 1}",
                  fontWeight = FontWeight.Bold,
                  color = EduNavy,
                  fontSize = 13.sp
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = lesson.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(2.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = when (lesson.type) {
                    ContentType.VIDEO -> Icons.Default.PlayCircleOutline
                    ContentType.PDF -> Icons.Default.Description
                    ContentType.EXERCISE -> Icons.Default.Code
                    ContentType.QUIZ -> Icons.Default.Quiz
                    ContentType.FINAL_ASSESSMENT -> Icons.Default.FactCheck
                  },
                  contentDescription = null,
                  modifier = Modifier.size(13.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${lesson.type.label} • ${lesson.durationMinutes} min",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 6. Regulatory Banner
      item {
        Box(modifier = Modifier.padding(16.dp)) {
          RegulatoryNoticeBanner()
        }
      }
    }
  }
}
