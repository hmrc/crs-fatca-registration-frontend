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

package pages

import models.matching.RegistrationInfo
import models.{Name, UniqueTaxpayerReference}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class DoYouHaveUniqueTaxPayerReferencePageSpec extends PageBehaviours {

  private val testParamGenerator = for {
    addressLookup    <- arbitrary[models.AddressLookup]
    address          <- arbitrary[models.Address]
    postcode         <- arbitrary[String]
    name             <- arbitrary[models.Name]
    booleanField     <- arbitrary[Boolean]
    nino             <- arbitrary[Nino]
    registrationInfo <- arbitrary[RegistrationInfo]
    dob              <- arbitrary[LocalDate]
  } yield (addressLookup, address, postcode, name, booleanField, nino, registrationInfo, dob)

  "IndDoYouHaveNINumberPage" - {

    beRetrievable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beSettable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beRemovable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)
  }

  "cleanup" - {
    "if answer is false" in {
      val ua = emptyUserAnswers
        .withPage(WhatIsYourUTRPage, UniqueTaxpayerReference("utr12345"))
        .withPage(WhatIsYourNamePage, Name("first", "second"))
        .withPage(BusinessNamePage, "businessName")
        .withPage(IsThisYourBusinessPage, true)

      val result = DoYouHaveUniqueTaxPayerReferencePage.cleanup(Some(false), ua).success.value

      result.get(WhatIsYourUTRPage) mustBe empty
      result.get(WhatIsYourNamePage) mustBe empty
      result.get(BusinessNamePage) mustBe empty
      result.get(IsThisYourBusinessPage) mustBe empty

    }

    "if answer is true" in {
      forAll(testParamGenerator) {
        case (addressLookup, address, postcode, name, booleanField, nino, registrationInfo, dob) =>
          val ua = emptyUserAnswers
            .withPage(BusinessNameWithoutIDPage, "BusinessNameWithoutID")
            .withPage(HaveTradingNamePage, booleanField)
            .withPage(BusinessTradingNameWithoutIDPage, "BusinessTradingNameWithoutID")
            .withPage(NonUKBusinessAddressWithoutIDPage, address)
            .withPage(BusinessNameWithoutIDPage, "BusinessNameWithoutIDPage")
            .withPage(HaveTradingNamePage, booleanField)
            .withPage(BusinessTradingNameWithoutIDPage, "BusinessTradingNameWithoutIDPage")
            .withPage(NonUKBusinessAddressWithoutIDPage, address)
            .withPage(IndWhatIsYourNINumberPage, nino)
            .withPage(IndContactNamePage, name)
            .withPage(IndDateOfBirthPage, LocalDate.now())
            .withPage(RegistrationInfoPage, registrationInfo)
            .withPage(IndWhatIsYourNamePage, name)
            .withPage(DateOfBirthWithoutIdPage, dob)
            .withPage(IndWhereDoYouLivePage, booleanField)
            .withPage(IndWhatIsYourPostcodePage, postcode)
            .withPage(AddressLookupPage, Seq(addressLookup))
            .withPage(IndSelectAddressPage, "something")
            .withPage(IndSelectedAddressLookupPage, addressLookup)
            .withPage(IsThisYourAddressPage, booleanField)
            .withPage(IndUKAddressWithoutIdPage, address)
            .withPage(IndNonUKAddressWithoutIdPage, address)
            .withPage(IndDoYouHaveNINumberPage, booleanField)

          val result = DoYouHaveUniqueTaxPayerReferencePage.cleanup(Some(true), ua).success.value

          result.get(BusinessNameWithoutIDPage) mustBe empty
          result.get(HaveTradingNamePage) mustBe empty
          result.get(BusinessTradingNameWithoutIDPage) mustBe empty
          result.get(NonUKBusinessAddressWithoutIDPage) mustBe empty
          result.get(IndWhatIsYourNINumberPage) mustBe empty
          result.get(IndContactNamePage) mustBe empty
          result.get(IndDateOfBirthPage) mustBe empty
          result.get(RegistrationInfoPage) mustBe empty
          result.get(IndWhatIsYourNamePage) mustBe empty
          result.get(DateOfBirthWithoutIdPage) mustBe empty
          result.get(IndWhereDoYouLivePage) mustBe empty
          result.get(IndWhatIsYourPostcodePage) mustBe empty
          result.get(AddressLookupPage) mustBe empty
          result.get(IndSelectAddressPage) mustBe empty
          result.get(IndSelectedAddressLookupPage) mustBe empty
          result.get(IsThisYourAddressPage) mustBe empty
          result.get(IndUKAddressWithoutIdPage) mustBe empty
          result.get(IndNonUKAddressWithoutIdPage) mustBe empty
          result.get(IndDoYouHaveNINumberPage) mustBe empty

      }
    }

  }

}
