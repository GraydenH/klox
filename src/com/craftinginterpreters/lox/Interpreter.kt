package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

internal class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit>  {

  // fields

  private var environment = Environment()
  private var hadBreak = false

  // types

  class Breakable: Throwable()

  // entry point

  fun interpret(statements: List<Stmt>) {
    try {
      for (statement in statements) {
        execute(statement)
      }
    } catch (error: RuntimeError) {
      Lox.runtimeError(error)
    }
  }

  // statement visitor implementation

  override fun visitBreakStmt(stmt: Stmt.Break) {
    hadBreak = true
    throw Breakable()
  }

  override fun visitWhileStmt(stmt: Stmt.While) {
    while (isTruthy(evaluate(stmt.condition)) && !hadBreak) {
      execute(stmt.body)
    }

    hadBreak = false
  }

  override fun visitIfStmt(stmt: Stmt.If) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.then)
    } else if (stmt.other !is Stmt.None) {
      execute(stmt.other)
    }
  }

  override fun visitBlockStmt(stmt: Stmt.Block) {
    executeBlock(stmt.statements, Environment(environment))
  }

  override fun visitPrintStmt(stmt: Stmt.Print) {
    val value = evaluate(stmt.expression)
    println(stringify(value))
  }

  override fun visitExpressionStmt(stmt: Stmt.Expression) {
    evaluate(stmt.expression)
  }

  override fun visitVarStmt(stmt: Stmt.Var) {
    environment.define(stmt.name.lexeme, evaluate(stmt.initializer))
  }

  // expression vistor implementation

  override fun visitLogicalExpr(expr: Expr.Logical): Any {
    val left = evaluate(expr.left)

    if (expr.operator.type === TokenType.OR) {
      if (isTruthy(left)) { return left }
    } else {
      if (!isTruthy(left)) { return left }
    }

    return evaluate(expr.right)
  }

  override fun visitAssignExpr(expr: Expr.Assign): Any {
    val value = evaluate(expr.value)

    environment.assign(expr.name, value)
    return value
  }

  override fun visitVariableExpr(expr: Expr.Variable): Any {
    return environment[expr.name]
  }

  override fun visitUnaryExpr(expr: Expr.Unary): Any {
    val right = evaluate(expr.right)

    return when (expr.operator.type) {
      MINUS -> {
        checkNumberOperand(expr.operator, right)
        -(right as Double)
      }
      BANG -> !isTruthy(right)
      else -> throw RuntimeError(expr.operator, "Expect unary operator.")
    }
  }

  override fun visitLiteralExpr(expr: Expr.Literal): Any =
    expr.value ?: "null"

  override fun visitGroupingExpr(expr: Expr.Grouping): Any =
    evaluate(expr.expression)

  override fun visitBinaryExpr(expr: Expr.Binary): Any {
    val left = evaluate(expr.left)
    val right = evaluate(expr.right)

    return when (expr.operator.type) {
      MINUS -> {
        checkNumberOperands(expr.operator, left, right)
        left as Double - right as Double
      }
      SLASH -> {
        checkNumberOperands(expr.operator, left, right)
        left as Double / right as Double
      }
      STAR -> {
        checkNumberOperands(expr.operator, left, right)
        if (right == 0) throw RuntimeError(expr.operator, "Divide by zero error.")
        left as Double * right as Double
      }
      GREATER -> {
        checkNumberOperands(expr.operator, left, right)
        left as Double > right as Double
      }
      GREATER_EQUAL -> {
        checkNumberOperands(expr.operator, left, right)
        left as Double >= right as Double
      }
      LESS -> {
        checkNumberOperands(expr.operator, left, right)
        right as Double > left as Double
      }
      LESS_EQUAL -> {
        checkNumberOperands(expr.operator, left, right)
        left as Double <= right as Double
      }
      BANG_EQUAL -> !isEqual(left, right)
      EQUAL_EQUAL -> isEqual(left, right)
      COMMA -> right
      PLUS -> if (left is Double && right is Double) {
          left + right
        } else if (left is String || right is String) {
          "$left$right"
        } else {
          throw RuntimeError(expr.operator, "Operand must be a number or string.")
        }
      else -> throw RuntimeError(expr.operator, "Expect binary operator.")
    }
  }

  // utils

  private fun execute(stmt: Stmt) =
    stmt.accept(this)

  private fun evaluate(expr: Expr): Any =
    expr.accept(this)

  private fun isTruthy(value: Any?): Boolean =
    if (value == null) false else value as Boolean

  private fun isEqual(a: Any?, b: Any?): Boolean =
    if (a == null && b == null) true else if (a == null) false else a == b

  private fun checkNumberOperand(operator: Token, operand: Any) =
    if (operand is Double) Unit else throw RuntimeError(operator, "Operand must be a number.")

  private fun checkNumberOperands(operator: Token, left: Any, right: Any) =
    if (left is Double && right is Double) Unit else throw RuntimeError(operator, "Operands must be numbers.")

  private fun stringify(value: Any?): String =
    if (value == null) "nil" else if (value is Double) trimInteger(value) else value.toString()

  private fun trimInteger(value: Double): String {
    val text = value.toString()
    if (text.endsWith(".0")) {
      return text.substring(0, text.length - 2)
    }

    return text
  }

  private fun executeBlock(statements: List<Stmt>, environment: Environment) {
    val previous = this.environment
    try {
      this.environment = environment

      for (statement in statements) {
        execute(statement)
      }
    } catch (b: Breakable) {} finally {
      this.environment = previous
    }
  }
}