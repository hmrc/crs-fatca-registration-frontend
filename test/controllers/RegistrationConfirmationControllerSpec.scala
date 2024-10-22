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
import models.UserAnswers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import generators.{ModelGenerators, UserAnswersGenerator}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.RegistrationConfirmationView

class RegistrationConfirmationControllerSpec extends SpecBase with UserAnswersGenerator {


  "RegistrationConfirmation Controller" - {

    "must return OK and the correct view for a GET with valid orgWithId userAnswers" in {

      forAll(orgWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers), AffinityGroup.Organisation).build()

          running(application) {
            val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RegistrationConfirmationView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view()(request, messages(application)).toString
          }
      }
    }
  }
}
