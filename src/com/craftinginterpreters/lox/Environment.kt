package com.craftinginterpreters.lox

import java.util.HashMap

internal class Environment(
  private val enclosing: Environment? = null,
  private val values: HashMap<String, Any> = HashMap()) {

  fun define(name: String, value: Any) {
    values[name] = value
  }

  operator fun get(name: Token): Any {
    return when {
      values.containsKey(name.lexeme) -> values[name.lexeme]!!
      enclosing != null -> enclosing[name]
      else -> throw RuntimeError(name, "Undefined variable '" + name.lexeme + "'.")
    }
  }

  fun getAt(distance: Int, name: String): Any {
    return ancestor(distance).values[name]!!
  }

  fun assign(name: Token, value: Any) {
    if (values.containsKey(name.lexeme)) {
      values[name.lexeme] = value
      return
    } else if (enclosing != null) {
      enclosing.assign(name, value)
      return
    }

    throw RuntimeError(name,"Undefined variable '" + name.lexeme + "'.")
  }

  fun assignAt(distance: Int, name: Token, value: Any) {
    ancestor(distance).values[name.lexeme] = value
  }

  private fun ancestor(distance: Int): Environment {
    var environment = this
    for (i in 0 until distance) {
      environment = environment.enclosing!!
    }

    return environment
  }
}
