package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduTopAppBar(
  currentUser: UserProfile,
  canNavigateBack: Boolean,
  onNavigateBack: () -> Unit,
  onSwitchRole: (UserRole) -> Unit,
  modifier: Modifier = Modifier
) {
  var showRoleMenu by remember { mutableStateOf(false) }

  Surface(
    color = EduNavy,
    shadowElevation = 4.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          if (canNavigateBack) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("nav_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
          }

          // Brand Logo badge
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(EduCyanDark, EduGold)
                )
              )
              .padding(2.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(EduNavyDark),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = "EDUKAZA Logo",
                tint = EduGold,
                modifier = Modifier.size(22.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "EDUKAZA",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = EduGoldDark,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "ANGOLA",
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Academia Digital Profissional",
              color = EduCyanLight.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
          }
        }

        // Role Switcher Button
        Box {
          Surface(
            color = when (currentUser.role) {
              UserRole.STUDENT -> Color(0xFF0369A1)
              UserRole.INSTRUCTOR -> Color(0xFFB45309)
              UserRole.ADMIN -> Color(0xFF047857)
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { showRoleMenu = true }
              .testTag("role_switcher_pill")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = when (currentUser.role) {
                  UserRole.STUDENT -> Icons.Default.Person
                  UserRole.INSTRUCTOR -> Icons.Default.CastForEducation
                  UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = currentUser.role.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.width(2.dp))
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Trocar Papel",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          DropdownMenu(
            expanded = showRoleMenu,
            onDismissRequest = { showRoleMenu = false }
          ) {
            DropdownMenuItem(
              text = {
                Column {
                  Text("Modo Estudante", fontWeight = FontWeight.Bold)
                  Text("Cursos, aulas e emissão de certificados", fontSize = 11.sp, color = Color.Gray)
                }
              },
              leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = EduBlueAccent) },
              onClick = {
                showRoleMenu = false
                onSwitchRole(UserRole.STUDENT)
              }
            )
            HorizontalDivider()
            DropdownMenuItem(
              text = {
                Column {
                  Text("Modo Instrutor", fontWeight = FontWeight.Bold)
                  Text("Gerir conteúdos e turmas", fontSize = 11.sp, color = Color.Gray)
                }
              },
              leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = EduGold) },
              onClick = {
                showRoleMenu = false
                onSwitchRole(UserRole.INSTRUCTOR)
              }
            )
            HorizontalDivider()
            DropdownMenuItem(
              text = {
                Column {
                  Text("Modo Administrador", fontWeight = FontWeight.Bold)
                  Text("Métricas gerais, utilizadores e acreditação", fontSize = 11.sp, color = Color.Gray)
                }
              },
              leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = EduSuccess) },
              onClick = {
                showRoleMenu = false
                onSwitchRole(UserRole.ADMIN)
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun EduBottomNavigation(
  currentScreenName: String,
  onNavigateHome: () -> Unit,
  onNavigateMyCourses: () -> Unit,
  onNavigateCertificates: () -> Unit,
  onNavigateProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    modifier = modifier.navigationBarsPadding()
  ) {
    NavigationBarItem(
      selected = currentScreenName == "Home",
      onClick = onNavigateHome,
      icon = { Icon(Icons.Default.Explore, contentDescription = "Cursos") },
      label = { Text("Cursos") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = EduNavy,
        indicatorColor = EduGoldLight
      )
    )

    NavigationBarItem(
      selected = currentScreenName == "MyCourses",
      onClick = onNavigateMyCourses,
      icon = { Icon(Icons.Default.MenuBook, contentDescription = "Meus Cursos") },
      label = { Text("Meus Cursos") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = EduNavy,
        indicatorColor = EduGoldLight
      )
    )

    NavigationBarItem(
      selected = currentScreenName == "Certificates",
      onClick = onNavigateCertificates,
      icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Certificados") },
      label = { Text("Certificados") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = EduNavy,
        indicatorColor = EduGoldLight
      )
    )

    NavigationBarItem(
      selected = currentScreenName == "Profile",
      onClick = onNavigateProfile,
      icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
      label = { Text("Perfil") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = EduNavy,
        indicatorColor = EduGoldLight
      )
    )
  }
}

