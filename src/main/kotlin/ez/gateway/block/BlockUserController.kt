package ez.gateway.block

import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CacheConfig(cacheNames = ["blockedUsers"])
@RequestMapping("/_admin/blockUser")
@RestController
class BlockUserController {
  @CachePut
  @PostMapping("/{userId}")
  fun blockUser(@PathVariable userId: String): String = ""

  @CacheEvict
  @DeleteMapping("/{userId}")
  fun unBlockUser(@PathVariable userId: String) = Unit

  @Cacheable(unless = "#result == null")
  @GetMapping("/{userId}")
  fun getBlockedUser(@PathVariable userId: String): String? = null
}