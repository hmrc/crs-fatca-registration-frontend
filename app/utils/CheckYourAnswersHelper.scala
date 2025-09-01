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

package utils

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers._

class CheckYourAnswersHelper(
  val userAnswers: UserAnswers,
  countryListFactory: CountryListFactory
)(implicit val messages: Messages) {

  def businessConfirmation: Option[SummaryListRow] = ConfirmedBusinessSummary.row(userAnswers, countryListFactory)

  def whatIsTradingName: Option[SummaryListRow] = BusinessTradingNameWithoutIDSummary.row(userAnswers)

  def haveTradingName: Option[SummaryListRow] = HaveTradingNameSummary.row(userAnswers)

  def selectAddress: Option[SummaryListRow] = IndSelectAddressSummary.row(userAnswers)

  def businessWithoutIDName: Option[SummaryListRow] = BusinessNameWithoutIDSummary.row(userAnswers)

  def nonUkName: Option[SummaryListRow] = IndWhatIsYourNameSummary.row(userAnswers)

  def individualAddressWithoutID: Option[SummaryListRow] = IndividualAddressWithoutIdSummary.row(userAnswers)

  def businessAddressWithoutID: Option[SummaryListRow] = BusinessAddressWithoutIDSummary.row(userAnswers)

  def whatIsYourDateOfBirth: Option[SummaryListRow] = IndDateOfBirthSummary.row(userAnswers)

  def dateOfBirthWithoutId: Option[SummaryListRow] = IndDateOfBirthWithoutIdSummary.row(userAnswers)

  def whatIsYourName: Option[SummaryListRow] = WhatIsYourNameSummary.row(userAnswers)

  def indContactName: Option[SummaryListRow] = IndContactNameSummary.row(userAnswers)

  def nino: Option[SummaryListRow] = IndWhatIsYourNINumberSummary.row(userAnswers)

  def reporterType: Option[SummaryListRow] = ReporterTypeSummary.row(userAnswers)

  def registeredAddressInUk: Option[SummaryListRow] = RegisteredAddressInUKSummary.row(userAnswers)

  def doYouHaveUniqueTaxPayerReference: Option[SummaryListRow] = DoYouHaveUniqueTaxPayerReferenceSummary.row(userAnswers)

  def doYouHaveNINumber: Option[SummaryListRow] = IndDoYouHaveNINumberSummary.row(userAnswers)

  def secondContactPhone: Option[SummaryListRow] = SecondContactPhoneSummary.row(userAnswers)

  def secondContactHavePhone: Option[SummaryListRow] = SecondContactHavePhoneSummary.row(userAnswers)

  def secondContactEmail: Option[SummaryListRow] = SecondContactEmailSummary.row(userAnswers)

  def sndContactName: Option[SummaryListRow] = SecondContactNameSummary.row(userAnswers)

  def secondContact: Option[SummaryListRow] = HaveSecondContactSummary.row(userAnswers)

  def contactPhone: Option[SummaryListRow] = ContactPhoneSummary.row(userAnswers)

  def contactHavePhone: Option[SummaryListRow] = ContactHavePhoneSummary.row(userAnswers)

  def contactName: Option[SummaryListRow] = ContactNameSummary.row(userAnswers)

  def contactEmail: Option[SummaryListRow] = ContactEmailSummary.row(userAnswers)

  def individualContactEmail: Option[SummaryListRow] = IndContactEmailSummary.row(userAnswers)

  def individualContactHavePhone: Option[SummaryListRow] = IndContactHavePhoneSummary.row(userAnswers)

  def individualContactPhone: Option[SummaryListRow] = IndContactPhoneSummary.row(userAnswers)

}
