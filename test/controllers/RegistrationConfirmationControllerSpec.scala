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
import generators.UserAnswersGenerator
import models.{SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => is}
import org.mockito.MockitoSugar.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.SubscriptionIDPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.{RegistrationConfirmationView, ThereIsAProblemView}

import scala.concurrent.Future

class RegistrationConfirmationControllerSpec extends SpecBase with UserAnswersGenerator {

  "RegistrationConfirmation Controller" - {

    "must return OK and the correct view for a GET with valid orgWithId userAnswers" in {

      forAll(orgWithId.arbitrary, arbitrarySubscriptionID.arbitrary) {
        (userAnswers: UserAnswers, subscriptionId: SubscriptionID) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers.withPage(SubscriptionIDPage, subscriptionId)), AffinityGroup.Organisation).build()
          when(mockSessionRepository.set(is(userAnswers.copy(data = Json.obj())))).thenReturn(Future.successful(true))
          running(application) {
            val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RegistrationConfirmationView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(subscriptionId.value)(request, messages(application)).toString
          }
      }
    }

    "must return OK and the correct view for a GET with valid indWithId userAnswers" in {

      forAll(indWithId.arbitrary, arbitrarySubscriptionID.arbitrary) {
        (userAnswers: UserAnswers, subscriptionId: SubscriptionID) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers.withPage(SubscriptionIDPage, subscriptionId)), AffinityGroup.Individual).build()
          when(mockSessionRepository.set(is(userAnswers.copy(data = Json.obj())))).thenReturn(Future.successful(true))
          running(application) {
            val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RegistrationConfirmationView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(subscriptionId.value)(request, messages(application)).toString
          }
      }
    }

    "must return OK and the there-is-a-problem view for a GET when unable to empty user answers data" in {
      forAll(orgWithId.arbitrary, arbitrarySubscriptionID.arbitrary) {
        (userAnswers: UserAnswers, subscriptionId: SubscriptionID) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers.withPage(SubscriptionIDPage, subscriptionId)), AffinityGroup.Organisation).build()

          when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(false))

          running(application) {
            val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[ThereIsAProblemView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view()(request, messages(application)).toString
          }
      }
    }

    "must return OK and the there-is-a-problem view for a GET when unable to find subscriptionId in user answers data" in {
      forAll(orgWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers), AffinityGroup.Organisation).build()

          when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(false))

          running(application) {
            val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[ThereIsAProblemView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view()(request, messages(application)).toString
          }
      }
    }
  }

}
