---
layout: home

---

# vault [![Build Status](https://travis-ci.com/ChristopherDavenport/vault.svg?branch=master)](https://travis-ci.com/ChristopherDavenport/vault) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/vault_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/vault-core_2.12)

## Project Goals

Vault is a tiny library that provides a single data structure called vault.

Inspiration was drawn from [HeinrichApfelmus/vault](https://github.com/HeinrichApfelmus/vault) and the original [blog post](https://apfelmus.nfshost.com/blog/2011/09/04-vault.html)

A vault is a type-safe, persistent storage for values of arbitrary types. Like `Ref`, it should be capable of storing values of any type in it, but unlike `Ref`, behave like a persistent, first-class data structure.

It is analogous to a bank vault, where you can access different bank boxes with different keys; hence the name.

## Quick Start

To use vault in an existing SBT project with Scala 2.11 or a later version, add the following dependencies to your
`build.sbt` depending on your needs:

```scala
libraryDependencies ++= Seq(
  "io.chrisdavenport" %% "vault" % "<version>",
)
```

First the imports

```tut:silent
import cats.effect._
import io.chrisdavenport.vault._
```

Then some basic operations

```tut:book
case class Bar(a: String, b: Int, c: Long)

// Creating keys are effects, but interacting with the vault
// not, it acts like a simple persistent store.

val basicLookup = for {
  key <- Key.newKey[IO, Bar]
} yield {
  Vault.empty
    .insert(key, Bar("", 1, 2L))
    .lookup(key)
}

basicLookup.unsafeRunSync

val lookupValuesOfDifferentTypes = for {
  key1 <- Key.newKey[IO, Bar]
  key2 <- Key.newKey[IO, String]
  key3 <- Key.newKey[IO, String]
} yield {
  val myvault = Vaul.empty
    .insert(key1, Bar("", 1, 2L))
    .insert(key2, "I'm at Key2")
    .insert(key3, "Key3 Reporting for Duty!")
  
  (myvault.lookup(key1), myvault.lookup(key2), myvault.lookup(key3))
    .mapN((_,_,_))
}

lookupValuesOfDifferentTypes.unsafeRunSync

val emptyLookup = for {
  key <- Key.newKey[IO, Bar]
} yield {
  Vault.empty
    .lookup(key)
}

emptyLookup.unsafeRunSync

val doubleInsertTakesMostRecent = for {
  key <- Key.newKey[IO, Bar]
} yield {
  Vault.empty
    .insert(key, Bar("", 1, 2L))
    .insert(key, Bar("Monkey", 7, 5L))
    .lookup(key)
}

doubleInsertTakesMostRecent.unsafeRunSync

val mergedVaultsTakesLatter = for {
  key <- Key.newKey[IO, Bar]
} yield {
  (
    Vault.empty.insert(key, Bar("", 1, 2L)) ++
    Vault.empty.insert(key, Bar("Monkey", 7, 5L))
  ).lookup(key)
}

mergedVaultsTakesLatter.unsafeRunSync

val deletedKeyIsMissing = for {
  key <- Key.newKey[IO, Bar]
} yield {
  Vault.empty
    .insert(key, Bar("", 1, 2L))
    .delete(key)
    .lookup(key)
}

deletedKeyIsMissing.unsafeRunSync
```

We can also interact with a single value `locker` instead of the
larger datastructure that a `vault` enables.

```tut:book
val lockerExample = for {
  key <- Key.newKey[IO, Bar]
} yield {
  Locker.lock(key, Bar("", 1, 2L))
    .unlock(key)
}

lockerExample.unsafeRunSync

val wrongLockerExample = for {
  key <- Key.newKey[IO, Bar]
  key2 <- Key.newKey[IO, Bar]
} yield {
  Locker.lock(key, Bar("", 1, 2L))
    .unlock(key2)
}

wrongLockerExample.unsafeRunSync
```
