package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

internal class Interpreter : Expr.Visitor<Any> {

  // entry point

  fun interpret(expression: Expr) {
    try {
      val value = evaluate(expression)
      println(stringify(value))
    } catch (error: RuntimeError) {
      Lox.runtimeError(error)
    }
  }

  // vistor pattern

  override fun visitTernaryExpr(expr: Expr.Ternary): Any {
    val left = evaluate(expr.left)

    if (expr.first.type == QUESTION && expr.second.type == COLON) {
      return if (isTruthy(left)) {
        evaluate(expr.middle)
      } else {
        evaluate(expr.right)
      }
    } else {
      throw RuntimeError(expr.first, "Expect ternary operator.")
    }
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
      SEMICOLON -> right
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
}