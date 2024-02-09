package org.up

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

class ThreadsVsVirtualThreads {

    @Test
    fun `many Coroutines`() = measure("Coroutines") {
        runBlocking {
            (1..100_000).map {
                launch {
                    counter.incrementAndGet()
                    delay(2000)
                }
            }
        }
        println("Coroutines: Ready to Roll")
    }

    @Test
    fun `many Threads`() {
        measure("Threads") {
            val threads = (1..100_000).map {
                thread {
                    counter.incrementAndGet()
                    sleep(2000)
                }
            }
            println("Threads: Ready to Roll")
            threads.forEach {
                it.join()
            }
        }
    }

    @Test
    fun `many virtual Threads`() {
        measure("VirtualThreads") {
            val threads = (1..100_000).map {
                Thread.startVirtualThread {
                    counter.incrementAndGet()
                    sleep(2000)
                }
            }
            println("Virtual Threads: Ready to Roll")
            threads.forEach {
                it.join()
            }
        }
    }
}


fun <T> measure(construct:String, block: () -> T): T {
    val start = System.currentTimeMillis()
    try {
        return block().also {
            println("Took: ${System.currentTimeMillis() - start} ms with: ${counter.get()} $construct")
        }
    } catch(ex:Throwable) {
        println("Exception occured. $construct so far: ${counter.get()}. Exception: ${ex.message}")
        throw ex
    }
}
val counter = AtomicLong(0)
