package com.craftinginterpreters.lox

internal class LoxFunction(
	private val declaration: Expr.Func,
	private val closure: Environment) : LoxCallable {

	override fun arity(): Int {
		return declaration.params.size
	}

	override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
		val environment = Environment(closure)
		for (i in 0 until declaration.params.size) {
			environment.define(declaration.params[i].lexeme, arguments[i])
		}

		try {
			interpreter.executeBlock(declaration.body, environment)
		} catch (returnValue: Return) {
			return returnValue.value
		}

		return None()
	}

	override fun toString(): String {
		return "<fn foreign>"
	}
}
