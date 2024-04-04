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

import models.ReporterType
import models.ReporterType._
import models.matching.RegistrationInfo
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ReporterTypePageSpec extends PageBehaviours {

  private val testParamGenerator = for {
    addressLookup    <- arbitrary[models.AddressLookup]
    address          <- arbitrary[models.Address]
    postcode         <- arbitrary[String]
    name             <- arbitrary[models.Name]
    booleanField     <- arbitrary[Boolean]
    nino             <- arbitrary[Nino]
    registrationInfo <- arbitrary[RegistrationInfo]
    dob              <- arbitrary[LocalDate]
    stringField      <- arbitrary[String]
  } yield (addressLookup, address, postcode, name, booleanField, nino, registrationInfo, dob, stringField)

  "ReporterTypePage" - {

    beRetrievable[ReporterType](ReporterTypePage)

    beSettable[ReporterType](ReporterTypePage)

    beRemovable[ReporterType](ReporterTypePage)

    "cleanUp" - {
      "must clear answers" - {
        "when answer changes to anything other than 'An individual not connected to a business' or 'Sole Trader'" in {
          forAll(testParamGenerator) {
            case (addressLookup, address, postcode, name, booleanField, nino, registrationInfo, dob, stringField) =>
              val ua = emptyUserAnswers
                .withPage(IndWhatIsYourNINumberPage, nino)
                .withPage(IndContactNamePage, name)
                .withPage(IndDateOfBirthPage, dob)
                .withPage(RegistrationInfoPage, registrationInfo)
                .withPage(IndWhatIsYourNamePage, name)
                .withPage(DateOfBirthWithoutIdPage, dob)
                .withPage(IndWhereDoYouLivePage, booleanField)
                .withPage(IndWhatIsYourPostcodePage, postcode)
                .withPage(AddressLookupPage, Seq(addressLookup))
                .withPage(IndSelectAddressPage, stringField)
                .withPage(IndSelectedAddressLookupPage, addressLookup)
                .withPage(IsThisYourAddressPage, booleanField)
                .withPage(IndUKAddressWithoutIdPage, address)
                .withPage(IndNonUKAddressWithoutIdPage, address)
                .withPage(IndContactEmailPage, stringField)
                .withPage(IndContactHavePhonePage, booleanField)
                .withPage(IndContactPhonePage, stringField)
                .withPage(IndDoYouHaveNINumberPage, booleanField)

              val result = ReporterTypePage.cleanup(Some(LimitedCompany), ua).success.value

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
              result.get(IndContactEmailPage) mustBe empty
              result.get(IndContactHavePhonePage) mustBe empty
              result.get(IndContactPhonePage) mustBe empty
              result.get(IndDoYouHaveNINumberPage) mustBe empty
          }
        }
      }
    }

  }

}
