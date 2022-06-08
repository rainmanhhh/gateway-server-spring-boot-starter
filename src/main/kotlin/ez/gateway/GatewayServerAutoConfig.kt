package ez.gateway

import ez.auth.sak.AuthServiceApiKeyAutoConfiguration
import ez.auth.sak.ServiceApiKey
import ez.jwt.JwtAutoConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClient

@Suppress("unused")
@Import(
  AuthServiceApiKeyAutoConfiguration::class,
  JwtAutoConfiguration::class
)
@ComponentScan(basePackages = ["ez.gateway"])
@ConfigurationProperties("ez.gateway")
@EnableDiscoveryClient
@EnableCaching
class GatewayServerAutoConfig(
  private val serviceApiKey: ServiceApiKey
) {
  /**
   * uri to load rules. eg: `/BASE-DATA/rules/`
   */
  var ruleUri: String? = null

  @Bean
  fun gatewayClient(
    @Value("\${server.port}") port: String
  ) = WebClient.builder()
    .baseUrl("http://127.0.0.1:$port")
    .defaultHeaders { headers ->
      serviceApiKey.encodeLocal()?.let {
        headers.set(serviceApiKey.name, it)
      }
    }
    .build()
}