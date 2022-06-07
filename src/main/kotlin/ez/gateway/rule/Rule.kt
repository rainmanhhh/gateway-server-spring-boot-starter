package ez.gateway.rule

/**
 * 权限规则
 */
class Rule {
  /**
   * 权限分组，空字符串表示公共规则
   */
  var group: String? = null

  /**
   * 请求路径表达式，glob，以`/`开头
   */
  var pattern: String? = null

  /**
   * 类型，基本与shiro一致，但只有`user`而没有`authc`
   */
  var type: String? = null

  /**
   * 参数，例如`type`为`perms`时`param`为权限名，`type`为`roles`时`param`为角色名
   */
  var param: String? = null

  /**
   * 优先级，如果[strict]未设置，优先级大于0的视为硬规则，反之为软规则
   */
  var priority: Int? = null

  /**
   * 是否为严格模式（硬规则）
   */
  var strict: Boolean? = null

  /**
   * 备注
   */
  var remark: String? = null
}