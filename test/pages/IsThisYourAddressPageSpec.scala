/*
 * Copyright 2025 HM Revenue & Customs
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

package pages

import base.{SpecBase, TestValues}
import models.{AddressLookup, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class IsThisYourAddressPageSpec extends SpecBase with PageBehaviours with TestValues {

  "cleanUp" - {
    "must clear Individual address page object when No" in {
      val ua     = createUserAnswersForCleanup.sample.get
      val result = IsThisYourAddressPage.cleanup(Some(false), ua).success.value

      result.get(IndSelectAddressPage) mustBe empty
      result.get(IndSelectedAddressLookupPage) mustBe empty
    }

  }

  def createUserAnswersForCleanup: Gen[UserAnswers] = {

    val testParamGenerator = for {
      string        <- arbitrary[String]
      addressLookup <- arbitrary[AddressLookup]

    } yield (string, addressLookup)

    for {
      (string, addressLookup) <- testParamGenerator.suchThat(_ != null)
    } yield emptyUserAnswers
      .withPage(IndSelectAddressPage, string)
      .withPage(IndSelectedAddressLookupPage, addressLookup)
  }

}
