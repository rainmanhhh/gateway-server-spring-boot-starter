package ez.gateway.extension

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun HttpStatus.thr(
  cause: Throwable,
  reason: String = cause.javaClass.name + ':' + cause.message
): Nothing =
  throw ResponseStatusException(this, reason, cause)
