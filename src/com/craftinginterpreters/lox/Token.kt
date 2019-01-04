/**
 * Created by Grayden on 2017-02-04.
 */

package com.craftinginterpreters.lox

class Token(val type: TokenType, val lexeme: String, val literal: Any, val line: Int) {

  override fun toString(): String {
    return type.toString() + " " + lexeme + " " + literal
  }
}