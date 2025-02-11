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

import models.Address
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Value}
import viewmodels.govuk.summarylist._

object Util {

  def changeAction(messageKey: String, href: String)(implicit messages: Messages): ActionItem =
    ActionItemViewModel(
      content = HtmlContent(
        s"""
           |<span aria-hidden="true">${messages("site.change")}</span>
           |<span class="govuk-visually-hidden"> ${messages(messageKey + ".change.hidden")}</span>
           |""".stripMargin
      ),
      href = href
    )

  def changeDetailsAction(messageKey: String, href: String)(implicit messages: Messages): ActionItem =
    ActionItemViewModel(
      content = HtmlContent(
        s"""
           |<span aria-hidden="true">${messages(messageKey + ".changeDetails")}</span>
           |<span class="govuk-visually-hidden"> ${messages(messageKey + ".changeDetails.hidden")}</span>
           |""".stripMargin
      ),
      href = href
    )

  def yesOrNo(answer: Boolean): String = if (answer) "site.yes" else "site.no"

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
