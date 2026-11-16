package br.com.lucad.nowinlsg.util

import android.app.MemoryBudgetManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.imageLoader

/**
 * Callback para limpeza rápida de memória sob pressão.
 */
internal fun interface OverBudgetTrimHandler {
    fun trimMemory(budgetBytes: Long)
}

internal interface MemoryBudgetGateway {
    fun registerProcessOverBudgetListener(listener: OverBudgetTrimHandler): Boolean
    fun unregisterProcessOverBudgetListener()
    fun setProcessBudgetBytes(budgetBytes: Long)
    fun clearProcessBudget()
}

/**
 * Monitora o orçamento de memória do processo via [MemoryBudgetManager] (Android 17 QPR2 / API 37.2+)
 * usando [MemoryBudgetManager.OnOverBudgetListener] vinculado ao ciclo de vida ([onStart]/[onStop]).
 *
 * Seguindo as boas práticas oficiais:
 * - Executa limpeza rápida, sem alocações ou novas threads
 * - Evita apertar dinamicamente o orçamento em builds debuggable (que possuem overhead extra).
 */
class MemoryBudgetMonitor internal constructor(
    private val gateway: MemoryBudgetGateway,
    private val trimHandler: OverBudgetTrimHandler,
    private val isDebuggable: Boolean,
    private val onWarning: (String, Throwable?) -> Unit = { message, error ->
        Log.w(TAG, message, error)
    },
) : DefaultLifecycleObserver {

    constructor(context: Context) : this(
        gateway = createPlatformGateway(context.applicationContext),
        trimHandler = createDefaultTrimHandler(context.applicationContext),
        isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
    )

    private var isRegistered: Boolean = false
    private var lastAppliedDynamicBudgetBytes: Long? = null

    override fun onStart(owner: LifecycleOwner) {
        if (isRegistered) return
        isRegistered = gateway.registerProcessOverBudgetListener(trimHandler)
    }

    override fun onStop(owner: LifecycleOwner) {
        if (!isRegistered) return
        gateway.unregisterProcessOverBudgetListener()
        isRegistered = false
    }

    /**
     * Aplica um orçamento dinâmico mais restritivo para fluxos leves (ex.: listagem sem WebView).
     */
    fun applyTighterProcessBudget(budgetBytes: Long) {
        if (isDebuggable || (budgetBytes <= 0L)) return
        if (lastAppliedDynamicBudgetBytes == budgetBytes) return

        try {
            gateway.setProcessBudgetBytes(budgetBytes)
            lastAppliedDynamicBudgetBytes = budgetBytes
        } catch (e: IllegalArgumentException) {
            onWarning("Requested dynamic memory budget exceeds manifest or system ceiling", e)
        }
    }

    /**
     * Limpa o orçamento dinâmico para restaurar o teto declarado no AndroidManifest.xml
     * (ex.: ao abrir conteúdo pesado como o WebView).
     */
    fun restoreManifestBudget() {
        if (isDebuggable) return
        if (lastAppliedDynamicBudgetBytes == null) return

        gateway.clearProcessBudget()
        lastAppliedDynamicBudgetBytes = null
    }

    companion object {
        private const val TAG = "MemoryBudgetMonitor"
        const val BROWSING_BUDGET_BYTES: Long = 128L * 1024L * 1024L

        private fun createDefaultTrimHandler(appContext: Context): OverBudgetTrimHandler {
            return OverBudgetTrimHandler {
                // Alvo de alto rendimento e liberação imediata, sem novas alocações ou threads.
                appContext.imageLoader.memoryCache?.clear()
            }
        }

        private fun createPlatformGateway(appContext: Context): MemoryBudgetGateway {
            return if (Build.VERSION.SDK_INT_FULL >= Build.VERSION_CODES_FULL.CINNAMON_BUN_2) {
                Api37Qpr2MemoryBudgetGateway(appContext)
            } else {
                NoOpMemoryBudgetGateway
            }
        }
    }
}

private object NoOpMemoryBudgetGateway : MemoryBudgetGateway {
    override fun registerProcessOverBudgetListener(listener: OverBudgetTrimHandler): Boolean = false
    override fun unregisterProcessOverBudgetListener() = Unit
    override fun setProcessBudgetBytes(budgetBytes: Long) = Unit
    override fun clearProcessBudget() = Unit
}

@RequiresApi(Build.VERSION_CODES_FULL.CINNAMON_BUN_2)
private class Api37Qpr2MemoryBudgetGateway(
    private val appContext: Context,
) : MemoryBudgetGateway {

    private val budgetManager: MemoryBudgetManager? by lazy(LazyThreadSafetyMode.NONE) {
        appContext.getSystemService(MemoryBudgetManager::class.java)
    }

    private val mainLooper: Looper = appContext.mainLooper
    private var activeListener: MemoryBudgetManager.OnOverBudgetListener? = null

    override fun registerProcessOverBudgetListener(listener: OverBudgetTrimHandler): Boolean {
        val manager = budgetManager ?: return false
        if (activeListener != null) return true

        val platformListener = MemoryBudgetManager.OnOverBudgetListener { budgetBytes ->
            // Mensagem constante evita alocação de String sob pressão de memória.
            Log.w(TAG, "Process exceeded memory budget; evicting image memory cache")
            listener.trimMemory(budgetBytes)
        }
        activeListener = platformListener
        manager.registerProcessOverBudgetListener(mainLooper, platformListener)
        return true
    }

    override fun unregisterProcessOverBudgetListener() {
        val manager = budgetManager ?: return
        val listener = activeListener ?: return
        manager.unregisterProcessOverBudgetListener(listener)
        activeListener = null
    }

    override fun setProcessBudgetBytes(budgetBytes: Long) {
        budgetManager?.processBudgetBytes = budgetBytes
    }

    override fun clearProcessBudget() {
        budgetManager?.clearProcessBudget()
    }

    private companion object {
        private const val TAG = "MemoryBudgetMonitor"
    }
}
