package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*
import java.lang.RuntimeException

class Parser (private val tokens: List<Token>) {

  // fields

  private var current = 0

  // types

  private class ParseError: RuntimeException()

  // entry point

  fun parse(): Expr? {
    return try {
      expression()
    } catch (error: ParseError) {
      null
    }
  }

  // utils

  private fun isAtEnd() = peek().type == EOF
  private fun peek() = tokens[current]
  private fun previous() = tokens[current - 1]

  private fun check(type: TokenType): Boolean {
    return if (isAtEnd()) {
      false
    } else {
      peek().type == type
    }
  }

  private fun match (vararg types: TokenType): Boolean {
    for (type in types) {
      if (check(type)) {
        advance()
        return true
      }
    }

    return false
  }

  private fun advance(): Token {
    if (!isAtEnd()) {
      current++
    }

    return previous()
  }

  private fun leftAssociate(operand: () -> Expr, vararg types: TokenType): Expr {
    var expr = operand()

    while (match(*types)) {
      val operator = previous()
      val right = operand()
      expr = Expr.Binary(expr, operator, right)
    }

    return expr
  }

  private fun consume(type: TokenType, message: String): Token {
    if (check(type)) {
      return advance()
    } else {
      throw error(peek(), message)
    }
  }

  private fun error(token: Token, message: String): ParseError {
    Lox.error(token, message)
    return ParseError()
  }

  private fun synchronize() {
    advance()

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON ||
        peek().type == CLASS || peek().type == FUN ||
        peek().type == VAR || peek().type == FOR ||
        peek().type == IF || peek().type == WHILE ||
        peek().type == PRINT || peek().type == RETURN) {
        return
      }

      advance()
    }
  }

  // rules

  private fun expression() = statement()

  private fun statement(): Expr {
    return leftAssociate(::ternary, SEMICOLON)
  }

  private fun ternary(): Expr {
    var expr = equality()

    while (match(QUESTION)) {
      val first = previous()
      val middle = equality()

      if (match(COLON)) {
        val second = previous()
        val right = equality()

        expr = Expr.Ternary(expr, first, middle, second, right)
      } else {
        throw error(peek(), "Expected additonal operator.")
      }
    }

    return expr
  }

  private fun equality(): Expr {
    return leftAssociate(::comparison, BANG_EQUAL, EQUAL_EQUAL)
  }

  private fun comparison(): Expr {
    return leftAssociate(::addition, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
  }

  private fun addition(): Expr {
    return leftAssociate(::multiplication, MINUS, PLUS)
  }

  private fun multiplication(): Expr {
    return leftAssociate(::unary, SLASH, STAR)
  }

  private fun unary(): Expr {
    if (match(BANG, MINUS)) {
      val operator = previous()
      val right = unary()
      return Expr.Unary(operator, right)
    }

    return primary()
  }

  private fun primary(): Expr {
    return when {
      match(FALSE) -> Expr.Literal(false)
      match(TRUE) -> Expr.Literal(true)
      match(NIL) -> Expr.Literal(null)
      match(NUMBER, STRING) -> Expr.Literal(previous().literal)
      match(LEFT_PAREN) -> {
        val expr = expression()
        consume(RIGHT_PAREN, "Expect ')' after expression.")
        Expr.Grouping(expr)
      }
      else -> throw error(peek(), "Expect expression.")
    }
  }
}