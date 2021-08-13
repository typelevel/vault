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

import org.typelevel.unique.Unique

/**
 * Vault - A persistent store for values of arbitrary types.
 * This extends the behavior of the locker, into a Map
 * that maps Keys to Lockers, creating a heterogenous
 * store of values, accessible by keys. Such that the Vault
 * has no type information, all the type information is contained
 * in the keys.
 */
final class Vault private (private val m: Map[Unique, Locker]) {

  /**
   * Empty this Vault
   */
  def empty: Vault = Vault.empty

  /**
   * Lookup the value of a key in this vault
   */
  def lookup[A](k: Key[A]): Option[A] = Vault.lookup(k, this)

  /**
   * Insert a value for a given key. Overwrites any previous value.
   */
  def insert[A](k: Key[A], a: A): Vault = Vault.insert(k, a, this)

  /**
   * Checks whether this Vault is empty
   */
  def isEmpty: Boolean = Vault.isEmpty(this)

  /**
   * Delete a key from the vault
   */
  def delete[A](k: Key[A]): Vault = Vault.delete(k, this)

  /**
   * Adjust the value for a given key if it's present in the vault.
   */
  def adjust[A](k: Key[A], f: A => A): Vault = Vault.adjust(k, f, this)

  /**
   * Merge Two Vaults. that is prioritized.
   */
  def ++(that: Vault): Vault = Vault.union(this, that)
}
object Vault {

  /**
   * The Empty Vault
   */
  def empty = new Vault(Map.empty)

  /**
   * Lookup the value of a key in the vault
   */
  def lookup[A](k: Key[A], v: Vault): Option[A] =
    v.m.get(k.unique).flatMap(Locker.unlock(k, _))

  /**
   * Insert a value for a given key. Overwrites any previous value.
   */
  def insert[A](k: Key[A], a: A, v: Vault): Vault =
    new Vault(v.m + (k.unique -> Locker.lock(k, a)))

  /**
   * Checks whether the given Vault is empty
   */
  def isEmpty(v: Vault): Boolean =
    v.m.isEmpty

  /**
   * Delete a key from the vault
   */
  def delete[A](k: Key[A], v: Vault): Vault =
    new Vault(v.m - k.unique)

  /**
   * Adjust the value for a given key if it's present in the vault.
   */
  def adjust[A](k: Key[A], f: A => A, v: Vault): Vault =
    lookup(k, v).fold(v)(a => insert(k, f(a), v))

  /**
   * Merge Two Vaults. v2 is prioritized.
   */
  def union(v1: Vault, v2: Vault): Vault =
    new Vault(v1.m ++ v2.m)

}
