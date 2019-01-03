/**
 * Created by Grayden on 2017-02-04.
 */

package com.craftinginterpreters.lox

import java.util.ArrayList
import java.util.HashMap

import com.craftinginterpreters.lox.TokenType.*

class Scanner(private val source: String) {

  // fields

  private val tokens = ArrayList<Token>()
  private var start = 0
  private var current = 0
  private var line = 1
  private var comments = 0

  private val isAtEnd: Boolean
    get() = current >= source.length

  private val peek: Char
    get() = if (current >= source.length) '\u0000' else source[current]

  private val peekNext: Char
    get() = if (current + 1 >= source.length) '\u0000' else source[current + 1]

  fun scanTokens(): List<Token> {
    while (!isAtEnd) {
      // We are the beginning of the next lexeme.
      start = current
      scanToken()
    }

    tokens.add(Token(EOF, "", null, line))
    return tokens
  }

  private fun scanToken() {
    val c = advance()
    when (c) {
      '(' -> addToken(LEFT_PAREN)
      ')' -> addToken(RIGHT_PAREN)
      '{' -> addToken(LEFT_BRACE)
      '}' -> addToken(RIGHT_BRACE)
      ',' -> addToken(COMMA)
      '.' -> addToken(DOT)
      '-' -> addToken(MINUS)
      '+' -> addToken(PLUS)
      ';' -> addToken(SEMICOLON)
      ':' -> addToken(COLON)
      '?' -> addToken(QUESTION)
      '*' -> addToken(STAR)
      '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
      '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
      '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
      '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
      '/' -> when {
        match('/') -> // A comment goes until the end of the line.
          while (peek != '\n' && !isAtEnd) {
            advance()
          }
        match('*') -> { // multi-line comment
          comments++
          comment()
        }
        else -> addToken(SLASH)
      }

      ' ', '\r', '\t' -> {}
      '\n' -> line++
      '"' -> string()

      else -> when {
        isDigit(c) -> number()
        isAlpha(c) -> identifier()
        else -> Lox.error(line, "Unexpected character.")
      }
    } // Ignore whitespace.
  }

  private fun identifier() {
    while (isAlphaNumeric(peek)) {
      advance()
    }

    // See if the identifier is a reserved word.
    val text = source.substring(start, current)
    val type = keywords[text] ?: IDENTIFIER

    addToken(type)
  }

  private fun number() {
    while (isDigit(peek)) {
      advance()
    }

    // Look for a fractional part.
    if (peek == '.' && isDigit(peekNext)) {
      // Consume the "."
      advance()
      while (isDigit(peek)) {
        advance()
      }
    }

    addToken(NUMBER, java.lang.Double.parseDouble(source.substring(start, current)))
  }

  private fun string() {
    while (peek != '"' && !isAtEnd) {
      if (peek == '\n') {
        line++
      }

      advance()
    }

    // Unterminated string.
    if (isAtEnd) {
      Lox.error(line, "Unterminated string.")
      return
    }

    // The closing ".
    advance()

    // Trim the surrounding quotes.
    // @todo unescape escape sequences here
    val value = escape(source.substring(start + 1, current - 1))
    addToken(STRING, value)
  }

  private fun escape(string: String): String {
    var result = string

    for (char in listOf("\\t", "\\n", "\\r")) {
      result = result.replace(char, char.drop(1))
    }

    return result
  }

  private fun comment() {
    while (!lookAhead('*', '/') && !lookAhead('/', '*') && !isAtEnd) {
      if (peek == '\n') {
        line++
      }

      advance()
    }

    if (lookAhead('/', '*')) {
      comments++
      advance()
      advance()
      comment()
    } else if (lookAhead('*', '/')) {
      comments--
      advance()
      advance()
    }

    if (comments > 0) {
      comment()
    }
  }


  private fun match(expected: Char): Boolean {
    if (isAtEnd || source[current] != expected) {
      return false
    }

    current++
    return true
  }

  private fun lookAhead(c1: Char, c2: Char): Boolean =
    peek == c1 && peekNext == c2

  private fun isAlpha(c: Char): Boolean =
    c in 'a'..'z' || c in 'A'..'Z' || c == '_'

  private fun isAlphaNumeric(c: Char): Boolean =
    isAlpha(c) || isDigit(c)

  private fun isDigit(c: Char): Boolean =
    c in '0'..'9'

  private fun advance(): Char {
    current++
    return source[current - 1]
  }

  private fun addToken(type: TokenType, literal: Any? = null) {
    val text = source.substring(start, current)
    tokens.add(Token(type, text, literal, line))
  }

  companion object {
    private val keywords: MutableMap<String, TokenType>

    init {
      keywords = HashMap()
      keywords["and"] = AND
      keywords["class"] = CLASS
      keywords["else"] = ELSE
      keywords["false"] = FALSE
      keywords["for"] = FOR
      keywords["fun"] = FUN
      keywords["if"] = IF
      keywords["nil"] = NIL
      keywords["or"] = OR
      keywords["print"] = PRINT
      keywords["return"] = RETURN
      keywords["super"] = SUPER
      keywords["this"] = THIS
      keywords["true"] = TRUE
      keywords["var"] = VAR
      keywords["while"] = WHILE
    }
  }
}
