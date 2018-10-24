package io.chrisdavenport.vault

import cats.implicits._
import cats.effect._
import cats.effect.concurrent.Ref

object Vault {
  private[vault] final class Unique
  private[vault] object Unique {
    def newUnique[F[_]: Sync]: F[Unique] = Sync[F].delay(new Unique)
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
    // Equality By Reference Equality
    case (Key(u1, ref), Locker(u2, m)) if u1 == u2 => 
      (m >> ref.get).unsafeRunSync // Race Condition
    case _ => None
  }

  /**
    * Vault
    * Implemented as a collection of lockers
    **/
  final case class Vault private (private[vault] m: Map[Unique, Locker])

  def empty = Vault(Map.empty)

  def newKey[A]: IO[Key[A]] = for {
    unique <- Unique.newUnique[IO]
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