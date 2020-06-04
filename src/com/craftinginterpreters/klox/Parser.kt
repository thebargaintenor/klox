package com.craftinginterpreters.klox

import java.lang.RuntimeException

class Parser(val tokens: List<Token>) {
    private class ParseError: RuntimeException()

    private var current = 0

    fun maybeParse(): Expr? {
        return try {
            expression()
        } catch (err: ParseError) {
            null
        }
    }

    private fun expression(): Expr = equality()

    private fun equality(): Expr {
        var expr = comparison()

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            expr = Expr.Binary(expr, previous(), comparison())
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = addition()

        while (
            match(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL
            )
        ) {
            expr = Expr.Binary(expr, previous(), addition())
        }

        return expr
    }

    private fun addition(): Expr {
        var expr = multiplication()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            expr = Expr.Binary(expr, previous(), multiplication())
        }

        return expr
    }

    private fun multiplication(): Expr {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            expr = Expr.Binary(expr, previous(), unary())
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            return Expr.Unary(previous(), unary())
        }

        return primary()
    }

    private fun primary(): Expr {
        when {
            match(TokenType.FALSE) -> return Expr.Literal(false)
            match(TokenType.TRUE) -> return Expr.Literal(true)
            match(TokenType.NIL) -> return Expr.Literal(null)
            match(TokenType.NUMBER, TokenType.STRING) -> return Expr.Literal(previous().literal)
            match(TokenType.LEFT_PAREN) -> {
                val expr = expression()
                consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
                return Expr.Grouping(expr)
            }
            else -> throw error(peek(), "Expect expression.")
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        for(type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false else peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): ParseError {
        Klox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS,
                TokenType.FUN,
                TokenType.VAR,
                TokenType.FOR,
                TokenType.IF,
                TokenType.WHILE,
                TokenType.PRINT,
                TokenType.RETURN
                    -> return
            }

            advance()
        }
    }
}