package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen

@Composable
fun AdminDashboardScreen(
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateTo(Screen.Home)
  }

  val courses by viewModel.courses.collectAsState()
  val allUsers by viewModel.allUsers.collectAsState()
  val certificates by viewModel.certificates.collectAsState()
  val stats = viewModel.getPlatformStats()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Visão Geral, 1: Gerir Cursos, 2: Utilizadores, 3: Certificados
  var showCreateCourseDialog by remember { mutableStateOf(false) }

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
              text = "Painel de Administração",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
            Text(
              text = "EDUKAZA - Academia Digital Angola",
              color = EduCyanLight,
              fontSize = 11.sp
            )
          }
          Surface(
            color = EduSuccess,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "ADMIN",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    },
    floatingActionButton = {
      if (selectedTab == 1) {
        FloatingActionButton(
          onClick = { showCreateCourseDialog = true },
          containerColor = EduGold,
          contentColor = Color(0xFF2E1700),
          modifier = Modifier.testTag("admin_add_course_fab")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Criar Novo Curso")
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Tab selector
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = EduNavy,
        edgePadding = 16.dp
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Visão Geral", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Cursos (${courses.size})", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Utilizadores (${allUsers.size})", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("Certificados (${certificates.size})", fontWeight = FontWeight.SemiBold) }
        )
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
      ) {
        when (selectedTab) {
          0 -> {
            // TAB 0: Executive Overview
            item {
              // INEFOP Accreditation Banner
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EduNavyMedium),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EduGold, modifier = Modifier.size(28.dp))
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = "Acreditação Institucional em Angola",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "Autorização INEFOP e Acreditação INQ: Processo em Tramitação Legal.",
                      color = EduCyanLight,
                      fontSize = 11.sp
                    )
                  }
                }
              }
            }

            item {
              // 4 Metrics Grid
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MetricCard(
                  label = "Alunos Inscritos",
                  value = "${stats.totalStudents}",
                  icon = Icons.Default.Groups,
                  color = EduBlueAccent,
                  modifier = Modifier.weight(1f)
                )
                MetricCard(
                  label = "Cursos Ativos",
                  value = "${stats.totalCourses}",
                  icon = Icons.Default.MenuBook,
                  color = EduGoldDark,
                  modifier = Modifier.weight(1f)
                )
              }
            }

            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MetricCard(
                  label = "Certificados Emitidos",
                  value = "${stats.totalCertificates}",
                  icon = Icons.Default.WorkspacePremium,
                  color = EduSuccess,
                  modifier = Modifier.weight(1f)
                )
                MetricCard(
                  label = "Taxa de Conclusão",
                  value = "${stats.completionRatePercent}%",
                  icon = Icons.Default.TrendingUp,
                  color = Color(0xFF7E22CE),
                  modifier = Modifier.weight(1f)
                )
              }
            }

            item {
              // Provincial distribution in Angola
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Text(
                    text = "Distribuição de Alunos por Províncias de Angola",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Spacer(modifier = Modifier.height(12.dp))

                  stats.provinceDistribution.forEach { (province, count) ->
                    val percentage = (count.toFloat() / stats.totalStudents.toFloat()).coerceIn(0f, 1f)
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(text = province, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = "$count alunos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      LinearProgressIndicator(
                        progress = { percentage },
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(5.dp)
                          .clip(RoundedCornerShape(3.dp)),
                        color = EduNavy,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                      )
                    }
                  }
                }
              }
            }
          }

          1 -> {
            // TAB 1: Manage Courses
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Cursos na Plataforma",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )

                Button(
                  onClick = { showCreateCourseDialog = true },
                  colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Novo Curso", fontSize = 12.sp)
                }
              }
            }

            items(courses, key = { it.id }) { course ->
              Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Surface(
                      color = EduNavyMedium,
                      shape = RoundedCornerShape(4.dp)
                    ) {
                      Text(
                        text = course.code,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Surface(
                        color = if (course.isPublished) EduSuccessContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                      ) {
                        Text(
                          text = if (course.isPublished) "Publicado" else "Rascunho",
                          color = if (course.isPublished) EduSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }

                      Spacer(modifier = Modifier.width(6.dp))

                      Switch(
                        checked = course.isPublished,
                        onCheckedChange = { viewModel.toggleCoursePublish(course.id) },
                        modifier = Modifier.scale(0.8f)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "Instrutor: ${course.instructorName} • ${course.workloadHours}h • ${course.lessons.size} módulos",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )

                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                  ) {
                    TextButton(
                      onClick = { viewModel.navigateTo(Screen.CourseDetail(course.id)) }
                    ) {
                      Text("Ver Detalhes", fontSize = 12.sp)
                    }

                    TextButton(
                      onClick = { viewModel.deleteCourse(course.id) }
                    ) {
                      Text("Remover", fontSize = 12.sp, color = EduError)
                    }
                  }
                }
              }
            }
          }

          2 -> {
            // TAB 2: Manage Users
            item {
              Text(
                text = "Utilizadores Registados",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            }

            items(allUsers, key = { it.id }) { user ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EduCyanLight),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Person, contentDescription = null, tint = EduNavy, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      Text(text = "${user.email} • ${user.province}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }

                  // Role selector chip
                  var showRoleDialog by remember { mutableStateOf(false) }
                  Surface(
                    color = when (user.role) {
                      UserRole.STUDENT -> EduBlueAccent
                      UserRole.INSTRUCTOR -> EduGoldDark
                      UserRole.ADMIN -> EduSuccess
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { showRoleDialog = true }
                  ) {
                    Text(
                      text = user.role.label,
                      color = Color.White,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }

                  if (showRoleDialog) {
                    AlertDialog(
                      onDismissRequest = { showRoleDialog = false },
                      title = { Text("Alterar Papel de ${user.name}") },
                      text = {
                        Column {
                          UserRole.entries.forEach { r ->
                            Row(
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                  viewModel.updateUserRole(user.id, r)
                                  showRoleDialog = false
                                }
                                .padding(vertical = 8.dp)
                            ) {
                              RadioButton(selected = user.role == r, onClick = null)
                              Spacer(modifier = Modifier.width(8.dp))
                              Text(r.label)
                            }
                          }
                        }
                      },
                      confirmButton = {
                        TextButton(onClick = { showRoleDialog = false }) { Text("Cancelar") }
                      }
                    )
                  }
                }
              }
            }
          }

          3 -> {
            // TAB 3: Issued Certificates Audit
            item {
              Text(
                text = "Registo Geral de Certificados Emitidos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            }

            items(certificates, key = { it.id }) { cert ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { viewModel.navigateTo(Screen.CertificateDetail(cert.id)) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text(text = cert.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "${cert.courseTitle} • ${cert.workloadHours}h", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Código: ${cert.verificationCode}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = EduNavy)
                  }

                  Surface(
                    color = EduGoldLight,
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = "${cert.gradeScore}%",
                      fontWeight = FontWeight.Bold,
                      color = EduGoldDark,
                      fontSize = 12.sp,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Create Course Dialog
  if (showCreateCourseDialog) {
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("EDK" + String.format("%03d", courses.size + 1)) }
    var selectedCat by remember { mutableStateOf(CourseCategory.PROGRAMACAO) }
    var selectedLvl by remember { mutableStateOf(CourseLevel.BEGINNER) }
    var workload by remember { mutableStateOf("40") }
    var instructor by remember { mutableStateOf("Eng. Manuel Silva") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showCreateCourseDialog = false },
      title = { Text("Publicar Novo Curso", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título do Curso") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Código (ex: EDK008)") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = workload,
            onValueChange = { workload = it },
            label = { Text("Carga Horária (Horas)") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = instructor,
            onValueChange = { instructor = it },
            label = { Text("Nome do Instrutor") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição e Objetivos") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.createNewCourse(
                code = code,
                title = title,
                category = selectedCat,
                level = selectedLvl,
                hours = workload.toIntOrNull() ?: 40,
                description = description.ifBlank { "Curso profissional gratuito da EDUKAZA para o mercado angolano." },
                instructorName = instructor
              )
              showCreateCourseDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
          enabled = title.isNotBlank()
        ) {
          Text("Publicar Curso")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateCourseDialog = false }) { Text("Cancelar") }
      }
    )
  }
}

@Composable
fun MetricCard(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = value,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = label,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

// Helper extension for switch scaling
fun Modifier.scale(scale: Float): Modifier = this
