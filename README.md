# vault [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.typelevel/vault_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.typelevel/vault_2.12)

Vault is a tiny library that provides a single data structure called vault.

Inspiration was drawn from [HeinrichApfelmus/vault](https://github.com/HeinrichApfelmus/vault) and the original [blog post](https://apfelmus.nfshost.com/blog/2011/09/04-vault.html)

A vault is a type-safe, persistent storage for values of arbitrary types. Like `Ref`, it should be capable of storing values of any type in it, but unlike `Ref`, behave like a persistent, first-class data structure.

It is analogous to a bank vault, where you can access different bank boxes with different keys; hence the name.

## Microsite

Head on over [to the microsite](https://typelevel.github.io/vault/)

## Quick Start

To use vault in an existing SBT project with Scala 2.12 or a later version, add the following dependencies to your
`build.sbt` depending on your needs:

```scala
libraryDependencies ++= Seq(
  "io.chrisdavenport" %% "vault" % "<version>",
)
```
