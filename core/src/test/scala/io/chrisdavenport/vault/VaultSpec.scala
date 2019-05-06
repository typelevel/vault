package io.chrisdavenport.vault

// import cats._
// import cats.implicits._
import cats.effect._
import cats.tests.CatsSuite
// import org.scalacheck._

class VaultSpec extends CatsSuite {

  test("Vault contain a single value correctly"){

    i: Int => 
      val emptyVault : Vault = Vault.empty

      Key.newKey[IO, Int].map{k => 
        emptyVault.insert(k, i).lookup(k)
      }.unsafeRunSync === Some(i)

  }
  test("Vault contain only the last value after inserts"){
    l: List[String]=> 
      val emptyVault : Vault = Vault.empty
      val test : IO[Option[String]] = Key.newKey[IO, String].map{k => 
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).lookup(k)
      }
      test.unsafeRunSync === l.headOption
  }

  test("Vault contain no value after being emptied"){
    l: List[String]=> 
      val emptyVault : Vault = Vault.empty
      val test : IO[Option[String]] = Key.newKey[IO, String].map{k => 
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).empty.lookup(k)
      }
      test.unsafeRunSync === None
  }

  test("Vault not be accessible via a different key") { i: Int => 
      val test = for {
        key1 <- Key.newKey[IO, Int]
        key2 <- Key.newKey[IO, Int]
      } yield Vault.empty.insert(key1, i).lookup(key2)
      test.unsafeRunSync === None
  }


}