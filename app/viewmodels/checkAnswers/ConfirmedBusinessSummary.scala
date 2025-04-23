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

import controllers.routes
import models.Address.GBCountryCode
import models.matching.OrgRegistrationInfo
import models.register.response.details.AddressResponse
import models.{CheckMode, UserAnswers}
import pages.{AutoMatchedUTRPage, RegistrationInfoPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.CountryListFactory
import viewmodels.checkAnswers.Util.changeAction
import viewmodels.govuk.summarylist._

object ConfirmedBusinessSummary {

  private val ParagraphClass = "govuk-!-margin-0"

  def row(answers: UserAnswers, countryListFactory: CountryListFactory)(implicit messages: Messages): Option[SummaryListRow] = {

    val href = if (answers.get(AutoMatchedUTRPage).isEmpty) {
      routes.ReporterTypeController.onPageLoad(CheckMode).url
    } else {
      controllers.organisation.routes.UnableToChangeBusinessController.onPageLoad().url
    }

    (answers.get(pages.IsThisYourBusinessPage), answers.get(RegistrationInfoPage)) match {
      case (Some(true), Some(registrationInfo: OrgRegistrationInfo)) =>
        val businessName = registrationInfo.name
        val address      = registrationInfo.address
        for {
          countryName <- countryListFactory.getDescriptionFromCode(address.countryCode)
        } yield SummaryListRowViewModel(
          key = Key(Text(messages("businessWithIDName.checkYourAnswersLabel")), "govuk-!-width-one"),
          value = Value(confirmedBusinessDetails(businessName, address, countryName)),
          actions = Seq(
            changeAction("businessWithIDName", href)
          )
        )
      case _ => None
    }
  }

  private def confirmedBusinessDetails(businessName: String, address: AddressResponse, countryName: String): HtmlContent =
    HtmlContent(
      s"""
      |<p>$businessName</p>
      |<p class=$ParagraphClass>${address.addressLine1}</p>
      |${addressLine(address.addressLine2)}
      |${addressLine(address.addressLine3)}
      |${addressLine(address.addressLine4)}
      |<p class=$ParagraphClass>${address.postCodeFormatter(address.postalCode).getOrElse("")}</p>
      |${if (address.countryCode.toUpperCase != GBCountryCode) s"<p class=$ParagraphClass>$countryName</p>" else ""}
      |""".stripMargin
    )

  private def addressLine(line: Option[String]) =
    line.fold("")(
      address => s"<p class=$ParagraphClass>$address</p>"
    )

}
