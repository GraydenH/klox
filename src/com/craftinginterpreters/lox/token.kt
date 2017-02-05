/**
 * Created by Grayden on 2017-02-04.
 */

package com.craftinginterpreters.lox

internal class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {

  override fun toString(): String {
    return type.toString() + " " + lexeme + " " + literal
  }
}


internal enum class TokenType {
  // Single-character tokens.
  LEFT_PAREN,
  RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

  // One or two character tokens.
  BANG,
  BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESS, LESS_EQUAL,

  // Literals.
  IDENTIFIER,
  STRING, NUMBER,

  // Keywords.
  AND,
  CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
  PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

  EOF
}
