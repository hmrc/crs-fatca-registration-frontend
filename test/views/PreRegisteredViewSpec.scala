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
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, route, running, writeableOf_AnyContentAsEmpty, GET}

class PreRegisteredViewSpec extends SpecBase {

  "PreRegisteredView must have" - {
    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    running(application) {
      val request = FakeRequest(GET, routes.PreRegisteredController.onPageLoad().url)

      val result      = route(application, request).value
      val htmlContent = Jsoup.parse(contentAsString(result)).text()

      "a heading" in {
        htmlContent must include("Your organisation is already registered to use this service")
      }
      "a list" in {
        htmlContent must include("To access the CRS and FATCA service, you must either:")
        htmlContent must include("sign in with the Government Gateway user ID used to register for this service")
        htmlContent must include("make sure you have permission to use this service through the tax and scheme management service")
      }
      "a paragraph" in {
        htmlContent must include(
          "You can email aeoi.enquiries@hmrc.gov.uk if you’re having problems accessing the service."
        )
      }

    }
  }

}
