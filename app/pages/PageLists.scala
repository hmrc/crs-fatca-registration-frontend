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

import utils.UserAnswersHelper

object PageLists extends UserAnswersHelper {

  val businessWithIdPages: Seq[QuestionPage[_]] = List(
    WhatIsYourUTRPage,
    BusinessNamePage,
    WhatIsYourNamePage,
    IsThisYourBusinessPage,
    RegistrationInfoPage,
    ContactEmailPage,
    ContactHavePhonePage,
    ContactNamePage,
    ContactPhonePage,
    HaveSecondContactPage,
    HaveTradingNamePage,
    SecondContactEmailPage,
    SecondContactHavePhonePage,
    SecondContactNamePage,
    SecondContactPhonePage
  )

  val individualAndWithoutIdPages: Seq[QuestionPage[_]] = List(
    WhatIsYourNamePage,
    IndWhereDoYouLivePage,
    IndWhatIsYourPostcodePage,
    IndSelectAddressPage,
    IndSelectedAddressLookupPage,
    AddressLookupPage,
    IndUKAddressWithoutIdPage,
    IndNonUKAddressWithoutIdPage,
    IndContactEmailPage,
    IndContactHavePhonePage,
    IndContactNamePage,
    IndContactPhonePage,
    IndDateOfBirthPage,
    DateOfBirthWithoutIdPage,
    IndDoYouHaveNINumberPage,
    IndWhatIsYourNamePage,
    IndWhatIsYourNINumberPage,
    NonUKBusinessAddressWithoutIDPage,
    BusinessNameWithoutIDPage,
    HaveTradingNamePage,
    BusinessTradingNameWithoutIDPage,
    RegistrationInfoPage
  )

  val businessWithoutIDandIndPages: Seq[QuestionPage[_]] = List(
    WhatIsYourNamePage,
    IndWhereDoYouLivePage,
    IsThisYourAddressPage,
    IndWhatIsYourPostcodePage,
    IndSelectAddressPage,
    IndSelectedAddressLookupPage,
    AddressLookupPage,
    IndUKAddressWithoutIdPage,
    IndNonUKAddressWithoutIdPage,
    IndContactNamePage,
    IndDateOfBirthPage,
    DateOfBirthWithoutIdPage,
    IndDoYouHaveNINumberPage,
    IndWhatIsYourNamePage,
    IndWhatIsYourNINumberPage,
    NonUKBusinessAddressWithoutIDPage,
    BusinessNameWithoutIDPage,
    HaveTradingNamePage,
    BusinessTradingNameWithoutIDPage,
    DoYouHaveUniqueTaxPayerReferencePage,
    RegistrationInfoPage
  )

}
