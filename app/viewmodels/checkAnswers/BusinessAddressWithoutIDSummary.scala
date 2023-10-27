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

import controllers.organisation.routes
import models.{Address, CheckMode, UserAnswers}
import pages.BusinessAddressWithoutIDPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessAddressWithoutIDSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BusinessAddressWithoutIDPage).map {
      answer =>
        SummaryListRowViewModel(
          key = "businessWithoutIDAddress.checkYourAnswersLabel",
          value = formatAddress(answer),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BusinessAddressWithoutIDController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("businessWithoutIDAddress.change.hidden"))
          )
        )
    }

  def formatAddress(answer: Address): Value =
    Value(HtmlContent(s"""
          ${answer.addressLine1}<br>
          ${answer.addressLine2.fold("")(
        address => s"$address<br>"
      )}
          ${answer.addressLine3}<br>
          ${answer.addressLine4.fold("")(
        address => s"$address<br>"
      )}
          ${answer.postCode.fold("")(
        postcode => s"$postcode<br>"
      )}
          ${answer.country.description}
       """))

}
