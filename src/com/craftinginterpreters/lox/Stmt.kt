package com.craftinginterpreters.lox

abstract class Stmt {

  internal abstract fun <R> accept(visitor: Visitor<R>): R

  internal interface Visitor<R> {
    fun visitExpressionStmt(stmt: Expression): R
    fun visitPrintStmt(stmt: Print): R
    fun visitVarStmt(stmt: Var): R
    fun visitBlockStmt(stmt: Block): R
  }

  internal class Error : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      throw error("Visited error statement.")
  }

  internal class Expression(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitExpressionStmt(this)
  }

  internal class Print(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitPrintStmt(this)
  }

  internal class Var(val name: Token, val initializer: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitVarStmt(this)
  }

  internal class Block(val statements: List<Stmt>) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitBlockStmt(this)
  }
}
