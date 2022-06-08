package ez.gateway.rule

import org.springframework.web.bind.annotation.*

@RequestMapping("/_admin/rule")
@RestController
class RuleController(
  private val ruleService: RuleService
) {
  @GetMapping
  fun getRules() = ruleService.ruleMap

  @PutMapping
  fun putRules(@RequestBody rules: List<Rule>) = ruleService.updateRules(rules)

  @GetMapping("/load")
  suspend fun loadRules() = ruleService.loadRules()
}