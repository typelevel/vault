/*
 * Copyright (c) 2021 Christopher Davenport
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.typelevel.vault

import cats.effect._
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF

class VaultSuite extends CatsEffectSuite with ScalaCheckEffectSuite {
  test("Vault should contain a single value correctly") {
    PropF.forAllF { (i: Int) =>
      val emptyVault: Vault = Vault.empty
      val test = Key.newKey[IO, Int].map(k => emptyVault.insert(k, i).lookup(k))

      assertIO(test, Some(i))
    }
  }

  test("Vault should contain only the last value after inserts") {
    PropF.forAllF { (l: List[String]) =>
      val emptyVault: Vault = Vault.empty
      val test = Key.newKey[IO, String].map { k =>
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).lookup(k)
      }

      assertIO(test, l.headOption)
    }
  }

  test("Vault should contain no value after being emptied") {
    PropF.forAllF { (l: List[String]) =>
      val emptyVault: Vault = Vault.empty
      val test: IO[Option[String]] = Key.newKey[IO, String].map { k =>
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).empty.lookup(k)
      }

      assertIO(test, None)
    }
  }

  test("Vault should not be accessible via a different key") {
    PropF.forAllF { (i: Int) =>
      val test = for {
        key1 <- Key.newKey[IO, Int]
        key2 <- Key.newKey[IO, Int]
      } yield Vault.empty.insert(key1, i).lookup(key2)

      assertIO(test, None)
    }
  }

  test("Vault should contain mapped value inserted with unmapped key") {
    PropF.forAllF { (i: Int) =>
      val emptyVault: Vault = Vault.empty
      val test =
        for {
          k  <- Key.newKey[IO, Int]
          kʹ  = k.imap(_.toString)(_.toInt)  // create a mapped key
          vʹ  = emptyVault.insert(k, i)      // insert using unmapped key
          s   = vʹ.lookup(kʹ)                // read using mapped key
        } yield s
      assertIO(test, Some(i.toString))
    }
  }

  test("Vault should contain unmapped value inserted with mapped key") {
    PropF.forAllF { (i: Int) =>
      val emptyVault: Vault = Vault.empty
      val test =
        for {
          k  <- Key.newKey[IO, Int]
          kʹ  = k.imap(_.toString)(_.toInt)       // create a mapped key
          vʹ  = emptyVault.insert(kʹ, i.toString) // insert using mapped key
          n   = vʹ.lookup(k)                      // read using unmapped key
        } yield n
      assertIO(test, Some(i))
    }
  }

  test("Vault should contain mapped value inserted with mapped key") {
    PropF.forAllF { (i: Int) =>
      val emptyVault: Vault = Vault.empty
      val test =
        for {
          k  <- Key.newKey[IO, Int]
          kʹ  = k.imap(_.toString)(_.toInt)       // create a mapped key
          vʹ  = emptyVault.insert(kʹ, i.toString) // insert using mapped key
          n   = vʹ.lookup(kʹ)                      // read using mapped key
        } yield n
      assertIO(test, Some(i.toString))
    }
  }

}
