/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import base.SpecBase
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, writeableOf_AnyContentAsEmpty}

class UnauthorisedStandardUserViewSpec extends SpecBase {

  "UnauthorisedStandardUserView must have" - {
    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    running(application) {
      val request = FakeRequest(GET, routes.UnauthorisedStandardUserController.onPageLoad().url)

      val result      = route(application, request).value
      val htmlContent = contentAsString(result)

      "a heading" in {
        htmlContent must include("You’re currently unable to access this service")
      }
      "paragraph 1" in {
        htmlContent must include(
          "You have signed in as a standard user. Someone in your organisation with an administrator role must give you permission to access this service."
        )
      }
      "a subheading" in {
        htmlContent must include("What your administrator must do next")
      }
      "a 3 point list" in {
        htmlContent must include("Sign in with their Government Gateway details.")
        htmlContent must include("Register for the CRS and FATCA service, if they have not done so already.")
        htmlContent must include("Give you permission to use this service through the tax and scheme management service.")
      }
      "paragraph 2" in {
        htmlContent must include(
          "If your administrator does not have the link for the tax and scheme management service, then you can share this link with them. The link will only work for your administrator:"
        )
      }
      "a link as text" in {
        htmlContent must include("www.tax.service.gov.uk/tax-and-scheme-management/services")
      }
      "paragraph 3" in {
        htmlContent must include(
          "Once your administrator has completed these steps, you will then be able to sign in with your own Government Gateway details to use this service."
        )
      }
    }
  }

}
