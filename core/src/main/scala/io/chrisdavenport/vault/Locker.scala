package io.chrisdavenport.vault

/**
 * Locker - A persistent store for a single value
 **/
final class Locker private[vault] (private[vault] val unique: Unique, private[vault] val a: Any){
  /**
   * Retrieve the value from the Locker
   */
  def unlock[A](k: Key[A]): Option[A] = Locker.unlock(k, this)
}

object Locker {
  /**
   * Put a single value into a Locker
   */
  def lock[A](k: Key[A], a: A): Locker = new Locker(k.unique, a.asInstanceOf[Any])

  /**
   * Retrieve the value from the Locker
   */
  def unlock[A](k: Key[A], l: Locker): Option[A] = 
    // Equality By Reference Equality
    if (k.unique == l.unique) Some(l.a.asInstanceOf[A])
    else None
}