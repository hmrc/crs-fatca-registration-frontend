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
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class RegisteredAddressInUKPageSpec extends PageBehaviours {

  private val businessWithoutIdTestParamGenerator = for {
    addressLookup    <- arbitrary[models.AddressLookup]
    address          <- arbitrary[models.Address]
    postcode         <- arbitrary[String]
    name             <- arbitrary[models.Name]
    booleanField     <- arbitrary[Boolean]
    nino             <- arbitrary[Nino]
    date             <- arbitrary[LocalDate]
    stringField      <- arbitrary[String]
    registrationInfo <- arbitrary[RegistrationInfo]
  } yield (addressLookup, address, postcode, name, booleanField, nino, date, stringField, registrationInfo)

  "RegisteredAddressInUKPage" - {

    beRetrievable[Boolean](RegisteredAddressInUKPage)

    beSettable[Boolean](RegisteredAddressInUKPage)

    beRemovable[Boolean](RegisteredAddressInUKPage)

    "cleanup" - {
      "must remove business without Id answers when true" in {
        forAll(businessWithoutIdTestParamGenerator) {
          case (addressLookup, address, postcode, name, booleanField, nino, date, stringField, registrationInfo) =>
            val userAnswers = emptyUserAnswers
              .withPage(DateOfBirthWithoutIdPage, LocalDate.now())
              .withPage(IndWhereDoYouLivePage, booleanField)
              .withPage(IndWhatIsYourPostcodePage, postcode)
              .withPage(AddressLookupPage, Seq(addressLookup))
              .withPage(IndSelectAddressPage, addressLookup.format)
              .withPage(IndSelectedAddressLookupPage, addressLookup)
              .withPage(IsThisYourAddressPage, booleanField)
              .withPage(IndUKAddressWithoutIdPage, address)
              .withPage(IndNonUKAddressWithoutIdPage, address)
              .withPage(IndWhatIsYourNINumberPage, nino)
              .withPage(BusinessNameWithoutIDPage, stringField)
              .withPage(HaveTradingNamePage, booleanField)
              .withPage(BusinessTradingNameWithoutIDPage, stringField)
              .withPage(NonUKBusinessAddressWithoutIDPage, address)
              .withPage(IndContactNamePage, name)
              .withPage(IndDateOfBirthPage, date)
              .withPage(IndDoYouHaveNINumberPage, booleanField)
              .withPage(DoYouHaveUniqueTaxPayerReferencePage, booleanField)
              .withPage(RegistrationInfoPage, registrationInfo)

            val result = RegisteredAddressInUKPage.cleanup(Some(true), userAnswers).success.value

            result.get(DateOfBirthWithoutIdPage) mustBe empty
            result.get(IndWhereDoYouLivePage) mustBe empty
            result.get(IndWhatIsYourPostcodePage) mustBe empty
            result.get(AddressLookupPage) mustBe empty
            result.get(IndSelectAddressPage) mustBe empty
            result.get(IndSelectedAddressLookupPage) mustBe empty
            result.get(IsThisYourAddressPage) mustBe empty
            result.get(IndUKAddressWithoutIdPage) mustBe empty
            result.get(IndNonUKAddressWithoutIdPage) mustBe empty
            result.get(IndWhatIsYourNINumberPage) mustBe empty
            result.get(BusinessNameWithoutIDPage) mustBe empty
            result.get(HaveTradingNamePage) mustBe empty
            result.get(BusinessTradingNameWithoutIDPage) mustBe empty
            result.get(NonUKBusinessAddressWithoutIDPage) mustBe empty
            result.get(IndContactNamePage) mustBe empty
            result.get(IndDateOfBirthPage) mustBe empty
            result.get(IndDoYouHaveNINumberPage) mustBe empty
            result.get(DoYouHaveUniqueTaxPayerReferencePage) mustBe empty
            result.get(RegistrationInfoPage) mustBe empty
        }
      }
    }
  }

}
