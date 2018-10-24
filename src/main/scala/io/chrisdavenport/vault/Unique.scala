package io.chrisdavenport.vault

import cats.effect.Sync

private[vault] final class Unique

private[vault] object Unique {
  def newUnique[F[_]: Sync]: F[Unique] = Sync[F].delay(new Unique)
}