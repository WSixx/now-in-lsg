package br.com.lucad.nowinlsg.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryBudgetMonitorTest {

    @Test
    fun lifecycleStartAndStop_registersAndUnregistersListenerIdempotently() {
        val gateway = FakeMemoryBudgetGateway()
        val trimCalls = mutableListOf<Long>()
        val monitor = MemoryBudgetMonitor(
            gateway = gateway,
            trimHandler = { trimCalls.add(it) },
            isDebuggable = false,
        )

        val owner = TestLifecycleOwner()
        owner.registry.addObserver(monitor)

        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(1, gateway.registerCount)
        assertTrue(gateway.hasActiveListener)

        // Simula chamada redundante de onStart sem onStop intermediário
        monitor.onStart(owner)
        assertEquals(1, gateway.registerCount)

        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        assertEquals(1, gateway.unregisterCount)
        assertFalse(gateway.hasActiveListener)

        // Simula chamada redundante de onStop após já desregistrado
        monitor.onStop(owner)
        assertEquals(1, gateway.unregisterCount)
    }

    @Test
    fun onOverBudget_invokesTrimHandlerOnlyWhileRegistered() {
        val gateway = FakeMemoryBudgetGateway()
        val trimCalls = mutableListOf<Long>()
        val monitor = MemoryBudgetMonitor(
            gateway = gateway,
            trimHandler = { trimCalls.add(it) },
            isDebuggable = false,
        )

        val owner = TestLifecycleOwner()
        owner.registry.addObserver(monitor)

        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        gateway.simulateOverBudget(128L * 1024L * 1024L)
        assertEquals(listOf(128L * 1024L * 1024L), trimCalls)

        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        gateway.simulateOverBudget(64L * 1024L * 1024L)
        assertEquals(1, trimCalls.size)
    }

    @Test
    fun dynamicBudget_isIdempotentAndRestoresManifestCeiling() {
        val gateway = FakeMemoryBudgetGateway()
        val monitor = MemoryBudgetMonitor(
            gateway = gateway,
            trimHandler = {},
            isDebuggable = false,
        )

        val browsingBudget = MemoryBudgetMonitor.BROWSING_BUDGET_BYTES
        monitor.applyTighterProcessBudget(browsingBudget)
        monitor.applyTighterProcessBudget(browsingBudget)

        assertEquals(listOf(browsingBudget), gateway.appliedBudgets)
        assertEquals(0, gateway.clearCount)

        monitor.restoreManifestBudget()
        monitor.restoreManifestBudget()

        assertEquals(1, gateway.clearCount)

        // Reaplicar após restaurar deve enviar novamente ao sistema
        monitor.applyTighterProcessBudget(browsingBudget)
        assertEquals(listOf(browsingBudget, browsingBudget), gateway.appliedBudgets)
    }

    @Test
    fun dynamicBudget_isSkippedInDebuggableBuilds() {
        val gateway = FakeMemoryBudgetGateway()
        val monitor = MemoryBudgetMonitor(
            gateway = gateway,
            trimHandler = {},
            isDebuggable = true,
        )

        monitor.applyTighterProcessBudget(MemoryBudgetMonitor.BROWSING_BUDGET_BYTES)
        monitor.restoreManifestBudget()

        assertTrue(gateway.appliedBudgets.isEmpty())
        assertEquals(0, gateway.clearCount)
    }

    @Test
    fun dynamicBudget_handlesIllegalArgumentExceptionGracefully() {
        val gateway = FakeMemoryBudgetGateway(throwOnSetBudget = true)
        var capturedError: Throwable? = null
        val monitor = MemoryBudgetMonitor(
            gateway = gateway,
            trimHandler = {},
            isDebuggable = false,
            onWarning = { _, error -> capturedError = error },
        )

        monitor.applyTighterProcessBudget(512L * 1024L * 1024L)

        assertTrue(capturedError is IllegalArgumentException)
        assertNull(gateway.appliedBudgets.firstOrNull())

        // Como não foi aplicado com sucesso, restoreManifestBudget não deve chamar clearProcessBudget
        monitor.restoreManifestBudget()
        assertEquals(0, gateway.clearCount)
    }

    private class TestLifecycleOwner : LifecycleOwner {
        val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle
            get() = registry
    }

    private class FakeMemoryBudgetGateway(
        private val throwOnSetBudget: Boolean = false,
    ) : MemoryBudgetGateway {
        var registerCount: Int = 0
            private set
        var unregisterCount: Int = 0
            private set
        var clearCount: Int = 0
            private set
        val appliedBudgets: MutableList<Long> = mutableListOf()

        private var activeListener: OverBudgetTrimHandler? = null

        val hasActiveListener: Boolean
            get() = activeListener != null

        override fun registerProcessOverBudgetListener(listener: OverBudgetTrimHandler): Boolean {
            registerCount++
            activeListener = listener
            return true
        }

        override fun unregisterProcessOverBudgetListener() {
            unregisterCount++
            activeListener = null
        }

        override fun setProcessBudgetBytes(budgetBytes: Long) {
            if (throwOnSetBudget) {
                throw IllegalArgumentException("Exceeds manifest limit")
            }
            appliedBudgets.add(budgetBytes)
        }

        override fun clearProcessBudget() {
            clearCount++
        }

        fun simulateOverBudget(budgetBytes: Long) {
            activeListener?.trimMemory(budgetBytes)
        }
    }
}
