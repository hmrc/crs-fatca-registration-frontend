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

package controllers.changeContactDetails

import base.SpecBase
import controllers.routes
import forms.changeContactDetails.OrganisationHavePhoneFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.changeContactDetails.{OrganisationContactNamePage, OrganisationHavePhonePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.changeContactDetails.OrganisationHavePhoneView

import scala.concurrent.Future

class OrganisationHavePhoneControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new OrganisationHavePhoneFormProvider()
  val form         = formProvider()

  val contactName = "John Doe"

  lazy val organisationHavePhoneRoute = controllers.changeContactDetails.routes.OrganisationHavePhoneController.onPageLoad(NormalMode).url

  "OrganisationHavePhone Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(OrganisationContactNamePage, contactName).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, organisationHavePhoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OrganisationHavePhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, contactName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = for {
        nameAnswered             <- UserAnswers(userAnswersId).set(OrganisationContactNamePage, contactName).success
        haveContactPhoneAnswered <- nameAnswered.set(OrganisationHavePhonePage, true).success
      } yield haveContactPhoneAnswered

      val application = applicationBuilder(userAnswers = Some(userAnswers.success.value)).build()

      running(application) {
        val request = FakeRequest(GET, organisationHavePhoneRoute)

        val view = application.injector.instanceOf[OrganisationHavePhoneView]

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

          val userAnswers = UserAnswers(userAnswersId).set(OrganisationContactNamePage, contactName).success.value
          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, organisationHavePhoneRoute)
                .withFormUrlEncodedBody(("value", booleanAnswerAsString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId).set(OrganisationContactNamePage, contactName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, organisationHavePhoneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[OrganisationHavePhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, contactName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, organisationHavePhoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, organisationHavePhoneRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
