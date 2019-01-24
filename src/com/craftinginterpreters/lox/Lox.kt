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
  private val interpreter = Interpreter()

  var hadError = false
  var hadRuntimeError = false

  @JvmStatic
  fun main(args: Array<String>) {
    when {
      args.size > 1 -> println("Usage: klox [script]")
      args.size == 1 -> runFile(args[0])
      else -> runPrompt()
    }
  }

  @Throws(IOException::class)
  fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))

    if (hadError) {
      System.exit(65)
    } else if (hadRuntimeError) {
      System.exit(70)
    }
  }

  @Throws(IOException::class)
  private fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
      print("> ")

      try {
        run(reader.readLine())
      } catch (e: Exception) {

      }

      hadError = false
    }
  }

  fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val statements = parser.parse()

    // Stop if there was a syntax error.
    if (hadError) {
      return
    }

    val resolver = Resolver(interpreter)
    resolver.resolve(statements)

    if (hadError) {
      return
    }

    interpreter.interpret(statements)
  }

  fun runtimeError(error: RuntimeError) {
    println(error.message + "\n[line " + error.token.line + "]")
    hadRuntimeError = true
  }

  internal fun error(line: Int, message: String) =
    report(line, "", message)

  internal fun error(token: Token, message: String) {
    if (token.type == Token.Type.EOF) {
      report(token.line, " at end", message)
    } else {
      report(token.line, " at '" + token.lexeme + "'", message)
    }
  }

  private fun report(line: Int, where: String, message: String) {
    println("[line $line] Error $where: $message")
    hadError = true
  }
}
