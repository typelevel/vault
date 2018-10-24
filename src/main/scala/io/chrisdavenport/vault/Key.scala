package io.chrisdavenport.vault

import cats.effect.Sync
import cats.implicits._

final case class Key[A] private[vault] (private[vault] unique: Unique)

object Key {
  /**
   * Create A Typed Key
   */
  def newKey[F[_]: Sync, A]: F[Key[A]] = Unique.newUnique[F].map(Key[A])
}