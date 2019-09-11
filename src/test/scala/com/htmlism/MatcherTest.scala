package com.htmlism

import org.scalatest._

class MatcherTest extends FunSuite with Matchers with MyCustomMatchers {
  test("foo") {
    2 should beEven
  }
}
