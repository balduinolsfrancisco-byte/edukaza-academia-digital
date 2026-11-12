package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.UserRole
import com.example.ui.components.RegulatoryNoticeBanner
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen

@Composable
fun ProfileScreen(
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val enrollments by viewModel.enrollments.collectAsState()
  val certificates by viewModel.certificates.collectAsState()

  val userEnrollments = enrollments.filter { it.userId == currentUser.id }
  val userCertificates = certificates.filter { it.userId == currentUser.id }

  var dataSaverMode by remember { mutableStateOf(true) }
  var notificationsEnabled by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("profile_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Profile Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = EduNavy),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(70.dp)
              .clip(CircleShape)
              .background(EduGold),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (currentUser.role) {
                UserRole.STUDENT -> Icons.Default.Person
                UserRole.INSTRUCTOR -> Icons.Default.CastForEducation
                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
              },
              contentDescription = null,
              tint = Color(0xFF2E1700),
              modifier = Modifier.size(40.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = currentUser.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )

          Text(
            text = currentUser.email,
            color = EduCyanLight,
            fontSize = 13.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            color = when (currentUser.role) {
              UserRole.STUDENT -> EduBlueAccent
              UserRole.INSTRUCTOR -> EduGoldDark
              UserRole.ADMIN -> EduSuccess
            },
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "Papel: ${currentUser.role.label}",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = EduGold, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Província: ${currentUser.province}, Angola",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // 2. Academic Stats Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(12.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "${userEnrollments.size}",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = EduNavy
            )
            Text(
              text = "Inscrições",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(12.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "${userEnrollments.count { it.isCompleted }}",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = EduSuccess
            )
            Text(
              text = "Concluídos",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(12.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "${userCertificates.size}",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = EduGoldDark
            )
            Text(
              text = "Certificados",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // 3. Mode Switcher Section
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Trocar Perfil de Demonstração",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Teste as três experiências completas da plataforma:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.switchUserRole(UserRole.STUDENT) },
              modifier = Modifier.weight(1f)
            ) {
              Text("Aluno", fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = { viewModel.switchUserRole(UserRole.INSTRUCTOR) },
              modifier = Modifier.weight(1f)
            ) {
              Text("Instrutor", fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = { viewModel.switchUserRole(UserRole.ADMIN) },
              modifier = Modifier.weight(1f)
            ) {
              Text("Admin", fontSize = 11.sp)
            }
          }
        }
      }
    }

    // 4. Data Saver & Angola Offline Options
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Preferências de Rede & Poupança de Dados",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Modo Económico de Internet", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              Text("Reduz o consumo de dados móveis (Unitel/Movicel)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = dataSaverMode,
              onCheckedChange = {
                dataSaverMode = it
                viewModel.showToast(if (it) "Modo económico ativado" else "Modo económico desativado")
              }
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Notificações de Estudo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              Text("Lembretes semanais de aulas e avaliações", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = notificationsEnabled,
              onCheckedChange = {
                notificationsEnabled = it
                viewModel.showToast(if (it) "Lembretes ativados" else "Lembretes desativados")
              }
            )
          }
        }
      }
    }

    // 5. Institutional Info (From Project Specification)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "EDUKAZA - Academia Digital",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = EduNavy
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• Missão: Democratizar o acesso à formação profissional e digital em Angola.\n• Visão: Tornar-se uma referência nacional na educação digital.\n• Plataformas: Android, iOS, Web e Desktop.\n• Autorização INEFOP e Acreditação INQ: Em processo legal de tramitação.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        }
      }
    }

    // 6. Regulatory Banner
    item {
      RegulatoryNoticeBanner()
    }
  }
}
