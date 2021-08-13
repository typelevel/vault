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
import org.specs2.mutable.Specification
import org.specs2.ScalaCheck

class VaultSpec extends Specification with ScalaCheck {

  "Vault" should {
    "contain a single value correctly" >> prop { (i: Int) =>
      val emptyVault: Vault = Vault.empty

      Key
        .newKey[IO, Int]
        .map { k =>
          emptyVault.insert(k, i).lookup(k)
        }
        .unsafeRunSync() === Some(i)

    }
    "contain only the last value after inserts" >> prop { (l: List[String]) =>
      val emptyVault: Vault = Vault.empty
      val test: IO[Option[String]] = Key.newKey[IO, String].map { k =>
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).lookup(k)
      }
      test.unsafeRunSync() === l.headOption
    }

    "contain no value after being emptied" >> prop { (l: List[String]) =>
      val emptyVault: Vault = Vault.empty
      val test: IO[Option[String]] = Key.newKey[IO, String].map { k =>
        l.reverse.foldLeft(emptyVault)((v, a) => v.insert(k, a)).empty.lookup(k)
      }
      test.unsafeRunSync() === None
    }

    "not be accessible via a different key" >> prop { (i: Int) =>
      val test = for {
        key1 <- Key.newKey[IO, Int]
        key2 <- Key.newKey[IO, Int]
      } yield Vault.empty.insert(key1, i).lookup(key2)
      test.unsafeRunSync() === None
    }
  }

}
