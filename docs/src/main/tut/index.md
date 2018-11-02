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
```