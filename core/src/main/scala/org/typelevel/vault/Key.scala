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

import cats.Contravariant
import cats.Functor
import cats.effect.kernel.Unique
import cats.Hash
import cats.implicits._
import cats.Invariant
import cats.data.AndThen

/**
 * A unique value tagged with a specific type to that unique. Since it can only be created as a result of that, it links
 * a Unique identifier to a type known by the compiler.
 */
final class Key[A] private (
  private[vault] val unique: Unique.Token,
  private[vault] val imapping: InvariantMapping[A]
) extends InsertKey[A]
    with LookupKey[A] {

  // Delegates, for convenience.
  private[vault] type I = imapping.I
  private[vault] val in = imapping.in
  private[vault] val out = imapping.out

  // Overloaded ctor to preserve bincompat
  def this(unique: Unique.Token) =
    this(unique, InvariantMapping.id[A])

  /**
   * Create a copy of this key that references the same underlying vault element, transformed from type `B` before
   * insert, and to `B` after lookup.
   */
  def imap[B](f: A => B)(g: B => A): Key[B] =
    new Key(unique, imapping.imap(f)(g))

  override def hashCode(): Int = unique.hashCode()

}

sealed trait InsertKey[-A] { outer =>
  private[vault] def unique: Unique.Token
  private[vault] type I
  private[vault] def in: A => I

  def contramap[B](f: B => A): InsertKey[B] =
    new InsertKey[B] {
      val unique = outer.unique
      type I = outer.I
      val in = AndThen(f).andThen(outer.in)
    }
}

object InsertKey {
  implicit val ContravariantInsertKey: Contravariant[InsertKey] =
    new Contravariant[InsertKey] {
      def contramap[A, B](fa: InsertKey[A])(f: B => A): InsertKey[B] =
        fa.contramap(f)
    }
}

sealed trait LookupKey[+A] { outer =>
  private[vault] def unique: Unique.Token
  private[vault] type I
  private[vault] def out: I => A

  def map[B](f: A => B): LookupKey[B] =
    new LookupKey[B] {
      val unique = outer.unique
      type I = outer.I
      val out = AndThen(outer.out).andThen(f)
    }
}

object LookupKey {
  implicit val FunctorLookupKey: Functor[LookupKey] =
    new Functor[LookupKey] {
      def map[A, B](fa: LookupKey[A])(f: A => B): LookupKey[B] =
        fa.map(f)
    }
}

object Key {

  /**
   * Create A Typed Key
   */
  def newKey[F[_]: Functor: Unique, A]: F[Key[A]] = Unique[F].unique.map(new Key[A](_))

  implicit def keyInstances[A]: Hash[Key[A]] = new Hash[Key[A]] {
    // Members declared in cats.kernel.Eq
    def eqv(x: Key[A], y: Key[A]): Boolean =
      x.unique === y.unique

    // Members declared in cats.kernel.Hash
    def hash(x: Key[A]): Int = Hash[Unique.Token].hash(x.unique)
  }

  implicit val InvariantKey: Invariant[Key] =
    new Invariant[Key] {
      def imap[A, B](fa: Key[A])(f: A => B)(g: B => A): Key[B] =
        fa.imap(f)(g)
    }

}
