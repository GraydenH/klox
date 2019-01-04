package com.craftinginterpreters.lox

abstract class Expr {

  // abstract functions

  internal abstract fun <R> accept(visitor: Visitor<R>): R

  // implementing types

  internal interface Visitor<R> {
    fun visitBinaryExpr(expr: Binary): R
    fun visitGroupingExpr(expr: Grouping): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitUnaryExpr(expr: Unary): R
    fun visitVariableExpr(expr: Variable): R
    fun visitAssignExpr(expr: Assign): R
    fun visitLogicalExpr(expr: Logical): R
    fun visitCallExpr(expr: Call): R
    fun visitFuncExpr(expr: Func): R
  }

  class None : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      throw error("Visited non expression.")
  }

  class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitBinaryExpr(this)
  }

  class Grouping(val expression: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitGroupingExpr(this)
  }

  class Literal(val value: Any) : Expr() {
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

  class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitLogicalExpr(this)
  }

  class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitCallExpr(this)
  }

  class Func(val params: List<Token>, val body: List<Stmt>) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitFuncExpr(this)
  }
}
