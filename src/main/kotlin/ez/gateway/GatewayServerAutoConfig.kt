package ez.gateway

import ez.auth.sak.AuthServiceApiKeyAutoConfiguration
import ez.jwt.JwtAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@Suppress("unused")
@Import(
  AuthServiceApiKeyAutoConfiguration::class,
  JwtAutoConfiguration::class
)
@ComponentScan(basePackages = ["ez.gateway"])
@ConfigurationProperties("gateway")
@EnableCaching
class GatewayServerAutoConfig