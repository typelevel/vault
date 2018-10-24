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

      Key.createKey[IO, Int].map{k => 
        emptyVault.insert(k, i).lookup(k)
      }.unsafeRunSync must_=== Some(i)
    }
    "contain only the last value after inserts" >> prop { l: List[String]=> 
      val emptyVault : Vault = Vault.empty
      val test : IO[Option[String]] = Key.createKey[IO, String].map{k => 
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).lookup(k)
      }
      test.unsafeRunSync must_=== l.headOption
    }
    "contain no value after being emptied" >> prop { l: List[String]=> 
      val emptyVault : Vault = Vault.empty
      val test : IO[Option[String]] = Key.createKey[IO, String].map{k => 
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).empty.lookup(k)
      }
      test.unsafeRunSync must_=== None
    }
  }

}