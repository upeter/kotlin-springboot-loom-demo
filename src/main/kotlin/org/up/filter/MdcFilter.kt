package org.up.filter


import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.*
import kotlin.math.absoluteValue
import kotlin.random.Random

@Component
@Order(1)
class MDCRequestFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        try {
            MDC.put(MDC_REQUEST_ID, request.getParameter("mdc") ?: "gen-mdc-${Random.nextInt().absoluteValue}")
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

    override fun destroy() {}

    companion object {
        const val MDC_REQUEST_ID = "request_id"
    }
}
