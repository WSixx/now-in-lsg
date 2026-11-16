package br.com.lucad.nowinlsg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import br.com.lucad.nowinlsg.ui.screen.PokemonAdaptiveApp
import br.com.lucad.nowinlsg.ui.theme.NowInLSGTheme
import br.com.lucad.nowinlsg.util.MemoryBudgetMonitor

class MainActivity : ComponentActivity() {
    private lateinit var memoryBudgetMonitor: MemoryBudgetMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memoryBudgetMonitor = MemoryBudgetMonitor(this).also {
            lifecycle.addObserver(it)
        }
        enableEdgeToEdge()
        setContent {
            NowInLSGTheme {
                PokemonAdaptiveApp(
                    modifier = Modifier.fillMaxSize(),
                ) { isWebViewVisible ->
                    if (isWebViewVisible) {
                        memoryBudgetMonitor.restoreManifestBudget()
                    } else {
                        memoryBudgetMonitor.applyTighterProcessBudget(
                            MemoryBudgetMonitor.BROWSING_BUDGET_BYTES,
                        )
                    }
                }
            }
        }
    }
}
