package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ReporterTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ReporterType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ReporterType.values.toSeq)

      forAll(gen) {
        reporterType =>

          JsString(reporterType.toString).validate[ReporterType].asOpt.value mustEqual reporterType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ReporterType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ReporterType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ReporterType.values.toSeq)

      forAll(gen) {
        reporterType =>

          Json.toJson(reporterType) mustEqual JsString(reporterType.toString)
      }
    }
  }
}
