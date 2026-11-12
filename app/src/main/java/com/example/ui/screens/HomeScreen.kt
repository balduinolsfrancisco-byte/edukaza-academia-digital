package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CourseCategory
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.CourseCard
import com.example.ui.components.RegulatoryNoticeBanner
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen

@Composable
fun HomeScreen(
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  val courses by viewModel.filteredCourses.collectAsState()
  val enrollments by viewModel.enrollments.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val stats = viewModel.getPlatformStats()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen_list"),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    // 1. Hero Banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.edukaza_banner),
          contentDescription = "Alunos da EDUKAZA em Angola a estudar tecnologia",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Gradient overlay for contrast
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.25f),
                  Color.Black.copy(alpha = 0.85f)
                )
              )
            )
        )

        // Text in hero
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
        ) {
          Surface(
            color = EduGold,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "FORMAÇÃO PROFISSIONAL 100% GRATUITA",
              color = Color(0xFF2E1700),
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Democratizar a Educação Digital em Angola",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp
          )

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "Olá, ${currentUser.name}! Desenvolva competências em tecnologia e receba certificados.",
            color = EduCyanLight.copy(alpha = 0.9f),
            fontSize = 12.sp
          )
        }
      }
    }

    // 2. Search Field
    item {
      Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("course_search_input"),
          placeholder = { Text("Pesquisar cursos, informática, Python, IA...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Pesquisar", tint = EduNavy)
          },
          trailingIcon = if (searchQuery.isNotEmpty()) {
            {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Close, contentDescription = "Limpar")
              }
            }
          } else null,
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EduNavy,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )
      }
    }

    // 3. Category Chips
    item {
      CategoryChipsRow(
        selectedCategory = selectedCategory,
        onSelectCategory = { viewModel.setCategoryFilter(it) }
      )
    }

    // 4. Quick Highlights Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HighlightPill(
          icon = Icons.Default.Groups,
          label = "${stats.totalStudents}+",
          sub = "Alunos",
          modifier = Modifier.weight(1f)
        )
        HighlightPill(
          icon = Icons.Default.MenuBook,
          label = "${stats.totalCourses}",
          sub = "Cursos",
          modifier = Modifier.weight(1f)
        )
        HighlightPill(
          icon = Icons.Default.WorkspacePremium,
          label = "${stats.totalCertificates}+",
          sub = "Certificados",
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 5. Section Title
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (selectedCategory != null) selectedCategory!!.displayName else "Catálogo de Cursos",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = "${courses.size} disponíveis",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 6. Course List
    if (courses.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.SearchOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Nenhum curso encontrado",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tente alterar os termos de pesquisa ou remover os filtros.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(courses, key = { it.id }) { course ->
        val enrollment = enrollments.find { it.userId == currentUser.id && it.courseId == course.id }
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          CourseCard(
            course = course,
            enrollment = enrollment,
            onClick = { viewModel.navigateTo(Screen.CourseDetail(course.id)) }
          )
        }
      }
    }

    // 7. Regulatory Footer Notice
    item {
      Box(modifier = Modifier.padding(16.dp)) {
        RegulatoryNoticeBanner()
      }
    }
  }
}

@Composable
fun HighlightPill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  sub: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(EduCyanLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = EduNavy,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = label,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = sub,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
