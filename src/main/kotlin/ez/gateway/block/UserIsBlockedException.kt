package ez.gateway.block

import io.jsonwebtoken.JwtException

class UserIsBlockedException(userId: String): JwtException(userId)