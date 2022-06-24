package ez.gateway.rule

import ez.gateway.GatewayServerAutoConfig
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import javax.annotation.PostConstruct

@RefreshScope
@Component
class RuleService(
  private val config: GatewayServerAutoConfig,
  private val gatewayClient: WebClient
) {
  companion object {
    private val logger = LoggerFactory.getLogger(RuleService::class.java)
  }

  @Volatile
  var ruleMap: Map<String, List<Rule>> = mapOf()
    protected set

  /**
   * - associate rules by group
   * - group name is empty string means it's a common rule
   * - if mode not set, rule with priority greater than 0 will be treated as hard
   */
  fun updateRules(rules: Iterable<Rule>) {
    val groupMap = hashMapOf<String, MutableList<Rule>>()
    val commonList = arrayListOf<Rule>()
    for (rule in rules) {
      rule.validate()
      if (rule.strict == null) rule.strict = rule.priority!! > 0
      val group = rule.group!!
      if (group == "") commonList.add(rule)
      else {
        val subList = groupMap[group] ?: kotlin.run {
          val newList = arrayListOf<Rule>()
          groupMap[group] = newList
          newList
        }
        subList.add(rule)
      }
    }
    for (value in groupMap.values) {
      value.addAll(commonList)
      value.sortBy { it.priority!! }
    }
    groupMap[""] = commonList.apply { sortBy { it.priority } }
    ruleMap = groupMap
  }

  @PostConstruct
  protected fun initLoad() = runBlocking {
    loadRules()
  }

  /**
   * @return true - load(and update) success; false -
   */
  suspend fun loadRules(): Boolean {
    val ruleUri = config.ruleUri
    return if (ruleUri.isNullOrBlank()) {
      logger.warn("ruleUri is empty")
      false
    } else {
      logger.info("loading rules")
      try {
        val rules = gatewayClient.get()
          .uri(ruleUri).accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .awaitBody<List<Rule>>()
        updateRules(rules)
        true
      } catch (e: Throwable) {
        logger.error("load rules failed", e)
        false
      }
    }
  }
}
