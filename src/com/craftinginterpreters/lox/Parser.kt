package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*
import java.lang.RuntimeException
import java.util.ArrayList
import java.time.temporal.TemporalAdjusters.previous



class Parser (private val tokens: List<Token>) {

  // fields

  private var current = 0

  private val isAtEnd
    get() = peek.type == EOF

  private val peek
    get() = tokens[current]

  private val previous
    get() = tokens[current - 1]

  // types

  private class ParseError: RuntimeException()

  // entry point

  fun parse(): List<Stmt> {
    val statements = ArrayList<Stmt>()

    while (!isAtEnd) {
      statements.add(declaration())
    }

    return statements
  }

  // utils

  private fun check(type: TokenType): Boolean {
    return if (isAtEnd) {
      false
    } else {
      peek.type == type
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
    if (!isAtEnd) {
      current++
    }

    return previous
  }

  private fun leftAssociate(operand: () -> Expr, vararg types: TokenType): Expr {
    var expr = operand()
    val error = expr is Expr.Error

    while (match(*types)) {
      val operator = previous
      val right = operand()
      expr = Expr.Binary(expr, operator, right)
    }

    if (error) {
      return Expr.Error()
    }

    return expr
  }

  private fun consume(type: TokenType): Token {
    if (check(type)) {
      return advance()
    } else {
      throw error(peek, "Expect token '${type.name}'.")
    }
  }

  private fun error(token: Token, message: String): ParseError {
    Lox.error(token, message)
    return ParseError()
  }

  private fun synchronize() {
    advance()

    while (!isAtEnd) {
      if (previous.type == SEMICOLON ||
        peek.type == CLASS || peek.type == FUN ||
        peek.type == VAR || peek.type == FOR ||
        peek.type == IF || peek.type == WHILE ||
        peek.type == PRINT || peek.type == RETURN) {
        return
      }

      advance()
    }
  }

  // rules

  private fun declaration(): Stmt {
    return try {
      if (match(VAR)) varDeclaration() else statement()
    } catch (error: ParseError) {
      synchronize()
      Stmt.Error()
    }
  }

  private fun varDeclaration(): Stmt {
    val name = consume(IDENTIFIER)

    val initializer = if (match(EQUAL)) {
      expression()
    } else {
      Expr.None()
    }

    consume(SEMICOLON)
    return Stmt.Var(name, initializer)
  }

  private fun statement(): Stmt {
    return when {
      match(PRINT) -> printStatement()
      match(LEFT_BRACE) -> Stmt.Block(block())
      else -> expressionStatement()
    }
  }

  private fun block(): List<Stmt> {
    val statements = ArrayList<Stmt>()

    while (!check(RIGHT_BRACE) && !isAtEnd) {
      statements.add(declaration())
    }

    consume(RIGHT_BRACE)
    return statements
  }

  private fun printStatement(): Stmt {
    val value = expression()
    consume(SEMICOLON)
    return Stmt.Print(value)
  }

  private fun expressionStatement(): Stmt {
    val expr = expression()
    consume(SEMICOLON)
    return Stmt.Expression(expr)
  }

  private fun expression() = assignment()

  private fun assignment(): Expr {
    val expr = equality()

    if (match(EQUAL)) {
      val equals = previous
      val value = assignment()

      if (expr is Expr.Variable) {
        val name = expr.name
        return Expr.Assign(name, value)
      }

      error(equals, "Invalid assignment target.")
    }

    return expr
  }

  private fun seperator(): Expr =
    leftAssociate(::ternary, COMMA)

  private fun ternary(): Expr {
    var expr = equality()

    while (match(QUESTION)) {
      val first = previous
      val middle = equality()

      if (match(COLON)) {
        val second = previous
        val right = equality()

        expr = Expr.Ternary(expr, first, middle, second, right)
      } else {
        error(peek, "Expected additonal operator.")
        synchronize()
        return Expr.Error()
      }
    }

    return expr
  }

  private fun equality(): Expr =
    leftAssociate(::comparison, BANG_EQUAL, EQUAL_EQUAL)

  private fun comparison(): Expr =
    leftAssociate(::addition, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)

  private fun addition(): Expr =
    leftAssociate(::multiplication, MINUS, PLUS)

  private fun multiplication(): Expr =
    leftAssociate(::unary, SLASH, STAR)

  private fun unary(): Expr {
    if (match(BANG, MINUS)) {
      val operator = previous
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
      match(NUMBER, STRING) -> Expr.Literal(previous.literal)
      match(IDENTIFIER) -> Expr.Variable(previous)
      match(LEFT_PAREN) -> {
        val expr = expression()
        consume(RIGHT_PAREN)
        Expr.Grouping(expr)
      }
      else -> {
        error(peek, "Expect expression.")
        Expr.Error()
      }
    }
  }
}