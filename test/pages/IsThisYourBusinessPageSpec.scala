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

import models.matching.RegistrationInfo
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class IsThisYourBusinessPageSpec extends PageBehaviours {

  private val testParamGenerator = for {
    addressLookup    <- arbitrary[models.AddressLookup]
    address          <- arbitrary[models.Address]
    postcode         <- arbitrary[String]
    registrationInfo <- arbitrary[RegistrationInfo]
  } yield (addressLookup, address, postcode, registrationInfo)

  "IsThisYourBusinessPage" - {

    beRetrievable[Boolean](IsThisYourBusinessPage)

    beSettable[Boolean](IsThisYourBusinessPage)

    beRemovable[Boolean](IsThisYourBusinessPage)
  }

  "cleanup" - {
    "must remove RegistrationInfoPage answers when No" in {
      forAll(testParamGenerator) {
        case (addressLookup, address, postcode, registrationInfo) =>
          val userAnswers = emptyUserAnswers.withPage(RegistrationInfoPage, registrationInfo)

          val result = IsThisYourBusinessPage.cleanup(Some(false), userAnswers).success.value

          result.get(RegistrationInfoPage) mustBe empty
      }
    }
    "must remove answers answers when Yes" in {

      forAll(testParamGenerator) {
        case (addressLookup, address, postcode, registrationInfo) =>
          val userAnswers = emptyUserAnswers
            .withPage(IndWhatIsYourPostcodePage, postcode)
            .withPage(IndSelectAddressPage, "someString")
            .withPage(IndUKAddressWithoutIdPage, address)
            .withPage(AddressLookupPage, Seq(addressLookup))
            .withPage(NonUKBusinessAddressWithoutIDPage, address)

          val result = IsThisYourBusinessPage.cleanup(Some(true), userAnswers).success.value

          result.get(IndWhatIsYourPostcodePage) mustBe empty
          result.get(IndSelectAddressPage) mustBe empty
          result.get(IndUKAddressWithoutIdPage) mustBe empty
          result.get(AddressLookupPage) mustBe empty
          result.get(NonUKBusinessAddressWithoutIDPage) mustBe empty
      }
    }

  }

}
