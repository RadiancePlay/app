package com.example

import com.example.data.update.UpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testVersionComparison() {
    assertTrue(UpdateManager.isVersionNewer("1.0", "1.1"))
    assertTrue(UpdateManager.isVersionNewer("1.0", "1.0.1"))
    assertTrue(UpdateManager.isVersionNewer("1.0", "2.0"))
    assertFalse(UpdateManager.isVersionNewer("1.0", "1.0"))
    assertFalse(UpdateManager.isVersionNewer("1.1", "1.0"))
    assertFalse(UpdateManager.isVersionNewer("2.0", "1.9.9"))
  }
}
