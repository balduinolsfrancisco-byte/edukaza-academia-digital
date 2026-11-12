package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.components.EduBottomNavigation
import com.example.ui.components.EduTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.EdukazaTheme
import com.example.viewmodel.EduViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      EdukazaTheme {
        EdukazaApp()
      }
    }
  }
}

@Composable
fun EdukazaApp(
  viewModel: EduViewModel = viewModel()
) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val snackbarMessage by viewModel.snackbarMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  // Handle snackbar notifications
  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let { msg ->
      coroutineScope.launch {
        snackbarHostState.showSnackbar(msg)
        viewModel.clearToast()
      }
    }
  }

  // Back handler for custom screen stack
  val canNavigateBack = currentScreen !is Screen.Home

  BackHandler(enabled = canNavigateBack) {
    if (!viewModel.navigateBack()) {
      // At root
    }
  }

  val isMainScreen = currentScreen is Screen.Home ||
      currentScreen is Screen.MyCourses ||
      currentScreen is Screen.Certificates ||
      currentScreen is Screen.Profile

  val currentScreenName = when (currentScreen) {
    is Screen.Home -> "Home"
    is Screen.MyCourses -> "MyCourses"
    is Screen.Certificates -> "Certificates"
    is Screen.Profile -> "Profile"
    else -> ""
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      // Top bar is hidden only in sub-screens with their own custom header like LessonPlayer and CertificateDetail
      if (currentScreen !is Screen.LessonPlayer &&
          currentScreen !is Screen.CertificateDetail &&
          currentScreen !is Screen.AdminDashboard &&
          currentScreen !is Screen.InstructorDashboard
      ) {
        EduTopAppBar(
          currentUser = currentUser,
          canNavigateBack = canNavigateBack,
          onNavigateBack = { viewModel.navigateBack() },
          onSwitchRole = { newRole -> viewModel.switchUserRole(newRole) }
        )
      }
    },
    bottomBar = {
      if (isMainScreen) {
        EduBottomNavigation(
          currentScreenName = currentScreenName,
          onNavigateHome = { viewModel.navigateTo(Screen.Home) },
          onNavigateMyCourses = { viewModel.navigateTo(Screen.MyCourses) },
          onNavigateCertificates = { viewModel.navigateTo(Screen.Certificates) },
          onNavigateProfile = { viewModel.navigateTo(Screen.Profile) }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
          is Screen.Home -> HomeScreen(viewModel = viewModel)
          is Screen.MyCourses -> MyCoursesScreen(viewModel = viewModel)
          is Screen.Certificates -> CertificatesScreen(viewModel = viewModel)
          is Screen.Profile -> ProfileScreen(viewModel = viewModel)
          is Screen.CourseDetail -> CourseDetailScreen(courseId = screen.courseId, viewModel = viewModel)
          is Screen.LessonPlayer -> LessonPlayerScreen(courseId = screen.courseId, lessonId = screen.lessonId, viewModel = viewModel)
          is Screen.CertificateDetail -> CertificateDetailScreen(certificateId = screen.certificateId, viewModel = viewModel)
          is Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
          is Screen.InstructorDashboard -> InstructorDashboardScreen(viewModel = viewModel)
        }
      }
    }
  }
}
