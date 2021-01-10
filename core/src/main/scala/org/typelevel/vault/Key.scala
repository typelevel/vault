/*
 * Copyright (c) 2020 Christopher Davenport
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

import cats.effect.Sync
import cats.Hash
import cats.implicits._
import io.chrisdavenport.unique.Unique

/**
  * A unique value tagged with a specific type to that unique.
  * Since it can only be created as a result of that, it links
  * a Unique identifier to a type known by the compiler.
  */
final class Key[A] private (private[vault] val unique: Unique) {
  override def hashCode(): Int = unique.hashCode()
}

object Key {
  /**
   * Create A Typed Key
   */
  def newKey[F[_]: Sync, A]: F[Key[A]] = Unique.newUnique[F].map(new Key[A](_))

  implicit def keyInstances[A]: Hash[Key[A]] = new Hash[Key[A]]{
    // Members declared in cats.kernel.Eq
    def eqv(x: Key[A],y: Key[A]): Boolean =
      x.unique === y.unique
    
    // Members declared in cats.kernel.Hash
    def hash(x: Key[A]): Int = Hash[Unique].hash(x.unique)
  }
}
