package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.shizuku.ShizukuBridge
import com.example.ui.RadiancePlayApp
import com.example.ui.theme.RadiancePlayTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ShizukuBridge.init()
    enableEdgeToEdge()
    setContent {
      RadiancePlayTheme {
        RadiancePlayApp()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // Re-check Shizuku status as soon as user returns to the app from Shizuku
    ShizukuBridge.checkStatus()
  }
}

@Preview(showBackground = true)
@Composable
fun RadiancePlayPreview() {
  RadiancePlayTheme {
    RadiancePlayApp()
  }
}
