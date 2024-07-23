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

import org.scalacheck.Arbitrary

trait PageGenerators {

  implicit lazy val arbitraryIndividualContactPhonePage: Arbitrary[pages.IndContactPhonePage.type] =
    Arbitrary(pages.IndContactPhonePage)

  implicit lazy val arbitraryIndividualHaveContactTelephonePage: Arbitrary[pages.IndContactHavePhonePage.type] =
    Arbitrary(pages.IndContactHavePhonePage)

  implicit lazy val arbitraryIndividualContactEmailPage: Arbitrary[pages.IndContactEmailPage.type] =
    Arbitrary(pages.IndContactEmailPage)

  implicit lazy val arbitraryBusinessWithoutIDNamePage: Arbitrary[pages.BusinessNameWithoutIDPage.type] =
    Arbitrary(pages.BusinessNameWithoutIDPage)

  implicit lazy val arbitraryWhatIsYourPostcodePage: Arbitrary[pages.IndWhatIsYourPostcodePage.type] =
    Arbitrary(pages.IndWhatIsYourPostcodePage)

  implicit lazy val arbitrarySoleNamePage: Arbitrary[pages.IndContactNamePage.type] =
    Arbitrary(pages.IndContactNamePage)

  implicit lazy val arbitraryAddressWithoutIdPage: Arbitrary[pages.NonUKBusinessAddressWithoutIDPage.type] =
    Arbitrary(pages.NonUKBusinessAddressWithoutIDPage)

  implicit lazy val arbitraryBusinessTradingNameWithoutIdPage: Arbitrary[pages.BusinessTradingNameWithoutIDPage.type] =
    Arbitrary(pages.BusinessTradingNameWithoutIDPage)

  implicit lazy val arbitraryWhatIsYourDateOfBirthPage: Arbitrary[pages.IndDateOfBirthPage.type] =
    Arbitrary(pages.IndDateOfBirthPage)

  implicit lazy val arbitraryWhatIsYourNamePage: Arbitrary[pages.WhatIsYourNamePage.type] =
    Arbitrary(pages.WhatIsYourNamePage)

  implicit lazy val arbitraryIndWhatIsYourNamePage: Arbitrary[pages.IndWhatIsYourNamePage.type] =
    Arbitrary(pages.IndWhatIsYourNamePage)

  implicit lazy val arbitraryIndWhereDoYouLivePage: Arbitrary[pages.IndWhereDoYouLivePage.type] =
    Arbitrary(pages.IndWhereDoYouLivePage)

  implicit lazy val arbitraryIndNonUKAddressWithoutIdPage: Arbitrary[pages.IndNonUKAddressWithoutIdPage.type] =
    Arbitrary(pages.IndNonUKAddressWithoutIdPage)

  implicit lazy val arbitraryIndUKAddressWithoutIdPage: Arbitrary[pages.IndUKAddressWithoutIdPage.type] =
    Arbitrary(pages.IndUKAddressWithoutIdPage)

  implicit lazy val arbitraryWhatIsYourNationalInsuranceNumberPage: Arbitrary[pages.IndWhatIsYourNINumberPage.type] =
    Arbitrary(pages.IndWhatIsYourNINumberPage)

  implicit lazy val arbitraryIsThisYourBusinessPage: Arbitrary[pages.IsThisYourBusinessPage.type] =
    Arbitrary(pages.IsThisYourBusinessPage)

  implicit lazy val arbitraryBusinessNamePage: Arbitrary[pages.BusinessNamePage.type] =
    Arbitrary(pages.BusinessNamePage)

  implicit lazy val arbitraryUTRPage: Arbitrary[pages.WhatIsYourUTRPage.type] =
    Arbitrary(pages.WhatIsYourUTRPage)

  implicit lazy val arbitraryBussinessTypePage: Arbitrary[pages.ReporterTypePage.type] =
    Arbitrary(pages.ReporterTypePage)

  implicit lazy val arbitraryDoYouHaveUniqueTaxPayerReferencePage: Arbitrary[pages.DoYouHaveUniqueTaxPayerReferencePage.type] =
    Arbitrary(pages.DoYouHaveUniqueTaxPayerReferencePage)

  implicit lazy val arbitraryDoYouHaveNINPage: Arbitrary[pages.IndDoYouHaveNINumberPage.type] =
    Arbitrary(pages.IndDoYouHaveNINumberPage)

  implicit lazy val arbitrarySndConHavePhonePage: Arbitrary[pages.SecondContactHavePhonePage.type] =
    Arbitrary(pages.SecondContactHavePhonePage)

  implicit lazy val arbitrarySndContactPhonePage: Arbitrary[pages.SecondContactPhonePage.type] =
    Arbitrary(pages.SecondContactPhonePage)

  implicit lazy val arbitrarySndContactEmailPage: Arbitrary[pages.SecondContactEmailPage.type] =
    Arbitrary(pages.SecondContactEmailPage)

  implicit lazy val arbitrarySndContactNamePage: Arbitrary[pages.SecondContactNamePage.type] =
    Arbitrary(pages.SecondContactNamePage)

  implicit lazy val arbitrarySecondContactPage: Arbitrary[pages.HaveSecondContactPage.type] =
    Arbitrary(pages.HaveSecondContactPage)

  implicit lazy val arbitraryContactHavePhonePage: Arbitrary[pages.ContactHavePhonePage.type] =
    Arbitrary(pages.ContactHavePhonePage)

  implicit lazy val arbitraryContactPhonePage: Arbitrary[pages.ContactPhonePage.type] =
    Arbitrary(pages.ContactPhonePage)

  implicit lazy val arbitraryContactNamePage: Arbitrary[pages.ContactNamePage.type] =
    Arbitrary(pages.ContactNamePage)

  implicit lazy val arbitraryContactEmailPage: Arbitrary[pages.ContactEmailPage.type] =
    Arbitrary(pages.ContactEmailPage)

  implicit lazy val arbitraryDateOfBirthWithoutIdPage: Arbitrary[pages.DateOfBirthWithoutIdPage.type] =
    Arbitrary(pages.DateOfBirthWithoutIdPage)

}
