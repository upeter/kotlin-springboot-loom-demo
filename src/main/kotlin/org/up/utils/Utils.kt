package org.up.utils

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.lang.reflect.Field
import java.util.concurrent.Executors
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> org.springframework.http.HttpEntity<T>.body():T = this.body ?: throw IllegalArgumentException("Body was null")

fun <T> runVirtual(context:CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T =
    Executors.newVirtualThreadPerTaskExecutor().use { threadPool ->
        runBlocking(context + threadPool.asCoroutineDispatcher(), block)
    }


val Dispatchers.VT
    get() =  Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()


inline val <reified T:Any> T.logger: Logger
    get() = LoggerFactory.getLogger(T::class.java)

/**
 * Don't use this in production! Just a try-out
 */
class TransactionResourcesThreadContextElement(private val contextMap:MutableMap<Any, Any> = TransactionSynchronizationManager.getResourceMap()) :
    ThreadContextElement<Map<Any, Any>>, AbstractCoroutineContextElement(Key){
    override fun updateThreadContext(context: CoroutineContext): Map<Any, Any> {
        return contextMap.apply { setCurrent(this) }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Map<Any, Any>) {
        setCurrent(oldState)
    }

    private fun setCurrent(context: Map<Any, Any>) {
        context.forEach{(key, value) ->
            TransactionSynchronizationManager.unbindResourceIfPossible(key)
            TransactionSynchronizationManager.bindResource(key, value)
        }
    }
    companion object Key : CoroutineContext.Key<TransactionResourcesThreadContextElement>
}


class TransactionSynchronizationThreadContextElement(private val state:Set<TransactionSynchronization> = TransactionSynchronizationManager.getSynchronizations().toSet()) :
    ThreadContextElement<Set<TransactionSynchronization>>, AbstractCoroutineContextElement(Key){
    override fun updateThreadContext(context: CoroutineContext): Set<TransactionSynchronization> {
        return state.apply {  setCurrent(this)}
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Set<TransactionSynchronization>) {
        setCurrent(oldState)
    }


    private fun setCurrent(context: Set<TransactionSynchronization>) {
        synchronization().set(context)
    }

    private fun synchronization(): ThreadLocal<Set<TransactionSynchronization>> {
        val field: Field = TransactionSynchronizationManager::class.java.getDeclaredField("synchronizations")
        field.setAccessible(true)
        return (field.get(null) as ThreadLocal<Set<TransactionSynchronization>>)
    }
    companion object Key : CoroutineContext.Key<TransactionSynchronizationThreadContextElement>
}



class TransactionNameThreadContextElement(private val state:String? = TransactionSynchronizationManager.getCurrentTransactionName()) :
    ThreadContextElement<String?>, AbstractCoroutineContextElement(Key){
    override fun updateThreadContext(context: CoroutineContext): String? {
        return state.apply { setCurrent(this) }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: String?) {
        setCurrent(oldState)
    }

    private fun setCurrent(context: String?) {
        TransactionSynchronizationManager.setCurrentTransactionName(context)
    }
    companion object Key : CoroutineContext.Key<TransactionNameThreadContextElement>
}

class TransactionReadOnlyThreadContextElement(private val state:Boolean = TransactionSynchronizationManager.isCurrentTransactionReadOnly()) :
    ThreadContextElement<Boolean>, AbstractCoroutineContextElement(Key){
    override fun updateThreadContext(context: CoroutineContext): Boolean {
        return state.apply { setCurrent(this) }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Boolean) {
        setCurrent(oldState)
    }

    private fun setCurrent(context: Boolean) {
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(context)
    }
    companion object Key : CoroutineContext.Key<TransactionReadOnlyThreadContextElement>
}

class TransactionIsolationLevelThreadContextElement(private val state:Int? = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel()) :
    ThreadContextElement<Int?>, AbstractCoroutineContextElement(Key){
    override fun updateThreadContext(context: CoroutineContext): Int? {
        return state.apply { setCurrent(this) }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Int?) {
        setCurrent(oldState)
    }

    private fun setCurrent(context: Int?) {
        TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(context)
    }
    companion object Key : CoroutineContext.Key<TransactionIsolationLevelThreadContextElement>
}

class TransactionActiveThreadContextElement(private val state:Boolean = TransactionSynchronizationManager.isActualTransactionActive()) :
    ThreadContextElement<Boolean>, AbstractCoroutineContextElement(Key){
    override fun updateThreadContext(context: CoroutineContext): Boolean {
        return state.apply { setCurrent(this) }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Boolean) {
        setCurrent(oldState)
    }

    private fun setCurrent(context: Boolean) {
        TransactionSynchronizationManager.setActualTransactionActive(context)
    }
    companion object Key : CoroutineContext.Key<TransactionActiveThreadContextElement>
}

fun TransactionContext(): CoroutineContext = TransactionReadOnlyThreadContextElement() + TransactionActiveThreadContextElement() + TransactionNameThreadContextElement()+ TransactionResourcesThreadContextElement() + TransactionIsolationLevelThreadContextElement() + TransactionSynchronizationThreadContextElement()