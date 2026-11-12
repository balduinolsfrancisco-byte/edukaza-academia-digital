package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Certificate
import com.example.ui.components.RegulatoryNoticeBanner
import com.example.ui.theme.*
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen

@Composable
fun CertificatesScreen(
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  val certificates by viewModel.certificates.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val verificationResult by viewModel.verificationResult.collectAsState()
  val isVerifying by viewModel.isVerifying.collectAsState()

  var inputCode by remember { mutableStateOf("") }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Meus Certificados, 1: Validar Código

  val userCertificates = certificates.filter { it.userId == currentUser.id }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("certificates_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Tabs
    item {
      Column {
        Text(
          text = "Certificados EDUKAZA",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Comprovativos digitais de conclusão com código de autenticidade único para o mercado de trabalho angolano.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = EduNavy
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Meus Certificados (${userCertificates.size})", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Validar Código", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp)) }
          )
        }
      }
    }

    if (selectedTab == 0) {
      // TAB 0: User Certificates
      if (userCertificates.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(CircleShape)
                  .background(EduGoldLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.WorkspacePremium,
                  contentDescription = null,
                  tint = EduGoldDark,
                  modifier = Modifier.size(32.dp)
                )
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Ainda não possui certificados",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Conclua todos os módulos e avaliações de qualquer curso gratuito para receber automaticamente o seu certificado oficial da EDUKAZA.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { viewModel.navigateTo(Screen.Home) },
                colors = ButtonDefaults.buttonColors(containerColor = EduNavy)
              ) {
                Text("Explorar Cursos")
              }
            }
          }
        }
      } else {
        items(userCertificates, key = { it.id }) { cert ->
          CertificateSummaryCard(
            certificate = cert,
            onClick = { viewModel.navigateTo(Screen.CertificateDetail(cert.id)) }
          )
        }
      }
    } else {
      // TAB 1: Code Verification Tool
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(16.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = EduNavy)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Validador Público de Certificados",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Empresas, recrutadores e instituições em Angola podem validar a autenticidade de qualquer certificado emitido pela EDUKAZA inserindo o código impresso.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = inputCode,
              onValueChange = { inputCode = it },
              placeholder = { Text("Ex: EDK-AO-2026-X8B9") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("verify_code_input"),
              singleLine = true,
              leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = EduNavy) },
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick demo fill buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "Exemplo:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
              )
              AssistChip(
                onClick = { inputCode = "EDK-AO-2026-X8B9" },
                label = { Text("EDK-AO-2026-X8B9", fontSize = 11.sp) }
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = { viewModel.verifyCertificate(inputCode) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("verify_certificate_button"),
              colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
              shape = RoundedCornerShape(10.dp),
              enabled = inputCode.isNotBlank() && !isVerifying
            ) {
              if (isVerifying) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("A consultar base de dados...")
              } else {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verificar Autenticidade", fontWeight = FontWeight.Bold)
              }
            }

            // Result Display
            AnimatedVisibility(visible = verificationResult != null) {
              val res = verificationResult
              if (res != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                  colors = CardDefaults.cardColors(containerColor = EduSuccessContainer),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EduSuccess)
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = "CERTIFICADO AUTÊNTICO E VÁLIDO",
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Aluno(a): ${res.userName}",
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Curso: ${res.courseTitle} (${res.workloadHours} horas)",
                      fontSize = 13.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Nota Final: ${res.gradeScore}% • Data: ${res.issueDate}",
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Código de Registo: ${res.verificationCode}",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = EduNavy
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                      onClick = { viewModel.navigateTo(Screen.CertificateDetail(res.id)) },
                      colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text("Visualizar Certificado Completo")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Regulatory Banner
    item {
      RegulatoryNoticeBanner()
    }
  }
}

@Composable
fun CertificateSummaryCard(
  certificate: Certificate,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("certificate_card_${certificate.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column {
      // Golden top border
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .background(
            Brush.horizontalGradient(listOf(EduGold, EduCyan, EduGold))
          )
      )

      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(EduNavy),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.School, contentDescription = null, tint = EduGold, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "EDUKAZA ACADEMIA DIGITAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EduGoldDark,
                letterSpacing = 0.5.sp
              )
              Text(
                text = certificate.courseTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Surface(
            color = EduGoldLight,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "${certificate.gradeScore}%",
              color = Color(0xFF78350F),
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Código de Verificação:",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = certificate.verificationCode,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = EduNavy
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Carga Horária:",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${certificate.workloadHours} Horas",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Emitido a ${certificate.issueDate}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Text(
            text = "Toque para abrir certificado >",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = EduBlueAccent
          )
        }
      }
    }
  }
}

@Composable
fun CertificateDetailScreen(
  certificateId: String,
  viewModel: EduViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val certificates by viewModel.certificates.collectAsState()
  val certificate = certificates.find { it.id == certificateId } ?: return

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
          IconButton(onClick = { viewModel.navigateBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
          }
          Text(
            text = "Certificado de Conclusão",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
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
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(certificate.verificationCode))
              viewModel.showToast("Código ${certificate.verificationCode} copiado!")
            },
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copiar Código", fontSize = 12.sp)
          }

          Button(
            onClick = {
              viewModel.showToast("Certificado PDF exportado e pronto a partilhar!")
            },
            colors = ButtonDefaults.buttonColors(containerColor = EduNavy),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Partilhar / PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // THE DIPLOMA ITSELF
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, EduGold, RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFE)),
          shape = RoundedCornerShape(16.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Certificate Top Header
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(EduNavy),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.School, contentDescription = null, tint = EduGold, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "EDUKAZA",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 20.sp,
                  color = EduNavy,
                  letterSpacing = 1.sp
                )
                Text(
                  text = "ACADEMIA DIGITAL • REPÚBLICA DE ANGOLA",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = EduGoldDark,
                  letterSpacing = 0.5.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = EduGold.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "CERTIFICADO DE CONCLUSÃO",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = EduNavy,
              letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Certifica-se que",
              fontSize = 12.sp,
              color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = certificate.userName,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = EduNavy,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "concluiu com aproveitamento e dedicação exemplar o curso de formação profissional livre de",
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
              color = Color(0xFF334155),
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = certificate.courseTitle,
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = EduGoldDark,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "com uma carga horária total de ${certificate.workloadHours} horas e avaliação final de ${certificate.gradeScore}%.",
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
              color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(14.dp))

            // Signatures row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = certificate.instructorName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = EduNavy
                )
                Text(
                  text = "Instrutor Responsável",
                  fontSize = 10.sp,
                  color = Color.Gray
                )
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = certificate.directorName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = EduNavy
                )
                Text(
                  text = "Direção Geral EDUKAZA",
                  fontSize = 10.sp,
                  color = Color.Gray
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Code & QR badge
            Surface(
              color = Color(0xFFF1F5F9),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = "CÓDIGO ÚNICO DE AUTENTICIDADE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                  Text(text = certificate.verificationCode, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EduNavy)
                  Text(text = "Emitido em ${certificate.issueDate}", fontSize = 10.sp, color = Color.Gray)
                }

                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.QrCode, contentDescription = null, tint = EduNavy, modifier = Modifier.size(34.dp))
                }
              }
            }
          }
        }
      }

      // Legal condition notice card (per specification)
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = EduNavy, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Nota Legal & Acreditação em Angola",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = certificate.legalNotice,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 16.sp
            )
          }
        }
      }
    }
  }
}
