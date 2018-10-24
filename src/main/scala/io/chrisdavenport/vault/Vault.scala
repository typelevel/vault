package io.chrisdavenport.vault

import cats.Monoid

/**
  * Vault - A persistent store for values of arbitrary types.
  **/
final case class Vault private (private[vault] m: Map[Unique, Locker]) {
  def empty : Vault = Vault.empty
  def lookup[A](k: Key[A]): Option[A] = Vault.lookup(k, this)
  def insert[A](k: Key[A], a: A): Vault = Vault.insert(k, a, this)
  def delete[A](k: Key[A]): Vault = Vault.delete(k, this)
}
object Vault {
  /**
    * The Empty Vault 
    */
  def empty = Vault(Map.empty)

  /**
    * Lookup the value of a key in the vault
    **/
  def lookup[A](k: Key[A], v: Vault): Option[A] = (k, v) match {
    case (key@Key(k), Vault(m)) => m.get(k).flatMap{locker => Locker.unlock(key, locker)}
  }

  /**
    * Insert a value for a given key. Overwrites any previous value.
    */
  def insert[A](k: Key[A], a:A, v: Vault): Vault = (k, v) match {
    case (key@Key(k), Vault(m)) => Vault(m + (k -> Locker.lock(key, a)))
  }
  
  /**
    * Delete a key from the vault
    **/
  def delete[A](k: Key[A], v: Vault): Vault = (k, v) match {
    case (Key(k), Vault(m)) => Vault(m - k)
  }

  /**
    * Adjust the value for a given key if it's present in the vault.
    **/
  def adjust[A](k: Key[A], f: A => A, v: Vault): Vault = 
    lookup(k, v).fold(v)(a => insert(k, f(a), v))

  /**
    * Merge Two Vaults
    */
  def union(v1: Vault, v2: Vault): Vault = 
    Vault(v1.m ++ v2.m)

  /**
    * Vault Instances
    * Semigroup
    * Monoid
    **/
  implicit val vaultInstances : Monoid[Vault] = new Monoid[Vault]{
    def empty: Vault = 
      Vault.empty
    def combine(x: Vault,y: Vault): Vault = 
      Vault.union(x, y)
  }
}