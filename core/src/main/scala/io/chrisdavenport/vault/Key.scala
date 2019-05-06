package io.chrisdavenport.vault

import cats.effect.Sync
import cats.Hash
import cats.implicits._
import io.chrisdavenport.unique.Unique

/**
  * A unique value tagged with a specific type to that unique.
  * Since it can only be created as a result of that, it links
  * a Unique identifier to a type known by the compiler.
  */
final class Key[A] private (private[vault] val unique: Unique) {
  override def hashCode(): Int = unique.hashCode()
}

object Key {
  /**
   * Create A Typed Key
   */
  def newKey[F[_]: Sync, A]: F[Key[A]] = Unique.newUnique[F].map(new Key[A](_))

  implicit def keyInstances[A]: Hash[Key[A]] = new Hash[Key[A]]{
    // Members declared in cats.kernel.Eq
    def eqv(x: Key[A],y: Key[A]): Boolean =
      x.unique === y.unique
    
    // Members declared in cats.kernel.Hash
    def hash(x: Key[A]): Int = Hash[Unique].hash(x.unique)
  }
}