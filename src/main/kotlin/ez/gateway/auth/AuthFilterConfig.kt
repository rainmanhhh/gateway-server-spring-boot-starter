package ez.gateway.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("ez.gateway.auth")
@Component
class AuthFilterConfig {
  /**
   * filter order
   */
  var order = 200

  /**
   * if no jwt token in [org.springframework.http.HttpHeaders.AUTHORIZATION], try to parse token in cookie with this name
   */
  var cookieName = "JWT"

  /**
   * sliding renewal grace window for cookie-sourced expired jwt, unit: seconds.
   * if the cookie token expired within this window, gateway re-issues a new token transparently
   * and continues the chain; otherwise the cookie is cleared and 401 is returned.
   * set to 0 to disable sliding renewal.
   */
  var renewGraceSeconds = 7 * 24 * 3600L
}
