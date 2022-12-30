/*
 * Copyright (c) 2021 Typelevel
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

import cats.effect.kernel.Unique

/**
 * Vault - A persistent store for values of arbitrary types. This extends the behavior of the locker, into a Map that
 * maps Keys to Lockers, creating a heterogenous store of values, accessible by keys. Such that the Vault has no type
 * information, all the type information is contained in the keys.
 */
final class Vault private (private val m: Map[Unique.Token, Locker]) {

  /**
   * Empty this Vault
   */
  def empty: Vault = Vault.empty

  /**
   * Lookup the value of a key in this vault
   */
  def lookup[A](k: LookupKey[A]): Option[A] = m.get(k.unique).flatMap(_.unlock(k))

  /**
   * Lookup the value of a key in this vault
   */
  private[vault] def lookup[A](k: Key[A]): Option[A] = lookup(k: LookupKey[A])

  /**
   * Checks if the value of a key is in this vault
   */
  def contains[A](k: LookupKey[A]): Boolean = m.contains(k.unique)

  /**
   * Insert a value for a given key. Overwrites any previous value.
   */
  def insert[A](k: InsertKey[A], a: A): Vault = new Vault(m + (k.unique -> Locker(k, a)))

  /**
   * Insert a value for a given key. Overwrites any previous value.
   */
  private[vault] def insert[A](k: Key[A], a: A): Vault = insert(k: InsertKey[A], a)

  /**
   * Checks whether this Vault is empty
   */
  def isEmpty: Boolean = m.isEmpty

  /**
   * Delete a key from the vault
   */
  // Keeping unused type parameter for source compat
  def delete[A](k: DeleteKey): Vault = new Vault(m - k.unique)

  /**
   * Delete a key from the vault
   */
  private[vault] def delete[A](k: Key[A]): Vault = delete(k: DeleteKey)

  /**
   * Adjust the value for a given key if it's present in the vault.
   */
  def adjust[A](k: Key[A], f: A => A): Vault = lookup(k).fold(this)(a => insert(k, f(a)))

  /**
   * Merge Two Vaults. `that` is prioritized.
   */
  def ++(that: Vault): Vault = new Vault(this.m ++ that.m)

  /**
   * The size of the vault
   */
  def size: Int = m.size
}
object Vault {

  /**
   * The Empty Vault
   */
  def empty = new Vault(Map.empty)

  /**
   * Lookup the value of a key in the vault
   */
  @deprecated("Use v.lookup(k)", "3.1.0")
  def lookup[A](k: Key[A], v: Vault): Option[A] =
    v.lookup(k)

  /**
   * Insert a value for a given key. Overwrites any previous value.
   */
  @deprecated("Use v.insert(k, a)", "3.1.0")
  def insert[A](k: Key[A], a: A, v: Vault): Vault =
    v.insert(k, a)

  /**
   * Checks whether the given Vault is empty
   */
  @deprecated("Use v.isEmpty", "3.1.0")
  def isEmpty(v: Vault): Boolean = v.isEmpty

  /**
   * Delete a key from the vault
   */
  @deprecated("Use v.delete(k)", "3.1.0")
  def delete[A](k: Key[A], v: Vault): Vault = v.delete(k)

  /**
   * Adjust the value for a given key if it's present in the vault.
   */
  @deprecated("Use v.adjust(k, f)", "3.1.0")
  def adjust[A](k: Key[A], f: A => A, v: Vault): Vault =
    v.adjust(k, f)

  /**
   * Merge Two Vaults. v2 is prioritized.
   */
  @deprecated("Use v2 ++ v2", "3.1.0")
  def union(v1: Vault, v2: Vault): Vault = v1 ++ v2

}
