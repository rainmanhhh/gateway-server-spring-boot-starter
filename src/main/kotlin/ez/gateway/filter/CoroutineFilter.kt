package ez.gateway.filter

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

interface CoroutineFilter {
  /**
   * true - only execute once during one request; false - execute every time even if the request was forwarded by handler A to handler B
   */
  val once: Boolean
  val processFlag: String

  fun doFilter(
    exchange: ServerWebExchange,
    chain: (exchange: ServerWebExchange) -> Mono<Void>
  ): Mono<Void> {
    val flag = if (once) exchange.getAttribute<Any>(processFlag) else null
    return if (flag == null) mono {
      if (beforeChain(exchange)) {
        chain(exchange).awaitFirstOrNull()
        afterChain(exchange)
      }
    }.onErrorStop().then() else chain(exchange)
  }

  /**
   * @return true - continue to next step; false - skip all remain steps and return to client
   */
  suspend fun beforeChain(exchange: ServerWebExchange): Boolean

  suspend fun afterChain(exchange: ServerWebExchange)
}