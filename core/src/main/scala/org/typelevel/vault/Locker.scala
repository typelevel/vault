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

import cats.implicits._
import cats.effect.kernel.Unique

/**
 * Locker - A persistent store for a single value.
 * This utilizes the fact that a unique is linked to a type.
 * Since the key is linked to a type, then we can cast the
 * value to Any, and join it to the Unique. Then if we
 * are then asked to unlock this locker with the same unique, we
 * know that the type MUST be the type of the Key, so we can
 * bring it back as that type safely.
 */
final class Locker private (private val unique: Unique.Token, private val a: Any) {

  /**
   * Retrieve the value from the Locker. If the reference equality
   * instance backed by a `Unique` value is the same then allows
   * conversion to that type, otherwise as it does not match
   * then this will be `None`
   *
   * @param k The key to check, if the internal Unique value matches
   * then this Locker can be unlocked as the specifed value
   */
  def unlock[A](k: LookupKey[A]): Option[A] = Locker.unlock(k, this)

  /**
   * Retrieve the value from the Locker. If the reference equality
   * instance backed by a `Unique` value is the same then allows
   * conversion to that type, otherwise as it does not match
   * then this will be `None`
   *
   * @param k The key to check, if the internal Unique value matches
   * then this Locker can be unlocked as the specifed value
   */
  private[vault] def unlock[A](k: Key[A]): Option[A] = unlock(k: LookupKey[A])
}

object Locker {

  /**
   * Put a single value into a Locker
   */
  def lock[A](k: InsertKey[A], a: A): Locker = new Locker(k.unique, a.asInstanceOf[Any])

  /**
   * Put a single value into a Locker
   */
  def lock[A](k: Key[A], a: A): Locker = lock(k: InsertKey[A], a)

  /**
   * Retrieve the value from the Locker. If the reference equality
   * instance backed by a `Unique` value is the same then allows
   * conversion to that type, otherwise as it does not match
   * then this will be `None`
   *
   * @param k The key to check, if the internal Unique value matches
   * then this Locker can be unlocked as the specifed value
   * @param l The locked to check against
   */
  def unlock[A](k: LookupKey[A], l: Locker): Option[A] =
    // Equality By Reference Equality
    if (k.unique === l.unique) Some(l.a.asInstanceOf[A])
    else None

  /**
   * Retrieve the value from the Locker. If the reference equality
   * instance backed by a `Unique` value is the same then allows
   * conversion to that type, otherwise as it does not match
   * then this will be `None`
   *
   * @param k The key to check, if the internal Unique value matches
   * then this Locker can be unlocked as the specifed value
   * @param l The locked to check against
   */
  def unlock[A](k: Key[A], l: Locker): Option[A] =
    unlock(k: LookupKey[A], l)
}
