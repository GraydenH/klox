package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*
import java.lang.RuntimeException
import java.util.ArrayList
import java.util.Arrays



class Parser (private val tokens: List<Token>) {

  // fields

  private var current = 0

  private var loops = 0

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
    val error = expr is Expr.None

    while (match(*types)) {
      val operator = previous
      val right = operand()
      expr = Expr.Binary(expr, operator, right)
    }

    if (error) {
      return Expr.None()
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
      Stmt.None()
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
      match(IF) -> ifStatement()
      match(WHILE) -> whileStatement()
      match(FOR) -> forStatement()
      match(BREAK) -> breakStatement()
      else -> expressionStatement()
    }
  }

  private fun breakStatement(): Stmt {
    return if (loops > 0) {
      consume(SEMICOLON)
      Stmt.Break()
    } else {
      throw error(peek, "'break' found outside loop")
    }
  }

  private fun forStatement(): Stmt {
    consume(LEFT_PAREN)

    val initializer = when {
      match(SEMICOLON) -> Stmt.None()
      match(VAR) -> varDeclaration()
      else -> expressionStatement()
    }

    var condition = if (!check(SEMICOLON)) expression() else Expr.None()
    consume(SEMICOLON)

    val increment = if (!check(RIGHT_PAREN)) expression() else Expr.None()
    consume(RIGHT_PAREN)

    consume(LEFT_BRACE)

    loops++
    var body: Stmt = Stmt.Block(block())
    loops--

    if (increment !is Expr.None) {
      body = Stmt.Block(Arrays.asList<Stmt>(body, Stmt.Expression(increment)))
    }

    if (condition is Expr.None) {
      condition = Expr.Literal(true)
    }

    body = Stmt.While(condition, body)

    if (initializer !is Stmt.None) {
      body = Stmt.Block(Arrays.asList(initializer, body))
    }

    return body
  }

  private fun whileStatement(): Stmt {
    consume(LEFT_PAREN)
    val condition = expression()
    consume(RIGHT_PAREN)

    consume(LEFT_BRACE)

    loops++
    val body = Stmt.Block(block())
    loops--

    return Stmt.While(condition, body)
  }

  private fun ifStatement(): Stmt {
    consume(LEFT_PAREN)
    val condition = expression()
    consume(RIGHT_PAREN)

    val thenBranch = Stmt.Block(block())
    return if (match(ELSE)) {
      Stmt.If(condition, thenBranch, Stmt.Block(block()))
    } else {
      Stmt.If(condition, thenBranch)
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
    val expr = or()

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

  private fun or(): Expr {
    var expr = and()

    while (match(OR)) {
      val operator = previous
      val right = and()
      expr = Expr.Logical(expr, operator, right)
    }

    return expr
  }

  private fun and(): Expr {
    var expr = seperator()

    while (match(AND)) {
      val operator = previous
      val right = seperator()
      expr = Expr.Logical(expr, operator, right)
    }

    return expr
  }

  private fun seperator(): Expr =
    leftAssociate(::equality, COMMA)

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
        Expr.None()
      }
    }
  }
}