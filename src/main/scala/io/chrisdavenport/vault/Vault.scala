package io.chrisdavenport.vault

import cats._
import cats.implicits._
import cats.effect.IO
import cats.effect.concurrent.Ref

object Vault {
  private[vault] final case class Unique (i: Int)
  private[vault] object Unique {
    implicit val uniqueInstances: Eq[Unique] = Eq.by(_.i)

    // Global Source of Uniqueness
    private lazy val uniqueSource: Ref[IO, Int] = Ref.of[IO, Int](0).unsafeRunSync
    def newUnique: IO[Unique] = uniqueSource.modify{value => 
      val z = value + 1 
      (z,z)
    }.map(Unique(_))

  }

  /**
    * Lockers
    **/
  final case class Key[A] private (private[vault] unique: Unique, private[vault] a: Ref[IO, Option[A]])
  final case class Locker private (private[vault] unique: Unique, private[vault] a: IO[Unit])

  def lock[A](k: Key[A], a: A): Locker = k match {
    case Key(u, ref) => Locker(u, ref.update(_ => Some(a)))
  }
  def unlock[A](k: Key[A], l: Locker): Option[A] = (k, l) match {
    case (Key(u1, ref), Locker(u2, m)) if u1 === u2 => 
      (m >> ref.get).unsafeRunSync // Race Condition
    case _ => None
  }

  /**
    * Vault
    * Implemented as a collection of lockers
    **/
  final case class Vault private (private[vault] m: Map[Unique, Locker])

  def empty = Vault(Map.empty)

  def newKey[S, A]: IO[Key[A]] = for {
    unique <- Unique.newUnique
    ref <- Ref.of[IO, Option[A]](None)
  } yield Key[A](unique, ref)

  def lookup[A](k: Key[A], v: Vault): Option[A] = (k, v) match {
    case (key@Key(k, _), Vault(m)) => m.get(k).flatMap{locker => unlock(key, locker)}
  }

  def insert[A](k: Key[A], a:A, v: Vault): Vault = (k, v) match {
    case (key@Key(k, _), Vault(m)) => Vault(m + (k -> lock(key, a)))
  }

  def delete[A](k: Key[A], v: Vault): Vault = (k, v) match {
    case (Key(k, _), Vault(m)) => Vault(m - k)
  }

}