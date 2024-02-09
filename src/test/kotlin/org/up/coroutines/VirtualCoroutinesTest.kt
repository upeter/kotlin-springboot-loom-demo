package org.up.coroutines

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.up.utils.runVirtual
import java.lang.Thread.sleep
import kotlin.system.measureTimeMillis

class VirtualCoroutinesTest {

    @Test
    fun testVirtual():Unit {
        assertMeasure(500) {
            runVirtual {

                val reply1 = async {
                    sleep(500)
                    Thread.currentThread().toString().also(::println)
                }
                val reply2 = async {
                    sleep(500)
                    Thread.currentThread().toString().also(::println)
                }

                println(reply1.await() + " " + reply2.await())
            }
        }
    }

    @Test
    fun testBlocking() {
        assertMeasure(1000) {
            runBlocking {

                val reply1 = async {
                    sleep(500)
                    Thread.currentThread().toString().also(::println)
                }
                val reply2 = async {
                    sleep(500)
                    Thread.currentThread().toString().also(::println)
                }

                println(reply1.await() + " " + reply2.await())
            }
        }
    }


    fun <T> assertMeasure(expected: Long, block: () -> T) =
        measureTimeMillis { block() }.toDouble().shouldBe(expected.toDouble().plusOrMinus(200.0))
}