@Composable
fun CategoryChipsRow(
  selectedCategory: CourseCategory?,
  onSelectCategory: (CourseCategory?) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    FilterChip(
      selected = selectedCategory == null,
      onClick = { onSelectCategory(null) },
      label = { Text("Todos") },
      leadingIcon = if (selectedCategory == null) {
        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
      } else null,
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = EduNavy,
        selectedLabelColor = Color.White
      )
    )

    CourseCategory.entries.forEach { cat ->
      val isSelected = selectedCategory == cat
      FilterChip(
        selected = isSelected,
        onClick = { onSelectCategory(if (isSelected) null else cat) },
        label = { Text(cat.displayName) },
        leadingIcon = if (isSelected) {
          { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = EduNavy,
          selectedLabelColor = Color.White
        )
      )
    }
  }
}

@Composable
fun CourseCard(
  course: Course,
  enrollment: Enrollment?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("course_card_${course.id}"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column {
      // Header Banner with Category Gradient & Code Badge
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
          .background(
            Brush.linearGradient(
              colors = when (course.category) {
                CourseCategory.INFORMATICA -> listOf(EduNavyMedium, EduBlueAccent)
                CourseCategory.PROGRAMACAO -> listOf(Color(0xFF1E293B), Color(0xFF0F766E))
                CourseCategory.INTELIGENCIA_ARTIFICIAL -> listOf(Color(0xFF311042), Color(0xFF7E22CE))
                CourseCategory.EMPREENDEDORISMO -> listOf(Color(0xFF78350F), Color(0xFFD97706))
                CourseCategory.DESIGN_GRAFICO -> listOf(Color(0xFF831843), Color(0xFFBE185D))
                CourseCategory.MARKETING_DIGITAL -> listOf(Color(0xFF064E3B), Color(0xFF059669))
              }
            )
          )
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          // Code badge
          Surface(
            color = Color.Black.copy(alpha = 0.4f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = course.code,
              color = EduGoldLight,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          // Category Badge
          Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = when (course.category) {
                  CourseCategory.INFORMATICA -> Icons.Default.Computer
                  CourseCategory.PROGRAMACAO -> Icons.Default.Code
                  CourseCategory.INTELIGENCIA_ARTIFICIAL -> Icons.Default.Psychology
                  CourseCategory.EMPREENDEDORISMO -> Icons.Default.TrendingUp
                  CourseCategory.DESIGN_GRAFICO -> Icons.Default.Palette
                  CourseCategory.MARKETING_DIGITAL -> Icons.Default.Campaign
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = course.category.displayName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Level & Workload bottom in banner
        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${course.workloadHours} horas",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Surface(
            color = EduGold,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "Gratuito",
              color = Color(0xFF2E1700),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Content
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = course.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = course.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Formats & Lessons count
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            course.formats.forEach { format ->
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = format.label.substringBefore(" "),
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = EduGoldDark,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "${course.rating}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = " (${course.enrolledCount})",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Enrollment Progress if enrolled
        if (enrollment != null) {
          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (enrollment.isCompleted) "Curso Concluído" else "O seu progresso",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (enrollment.isCompleted) EduSuccess else EduBlueAccent
            )
            Text(
              text = "${(enrollment.progressPercent * 100).toInt()}%",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { enrollment.progressPercent },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (enrollment.isCompleted) EduSuccess else EduGold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun RegulatoryNoticeBanner(modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(EduCyanLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.VerifiedUser,
          contentDescription = null,
          tint = EduNavy,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Text(
          text = "EDUKAZA - Academia Digital Angola",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Formação profissional digital gratuita. Cursos livres com certificados de conclusão verificáveis por código único (Tramitação INEFOP / INQ).",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 15.sp
        )
      }
    }
  }
}
