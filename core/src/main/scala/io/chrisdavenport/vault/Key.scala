package io.chrisdavenport.vault

import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.unique.Unique

/**
  * A unique value tagged with a specific type to that unique.
  * Since it can only be created as a result of that, it links
  * a Unique identifier to a type known by the compiler.
  */
sealed abstract case class Key[A] private (private[vault] val unique: Unique)

object Key {
  /**
   * Create A Typed Key
   */
  def newKey[F[_]: Sync, A]: F[Key[A]] = Unique.newUnique[F].map(new Key[A](_){})
}