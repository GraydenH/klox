package com.craftinginterpreters.lox

object RpnPrinter: Visitor<String> {

  @JvmStatic
  fun main(args: Array<String>) {
    val expression = Binary(
      Unary(
        Token(TokenType.MINUS, "-", null, 1),
        Literal(123)
      ),
      Token(TokenType.STAR, "*", null, 1),
      Grouping(Literal(45.67))
    )

    println(RpnPrinter.print(expression))
  }

  fun print(expr: Expr): String {
    return expr.accept(this)
  }

  override fun visitBinaryExpr(expr: Binary): String {
    return reverse(expr.operator.lexeme, expr.right, expr.left)
  }

  override fun visitGroupingExpr(expr: Grouping): String {
    return reverse("group", expr.expression)
  }

  override fun visitLiteralExpr(expr: Literal): String {
    return expr.value.toString()
  }

  override fun visitUnaryExpr(expr: Unary): String {
    return reverse(expr.operator.lexeme, expr.right)
  }

  private fun reverse(name: String, vararg exprs: Expr): String {
    val builder = StringBuilder()

    builder.insert(0, name)
    for (expr in exprs) {
      builder.insert(0, " ")
      builder.insert(0, expr.accept(this))
    }

    return builder.toString()
  }
}