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

package controllers.changeContactDetails

import base.{SpecBase, TestValues}
import controllers.actions.{FakeSubscriptionIdRetrievalAction, SubscriptionIdRetrievalAction}
import forms.changeContactDetails.OrganisationSecondContactPhoneFormProvider
import models.NormalMode
import models.subscription.response.OrganisationRegistrationType
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.changeContactDetails.{OrganisationSecondContactNamePage, OrganisationSecondContactPhonePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.changeContactDetails.OrganisationSecondContactPhoneView

import scala.concurrent.Future

class OrganisationSecondContactPhoneControllerSpec extends SpecBase with MockitoSugar with TestValues {

  val formProvider = new OrganisationSecondContactPhoneFormProvider()
  private val form = formProvider()

  private val mockSubscriptionIdRetrievalAction = mock[SubscriptionIdRetrievalAction]

  private lazy val secondContactPhoneRoute = routes.OrganisationSecondContactPhoneController.onPageLoad(NormalMode).url

  "Change Organisation SecondContactPhone Controller" - {
    when(mockSubscriptionIdRetrievalAction.apply(Some(OrganisationRegistrationType)))
      .thenReturn(new FakeSubscriptionIdRetrievalAction(subscriptionId, injectedParsers))

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.withPage(OrganisationSecondContactNamePage, name.fullName)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, secondContactPhoneRoute)
        val view    = application.injector.instanceOf[OrganisationSecondContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, name.fullName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .withPage(OrganisationSecondContactNamePage, name.fullName)
        .withPage(OrganisationSecondContactPhonePage, TestPhoneNumber)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, secondContactPhoneRoute)
        val view    = application.injector.instanceOf[OrganisationSecondContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TestPhoneNumber), NormalMode, name.fullName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
          .build()

      running(application) {
        val request = FakeRequest(POST, secondContactPhoneRoute).withFormUrlEncodedBody(("value", TestPhoneNumber))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.withPage(OrganisationSecondContactNamePage, name.fullName)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request   = FakeRequest(POST, secondContactPhoneRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[OrganisationSecondContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, name.fullName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, secondContactPhoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(POST, secondContactPhoneRoute).withFormUrlEncodedBody(("value", TestPhoneNumber))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
