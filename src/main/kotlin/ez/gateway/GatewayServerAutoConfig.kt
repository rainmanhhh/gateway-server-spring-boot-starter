package ez.gateway

import ez.gateway.block.BlockUserController
import ez.gateway.rule.RuleController
import ez.gateway.rule.RuleService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean

@Suppress("unused")
@ConfigurationProperties("gateway")
@EnableCaching
class GatewayServerAutoConfig {
  @Bean
  fun ruleService() = RuleService()

  @Bean
  fun ruleController(ruleService: RuleService) = RuleController(ruleService)

  @Bean
  fun blockUserController() = BlockUserController()
}