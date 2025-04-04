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

import models.ReporterType.{orgReporterTypes, Individual, Sole}
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo}
import models.{ReporterType, UserAnswers}
import play.api.libs.json.JsPath

import scala.util.Try

case object ReporterTypePage extends QuestionPage[ReporterType] {

  private val individualCleanup = List(
    WhatIsYourUTRPage,
    WhatIsYourNamePage,
    BusinessNamePage,
    IsThisYourBusinessPage,
    BusinessNameWithoutIDPage,
    HaveTradingNamePage,
    BusinessTradingNameWithoutIDPage,
    NonUKBusinessAddressWithoutIDPage,
    ContactNamePage,
    ContactEmailPage,
    ContactHavePhonePage,
    ContactPhonePage,
    HaveSecondContactPage,
    SecondContactNamePage,
    SecondContactEmailPage,
    SecondContactHavePhonePage,
    SecondContactPhonePage,
    RegisteredAddressInUKPage,
    DoYouHaveUniqueTaxPayerReferencePage
  )

  private val otherBusinessTypeCleanup = List(
    IndWhatIsYourNINumberPage,
    IndContactNamePage,
    IndDateOfBirthPage,
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
    IndContactEmailPage,
    IndContactHavePhonePage,
    IndContactPhonePage,
    IndDoYouHaveNINumberPage,
    WhatIsYourNamePage
  )

  private val soleTraderTypeCleanup = List(
    BusinessNamePage,
    IsThisYourBusinessPage,
    BusinessNameWithoutIDPage,
    HaveTradingNamePage,
    BusinessTradingNameWithoutIDPage,
    NonUKBusinessAddressWithoutIDPage,
    ContactNamePage,
    ContactEmailPage,
    ContactHavePhonePage,
    ContactPhonePage,
    HaveSecondContactPage,
    SecondContactNamePage,
    SecondContactEmailPage,
    SecondContactHavePhonePage,
    SecondContactPhonePage,
    WhatIsYourNamePage,
    IndDoYouHaveNINumberPage
  )

  override def path: JsPath = JsPath \ toString

  override def toString: String = "reporterType"

  override def cleanup(value: Option[ReporterType], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(Individual) =>
        cleanupPages(userAnswers, individualCleanup)
          .flatMap(registrationInfoCleanup(_, Individual))

      case Some(Sole) =>
        cleanupPages(userAnswers, soleTraderTypeCleanup)
          .flatMap(registrationInfoCleanup(_, Sole))

      case Some(reporterType) if orgReporterTypes.contains(reporterType) =>
        cleanupPages(userAnswers, otherBusinessTypeCleanup)
          .flatMap(registrationInfoCleanup(_, reporterType))

      case _ => super.cleanup(value, userAnswers)
    }

  private def cleanupPages(userAnswers: UserAnswers, pages: Seq[QuestionPage[_]]): Try[UserAnswers] =
    pages.foldLeft(Try(userAnswers))(PageLists.removePage)

  private def registrationInfoCleanup(userAnswers: UserAnswers, reporterType: ReporterType): Try[UserAnswers] =
    userAnswers.get(RegistrationInfoPage) match {
      case Some(_: OrgRegistrationInfo) if reporterType == Individual =>
        userAnswers.remove(RegistrationInfoPage)

      case Some(_: IndRegistrationInfo) if orgReporterTypes.contains(reporterType) =>
        userAnswers.remove(RegistrationInfoPage)

      case _ => Try(userAnswers)
    }

}
