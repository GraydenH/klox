package com.craftinginterpreters.lox

enum class TokenType {
  // Single-character tokens.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, QUESTION,
  COMMA, DOT, MINUS, PLUS, SEMICOLON, COLON, SLASH, STAR,

  // One or two character tokens.
  BANG,
  BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESS, LESS_EQUAL,

  // Literals.
  IDENTIFIER,
  STRING, NUMBER,

  // Keywords.
  AND,
  CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, BREAK,
  PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

  EOF
}
