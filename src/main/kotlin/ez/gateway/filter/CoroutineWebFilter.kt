package ez.gateway.filter

import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

abstract class CoroutineWebFilter(
  override val once: Boolean = false
) : WebFilter, CoroutineFilter {
  override val processFlag = "__FILTER_PROCESSED_" + javaClass.name

  final override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
    doFilter(exchange, chain::filter)
}
