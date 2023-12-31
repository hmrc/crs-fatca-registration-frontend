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
import models.ReporterType.{LimitedCompany, Sole}
import pages.ReporterTypePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SoleTraderNotIdentifiedView
import views.html.organisation.BusinessNotIdentifiedView

class BusinessNotIdentifiedControllerSpec extends SpecBase {
  val startUrl = controllers.routes.IndexController.onPageLoad.url

  "NoRecordsMatched Controller" - {

    lazy val findCompanyName: String = "https://find-and-update.company-information.service.gov.uk/"

    "return OK and the correct view for a GET with link for corporation tax enquiries" in {

      val userAnswers = emptyUserAnswers.set(ReporterTypePage, LimitedCompany).success.value
      val application = applicationBuilder(Option(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessNotIdentifiedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          findCompanyName,
          Some(LimitedCompany),
          controllers.routes.IndexController.onPageLoad.url
        )(request, messages(application)).toString

      }
    }

    "return OK and the correct view for a GET when a Sole Trader" in {

      val userAnswers = emptyUserAnswers.set(ReporterTypePage, Sole).success.value
      val application = applicationBuilder(Option(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SoleTraderNotIdentifiedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          controllers.routes.IndexController.onPageLoad.url
        )(request, messages(application)).toString

      }
    }

  }

}
