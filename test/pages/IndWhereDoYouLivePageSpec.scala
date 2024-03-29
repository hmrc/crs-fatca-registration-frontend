/*
 * Copyright 2023 HM Revenue & Customs
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

import helpers.JsonFixtures.TestPostCode
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.behaviours.PageBehaviours

class IndWhereDoYouLivePageSpec extends PageBehaviours {

  "IndWhereDoYouLivePage" - {

    beRetrievable[Boolean](IndWhereDoYouLivePage)

    beSettable[Boolean](IndWhereDoYouLivePage)

    beRemovable[Boolean](IndWhereDoYouLivePage)

    "cleanup" - {
      "must remove IndNonUKAddressWithoutIdPage answers when true" in {
        ScalaCheckPropertyChecks.forAll(arbitrary[models.Address]) {
          address =>
            val userAnswers = emptyUserAnswers.withPage(IndNonUKAddressWithoutIdPage, address)

            val result = IndWhereDoYouLivePage.cleanup(Some(true), userAnswers).success.value

            result.get(IndNonUKAddressWithoutIdPage) mustBe empty
        }
      }

      "must remove IndWhatIsYourPostcodePage, IndSelectAddressPage, IndUKAddressWithoutIdPage, IndSelectedAddressLookupPage, " +
        "and AddressLookupPage answers when false" in {
          val generator = for {
            addressLookup <- arbitrary[models.AddressLookup]
            address       <- arbitrary[models.Address]
          } yield (addressLookup, address)

          forAll(generator) {
            case (addressLookup, address) =>
              val userAnswers = emptyUserAnswers
                .withPage(IndSelectAddressPage, addressLookup.format)
                .withPage(IndWhatIsYourPostcodePage, TestPostCode)
                .withPage(IndSelectedAddressLookupPage, addressLookup)
                .withPage(AddressLookupPage, Seq(addressLookup))
                .withPage(IndUKAddressWithoutIdPage, address)

              val result = IndWhereDoYouLivePage.cleanup(Some(false), userAnswers).success.value

              result.get(IndSelectAddressPage) mustBe empty
              result.get(IndWhatIsYourPostcodePage) mustBe empty
              result.get(IndSelectedAddressLookupPage) mustBe empty
              result.get(AddressLookupPage) mustBe empty
              result.get(IndUKAddressWithoutIdPage) mustBe empty
          }
        }
    }
  }

}
