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

import models.{ReporterType, UserAnswers}
import models.subscription.request.ContactInformation.isRegisteringAsBusiness
import pages.{AutoMatchedUTRPage, IsThisYourBusinessPage, ReporterTypePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.{CheckYourAnswersHelper, CountryListFactory}
import viewmodels.Section

object CheckYourAnswersViewModel {

  def buildPages(userAnswers: UserAnswers, countryFactory: CountryListFactory)(implicit
    messages: Messages
  ): Seq[Section] = {

    val isBusiness = isRegisteringAsBusiness(userAnswers)
    val helper     = new CheckYourAnswersHelper(userAnswers, countryListFactory = countryFactory)
    val (contact, header) = (userAnswers.get(ReporterTypePage), userAnswers.get(AutoMatchedUTRPage)) match {
      case (Some(ReporterType.Individual), _) => ("contactDetails", "individualDetails")
      case (Some(ReporterType.Sole), _) =>
        val heading = if (userAnswers.get(IsThisYourBusinessPage).getOrElse(false)) "businessDetails" else "individualDetails"
        ("contactDetails", heading)
      case (Some(_), _)    => ("firstContact", "businessDetails")
      case (None, Some(_)) => ("firstContact", "businessDetails")
      case (None, None) =>
        throw new Exception("ReporterType and AutoMatchedUTR must not both be empty at check your answers page")
    }

    val regDetails     = messages(s"checkYourAnswers.$header.h2")
    val contactHeading = messages(s"checkYourAnswers.$contact.h2")
    val secondContact = if (isBusiness) {
      Seq(Section(messages("checkYourAnswers.secondContact.h2"), buildSecondContact(helper)))
    } else {
      Nil
    }

    Seq(
      Section(regDetails, buildDetails(userAnswers, helper, isBusiness)),
      Section(contactHeading, buildFirstContact(helper, isBusiness))
    ) ++: secondContact
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
        if (isBusiness) helper.whatIsTradingName else None,
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
