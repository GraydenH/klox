/**
 * Created by Grayden on 2017-02-04.
 */

package com.craftinginterpreters.lox

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object Lox {
  private var hadError = false

  @Throws(IOException::class)
  @JvmStatic
  fun main(args: Array<String>) {
    when {
      args.size > 1 -> println("Usage: klox [script]")
      args.size == 1 -> runFile(args[0])
      else -> runPrompt()
    }
  }

  @Throws(IOException::class)
  private fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))

    // Indicate an error in the exit code.
    if (hadError) {
      System.exit(65)
    }
  }

  @Throws(IOException::class)
  private fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
      print("> ")
      run(reader.readLine())
      hadError = false
    }
  }

  private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val expression = parser.parse()

    // Stop if there was a syntax error.
    if (hadError) {
      return
    }

    println(RpnPrinter.print(expression!!))
  }

  internal fun error(line: Int, message: String) {
    report(line, "", message)
  }

  internal fun error(token: Token, message: String) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message)
    } else {
      report(token.line, " at '" + token.lexeme + "'", message)
    }
  }

  private fun report(line: Int, where: String, message: String) {
    println("[line $line] Error$where: $message")
    hadError = true
  }
}
