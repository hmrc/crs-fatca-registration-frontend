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

import base.SpecBase
import helpers.JsonFixtures._
import models.matching.OrgRegistrationInfo
import models.register.response.details.AddressResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.{AutoMatchedUTRPage, IsThisYourBusinessPage, RegistrationInfoPage}
import play.api.test.Helpers
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Key, SummaryListRow, Value}

class CheckYourAnswersHelperSpec extends SpecBase with GuiceOneAppPerSuite {

  val addressResponse: AddressResponse = AddressResponse("line1", Some("line2"), None, None, Some("AB00 0CC"), "GB")

  val mockCountryListFactory: CountryListFactory = mock[CountryListFactory]

  "CheckYourAnswersHelper" - {

    val baseUrl = "/register-for-crs-and-fatca/register"

    "confirmBusiness must return a SummaryListRow with the business details with href to change-registration-type when AutoMatchedUTR is not set" in {
      val userAnswers = emptyUserAnswers
        .set(IsThisYourBusinessPage, true)
        .success
        .value
        .set(RegistrationInfoPage, OrgRegistrationInfo(safeId, OrgName, addressResponse))
        .success
        .value

      when(mockCountryListFactory.getDescriptionFromCode(any())).thenReturn(Some("United Kingdom"))

      val service = new CheckYourAnswersHelper(userAnswers, mockCountryListFactory)(Helpers.stubMessages())

      service.businessConfirmation mustBe Some(
        createBusinessConfirmationSummary(baseUrl + "/change-registration-type")
      )
    }

    "confirmBusiness must return a SummaryListRow with the business details with href to change-is-this-your-business when AutoMatchedUTR is set" in {
      val userAnswers = emptyUserAnswers
        .set(AutoMatchedUTRPage, utr)
        .success
        .value
        .set(IsThisYourBusinessPage, true)
        .success
        .value
        .set(RegistrationInfoPage, OrgRegistrationInfo(safeId, OrgName, addressResponse))
        .success
        .value

      when(mockCountryListFactory.getDescriptionFromCode(any())).thenReturn(Some("United Kingdom"))

      val service = new CheckYourAnswersHelper(userAnswers, mockCountryListFactory)(Helpers.stubMessages())

      service.businessConfirmation mustBe Some(createBusinessConfirmationSummary(baseUrl + "/problem/unable-to-change-business"))
    }

    "confirmBusiness must return None when the business details does not exist" in {
      val service = new CheckYourAnswersHelper(emptyUserAnswers, mockCountryListFactory)(Helpers.stubMessages())

      service.businessConfirmation mustBe None
    }
  }

  private def createBusinessConfirmationSummary(href: String) =
    SummaryListRow(
      Key(Text("businessWithIDName.checkYourAnswersLabel"), classes = "govuk-!-width-one"),
      value = Value(
        content = HtmlContent(
          s"""
            |<p>$OrgName</p>
            |<p class=govuk-!-margin-0>line1</p>
            |<p class=govuk-!-margin-0>line2</p>
            |
            |
            |<p class=govuk-!-margin-0>AB00  0CC</p>
            |
            |""".stripMargin
        )
      ),
      actions = Some(
        Actions(
          items = List(
            ActionItem(
              content = HtmlContent(
                s"""
                   |<span aria-hidden="true">site.change</span>
                   |<span class="govuk-visually-hidden"> businessWithIDName.change.hidden</span>
                   |""".stripMargin
              ),
              href = href
            )
          )
        )
      )
    )

}
