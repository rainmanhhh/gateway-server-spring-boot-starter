package ez.gateway.rule

import org.springframework.stereotype.Component

@Component
class RuleService {
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
      val priority = rule.priority ?: 0
      if (rule.strict == null) rule.strict = priority > 0
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
}
