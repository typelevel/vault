package io.chrisdavenport.vault

import cats._
import cats.implicits._
import cats.effect.IO
import cats.effect.concurrent.Ref

object Vault {
  final case class Unique(i: Int)
  object Unique {
    implicit val uniqueInstances: Eq[Unique] = Eq.by(_.i)

    private lazy val uniqueSource: Ref[IO, Int] = Ref.of[IO, Int](0).unsafeRunSync
    def newUnique: IO[Unique] = uniqueSource.modify{value => 
      val z = value + 1 
      (z,z)
    }.map(Unique(_))

  }

  /**
    * Lockers
    **/
  final case class Key[S, A](private[vault] unique: Unique, private[vault] a: Ref[IO, Option[A]])
  final case class Locker[S](private[vault] unique: Unique, private[vault] a: IO[Unit])

  def lock[S, A](k: Key[S, A], a: A): Locker[S] = k match {
    case Key(u, ref) => Locker(u, ref.update(_ => Some(a)))
  }
  def unlock[S, A](k: Key[S, A], l: Locker[S]): Option[A] = (k, l) match {
    case (Key(u1, ref), Locker(u2, m)) if u1 === u2 => 
      (m >> ref.get).unsafeRunSync
    case _ => None
  }

  /**
    * Vault
    * Implemented as a collection of lockers
    **/

  final case class Vault[S](private[vault] m: Map[Unique, Locker[S]])

  def empty[S] = Vault[S](Map.empty)

  def newKey[S, A]: IO[Key[S, A]] = for {
    unique <- Unique.newUnique
    ref <- Ref.of[IO, Option[A]](None)
  } yield Key[S, A](unique, ref)

}