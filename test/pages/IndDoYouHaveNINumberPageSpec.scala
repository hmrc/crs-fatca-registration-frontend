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

class IndDoYouHaveNINumberPageSpec extends PageBehaviours {

  private val individualWithoutIdTestParamGenerator = for {
    addressLookup <- arbitrary[models.AddressLookup]
    address       <- arbitrary[models.Address]
    postcode      <- arbitrary[String]
    name          <- arbitrary[models.Name]
    booleanField  <- arbitrary[Boolean]
  } yield (addressLookup, address, postcode, name, booleanField)

  private val individualWithIdTestParamGenerator = for {
    name             <- arbitrary[models.Name]
    nino             <- arbitrary[Nino]
    registrationInfo <- arbitrary[RegistrationInfo]
  } yield (name, nino, registrationInfo)

  "IndDoYouHaveNINumberPage" - {

    beRetrievable[Boolean](IndDoYouHaveNINumberPage)

    beSettable[Boolean](IndDoYouHaveNINumberPage)

    beRemovable[Boolean](IndDoYouHaveNINumberPage)

    "cleanup" - {
      "must remove individual without Id answers when true" in {
        forAll(individualWithoutIdTestParamGenerator) {
          case (addressLookup, address, postcode, name, booleanField) =>
            val userAnswers = emptyUserAnswers
              .withPage(IndWhatIsYourNamePage, name)
              .withPage(DateOfBirthWithoutIdPage, LocalDate.now())
              .withPage(IndWhereDoYouLivePage, booleanField)
              .withPage(IndWhatIsYourPostcodePage, postcode)
              .withPage(AddressLookupPage, Seq(addressLookup))
              .withPage(IndSelectAddressPage, addressLookup.format)
              .withPage(IndSelectedAddressLookupPage, addressLookup)
              .withPage(IsThisYourAddressPage, booleanField)
              .withPage(IndUKAddressWithoutIdPage, address)
              .withPage(IndNonUKAddressWithoutIdPage, address)

            val result = IndDoYouHaveNINumberPage.cleanup(Some(true), userAnswers).success.value

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
        }
      }

      "must remove individual with Id answers when false" in {
        forAll(individualWithIdTestParamGenerator) {
          case (name, nino, registrationInfo) =>
            val userAnswers = emptyUserAnswers
              .withPage(IndWhatIsYourNINumberPage, nino)
              .withPage(IndContactNamePage, name)
              .withPage(IndDateOfBirthPage, LocalDate.now())
              .withPage(RegistrationInfoPage, registrationInfo)

            val result = IndDoYouHaveNINumberPage.cleanup(Some(false), userAnswers).success.value

            result.get(IndWhatIsYourNINumberPage) mustBe empty
            result.get(IndContactNamePage) mustBe empty
            result.get(IndDateOfBirthPage) mustBe empty
            result.get(RegistrationInfoPage) mustBe empty
        }
      }
    }
  }

}
