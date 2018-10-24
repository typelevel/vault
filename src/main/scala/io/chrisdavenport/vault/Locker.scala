package io.chrisdavenport.vault

/**
  * Lockers
  **/
final case class Locker private[vault] (private[vault] unique: Unique, private[vault] a: Any){
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