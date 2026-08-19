package fr.claynum.fluflu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.claynum.fluflu.ui.FluFluApp
import fr.claynum.fluflu.ui.theme.FluFluTheme
import fr.claynum.fluflu.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FluFluTheme {
                val viewModel: MainViewModel = viewModel()
                FluFluApp(viewModel)
            }
        }
    }
}
