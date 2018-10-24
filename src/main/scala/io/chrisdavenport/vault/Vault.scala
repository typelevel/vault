package io.chrisdavenport.vault

import cats.implicits._
import cats.effect.Sync
object Vault {
  private[vault] final class Unique
  private[vault] object Unique {
    def newUnique[F[_]: Sync]: F[Unique] = Sync[F].delay(new Unique)
  }

  /**
    * Lockers
    **/
  final case class Key[A] private (private[vault] unique: Unique)
  object Key {
    def newKey[F[_]: Sync, A]: F[Key[A]] = Unique.newUnique[F].map(Key[A])
  }
  final case class Locker private (private[vault] unique: Unique, private[vault] a: Any){
    def unlock[A](k: Key[A]): Option[A] = Locker.unlock(k, this)
  }
  object Locker {
    def lock[A](k: Key[A], a: A): Locker = k match {
      case Key(u) => Locker(u, a.asInstanceOf[Any])
    }
    def unlock[A](k: Key[A], l: Locker): Option[A] = (k, l) match {
      // Equality By Reference Equality
      case (Key(u1), Locker(u2, a)) if u1 == u2 => 
        Some(a.asInstanceOf[A])
      case _ => None
    }
  }

  /**
    * Vault
    * Implemented as a collection of lockers
    **/
  final case class Vault private (private[vault] m: Map[Unique, Locker]){
    def lookup[A](k: Key[A]): Option[A] = Vault.lookup(k, this)
    def insert[A](k: Key[A], a: A): Vault = Vault.insert(k, a, this)
    def delete[A](k: Key[A]): Vault = Vault.delete(k, this)
  }
  object Vault {
    def empty = Vault(Map.empty)
    def lookup[A](k: Key[A], v: Vault): Option[A] = (k, v) match {
      case (key@Key(k), Vault(m)) => m.get(k).flatMap{locker => Locker.unlock(key, locker)}
    }
    def insert[A](k: Key[A], a:A, v: Vault): Vault = (k, v) match {
      case (key@Key(k), Vault(m)) => Vault(m + (k -> Locker.lock(key, a)))
    }
    def delete[A](k: Key[A], v: Vault): Vault = (k, v) match {
      case (Key(k), Vault(m)) => Vault(m - k)
    }
  }

}