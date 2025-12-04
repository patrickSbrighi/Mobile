package com.example.mobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.mobile.ui.NavGraph
import com.example.mobile.ui.theme.MobileTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(ctx, prefs)
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            MobileTheme {
                val navController = rememberNavController()
                NavGraph(navController)
            }
        }
    }
}