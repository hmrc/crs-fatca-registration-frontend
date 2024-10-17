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

package controllers.individual

import base.{ControllerMockFixtures, SpecBase}
import controllers.routes
import org.mockito.ArgumentMatchers.any
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.individual.IndCouldNotConfirmIdentityView
import org.mockito.Mockito.when
import scala.concurrent.Future

class IndCouldNotConfirmIdentityControllerSpec extends SpecBase with ControllerMockFixtures {

  "IndCouldNotConfirmIdentity Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      running(application) {
        val request = FakeRequest(GET, controllers.individual.routes.IndCouldNotConfirmIdentityController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndCouldNotConfirmIdentityView]
        status(result) mustEqual OK
        val continueUrl: String = routes.IndexController.onPageLoad.url
        contentAsString(result) mustEqual view(continueUrl)(request, messages(application)).toString
      }
    }
  }

}
