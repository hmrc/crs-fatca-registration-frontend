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
import controllers.actions.{FakeSubscriptionIdRetrievalAction, SubscriptionIdRetrievalAction}
import controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController
import controllers.routes
import generators.{ModelGenerators, UserAnswersGenerator}
import helpers.JsonFixtures.subscriptionId
import models.subscription.response.{DisplayResponseDetail, DisplaySubscriptionResponse, IndividualRegistrationType}
import models.{SubscriptionID, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq => mEq}
import org.mockito.MockitoSugar.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.changeContactDetails.ChangeContactDetailsInProgressPage
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ThereIsAProblemView

import scala.concurrent.{ExecutionContext, Future}

class ChangeIndividualContactDetailsControllerSpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with UserAnswersGenerator
    with ModelGenerators {

  private val mockSubscriptionService           = mock[SubscriptionService]
  private val mockSubscriptionIdRetrievalAction = mock[SubscriptionIdRetrievalAction]

  override def beforeEach(): Unit = {
    reset(mockSubscriptionService)
    super.beforeEach
  }

  "ChangeIndividualContactDetails Controller" - {
    when(mockSubscriptionIdRetrievalAction.apply(Some(IndividualRegistrationType)))
      .thenReturn(new FakeSubscriptionIdRetrievalAction(subscriptionId, injectedParsers))

    "onPageLoad" - {
      "must return OK and show 'confirm and send' button for a GET request when there is no change to the contact details" in {
        forAll(arbitrary[DisplaySubscriptionResponse], indChangeContact.arbitrary) {
          (subscription, userAnswers) =>
            when(mockSubscriptionService.getSubscription(mEq(subscriptionId))(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Option(subscription)))
            when(mockSubscriptionService.populateUserAnswersFromIndSubscription(any[UserAnswers](), any[DisplayResponseDetail]()))
              .thenReturn(Some(userAnswers))
            when(mockSubscriptionService.checkIfIndContactDetailsHasChanged(any[DisplaySubscriptionResponse](), any[UserAnswers]()))
              .thenReturn(Some(true))
            when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
            when(mockSessionRepository.set(userAnswers.withPage(ChangeContactDetailsInProgressPage, true)))
              .thenReturn(Future.successful(true))

            val application = applicationBuilder(Some(userAnswers))
              .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
              .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
              .build()

            running(application) {
              val request = FakeRequest(GET, ChangeIndividualContactDetailsController.onPageLoad().url)

              val result = route(application, request).value

              status(result) mustEqual OK
              val page = Jsoup.parse(contentAsString(result))
              page.getElementsContainingText("Confirm and send").isEmpty mustBe false

              page.getElementsContainingText("Back to manage your CRS and FATCA reports").toString
                .must(include(routes.IndexController.onPageLoad.url))
            }
        }
      }

      "must not populate userAnswers with subscription from backend when change contact details is in progress" in {
        forAll(arbitrary[DisplaySubscriptionResponse], indChangeContact.arbitrary) {
          (subscription, userAnswers) =>
            val userAnswersUpdated = Some(userAnswers.withPage(ChangeContactDetailsInProgressPage, true))

            when(mockSubscriptionService.getSubscription(mEq(subscriptionId))(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Option(subscription)))
            when(mockSubscriptionService.checkIfIndContactDetailsHasChanged(any[DisplaySubscriptionResponse](), any[UserAnswers]()))
              .thenReturn(Some(true))
            when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswersUpdated))

            val application = applicationBuilder(userAnswersUpdated)
              .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
              .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
              .build()

            running(application) {
              val request = FakeRequest(GET, ChangeIndividualContactDetailsController.onPageLoad().url)

              val result = route(application, request).value

              status(result) mustEqual OK
              val page = Jsoup.parse(contentAsString(result))
              page.getElementsContainingText("Confirm and send").isEmpty mustBe false
            }
        }
      }

      "must return OK but hide 'confirm and send' button for a GET request when there are changes to the contact details" in {
        forAll(arbitrary[DisplaySubscriptionResponse], indChangeContact.arbitrary) {
          (subscription, userAnswers) =>
            val userAnswersOpt = Some(userAnswers)

            when(mockSubscriptionService.getSubscription(mEq(subscriptionId))(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Option(subscription)))
            when(mockSubscriptionService.populateUserAnswersFromIndSubscription(any[UserAnswers](), any[DisplayResponseDetail]()))
              .thenReturn(userAnswersOpt)
            when(mockSubscriptionService.checkIfIndContactDetailsHasChanged(any[DisplaySubscriptionResponse](), any[UserAnswers]()))
              .thenReturn(Some(false))
            when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswersOpt))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val application = applicationBuilder(userAnswersOpt)
              .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
              .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
              .build()

            running(application) {
              val request = FakeRequest(GET, ChangeIndividualContactDetailsController.onPageLoad().url)

              val result = route(application, request).value

              status(result) mustEqual OK
              val page = Jsoup.parse(contentAsString(result))
              page.getElementsContainingText("Confirm and send").isEmpty mustBe true
            }
        }
      }

      "must return 'Internal server error' page on failing to read subscription details" in {
        val userAnswers = Some(emptyUserAnswers)

        when(mockSubscriptionService.getSubscription(mEq(subscriptionId))(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers)
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
          .build()

        running(application) {
          val request = FakeRequest(GET, ChangeIndividualContactDetailsController.onPageLoad().url)
          val view    = application.injector.instanceOf[ThereIsAProblemView]

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }
    }

    "onSubmit" - {
      "redirect to confirmation page on updating ContactDetails" in {
        val userAnswers = Some(emptyUserAnswers)

        when(mockSubscriptionService.updateIndContactDetails(mEq(subscriptionId), any[UserAnswers])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))

        val application = applicationBuilder(userAnswers)
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
          .build()

        running(application) {
          val request = FakeRequest(POST, ChangeIndividualContactDetailsController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.DetailsUpdatedController.onPageLoad().url
        }
      }

      "return 'technical difficulties' page on failing to update ContactDetails" in {
        val userAnswers = Some(emptyUserAnswers)

        when(mockSubscriptionService.updateIndContactDetails(any[SubscriptionID](), any[UserAnswers])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(false))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))

        val application = applicationBuilder(userAnswers)
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
          .build()

        running(application) {
          val request = FakeRequest(POST, ChangeIndividualContactDetailsController.onSubmit().url)
          val view    = application.injector.instanceOf[ThereIsAProblemView]

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }
    }
  }

}
