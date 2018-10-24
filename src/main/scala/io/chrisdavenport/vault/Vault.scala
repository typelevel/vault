package io.chrisdavenport.vault

/**
  * Vault
  * Implemented as a collection of lockers
  **/
final case class Vault private (private[vault] m: Map[Unique, Locker]) {
  def empty : Vault = Vault.empty
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