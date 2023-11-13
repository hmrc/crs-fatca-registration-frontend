package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class IndSelectAddressSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "IndSelectAddress" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(IndSelectAddress.values.toSeq)

      forAll(gen) {
        indSelectAddress =>
          JsString(indSelectAddress.toString).validate[IndSelectAddress].asOpt.value mustEqual indSelectAddress
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!IndSelectAddress.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[IndSelectAddress] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(IndSelectAddress.values.toSeq)

      forAll(gen) {
        indSelectAddress =>
          Json.toJson(indSelectAddress) mustEqual JsString(indSelectAddress.toString)
      }
    }
  }

}
