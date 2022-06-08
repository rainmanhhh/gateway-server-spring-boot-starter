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
}
