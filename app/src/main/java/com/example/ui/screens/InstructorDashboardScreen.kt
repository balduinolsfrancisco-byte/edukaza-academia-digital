package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Course
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen

@Composable
fun InstructorDashboardScreen(
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateTo(Screen.Home)
  }

  val currentUser by viewModel.currentUser.collectAsState()
  val courses by viewModel.courses.collectAsState()

  // Courses taught by this instructor (or all if demo)
  val myCourses = courses.filter {
    it.instructorName.contains(currentUser.name, ignoreCase = true) || currentUser.name.contains("Manuel", ignoreCase = true)
  }.ifEmpty { courses.take(3) }

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
          IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Área do Instrutor",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
            Text(
              text = "Gestão Pedagógica & Turmas",
              color = EduCyanLight,
              fontSize = 11.sp
            )
          }
          Surface(
            color = EduGoldDark,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "INSTRUTOR",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Instructor Banner
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(14.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(EduGoldLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Person, contentDescription = null, tint = EduGoldDark, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = currentUser.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = "Instrutor Autorizado EDUKAZA • Luanda, Angola",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Autorização de Publicação Pedagógica: Ativa",
                fontSize = 11.sp,
                color = EduSuccess,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // 2. Instructor Metrics
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricCard(
            label = "Meus Cursos",
            value = "${myCourses.size}",
            icon = Icons.Default.MenuBook,
            color = EduNavy,
            modifier = Modifier.weight(1f)
          )
          MetricCard(
            label = "Alunos Inscritos",
            value = "${myCourses.sumOf { it.enrolledCount }}",
            icon = Icons.Default.Groups,
            color = EduBlueAccent,
            modifier = Modifier.weight(1f)
          )
          MetricCard(
            label = "Avaliação Média",
            value = "4.9★",
            icon = Icons.Default.Star,
            color = EduGoldDark,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // 3. Section Title
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Cursos Sob Minha Coordenação",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      }

      // 4. Course items
      items(myCourses, key = { it.id }) { course ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.navigateTo(Screen.CourseDetail(course.id)) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(12.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = course.code,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EduNavy
              )
              Surface(
                color = EduSuccessContainer,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "${course.enrolledCount} alunos ativos",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = EduSuccess,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = course.title,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${course.workloadHours} horas • ${course.lessons.size} módulos formatados",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.CourseDetail(course.id)) }
              ) {
                Text("Gerir Aulas e Testes", fontSize = 11.sp)
              }
            }
          }
        }
      }

      // 5. Recent Student Activity / Submissions
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Atividade Recente de Alunos em Angola",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "• Paulo Nzau concluiu a Avaliação de Informática Básica com 96%.\n• Teresa Kiala submeteu o exercício de Orçamento Familiar em Kwanzas.\n• 48 novos alunos de Luanda, Huambo e Benguela inscritos esta semana.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
          }
        }
      }
    }
  }
}
