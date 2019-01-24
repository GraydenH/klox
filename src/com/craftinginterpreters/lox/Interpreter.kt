package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.Token.Type.*
import java.util.ArrayList
import java.util.HashMap

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit>  {

  // fields

  private val globals = Environment()
  private val locals = HashMap<Expr, Int>()
  private var environment = globals
  private var hadBreak = false

  // types

  private class Break: Throwable()

  // constructor

  init {
    globals.define("clock", object : LoxCallable {
      override fun arity(): Int {
        return 0
      }

      override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        return System.currentTimeMillis().toDouble() / 1000.0
      }

      override fun toString(): String {
        return "<native fn>"
      }
    })
  }

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

  override fun visitReturnStmt(stmt: Stmt.Return) {
    val value = if (stmt.value !is Expr.None) evaluate(stmt.value) else None()

    throw Return(value)
  }

  override fun visitFunctionStmt(stmt: Stmt.Function) {
    val function = LoxFunction(stmt.function, environment)
    environment.define(stmt.name.lexeme, function)
  }

  override fun visitBreakStmt(stmt: Stmt.Break) {
    hadBreak = true
    throw Break()
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

  override fun visitFuncExpr(expr: Expr.Func): Any {
    return LoxFunction(expr, environment)
  }

  override fun visitCallExpr(expr: Expr.Call): Any {
    val callee = evaluate(expr.callee)

    val arguments = ArrayList<Any>()
    for (argument in expr.arguments) {
      arguments.add(evaluate(argument))
    }

    if (callee !is LoxCallable) {
      throw RuntimeError(expr.paren, "Can only call functions and classes.")
    }

    if (arguments.size != callee.arity()) {
      throw RuntimeError(expr.paren, "Expected " +
        callee.arity() + " arguments but got " +
        arguments.size + ".")
    }

    return callee.call(this, arguments)
  }

  override fun visitLogicalExpr(expr: Expr.Logical): Any {
    val left = evaluate(expr.left)

    if (expr.operator.type === Token.Type.OR) {
      if (isTruthy(left)) { return left }
    } else {
      if (!isTruthy(left)) { return left }
    }

    return evaluate(expr.right)
  }

  override fun visitAssignExpr(expr: Expr.Assign): Any {
    val value = evaluate(expr.value)
    val distance = locals[expr]
    if (distance != null) {
      environment.assignAt(distance, expr.name, value)
    } else {
      globals.assign(expr.name, value)
    }

    return value
  }

  override fun visitVariableExpr(expr: Expr.Variable): Any {
    return lookUpVariable(expr.name, expr)
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
    expr.value

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

  fun resolve(expr: Expr, depth: Int) =
    locals.put(expr, depth)

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

  internal fun executeBlock(statements: List<Stmt>, environment: Environment) {
    val previous = this.environment
    try {
      this.environment = environment

      for (statement in statements) {
        execute(statement)
      }
    } catch (b: Break) {} finally {
      this.environment = previous
    }
  }

  private fun lookUpVariable(name: Token, expr: Expr): Any {
    val distance = locals[expr]
    return if (distance != null) {
      environment.getAt(distance, name.lexeme)
    } else {
      globals[name]
    }
  }
}