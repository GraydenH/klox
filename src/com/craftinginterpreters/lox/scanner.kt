/**
 * Created by Grayden on 2017-02-04.
 */

package com.craftinginterpreters.lox

import java.util.ArrayList
import java.util.HashMap

import com.craftinginterpreters.lox.TokenType.*

internal class Scanner(private val source: String) {
  private val tokens = ArrayList<Token>()
  private var start = 0
  private var current = 0
  private var line = 1
  private var comments = 0

  private val isAtEnd: Boolean
    get() = current >= source.length

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
      '*' -> addToken(STAR)
      '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
      '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
      '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
      '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
      '/' -> if (match('/')) {
        // A comment goes until the end of the line.
        while (peek() != '\n' && !isAtEnd) advance()
      } else if (match('*')) { // multi-line comment
        comments++
        comment()
      } else {
        addToken(SLASH)
      }

      ' ', '\r', '\t' -> {
      }

      '\n' -> line++

      '"' -> string()

      else -> if (isDigit(c)) {
        number()
      } else if (isAlpha(c)) {
        identifier()
      } else {
        Lox.error(line, "Unexpected character.")
      }
    }// Ignore whitespace.
  }

  private fun identifier() {
    while (isAlphaNumeric(peek())) advance()

    // See if the identifier is a reserved word.
    val text = source.substring(start, current)

    var type: TokenType? = keywords[text]
    if (type == null) type = IDENTIFIER
    addToken(type)
  }

  private fun number() {
    while (isDigit(peek())) advance()

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance()

      while (isDigit(peek())) advance()
    }

    addToken(NUMBER, java.lang.Double.parseDouble(source.substring(start, current)))
  }

  private fun string() {
    while (peek() != '"' && !isAtEnd) {
      if (peek() == '\n') line++
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
    val value = source.substring(start + 1, current - 1)
    addToken(STRING, value)
  }

  private fun comment() {
    while (!lookAhead('*', '/') && !lookAhead('/', '*') && !isAtEnd) {
      if (peek() === '\n') line++
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

    if (comments > 0) comment()
  }


  private fun match(expected: Char): Boolean {
    if (isAtEnd) return false
    if (source[current] != expected) return false

    current++
    return true
  }

  private fun peek(): Char {
    if (current >= source.length) return '\u0000'
    return source[current]
  }

  private fun peekNext(): Char {
    if (current + 1 >= source.length) return '\u0000'
    return source[current + 1]
  }

  private fun lookAhead(c1: Char, c2: Char): Boolean {
    return peek() == c1 && peekNext() == c2
  }

  private fun isAlpha(c: Char): Boolean {
    return c >= 'a' && c <= 'z' ||
            c >= 'A' && c <= 'Z' ||
            c == '_'
  }

  private fun isAlphaNumeric(c: Char): Boolean {
    return isAlpha(c) || isDigit(c)
  }

  private fun isDigit(c: Char): Boolean {
    return c >= '0' && c <= '9'
  }

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
      keywords = HashMap<String, TokenType>()
      keywords.put("and", AND)
      keywords.put("class", CLASS)
      keywords.put("else", ELSE)
      keywords.put("false", FALSE)
      keywords.put("for", FOR)
      keywords.put("fun", FUN)
      keywords.put("if", IF)
      keywords.put("nil", NIL)
      keywords.put("or", OR)
      keywords.put("print", PRINT)
      keywords.put("return", RETURN)
      keywords.put("super", SUPER)
      keywords.put("this", THIS)
      keywords.put("true", TRUE)
      keywords.put("var", VAR)
      keywords.put("while", WHILE)
    }
  }
}
