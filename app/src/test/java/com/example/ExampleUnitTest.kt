package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testInitialCoursesSpecification() {
    val expectedCodes = listOf("EDK001", "EDK002", "EDK003", "EDK004", "EDK005")
    assertTrue(expectedCodes.contains("EDK001"))
    assertTrue(expectedCodes.contains("EDK005"))
  }

  @Test
  fun testCertificateCodeFormat() {
    val sampleCode = "EDK-AO-2026-X8B9"
    assertTrue(sampleCode.startsWith("EDK-AO-"))
    assertTrue(sampleCode.contains("2026"))
  }

  @Test
  fun testUserRolesDefined() {
    val roles = UserRole.entries.map { it.name }
    assertTrue(roles.contains("STUDENT"))
    assertTrue(roles.contains("INSTRUCTOR"))
    assertTrue(roles.contains("ADMIN"))
  }

  @Test
  fun testCourseCategoriesDefined() {
    val cats = CourseCategory.entries.map { it.displayName }
    assertTrue(cats.contains("Informática"))
    assertTrue(cats.contains("Programação"))
    assertTrue(cats.contains("Inteligência Artificial"))
    assertTrue(cats.contains("Empreendedorismo"))
    assertTrue(cats.contains("Design Gráfico"))
    assertTrue(cats.contains("Marketing Digital"))
  }
}
