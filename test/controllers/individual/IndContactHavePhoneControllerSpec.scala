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

import base.SpecBase
import forms.IndContactHavePhoneFormProvider
import generators.UserAnswersGenerator
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.IndContactHavePhonePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.individual.IndContactHavePhoneView

import scala.concurrent.Future

class IndContactHavePhoneControllerSpec extends SpecBase with MockitoSugar with UserAnswersGenerator {

  lazy val indContactHavePhoneRoute = routes.IndContactHavePhoneController.onPageLoad(NormalMode).url

  val formProvider = new IndContactHavePhoneFormProvider()
  val form         = formProvider()

  "IndContactHavePhone Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(indWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
          when(mockSessionRepository.set(userAnswers.copy(data = userAnswers.data))).thenReturn(Future.successful(true))

          running(application) {
            val request = FakeRequest(GET, indContactHavePhoneRoute)

            val result = route(application, request).value

            val view        = application.injector.instanceOf[IndContactHavePhoneView]
            val updatedForm = userAnswers.get(IndContactHavePhonePage).map(form.fill).getOrElse(form)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(updatedForm, NormalMode)(request, messages(application)).toString
          }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IndContactHavePhonePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, indContactHavePhoneRoute)

        val view = application.injector.instanceOf[IndContactHavePhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      forAll(Arbitrary.arbitrary[Boolean]) {
        booleanAnswer =>
          val booleanAnswerAsString = booleanAnswer.toString

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = emptyUserAnswers
          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, indContactHavePhoneRoute)
                .withFormUrlEncodedBody(("value", booleanAnswerAsString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, indContactHavePhoneRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IndContactHavePhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, indContactHavePhoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, indContactHavePhoneRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
