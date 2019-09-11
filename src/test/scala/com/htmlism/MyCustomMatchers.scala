package com.htmlism

import org.scalatest.matchers._

trait MyCustomMatchers {
  class BeEvenMatcher extends Matcher[Int] {
    def apply(n: Int): MatchResult =
      MatchResult(n % 2 == 0, s"$n was not even", s"$n was even")
  }

  def beEven =
    new BeEvenMatcher
}
