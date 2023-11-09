package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class IndWhereDoYouLiveSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "IndWhereDoYouLive" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(IndWhereDoYouLive.values.toSeq)

      forAll(gen) {
        indWhereDoYouLive =>
          JsString(indWhereDoYouLive.toString).validate[IndWhereDoYouLive].asOpt.value mustEqual indWhereDoYouLive
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!IndWhereDoYouLive.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[IndWhereDoYouLive] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(IndWhereDoYouLive.values.toSeq)

      forAll(gen) {
        indWhereDoYouLive =>
          Json.toJson(indWhereDoYouLive) mustEqual JsString(indWhereDoYouLive.toString)
      }
    }
  }

}
