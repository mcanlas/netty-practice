package com

package object htmlism {
  def forEffect[A](x: A): Unit = {
    val _ = x
    ()
  }
}
