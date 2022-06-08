package ez.gateway.auth

import ez.auth.sak.ServiceApiKey
import ez.gateway.block.BlockUserController
import ez.gateway.block.UserIsBlockedException
import ez.gateway.extension.thr
import ez.gateway.filter.CoroutineWebFilter
import ez.gateway.rule.Rule
import ez.gateway.rule.RuleService
import ez.jwt.JwtUser
import ez.jwt.JwtUtil
import ez.jwt.isAnon
import io.jsonwebtoken.ExpiredJwtException
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Component
class AuthFilter(
  private val config: AuthFilterConfig,
  private val serviceApiKey: ServiceApiKey,
  private val ruleService: RuleService,
  private val applicationContext: ApplicationContext,
  private val jwtUtil: JwtUtil,
  private val blockUserController: BlockUserController
) : CoroutineWebFilter(true), Ordered {
  companion object {
    private val logger = LoggerFactory.getLogger(AuthFilter::class.java)
  }

  override fun getOrder(): Int = config.order

  private val pathMatcher = AntPathMatcher()

  private fun String.toPair(delimiter: Char = '/'): Pair<String, String> {
    val i = indexOfFirst { it == delimiter }
    return if (i < 0) "" to this
    else substring(0, i) to substring(i)
  }

  override suspend fun beforeChain(exchange: ServerWebExchange): Boolean {
    // call one service from another
    if (serviceApiKey.checkRemote(exchange.request.headers[serviceApiKey.name]?.firstOrNull())) return true

    // call service from other client(browser/app)
    // read auth rules
    val ruleMap = ruleService.ruleMap
    if (ruleMap.isEmpty()) throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "rules not loaded")
    // parse request path into group(service name) and sub path(service request path)
    val reqPath = exchange.request.uri.path ?: "/"
    val (group, path) =
      if (reqPath.length > 1 && reqPath[1].isUpperCase()) {
        reqPath.substring(1).toPair()
      } else {
        applicationContext.applicationName to reqPath
      }

    // parse jwt user
    val user = try {
      val u = jwtUtil.verifyAuthHeader(exchange.request.headers[HttpHeaders.AUTHORIZATION])
      if (u.isAnon && config.cookieName.isNotBlank()) { // try to use cookie
        val tokenInCookie =
          exchange.request.cookies.getFirst(config.cookieName)?.value?.substringBefore(';')
        if (tokenInCookie == null) u
        else jwtUtil.verifyTokenWithPrefix(URLDecoder.decode(tokenInCookie, StandardCharsets.UTF_8))
      } else u
    } catch (e: ExpiredJwtException) {
      HttpStatus.UNAUTHORIZED.thr(e)
    }
    // check whether the user is blocked
    val userId = user.id
    blockUserController.getBlockedUser(userId)?.let {
      logger.debug("user is blocked: {}", userId)
      HttpStatus.FORBIDDEN.thr(UserIsBlockedException(userId))
    }

    // choose rule list by group(if group is '', use common rule list)
    val ruleList = ruleMap[group] ?: ruleMap[""] ?: emptyList()

    // check path with rules
    /**
     * a "soft" rule means if checking not pass, loop will continue to next rule(unless it's the last matching rule);
     * a "hard" rule means the chain immediately throw 403 error if checking not pass;
     * rule with priority greater than 0 is a "hard" rule
     */
    var failedSoftRule: Rule? = null
    for (rule in ruleList) {
      if (rule.match(path)) {
        val checkResult = rule.check(exchange, user)
        logger.debug(
          "group:{}, pattern:{}, reqPath:{}, check result: {}",
          rule.group,
          rule.pattern,
          reqPath,
          checkResult
        )
        if (checkResult) return true
        else {
          if (rule.strict == true) HttpStatus.FORBIDDEN.thr(RuleCheckError(rule))
          else failedSoftRule = rule // continue to next rule
        }
      } // else continue to next rule
    }
    if (failedSoftRule != null) HttpStatus.FORBIDDEN.thr(RuleCheckError(failedSoftRule))
    return true
  }

  override suspend fun afterChain(exchange: ServerWebExchange) {
  }

  private fun Rule.match(path: String) = pathMatcher.match(pattern!!, path)

  /**
   * @return true-allowed; false-denied
   */
  private fun Rule.check(exchange: ServerWebExchange, user: JwtUser): Boolean {
    if (user.roles.contains(jwtUtil.config.adminRole)) return true
    return when (type) {
      "anon" -> true
      "user" -> !user.isAnon
      "roles" -> user.roles.contains(param ?: "")
      "perms" -> user.perms.contains(param ?: "")
      "rest" -> {
        val restPerm = param ?: ""
        if (user.perms.contains(restPerm)) true
        else {
          //methodValue is enum HttpMethod::name, so it should be uppercase(GET,POST,...)
          val operationPerm = restPerm + ":" + exchange.request.methodValue
          user.perms.contains(operationPerm)
        }
      }
      else -> false // unknown auth type
    }
  }
}
