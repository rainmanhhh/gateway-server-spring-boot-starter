package ez.gateway.auth

import ez.auth.sak.AuthServiceApiKeyAutoConfiguration
import ez.auth.sak.ServiceApiKey
import ez.gateway.block.BlockUserController
import ez.gateway.rule.RuleService
import ez.jwt.JwtAutoConfiguration
import ez.jwt.JwtUtil
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@Import(
  AuthServiceApiKeyAutoConfiguration::class,
  JwtAutoConfiguration::class
)
@ConfigurationProperties("gateway.auth")
class AuthFilterConfig {
  /**
   * filter order
   */
  var order = 200

  /**
   * if no jwt token in [org.springframework.http.HttpHeaders.AUTHORIZATION], try to parse token in cookie with this name
   */
  var cookieName = "JWT"

  @Bean
  fun authFilter(
    serviceApiKey: ServiceApiKey,
    ruleService: RuleService,
    applicationContext: ApplicationContext,
    jwtUtil: JwtUtil,
    blockUserController: BlockUserController
  ) =
    AuthFilter(this, serviceApiKey, ruleService, applicationContext, jwtUtil, blockUserController)
}
