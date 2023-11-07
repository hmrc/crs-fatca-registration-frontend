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

import models.UserAnswers

import scala.util.Try

object PageLists {

  val removePage: (Try[UserAnswers], QuestionPage[_]) => Try[UserAnswers] =
    (ua: Try[UserAnswers], page: QuestionPage[_]) => ua.flatMap(_.remove(page))

  val businessWithIdPages = List(
    BusinessNamePage,
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

  val individualAndWithoutIdPages = List(
    WhatIsYourNamePage,
    IndAddressWithoutIdPage,
    IndContactEmailPage,
    IndContactHavePhonePage,
    IndContactNamePage,
    IndContactPhonePage,
    IndDateOfBirthPage,
    IndDoYouHaveNINumberPage,
    IndWhatIsYourNamePage,
    IndWhatIsYourNINumberPage,
    BusinessAddressWithoutIDPage,
    BusinessNameWithoutIDPage,
    BusinessTradingNameWithoutIDPage
  )

}