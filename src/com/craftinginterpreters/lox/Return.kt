package com.craftinginterpreters.lox

internal class Return(val value: Any) : RuntimeException(null, null, false, false)