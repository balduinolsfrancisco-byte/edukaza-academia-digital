package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.Enrollment
import com.example.ui.components.CourseCard
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen

@Composable
fun MyCoursesScreen(
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  val courses by viewModel.courses.collectAsState()
  val enrollments by viewModel.enrollments.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()

  var filterStatus by remember { mutableStateOf("ALL") } // ALL, IN_PROGRESS, COMPLETED

  val userEnrollments = enrollments.filter { it.userId == currentUser.id }
  val filteredEnrollments = when (filterStatus) {
    "IN_PROGRESS" -> userEnrollments.filter { !it.isCompleted }
    "COMPLETED" -> userEnrollments.filter { it.isCompleted }
    else -> userEnrollments
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("my_courses_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    item {
      Column {
        Text(
          text = "Meus Cursos",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Acompanhe as suas aulas, módulos e certificados conquistados.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = filterStatus == "ALL",
            onClick = { filterStatus = "ALL" },
            label = { Text("Todos (${userEnrollments.size})") }
          )
          FilterChip(
            selected = filterStatus == "IN_PROGRESS",
            onClick = { filterStatus = "IN_PROGRESS" },
            label = { Text("A Decorrer (${userEnrollments.count { !it.isCompleted }})") }
          )
          FilterChip(
            selected = filterStatus == "COMPLETED",
            onClick = { filterStatus = "COMPLETED" },
            label = { Text("Concluídos (${userEnrollments.count { it.isCompleted }})") }
          )
        }
      }
    }

    if (filteredEnrollments.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(EduCyanLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.MenuBook, contentDescription = null, tint = EduNavy, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Nenhum curso nesta lista",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Consulte o catálogo da EDUKAZA e inscreva-se gratuitamente.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { viewModel.navigateTo(Screen.Home) },
              colors = ButtonDefaults.buttonColors(containerColor = EduNavy)
            ) {
              Text("Ir para Catálogo")
            }
          }
        }
      }
    } else {
      items(filteredEnrollments, key = { it.id }) { enrollment ->
        val course = courses.find { it.id == enrollment.courseId }
        if (course != null) {
          CourseCard(
            course = course,
            enrollment = enrollment,
            onClick = { viewModel.navigateTo(Screen.CourseDetail(course.id)) }
          )
        }
      }
    }
  }
}
