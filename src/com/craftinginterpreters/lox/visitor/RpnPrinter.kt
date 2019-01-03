// package com.craftinginterpreters.lox
//
// object RpnPrinter: Expr.Visitor<String> {
// override fun visitVariableExpr(expr: Expr.Variable): String {
// TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
// }
//
// @JvmStatic
// fun main(args: Array<String>) {
// val expression = Expr.Binary(
// Expr.Unary(
// Token(TokenType.MINUS, "-", null, 1),
// Expr.Literal(123)
// ),
// Token(TokenType.STAR, "*", null, 1),
// Expr.Grouping(Expr.Literal(45.67))
// )
//
// println(RpnPrinter.print(expression))
// }
//
// fun print(expr: Expr): String = expr.accept(this)
//
// override fun visitBinaryExpr(expr: Expr.Binary): String =
// reverse(expr.operator.lexeme, expr.right, expr.left)
//
// override fun visitGroupingExpr(expr: Expr.Grouping): String =
// reverse("group", expr.expression)
//
// override fun visitLiteralExpr(expr: Expr.Literal): String =
// expr.value.toString()
//
// override fun visitUnaryExpr(expr: Expr.Unary): String =
// reverse(expr.operator.lexeme, expr.right)
//
// override fun visitTernaryExpr(expr: Expr.Ternary): String =
// reverse(expr.first.lexeme + expr.second.lexeme, expr.right, expr.middle, expr.left)
//
// private fun reverse(name: String, vararg exprs: Expr): String {
// val builder = StringBuilder()
//
// builder.insert(0, name)
// for (expr in exprs) {
// builder.insert(0, " ")
// builder.insert(0, expr.accept(this))
// }
//
// return builder.toString()
// }
// }