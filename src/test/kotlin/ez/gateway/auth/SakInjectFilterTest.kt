package ez.gateway.auth

import ez.auth.sak.ServiceApiKey
import ez.gateway.GatewayServerAutoConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class SakInjectFilterTest {
  private val serviceApiKey = ServiceApiKey().apply { value = "test-value" }

  private fun newFilter(injectSak: Boolean = true): SakInjectFilter {
    val config = GatewayServerAutoConfig(serviceApiKey).apply { this.injectSak = injectSak }
    return SakInjectFilter(config, serviceApiKey)
  }

  private fun exchange(): MockServerWebExchange =
    MockServerWebExchange.from(MockServerHttpRequest.get("/FOO/bar").build())

  private fun forwarded(filter: SakInjectFilter, exchange: ServerWebExchange): ServerWebExchange {
    var seen: ServerWebExchange? = null
    filter.filter(exchange) { e ->
      seen = e
      Mono.empty()
    }.block()
    return requireNotNull(seen)
  }

  @Test
  fun `injects sak header when enabled`() {
    val ex = exchange()
    val forwarded = forwarded(newFilter(), ex)
    assertEquals(serviceApiKey.encodeLocal(), forwarded.request.headers.getFirst("X-SAK"))
  }

  @Test
  fun `overwrites forged sak header instead of appending`() {
    val ex = MockServerWebExchange.from(
      MockServerHttpRequest.get("/FOO/bar").header("X-SAK", "forged").build()
    )
    val forwarded = forwarded(newFilter(), ex)
    assertEquals(listOf(serviceApiKey.encodeLocal()), forwarded.request.headers["X-SAK"])
  }

  @Test
  fun `keeps request untouched when disabled`() {
    val ex = exchange()
    val forwarded = forwarded(newFilter(injectSak = false), ex)
    assertNull(forwarded.request.headers.getFirst("X-SAK"))
  }

  @Test
  fun `keeps request untouched when sak value is not configured`() {
    serviceApiKey.value = ""
    val ex = exchange()
    val forwarded = forwarded(newFilter(), ex)
    assertNull(forwarded.request.headers.getFirst("X-SAK"))
  }
}
