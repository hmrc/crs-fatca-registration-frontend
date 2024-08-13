/*
 * Copyright 2024 HM Revenue & Customs
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
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.ContactDetailsMissingView

class ContactDetailsMissingControllerSpec extends SpecBase {

  "ContactDetailsMissing Controller" - {

    "must return OK and the correct view with default continue url for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ContactDetailsMissingController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactDetailsMissingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(routes.IndexController.onPageLoad.url)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view with specific continue url for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.ContactDetailsMissingController.onPageLoad(
            Some(RedirectUrl(controllers.organisation.routes.ContactNameController.onPageLoad(NormalMode).url))
          ).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactDetailsMissingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(controllers.organisation.routes.ContactNameController.onPageLoad(NormalMode).url)(request, messages(application)).toString
      }
    }
  }

}
