package com.craftinginterpreters.lox

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

internal class LoxTest {

  @BeforeEach
  fun before() {
    Lox.hadError = false
    Lox.hadRuntimeError = false
  }

  @AfterEach
  fun after() {
    assert(!Lox.hadError)
    assert(!Lox.hadRuntimeError)
  }

  @Test
  fun test1() {
    val bytes = Files.readAllBytes(Paths.get("test/resources/test1.lox"))
    Lox.run(String(bytes, Charset.defaultCharset()))
  }

  @Test
  fun test2() {
    val bytes = Files.readAllBytes(Paths.get("test/resources/test2.lox"))
    Lox.run(String(bytes, Charset.defaultCharset()))
  }

  @Test
  fun test3() {
    val bytes = Files.readAllBytes(Paths.get("test/resources/test3.lox"))
    Lox.run(String(bytes, Charset.defaultCharset()))
  }

  @Test
  fun test4() {
    val bytes = Files.readAllBytes(Paths.get("test/resources/test4.lox"))
    Lox.run(String(bytes, Charset.defaultCharset()))
  }

  /*
  @Test
  fun test5() {
    val bytes = Files.readAllBytes(Paths.get("test/resources/test5.lox"))
    Lox.run(String(bytes, Charset.defaultCharset()))
  }
  */

  @Test
  fun test6() {
    val bytes = Files.readAllBytes(Paths.get("test/resources/test6.lox"))
    Lox.run(String(bytes, Charset.defaultCharset()))
  }

  @Test
  fun test7() {
    val bytes = Files.readAllBytes(Paths.get("test/resources/test7.lox"))
    Lox.run(String(bytes, Charset.defaultCharset()))
  }
}