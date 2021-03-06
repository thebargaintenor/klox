package com.craftinginterpreters.klox

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.exp
import kotlin.system.exitProcess

object Klox {
    private var hadError = false

    @JvmStatic // TODO: this is not idiomatic - refactor later
    @Throws(IOException::class)
    fun main(args: Array<String>) {
        when {
            args.size > 1 -> {
                println("Usage: klox [script]")
                exitProcess(64)
            }
            args.size == 1 -> {
                runFile(args[0])
            }
            else -> {
                runPrompt()
            }
        }
    }

    @Throws(IOException::class)
    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) {
            exitProcess(65)
        }
    }

    @Throws(IOException::class)
    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            run(reader.readLine())
            hadError = false // reset for next repl pass
        }
    }

    private fun run(source: String) {
        val tokens = Scanner(source).scanTokens()
        val parser = Parser(tokens)
        val expression = parser.maybeParse()

        // refine from nullable as it only results in error case
        if (hadError || expression == null) return

        print(AstPrinter().print(expression))
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun report(line: Int, where: String, message: String) {
        println("[Line $line] Error $where: $message")
        hadError = true
    }

    fun error(token: Token, message: String) {
        when (token.type) {
            TokenType.EOF -> report(token.line, " at end", message)
            else -> report(token.line, " at '${token.lexeme}'", message)
        }
    }
}