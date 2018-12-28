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
    fun visitTernaryExpr(expr: Ternary): R
  }

  class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitBinaryExpr(this)
    }
  }

  class Grouping(val expression: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitGroupingExpr(this)
    }
  }

  class Literal(val value: Any?) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitLiteralExpr(this)
    }
  }

  class Unary(val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitUnaryExpr(this)
    }
  }

  class Ternary(val left: Expr, val first: Token, val middle: Expr, val second: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitTernaryExpr(this)
    }
  }
}
