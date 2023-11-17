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

package controllers.organisation

import base.SpecBase
import models.ReporterType.{LimitedCompany, LimitedPartnership}
import pages.ReporterTypePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.organisation.BusinessNotIdentifiedView

class BusinessNotIdentifiedControllerSpec extends SpecBase {

  "NoRecordsMatched Controller" - {

    lazy val corporationTaxEnquiriesLink: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/corporation-tax-enquiries"
    lazy val selfAssessmentEnquiriesLink: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

    "return OK and the correct view for a GET with link for corporation tax enquiries" in {

      val userAnswers = emptyUserAnswers.set(ReporterTypePage, LimitedCompany).success.value
      val application = applicationBuilder(Option(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessNotIdentifiedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          corporationTaxEnquiriesLink,
          controllers.routes.IndexController.onPageLoad.url
        )(request, messages(application)).toString

      }
    }

    "return OK and the correct view for a GET when a Sole Trader" in {

      val userAnswers = emptyUserAnswers.set(ReporterTypePage, LimitedPartnership).success.value
      val application = applicationBuilder(Option(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessNotIdentifiedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          selfAssessmentEnquiriesLink,
          controllers.routes.IndexController.onPageLoad.url
        )(request, messages(application)).toString

      }
    }

  }

}
