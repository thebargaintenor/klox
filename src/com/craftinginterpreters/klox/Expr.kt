package com.craftinginterpreters.klox

/*
expression → literal
           | unary
           | binary
           | grouping ;

literal    → NUMBER | STRING | "false" | "true" | "nil" ;
grouping   → "(" expression ")" ;
unary      → ( "-" | "!" ) expression ;
binary     → expression operator expression ;
operator   → "==" | "!=" | "<" | "<=" | ">" | ">="
           | "+"  | "-"  | "*" | "/" ;

expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "false" | "true" | "nil"
               | "(" expression ")" ;
*/

abstract class Expr() {
    internal interface Visitor<T> {
        fun visitBinary(expr: Binary): T
        fun visitGrouping(expr: Grouping): T
        fun visitLiteral(expr: Literal): T
        fun visitUnary(expr: Unary): T
    }

    internal abstract fun <T> accept(visitor: Visitor<T>): T

    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBinary(this)
    }

    class Grouping(val expression: Expr) : Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGrouping(this)
    }

    class Literal(val value: Any?) : Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLiteral(this)
    }

    class Unary(val operator: Token, val right: Expr) : Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnary(this)
    }
}