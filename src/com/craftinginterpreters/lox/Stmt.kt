package com.craftinginterpreters.lox

abstract class Stmt {

  internal abstract fun <R> accept(visitor: Visitor<R>): R

  internal interface Visitor<R> {
    fun visitExpressionStmt(stmt: Expression): R
    fun visitPrintStmt(stmt: Print): R
    fun visitVarStmt(stmt: Var): R
    fun visitBlockStmt(stmt: Block): R
    fun visitIfStmt(stmt: If): R
    fun visitWhileStmt(stmt: While): R
    fun visitBreakStmt(stmt: Break): R
    fun visitFunctionStmt(stmt: Function): R
    fun visitReturnStmt(stmt: Return): R
  }

  internal class None : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      throw error("Visited non statement.")
  }

  class Expression(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitExpressionStmt(this)
  }

  class Print(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitPrintStmt(this)
  }

  class Var(val name: Token, val initializer: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitVarStmt(this)
  }

  class Block(val statements: List<Stmt>) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitBlockStmt(this)
  }

  class If(val condition: Expr, val then: Stmt, val other: Stmt= None()) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitIfStmt(this)
  }

  class While(val condition: Expr, val body: Stmt) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitWhileStmt(this)
  }

  class Break : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitBreakStmt(this)
  }

  class Function(val name: Token, val function: Expr.Func) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitFunctionStmt(this)
  }

  class Return(val keyword: Token, val value: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R =
      visitor.visitReturnStmt(this)
  }
}
