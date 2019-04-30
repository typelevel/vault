package io.chrisdavenport.vault

import org.specs2.mutable.Specification
import org.specs2.ScalaCheck

// import cats._
// import cats.implicits._
import cats.effect._
// import org.scalacheck._

class VaultSpec extends Specification with ScalaCheck {
  "Vault" should {
    "contain a single value correctly" >> prop { i: Int => 
      val emptyVault : Vault = Vault.empty

      Key.newKey[IO, Int].map{k => 
        emptyVault.insert(k, i).lookup(k)
      }.unsafeRunSync must_=== Some(i)
    }
    "contain only the last value after inserts" >> prop { l: List[String]=> 
      val emptyVault : Vault = Vault.empty
      val test : IO[Option[String]] = Key.newKey[IO, String].map{k => 
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).lookup(k)
      }
      test.unsafeRunSync must_=== l.headOption
    }
    "contain no value after being emptied" >> prop { l: List[String]=> 
      val emptyVault : Vault = Vault.empty
      val test : IO[Option[String]] = Key.newKey[IO, String].map{k => 
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).empty.lookup(k)
      }
      test.unsafeRunSync must_=== None
    }
    "not be accessible via a different key" >> prop { i: Int => 
      val test = for {
        key1 <- Key.newKey[IO, Int]
        key2 <- Key.newKey[IO, Int]
      } yield Vault.empty.insert(key1, i).lookup(key2)
      test.unsafeRunSync must_=== None
    }
    "be equal to an empty Vault, if it is empty too" >> {
      val emptyVault1: Vault = Vault.empty
      val emptyVault2: Vault = Vault.empty

      emptyVault1 must_=== emptyVault2
    }
  }

}