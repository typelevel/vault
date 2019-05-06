package io.chrisdavenport.vault

import org.scalacheck._
import cats.effect.IO
import cats.tests.CatsSuite
import cats.kernel.laws.discipline.{EqTests, HashTests}


class UniqueTests extends CatsSuite {

  implicit def functionArbitrary[B, A: Arbitrary]: Arbitrary[B => A] = Arbitrary{
    for {
      a <- Arbitrary.arbitrary[A]
    } yield {_: B => a}
  }

  implicit def uniqueKey[A]: Arbitrary[Key[A]] = Arbitrary{
    Arbitrary.arbitrary[Unit].map(_ => Key.newKey[IO, A].unsafeRunSync)
  }


  checkAll("Key", HashTests[Key[Int]].hash)
  checkAll("Key", EqTests[Key[Int]].eqv)
}