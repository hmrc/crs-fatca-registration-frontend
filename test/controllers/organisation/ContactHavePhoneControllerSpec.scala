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
import controllers.routes
import forms.ContactHavePhoneFormProvider
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.{ContactHavePhonePage, ContactNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.organisation.ContactHavePhoneView

import scala.concurrent.Future

class ContactHavePhoneControllerSpec extends SpecBase with MockitoSugar with Generators {

  val formProvider = new ContactHavePhoneFormProvider()
  private val form = formProvider()

  private val contactName = "test name"

  private lazy val contactHavePhoneRoute = controllers.organisation.routes.ContactHavePhoneController.onPageLoad(NormalMode).url

  private lazy val contactHavePhoneRouteCheckMode = controllers.organisation.routes.ContactHavePhoneController.onPageLoad(CheckMode).url

  "ContactHavePhone Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(ContactNamePage, contactName).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, contactHavePhoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactHavePhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, contactName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = for {
        nameAnswered             <- UserAnswers(userAnswersId).set(ContactNamePage, contactName).success
        haveContactPhoneAnswered <- nameAnswered.set(ContactHavePhonePage, true).success
      } yield haveContactPhoneAnswered

      val application = applicationBuilder(userAnswers = Some(userAnswers.success.value)).build()

      running(application) {
        val request = FakeRequest(GET, contactHavePhoneRoute)

        val view = application.injector.instanceOf[ContactHavePhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, contactName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      forAll(Arbitrary.arbitrary[Boolean]) {
        booleanAnswer =>
          val booleanAnswerAsString = booleanAnswer.toString

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = UserAnswers(userAnswersId).set(ContactNamePage, contactName).success.value
          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, contactHavePhoneRoute)
                .withFormUrlEncodedBody(("value", booleanAnswerAsString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
      }
    }

    "must redirect to Journey Recovery page when valid data is submitted but no existing data is found" in {

      forAll(Arbitrary.arbitrary[Boolean]) {
        booleanAnswer =>
          val booleanAnswerAsString = booleanAnswer.toString

          val application = applicationBuilder(userAnswers = None)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, contactHavePhoneRoute)
                .withFormUrlEncodedBody(("value", booleanAnswerAsString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
      }
    }

    "must redirect to CheckYourAnswersPage when valid data is submitted in CheckMode" in {

      forAll(Arbitrary.arbitrary[Boolean]) {
        booleanAnswer =>
          val booleanAnswerAsString = booleanAnswer.toString

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = UserAnswers(userAnswersId).set(ContactNamePage, contactName).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, contactHavePhoneRouteCheckMode)
                .withFormUrlEncodedBody(("value", booleanAnswerAsString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url
          }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId).set(ContactNamePage, contactName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, contactHavePhoneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ContactHavePhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, contactName)(request, messages(application)).toString
      }
    }
  }

}
