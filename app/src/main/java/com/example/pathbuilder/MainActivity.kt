package com.example.pathbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.preference.PreferenceManager
import com.example.pathbuilder.app.AppContainer
import com.example.pathbuilder.app.PathBuilderApp
import com.example.pathbuilder.ui.theme.PathbuilderTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        AppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        Configuration.getInstance().userAgentValue = packageName
        
        enableEdgeToEdge()
        setContent {
            PathbuilderTheme {
                PathBuilderApp(appContainer)
            }
        }
    }
}