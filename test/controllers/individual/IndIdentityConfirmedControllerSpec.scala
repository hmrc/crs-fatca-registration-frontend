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
//
//import base.SpecBase
//import models.NormalMode
//import play.api.test.FakeRequest
//import play.api.test.Helpers._
//import views.html.individual.IndIdentityConfirmedView
//
//class IndIdentityConfirmedControllerSpec extends SpecBase {
//
//  val continueUrl = routes.IndContactEmailController.onPageLoad(NormalMode).url
//
//  "IndIdentityConfirmed Controller" - {
//
//    "must return OK and the correct view for a GET" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad().url)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[IndIdentityConfirmedView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(NormalMode, continueUrl)(request, messages(application)).toString
//      }
//    }
//  }
//
//
//
//}

import base.{ControllerMockFixtures, SpecBase}
import controllers.routes
import models.error.ApiError.{BadRequestError, NotFoundError, ServiceUnavailableError}
import models.matching.{IndRegistrationInfo, SafeId}
import models.{Name, NormalMode, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.reset
import pages.{IndDateOfBirthPage, IndWhatIsYourNINumberPage, WhatIsYourNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BusinessMatchingWithIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.domain.Nino
import views.html.ThereIsAProblemView
import org.mockito.Mockito.when
import views.html.individual.IndIdentityConfirmedView

import java.time.LocalDate
import scala.concurrent.Future

class IndIdentityConfirmedControllerSpec extends SpecBase with ControllerMockFixtures {

  private val SafeIdValue = "XE0000123456789"
  val safeId: SafeId      = SafeId(SafeIdValue)
  val TestNiNumber        = "CC123456C"
  val FirstName           = "Fred"
  val LastName            = "Flintstone"
  val registrationInfo    = IndRegistrationInfo(safeId)
  val name: Name          = Name(FirstName, LastName)

  val validUserAnswers: UserAnswers = emptyUserAnswers
    .set(IndWhatIsYourNINumberPage, Nino(TestNiNumber))
    .success
    .value
    .set(WhatIsYourNamePage, name)
    .success
    .value
    .set(IndDateOfBirthPage, LocalDate.now())
    .success
    .value

  val mockMatchingService: BusinessMatchingWithIdService = mock[BusinessMatchingWithIdService]
  val mockSubscriptionService: SubscriptionService       = mock[SubscriptionService]
  val mockTaxEnrolmentService: TaxEnrolmentService       = mock[TaxEnrolmentService]

  private val mockedApp =
    guiceApplicationBuilder()
      .overrides(
        bind[BusinessMatchingWithIdService].toInstance(mockMatchingService),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentService)
      )
      .build()

  override def beforeEach(): Unit = {
    Seq(mockMatchingService, mockSubscriptionService, mockTaxEnrolmentService).foreach(reset(_))
    super.beforeEach
  }

  "IndIdentityConfirmed Controller" - {

    "return OK and the correct view for a GET when there is a match" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(registrationInfo)))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(None))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(None))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)
      val view    = app.injector.instanceOf[IndIdentityConfirmedView]
      val result  = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(NormalMode, onwardRoute.url)(request, messages).toString()

    }

    "must redirect to 'confirmation' page when there is an existing subscription" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(registrationInfo)))
      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("id"))))
      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any())).thenReturn(Future.successful(Right(OK)))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url // TODO : Replace with RegistationConfirmed controller
    }

    "render technical difficulties page when there is an existing subscription and fails to create an enrolment" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(registrationInfo)))
      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("id"))))
      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any())).thenReturn(Future.successful(Left(BadRequestError)))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }

    "return redirect for a GET when there is no match" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(NotFoundError)))

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.individual.routes.IndCouldNotConfirmIdentityController.onPageLoad("identity").url
    }

    "return return Internal Server Error for a GET when an error other than NotFoundError is returned" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(ServiceUnavailableError)))

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }

    "return return Internal Server Error for a GET when there is no data" in {

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }
  }

}
