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
import forms.changeContactDetails.OrganisationHaveSecondContactFormProvider
import models.NormalMode
import models.subscription.response.OrganisationRegistrationType
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages.changeContactDetails.OrganisationContactNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.changeContactDetails.OrganisationHaveSecondContactView

import scala.concurrent.Future

class OrganisationHaveSecondContactControllerSpec extends SpecBase with MockitoSugar with TableDrivenPropertyChecks with TestValues {

  private lazy val haveSecondContactRoute = routes.OrganisationHaveSecondContactController.onPageLoad(NormalMode).url

  val formProvider = new OrganisationHaveSecondContactFormProvider()
  private val form = formProvider()

  private val mockSubscriptionIdRetrievalAction = mock[SubscriptionIdRetrievalAction]

  "Change Organisation HaveSecondContact Controller" - {
    when(mockSubscriptionIdRetrievalAction.apply(Some(OrganisationRegistrationType)))
      .thenReturn(new FakeSubscriptionIdRetrievalAction(subscriptionId, injectedParsers))

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.withPage(OrganisationContactNamePage, name.fullName)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, haveSecondContactRoute)
        val view    = application.injector.instanceOf[OrganisationHaveSecondContactView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, name.fullName, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(POST, haveSecondContactRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.withPage(OrganisationContactNamePage, name.fullName)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request   = FakeRequest(POST, haveSecondContactRoute).withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[OrganisationHaveSecondContactView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, name.fullName, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, haveSecondContactRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(POST, haveSecondContactRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
