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
import play.api.libs.json.JsPath
import utils.UserAnswersHelper

import scala.util.Try

case object DoYouHaveUniqueTaxPayerReferencePage extends QuestionPage[Boolean] with UserAnswersHelper {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "doYouHaveUniqueTaxPayerReference"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(true)  => trueCleanupPages.foldLeft(Try(userAnswers))(removePage)
      case Some(false) => falseCleanupPages.foldLeft(Try(userAnswers))(removePage)
      case _           => super.cleanup(value, userAnswers)
    }

  private val falseCleanupPages: Seq[QuestionPage[_]] = List(
    WhatIsYourUTRPage,
    WhatIsYourNamePage,
    BusinessNamePage,
    IsThisYourBusinessPage,
    RegistrationInfoPage
  )

  private val trueCleanupPages: Seq[QuestionPage[_]] = List(
    IndWhatIsYourNamePage,
    DateOfBirthWithoutIdPage,
    IndWhereDoYouLivePage,
    IndWhatIsYourPostcodePage,
    AddressLookupPage,
    IndSelectAddressPage,
    IndSelectedAddressLookupPage,
    IsThisYourAddressPage,
    IndUKAddressWithoutIdPage,
    IndNonUKAddressWithoutIdPage,
    BusinessNameWithoutIDPage,
    HaveTradingNamePage,
    BusinessTradingNameWithoutIDPage,
    NonUKBusinessAddressWithoutIDPage,
    IndWhatIsYourNINumberPage,
    IndContactNamePage,
    IndDateOfBirthPage,
    RegistrationInfoPage,
    IndDoYouHaveNINumberPage
  )

}
