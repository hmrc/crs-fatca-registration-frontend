/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll

class RegexConstantsSpec extends SpecBase with RegexConstants with Generators {

  "Postcode regex" - {
    "match for valid postcodes" - {
      forAll(validPostCodes) {
        validPostcode =>
          s"\"$validPostcode\" must match" in {
            validPostcode.matches(regexPostcode) mustBe true
          }
      }

      "\"SW1A 2AA\" must match" in {
        "SW1A 2AA".matches(regexPostcode) mustBe true
      }
    }
    "not match for invalid postcodes" - {
      val invalidPostcodes = List(
        // missing chars
        "SW 2AA",
        "SWA 2AA",
        "SW1A AA",
        // too many chars
        "SW11A 2AA",
        "SW1A 12AA",
        // whitespace
        " SW1A 2AA",
        "SW1A 2AA ",
        "SW1A  2AA"
      )

      invalidPostcodes.map {
        invalidPostcode =>
          s"\"$invalidPostcode\" must not match" in {
            invalidPostcode.matches(regexPostcode) mustBe false
          }
      }
    }
  }

}
