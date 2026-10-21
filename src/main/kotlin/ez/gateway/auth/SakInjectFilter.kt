package ez.gateway.auth

import ez.auth.sak.ServiceApiKey
import ez.gateway.GatewayServerAutoConfig
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * inject SAK header(X-SAK by default) into the request forwarded to downstream services,
 * so that a downstream service can tell "forwarded by gateway" from "called directly on the internal network".
 *
 * it must `set`(overwrite) the header instead of `add`(append): a forged X-SAK from an external client
 * must never reach downstream services.
 */
@Component
class SakInjectFilter(
  private val config: GatewayServerAutoConfig,
  private val serviceApiKey: ServiceApiKey
) : GlobalFilter, Ordered {

  // only needs to run before NettyRoutingFilter(Ordered.LOWEST_PRECEDENCE) which sends the request out
  override fun getOrder() = 0

  override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
    if (!config.injectSak) return chain.filter(exchange)
    val sak = serviceApiKey.encodeLocal() ?: return chain.filter(exchange)
    val request = exchange.request.mutate()
      .headers { it.set(serviceApiKey.name, sak) }
      .build()
    return chain.filter(exchange.mutate().request(request).build())
  }
}
