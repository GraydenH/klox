package com.craftinginterpreters.lox

import java.util.HashMap
import java.util.Stack
import java.lang.reflect.Member.DECLARED
import javax.swing.UIManager.put



internal class Resolver(private val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
	private val scopes = Stack<MutableMap<String, Variable>>()
	private var currentFunction = FunctionType.NONE

	// types

	private enum class FunctionType {
		NONE,
		FUNCTION
	}

	private class Variable (internal val name: Token, internal var state: Variable.State) {
		enum class State {
			DECLARED,
			DEFINED,
			READ
		}
	}

	private fun beginScope() {
		scopes.push(HashMap())
	}

	private fun endScope() {
		val scope = scopes.pop()

		for ((_, value) in scope) {
			if (value.state === Variable.State.DEFINED) {
				Lox.error(value.name, "Local variable is not used.")
			}
		}
	}

	private fun declare(name: Token) {
		if (scopes.isEmpty()) return

		val scope = scopes.peek()
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name, "Variable with this name already declared in this scope.")
		}

		scope[name.lexeme] = Variable(name, Variable.State.DECLARED)
	}

	private fun define(name: Token) {
		if (scopes.isEmpty()) return
		scopes.peek().getValue(name.lexeme).state = Variable.State.DEFINED
	}

	private fun resolve(stmt: Stmt) {
		stmt.accept(this)
	}

	private fun resolve(expr: Expr) {
		expr.accept(this)
	}

	fun resolve(statements: List<Stmt>) {
		for (statement in statements) {
			resolve(statement)
		}
	}

	private fun resolveLocal(expr: Expr, name: Token, isRead: Boolean) {
		for (i in scopes.size - 1 downTo 0) {
			if (scopes[i].containsKey(name.lexeme)) {
				interpreter.resolve(expr, scopes.size - 1 - i)

				// Mark it used.
				if (isRead) {
					val scope = scopes[i].getValue(name.lexeme)
					scopes[i].getValue(name.lexeme).state = Variable.State.READ
				}

				return
			}
		}

		// Not found. Assume it is global.
	}

	private fun resolveFunction(function: Expr.Func, type: FunctionType) {
		val enclosingFunction = currentFunction
		currentFunction = type

		beginScope()
		for (param in function.params) {
			declare(param)
			define(param)
		}
		resolve(function.body)
		endScope()

		currentFunction = enclosingFunction;
	}

	override fun visitBlockStmt(stmt: Stmt.Block) {
		beginScope()
		resolve(stmt.statements)
		endScope()
	}

	override fun visitVarStmt(stmt: Stmt.Var) {
		declare(stmt.name)
		if (stmt.initializer !is Expr.None) {
			resolve(stmt.initializer)
		}
		define(stmt.name)
	}

	override fun visitVariableExpr(expr: Expr.Variable) {
		if (!scopes.isEmpty() &&
				scopes.peek().containsKey(expr.name.lexeme) &&
				scopes.peek()[expr.name.lexeme]!!.state === Variable.State.DECLARED) {
			Lox.error(expr.name,"Cannot read local variable in its own initializer.")
		}

		resolveLocal(expr, expr.name, true)
	}

	override fun visitAssignExpr(expr: Expr.Assign) {
		resolve(expr.value)
		resolveLocal(expr, expr.name, false)
	}

	override fun visitFunctionStmt(stmt: Stmt.Function) {
		declare(stmt.name)
		define(stmt.name)

		resolve(stmt.function)
	}

	override fun visitIfStmt(stmt: Stmt.If) {
		resolve(stmt.condition)
		resolve(stmt.then)
		if (stmt.other !is Stmt.None) {
			resolve(stmt.other)
		}
	}

	override fun visitExpressionStmt(stmt: Stmt.Expression) {
		resolve(stmt.expression)
	}

	override fun visitLogicalExpr(expr: Expr.Logical) {
		resolve(expr.left)
		resolve(expr.right)
	}

	override fun visitBinaryExpr(expr: Expr.Binary) {
		resolve(expr.left)
		resolve(expr.right)
	}

	override fun visitGroupingExpr(expr: Expr.Grouping) {
		resolve(expr.expression)
	}

	override fun visitUnaryExpr(expr: Expr.Unary) {
		resolve(expr.right)
	}

	override fun visitCallExpr(expr: Expr.Call) {
		resolve(expr.callee)

		for (argument in expr.arguments) {
			resolve(argument)
		}
	}

	override fun visitFuncExpr(expr: Expr.Func) {
		resolveFunction(expr, FunctionType.FUNCTION)
	}

	override fun visitPrintStmt(stmt: Stmt.Print) {
		resolve(stmt.expression)
	}

	override fun visitWhileStmt(stmt: Stmt.While) {
		resolve(stmt.condition)
		resolve(stmt.body)
	}

	override fun visitReturnStmt(stmt: Stmt.Return) {
		if (currentFunction == FunctionType.NONE) {
			Lox.error(stmt.keyword, "Cannot return from top-level code.")
		}

		if (stmt.value !is Expr.None) {
			resolve(stmt.value)
		}
	}

	override fun visitBreakStmt(stmt: Stmt.Break) {}

	override fun visitLiteralExpr(expr: Expr.Literal) {}
}