package com.craftinginterpreters.klox

class Scanner(val source: String) {
    private companion object {
        val keywords = hashMapOf<String, TokenType>(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
    }

    private val tokens = arrayListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): ArrayList<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd() = current >= source.length

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (matchNext('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (matchNext('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (matchNext('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (matchNext('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                if (matchNext('/')) { // consume single-line comment
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            ' ', '\r', '\t' -> {} // ignorable whitespace
            '\n' -> line++
            '"' -> addStringLiteral()
            else -> {
                when {
                    isDigit(c) -> addNumericLiteral()
                    isAlpha(c) -> addIdentifier()
                    else -> Klox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun addIdentifier() {
        while (isAlpha(peek())) advance()

        // check for reserved word
        addToken(
            keywords.getOrDefault(
                source.substring(start, current),
                TokenType.IDENTIFIER
            )
        )
    }

    private fun addNumericLiteral() {
        while (peek() in '0'..'9') advance()

        // check for fractional part
        if (peek() == '.' && isDigit(peekpeek())) {
            advance() // consume decimal point
            while (isDigit(peek())) advance()
        }

        addToken(
            TokenType.NUMBER,
            source.substring(start, current).toDouble()
        )
    }

    private fun addStringLiteral() {
        // multiline strings are allowed because reasons
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }

            advance()
        }

        if (isAtEnd()) {
            Klox.error(line, "Unterminated string.")
            return
        }

        advance()
        addToken(
            TokenType.STRING,
            source.substring(start + 1, current - 1)
        )
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun matchNext(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }

    private fun peekpeek(): Char {
        return if (current + 1 >= source.length) '\u0000' else source[current + 1]
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}