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

package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.UnauthorisedStandardUserView

class UnauthorisedStandardUserControllerSpec extends SpecBase {

  "UnauthorisedStandardUser Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.UnauthorisedStandardUserController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnauthorisedStandardUserView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must display the correct messages" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.UnauthorisedStandardUserController.onPageLoad().url)

        val result      = route(application, request).value
        val htmlContent = contentAsString(result)

        htmlContent must include("You’re unable to register for this service")
        htmlContent must include("What your administrator must do next")
        htmlContent must include("You have signed in as a standard user. " +
          "Only people in your organisation with an administrator role can register for this service.")

        htmlContent must include("Sign in with their Government Gateway details.")
        htmlContent must include("Register for the CRS and FATCA service.")
        htmlContent must include("Give you permission to use this service through the tax and scheme management service.")

        htmlContent must include("If your administrator does not have the link for the tax and scheme management service," +
          " then you can share this link with them.")

        htmlContent must include("www.tax.service.gov.uk/tax-and-scheme-management/services")
        htmlContent must include("Once your administrator has completed these steps," +
          " then you will then be able to sign in with your own Government Gateway details to use this service.")
      }
    }
  }

}
