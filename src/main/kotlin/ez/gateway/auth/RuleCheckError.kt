package ez.gateway.auth

import ez.gateway.rule.Rule

class RuleCheckError(rule: Rule) : RuntimeException(rule.group!! + ':' + rule.pattern!!)