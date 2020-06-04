package com.craftinginterpreters.klox

class AstPrinter: Expr.Visitor<String> {
    fun print(expr: Expr) = expr.accept(this)

    override fun visitBinary(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGrouping(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteral(expr: Expr.Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visitUnary(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()

        builder.append("(").append(name)
        exprs.forEach {
            builder.append(" ")
            builder.append(it.accept(this))
        }
        builder.append(")")

        return builder.toString()
    }
}