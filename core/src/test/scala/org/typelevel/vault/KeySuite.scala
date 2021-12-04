/*
 * Copyright (c) 2021 Christopher Davenport
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.typelevel.vault

import org.scalacheck._
import cats.effect.SyncIO
import cats.kernel.laws.discipline.{EqTests, HashTests}
import munit.DisciplineSuite
import cats.laws.discipline.InvariantTests

class KeySuite extends DisciplineSuite {
  implicit def functionArbitrary[B, A: Arbitrary]: Arbitrary[B => A] = Arbitrary {
    for {
      a <- Arbitrary.arbitrary[A]
    } yield { (_: B) => a }
  }

  implicit def uniqueKey[A]: Arbitrary[Key[A]] = Arbitrary {
    Arbitrary.arbitrary[Unit].map(_ => Key.newKey[SyncIO, A].unsafeRunSync())
  }

  checkAll("Key", HashTests[Key[Int]].hash)
  checkAll("Key", EqTests[Key[Int]].eqv)
  checkAll("Key", InvariantTests[Key].invariant[Int, String, Boolean])
}
