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

package generators

import models.matching.RegistrationInfo
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.domain.Nino

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryIndividualContactPhoneUserAnswersEntry: Arbitrary[(pages.IndContactPhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.IndContactPhonePage)

  implicit lazy val arbitraryIndividualHaveContactTelephoneUserAnswersEntry: Arbitrary[(pages.IndContactHavePhonePage.type, JsValue)] =
    modelArbitrary[pages.IndContactHavePhonePage.type, Boolean](pages.IndContactHavePhonePage)

  implicit lazy val arbitraryIndividualContactEmailUserAnswersEntry: Arbitrary[(pages.IndContactEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.IndContactEmailPage)

  implicit lazy val arbitraryWhatIsTradingNameUserAnswersEntry: Arbitrary[(pages.BusinessTradingNameWithoutIDPage.type, JsValue)] =
    alphaStrPageArbitrary(pages.BusinessTradingNameWithoutIDPage)

  implicit lazy val arbitraryBusinessWithoutIDNameUserAnswersEntry: Arbitrary[(pages.BusinessNameWithoutIDPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.BusinessNameWithoutIDPage)

  implicit lazy val arbitraryWhatIsYourPostcodeUserAnswersEntry: Arbitrary[(pages.IndWhatIsYourPostcodePage.type, JsValue)] =
    alphaStrPageArbitrary(pages.IndWhatIsYourPostcodePage)

  implicit lazy val arbitraryNameUserAnswersEntry: Arbitrary[(pages.WhatIsYourNamePage.type, JsValue)] =
    modelArbitrary[pages.WhatIsYourNamePage.type, models.Name](pages.WhatIsYourNamePage)

  implicit lazy val arbitraryIndNameUserAnswersEntry: Arbitrary[(pages.IndWhatIsYourNamePage.type, JsValue)] =
    modelArbitrary[pages.IndWhatIsYourNamePage.type, models.Name](pages.IndWhatIsYourNamePage)

  implicit lazy val arbitraryIndWhereDoYouLivePageEntry: Arbitrary[(pages.IndWhereDoYouLivePage.type, JsValue)] =
    modelArbitrary[pages.IndWhereDoYouLivePage.type, Boolean](pages.IndWhereDoYouLivePage)

  implicit lazy val arbitraryIndNonUKAddressWithoutIdPageEntry: Arbitrary[(pages.IndNonUKAddressWithoutIdPage.type, JsValue)] =
    modelArbitrary[pages.IndNonUKAddressWithoutIdPage.type, models.Address](pages.IndNonUKAddressWithoutIdPage)

  implicit lazy val arbitraryIndUKAddressWithoutIdPageEntry: Arbitrary[(pages.IndUKAddressWithoutIdPage.type, JsValue)] =
    modelArbitrary[pages.IndUKAddressWithoutIdPage.type, models.Address](pages.IndUKAddressWithoutIdPage)

  implicit lazy val arbitrarySoleNameUserAnswersEntry: Arbitrary[(pages.IndContactNamePage.type, JsValue)] =
    modelArbitrary[pages.IndContactNamePage.type, models.Name](pages.IndContactNamePage)

  implicit lazy val arbitraryAddressWithoutIdUserAnswersEntry: Arbitrary[(pages.NonUKBusinessAddressWithoutIDPage.type, JsValue)] =
    modelArbitrary[pages.NonUKBusinessAddressWithoutIDPage.type, models.Address](pages.NonUKBusinessAddressWithoutIDPage)

  implicit lazy val arbitraryWhatIsYourDateOfBirthUserAnswersEntry: Arbitrary[(pages.IndDateOfBirthPage.type, JsValue)] =
    modelArbitrary[pages.IndDateOfBirthPage.type, Int](pages.IndDateOfBirthPage)

  implicit lazy val arbitraryIsThisYourBusinessUserAnswersEntry: Arbitrary[(pages.IsThisYourBusinessPage.type, JsValue)] =
    modelArbitrary[pages.IsThisYourBusinessPage.type, Boolean](pages.IsThisYourBusinessPage)

  implicit lazy val arbitraryBusinessNameUserAnswersEntry: Arbitrary[(pages.BusinessNamePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.BusinessNamePage)

  implicit lazy val arbitraryUTRUserAnswersEntry: Arbitrary[(pages.WhatIsYourUTRPage.type, JsValue)] =
    modelArbitrary[pages.WhatIsYourUTRPage.type, models.UniqueTaxpayerReference](pages.WhatIsYourUTRPage)

  implicit lazy val arbitraryBusinessTypeUserAnswersEntry: Arbitrary[(pages.ReporterTypePage.type, JsValue)] =
    modelArbitrary[pages.ReporterTypePage.type, models.ReporterType](pages.ReporterTypePage)

  implicit lazy val arbitraryDoYouHaveUniqueTaxPayerReferenceUserAnswersEntry: Arbitrary[(pages.DoYouHaveUniqueTaxPayerReferencePage.type, JsValue)] =
    modelArbitrary[pages.DoYouHaveUniqueTaxPayerReferencePage.type, Boolean](pages.DoYouHaveUniqueTaxPayerReferencePage)

  implicit lazy val arbitraryDoYouHaveNINUserAnswersEntry: Arbitrary[(pages.IndDoYouHaveNINumberPage.type, JsValue)] =
    modelArbitrary[pages.IndDoYouHaveNINumberPage.type, Boolean](pages.IndDoYouHaveNINumberPage)

  implicit lazy val arbitrarySndConHavePhoneUserAnswersEntry: Arbitrary[(pages.SecondContactHavePhonePage.type, JsValue)] =
    modelArbitrary[pages.SecondContactHavePhonePage.type, Boolean](pages.SecondContactHavePhonePage)

  implicit lazy val arbitrarySndContactPhoneUserAnswersEntry: Arbitrary[(pages.SecondContactPhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.SecondContactPhonePage)

  implicit lazy val arbitrarySndContactEmailUserAnswersEntry: Arbitrary[(pages.SecondContactEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.SecondContactEmailPage)

  implicit lazy val arbitrarySndContactNameUserAnswersEntry: Arbitrary[(pages.SecondContactNamePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.SecondContactNamePage)

  implicit lazy val arbitrarySecondContactUserAnswersEntry: Arbitrary[(pages.HaveSecondContactPage.type, JsValue)] =
    modelArbitrary[pages.HaveSecondContactPage.type, Boolean](pages.HaveSecondContactPage)

  implicit lazy val arbitraryContactHavePhoneUserAnswersEntry: Arbitrary[(pages.ContactHavePhonePage.type, JsValue)] =
    modelArbitrary[pages.ContactHavePhonePage.type, Boolean](pages.ContactHavePhonePage)

  implicit lazy val arbitraryContactPhoneUserAnswersEntry: Arbitrary[(pages.ContactPhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.ContactPhonePage)

  implicit lazy val arbitraryContactNameUserAnswersEntry: Arbitrary[(pages.ContactNamePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.ContactNamePage)

  implicit lazy val arbitraryContactEmailUserAnswersEntry: Arbitrary[(pages.ContactEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.ContactEmailPage)

  implicit lazy val arbitraryNINumberUserAnswersEntry: Arbitrary[(pages.IndWhatIsYourNINumberPage.type, JsValue)] =
    modelArbitrary[pages.IndWhatIsYourNINumberPage.type, Nino](pages.IndWhatIsYourNINumberPage)

  implicit lazy val arbitraryDateOfBirthUserAnswersEntry: Arbitrary[(pages.DateOfBirthWithoutIdPage.type, JsValue)] =
    modelArbitrary[pages.DateOfBirthWithoutIdPage.type, Int](pages.DateOfBirthWithoutIdPage)

  implicit lazy val arbitraryRegistrationInfoEntry: Arbitrary[(pages.RegistrationInfoPage.type, JsValue)] =
    modelArbitrary[pages.RegistrationInfoPage.type, RegistrationInfo](pages.RegistrationInfoPage)

  implicit lazy val arbitraryIsThisYourAddressEntry: Arbitrary[(pages.IsThisYourAddressPage.type, JsValue)] =
    modelArbitrary[pages.IsThisYourAddressPage.type, Boolean](pages.IsThisYourAddressPage)

  implicit lazy val arbitraryIndSelectAddressEntry: Arbitrary[(pages.IndSelectAddressPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.IndSelectAddressPage)

  implicit lazy val arbitraryIndividualPhonePageEntry: Arbitrary[(pages.changeContactDetails.IndividualPhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.IndividualPhonePage)

  implicit lazy val arbitraryIndividualEmailPageEntry: Arbitrary[(pages.changeContactDetails.IndividualEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.IndividualEmailPage)

  implicit lazy val arbitraryOrganisationContactNamePageEntry: Arbitrary[(pages.changeContactDetails.OrganisationContactNamePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationContactNamePage)

  implicit lazy val arbitraryOrganisationContactEmailPageEntry: Arbitrary[(pages.changeContactDetails.OrganisationContactEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationContactEmailPage)

  implicit lazy val arbitraryOrganisationContactHavePhonePageEntry: Arbitrary[(pages.changeContactDetails.OrganisationContactHavePhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationContactHavePhonePage)

  implicit lazy val arbitraryOrganisationContactPhonePageEntry: Arbitrary[(pages.changeContactDetails.OrganisationContactPhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationContactPhonePage)

  implicit lazy val arbitraryOrganisationSecondContactNamePageEntry: Arbitrary[(pages.changeContactDetails.OrganisationSecondContactNamePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationSecondContactNamePage)

  implicit lazy val arbitraryOrganisationSecondContactEmailPageEntry: Arbitrary[(pages.changeContactDetails.OrganisationSecondContactEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationSecondContactEmailPage)

  implicit lazy val arbitraryOrganisationSecondContactHavePhonePageEntry
    : Arbitrary[(pages.changeContactDetails.OrganisationSecondContactHavePhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationSecondContactHavePhonePage)

  implicit lazy val arbitraryOrganisationSecondContactPhonePageEntry: Arbitrary[(pages.changeContactDetails.OrganisationSecondContactPhonePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.changeContactDetails.OrganisationSecondContactPhonePage)

  private def alphaStrNonEmptyPageArbitrary[T](page: T): Arbitrary[(T, JsValue)] = Arbitrary {
    for {
      value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
    } yield (page, value)
  }

  private def alphaStrPageArbitrary[T](page: T): Arbitrary[(T, JsValue)] = Arbitrary {
    for {
      value <- Gen.alphaStr.map(Json.toJson(_))
    } yield (page, value)
  }

  private def modelArbitrary[T, U](page: T)(implicit arb: Arbitrary[U], writes: Writes[U]): Arbitrary[(T, JsValue)] = Arbitrary {
    for {
      value <- arb.arbitrary.map(Json.toJson(_))
    } yield (page, value)
  }

}
