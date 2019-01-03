package com.craftinginterpreters.lox

abstract class Expr {

  // abstract functions

  internal abstract fun <R> accept(visitor: Visitor<R>): R

  // implementing types

  internal interface Visitor<R> {
    fun visitTernaryExpr(expr: Ternary): R
    fun visitBinaryExpr(expr: Binary): R
    fun visitGroupingExpr(expr: Grouping): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitUnaryExpr(expr: Unary): R
    fun visitVariableExpr(expr: Variable): R
    fun visitAssignExpr(expr: Assign): R
  }

  class None : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      throw error("Visited non expression.")
  }

  class Error : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      throw error("Visited error expression.")
  }

  class Ternary(val left: Expr, val first: Token, val middle: Expr, val second: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitTernaryExpr(this)
  }

  class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitBinaryExpr(this)
  }

  class Grouping(val expression: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitGroupingExpr(this)
  }

  class Literal(val value: Any?) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitLiteralExpr(this)
  }

  class Unary(val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitUnaryExpr(this)
  }

  class Variable(val name: Token) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitVariableExpr(this)
  }

  class Assign(val name: Token, val value: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitAssignExpr(this)
  }
}
