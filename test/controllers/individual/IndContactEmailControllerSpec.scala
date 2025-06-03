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
import controllers.ControllerHelper
import forms.IndContactEmailFormProvider
import generators.UserAnswersGenerator
import models.matching.SafeId
import models.{NormalMode, SubscriptionID, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.IndContactEmailPage
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import views.html.individual.IndContactEmailView

import scala.concurrent.Future

class IndContactEmailControllerSpec extends SpecBase with MockitoSugar with UserAnswersGenerator {

  val formProvider = new IndContactEmailFormProvider()
  val form         = formProvider()

  lazy val indContactEmailRoute                    = routes.IndContactEmailController.onPageLoad(NormalMode).url
  val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]
  val mockControllerHelper: ControllerHelper       = mock[ControllerHelper]

  private val subscriptionId = SubscriptionID("XE0000123456789")

  "IndContactEmail Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(indWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[ControllerHelper].toInstance(mockControllerHelper))
            .build()
          when(mockSessionRepository.set(userAnswers.copy(data = userAnswers.data))).thenReturn(Future.successful(true))
          when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

          running(application) {
            val request = FakeRequest(GET, indContactEmailRoute)

            val result = route(application, request).value

            val view        = application.injector.instanceOf[IndContactEmailView]
            val updatedForm = userAnswers.get(IndContactEmailPage).map(form.fill).getOrElse(form)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(updatedForm, NormalMode)(request, messages(application)).toString
          }
      }
    }

    "must update subscription ID and create enrollment if Safe id found" in {

      forAll(indWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[ControllerHelper].toInstance(mockControllerHelper))
            .build()
          when(mockSessionRepository.set(userAnswers.copy(data = userAnswers.data))).thenReturn(Future.successful(true))
          when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(Some(subscriptionId)))
          when(mockControllerHelper.updateSubscriptionIdAndCreateEnrolment(any(), any())(any(), any()))
            .thenReturn(Future.successful(Redirect(onwardRoute)))

          running(application) {
            val request = FakeRequest(GET, indContactEmailRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
          }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IndContactEmailPage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[ControllerHelper].toInstance(mockControllerHelper))
        .build()
      when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, indContactEmailRoute)

        val view = application.injector.instanceOf[IndContactEmailView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to PageUnavailable when UserAnswers is empty" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[ControllerHelper].toInstance(mockControllerHelper))
        .build()
      when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, indContactEmailRoute)

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
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ControllerHelper].toInstance(mockControllerHelper)
          )
          .build()
      when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

      running(application) {
        val request =
          FakeRequest(POST, indContactEmailRoute)
            .withFormUrlEncodedBody(("value", "email@email.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[ControllerHelper].toInstance(mockControllerHelper))
        .build()

      when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

      running(application) {
        val request =
          FakeRequest(POST, indContactEmailRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IndContactEmailView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[ControllerHelper].toInstance(mockControllerHelper))
        .build()

      when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, indContactEmailRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[ControllerHelper].toInstance(mockControllerHelper))
        .build()

      when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

      running(application) {
        val request =
          FakeRequest(POST, indContactEmailRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
