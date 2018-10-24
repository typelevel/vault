package io.chrisdavenport.vault

import cats.implicits._
import cats.effect.Sync
import cats.effect.concurrent.Ref

/**
 * Banks Own a Vault but they won't let you take it with you.
 */
trait Bank[F[_]]{
  def createKey[A]: F[Key[A]]
  def lookup[A](k: Key[A]): F[Option[A]]
  def empty : F[Unit]
  def insert[A](k: Key[A], a: A): F[Unit]
  def delete[A](k: Key[A]): F[Unit]
  def adjust[A](k: Key[A], f: A => A): F[Unit]
}

object Bank {
  private class BankImpl[F[_]: Sync](ref: Ref[F, Vault]) extends Bank[F]{
    def createKey[A]: F[Key[A]] = Key.createKey[F, A]
    def lookup[A](k: Key[A]): F[Option[A]] = 
      ref.get.map(_.lookup(k))
    def empty : F[Unit] = ref.set(Vault.empty)
    def insert[A](k: Key[A], a: A): F[Unit] = 
      ref.update(_.insert(k, a))
    def delete[A](k: Key[A]): F[Unit] =
      ref.update(_.delete(k))
    def adjust[A](k: Key[A], f: A => A): F[Unit] =
      ref.update(_.adjust(k, f))
  }

  def build[F[_]: Sync]: F[Bank[F]] = 
    Ref.of[F, Vault](Vault.empty).map(new BankImpl(_))
}