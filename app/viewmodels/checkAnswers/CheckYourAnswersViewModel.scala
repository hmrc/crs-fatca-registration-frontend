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

package viewmodels.checkAnswers

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.{CheckYourAnswersHelper, CountryListFactory}
import viewmodels.Section

object CheckYourAnswersViewModel {

  def buildPages(userAnswers: UserAnswers, countryFactory: CountryListFactory, isBusiness: Boolean)(implicit
    messages: Messages
  ): Seq[Section] = {

    val helper            = new CheckYourAnswersHelper(userAnswers, countryListFactory = countryFactory)
    val (contact, header) = if (isBusiness) ("firstContact", "businessDetails") else ("contactDetails", "individualDetails")

    val regDetails     = messages(s"checkYourAnswers.$header.h2")
    val contactHeading = messages(s"checkYourAnswers.$contact.h2")
    val secContact     = if (isBusiness) Seq(Section(messages("checkYourAnswers.secondContact.h2"), buildSecondContact(helper))) else Nil

    Seq(
      Section(regDetails, buildDetails(userAnswers, helper, isBusiness)),
      Section(contactHeading, buildFirstContact(helper, isBusiness))
    ) ++: secContact
  }

  private def buildDetails(userAnswers: UserAnswers, helper: CheckYourAnswersHelper, isBusiness: Boolean): Seq[SummaryListRow] =
    if (userAnswers.get(pages.WhatIsYourUTRPage).isDefined) {
      Seq(
        helper.businessConfirmation
      ).flatten
    } else {
      Seq(
        helper.reporterType,
        helper.registeredAddressInUk,
        helper.doYouHaveUniqueTaxPayerReference,
        helper.doYouHaveNINumber,
        helper.nino,
        helper.whatIsYourName,
        helper.indContactName,
        helper.nonUkName,
        helper.whatIsYourDateOfBirth,
        helper.dateOfBirthWithoutId,
        helper.businessWithoutIDName,
        helper.whatIsTradingName,
        if (isBusiness) helper.businessAddressWithoutID else helper.individualAddressWithoutID,
        helper.selectAddress
      ).flatten
    }

  private def buildSecondContact(helper: CheckYourAnswersHelper): Seq[SummaryListRow] =
    Seq(
      helper.secondContact,
      helper.sndContactName,
      helper.secondContactEmail,
      helper.secondContactPhone
    ).flatten

  private def buildFirstContact(helper: CheckYourAnswersHelper, isBusiness: Boolean): Seq[SummaryListRow] =
    if (isBusiness) {
      Seq(helper.contactName, helper.contactEmail, helper.contactPhone).flatten
    } else {
      Seq(helper.contactName, helper.individualContactEmail, helper.individualContactPhone).flatten
    }

}
