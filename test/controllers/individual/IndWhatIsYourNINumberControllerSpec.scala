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
import controllers.routes
import forms.IndWhatIsYourNINumberFormProvider
import generators.UserAnswersGenerator
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.IndWhatIsYourNINumberPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import views.html.individual.IndWhatIsYourNINumberView

import scala.concurrent.Future

class IndWhatIsYourNINumberControllerSpec extends SpecBase with MockitoSugar with UserAnswersGenerator {

  val formProvider = new IndWhatIsYourNINumberFormProvider()
  val form         = formProvider()
  val validAnswer  = Nino("CC123456C")

  lazy val whatIsYourNINumberRoute = controllers.individual.routes.IndWhatIsYourNINumberController.onPageLoad(NormalMode).url

  "WhatIsYourNINumber Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(indWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, whatIsYourNINumberRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndWhatIsYourNINumberView]
            val updatedForm = userAnswers.get(IndWhatIsYourNINumberPage).map(
              nino => form.fill(nino.nino)
            ).getOrElse(form)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(updatedForm, NormalMode)(request, messages(application)).toString
          }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IndWhatIsYourNINumberPage, validAnswer).success.value
      val filledForm  = form.bind(Map("ni-number" -> validAnswer.nino))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsYourNINumberRoute)

        val view = application.injector.instanceOf[IndWhatIsYourNINumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(filledForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to PageUnavailable when UserAnswers is empty" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsYourNINumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourNINumberRoute)
            .withFormUrlEncodedBody(("ni-number", "CC 12 34 56 C"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourNINumberRoute)
            .withFormUrlEncodedBody(("ni-number", ""))

        val boundForm = form.bind(Map("ni-number" -> ""))

        val view = application.injector.instanceOf[IndWhatIsYourNINumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatIsYourNINumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourNINumberRoute)
            .withFormUrlEncodedBody(("ni-number", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
