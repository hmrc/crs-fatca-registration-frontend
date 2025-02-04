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

import base.{ControllerMockFixtures, SpecBase}
import controllers.routes
import generators.ModelGenerators
import models.error.ApiError.{BadRequestError, NotFoundError, ServiceUnavailableError}
import models.matching.{IndRegistrationInfo, SafeId}
import models.{Name, NormalMode, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.{IndContactNamePage, IndDateOfBirthPage, IndWhatIsYourNINumberPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BusinessMatchingWithIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.domain.Nino
import views.html.ThereIsAProblemView
import views.html.individual.IndIdentityConfirmedView

import java.time.LocalDate
import scala.concurrent.Future

class IndIdentityConfirmedControllerSpec extends SpecBase with ControllerMockFixtures with ModelGenerators {
  private val SafeIdValue = "XE0000123456789"
  val safeId: SafeId      = SafeId(SafeIdValue)
  val registrationInfo: IndRegistrationInfo = IndRegistrationInfo(safeId)
  val TestNiNumber        = "CC123456C"
  val FirstName           = "Fred"
  val LastName            = "Flintstone"
  val name: Name          = Name(FirstName, LastName)

  val validUserAnswers: UserAnswers = emptyUserAnswers
    .set(IndWhatIsYourNINumberPage, Nino(TestNiNumber))
    .success
    .value
    .set(IndContactNamePage, name)
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
    reset(mockMatchingService)
    reset(mockSubscriptionService)
    reset(mockTaxEnrolmentService)
    super.beforeEach()
  }

  "WeHaveConfirmedYourIdentity Controller" - {

    "return OK and the correct view for a GET when there is a match" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(registrationInfo)))

      when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(None))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)
      val view    = mockedApp.injector.instanceOf[IndIdentityConfirmedView]
      val result  = route(mockedApp, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(NormalMode, onwardRoute.url)(request, messages).toString()

    }

    "must redirect to registration confirmation page when there is an existing subscription" in {

      forAll(Arbitrary.arbitrary[SubscriptionID]) {
        subscription =>
          when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
            .thenReturn(Future.successful(Right(registrationInfo)))
          when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(Some(subscription)))
          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any())).thenReturn(Future.successful(Right(OK)))

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          retrieveUserAnswersData(validUserAnswers)
          val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

          val result = route(mockedApp, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual routes.RegistrationConfirmationController.onPageLoad().url
      }
    }

    "render technical difficulties page when there is an existing subscription and fails to create an enrolment" in {

      forAll(Arbitrary.arbitrary[SubscriptionID]) {
        subscription =>
          when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
            .thenReturn(Future.successful(Right(registrationInfo)))
          when(mockSubscriptionService.getSubscription(any[SafeId]())(any())).thenReturn(Future.successful(Some(subscription)))
          when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any())).thenReturn(Future.successful(Left(BadRequestError)))

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          retrieveUserAnswersData(validUserAnswers)
          val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

          val result = route(mockedApp, request).value

          val view = mockedApp.injector.instanceOf[ThereIsAProblemView]

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) mustEqual view()(request, messages).toString
      }
    }

    "return redirect for a GET when there is no match" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(NotFoundError)))

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(mockedApp, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.individual.routes.IndCouldNotConfirmIdentityController.onPageLoad().url
    }

    "return return Internal Server Error for a GET when an error other than NotFoundError is returned" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(ServiceUnavailableError)))

      retrieveUserAnswersData(validUserAnswers)

      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(mockedApp, request).value

      val view = mockedApp.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString

    }

    "return return Internal Server Error for a GET when there is no data" in {

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode).url)

      val result = route(mockedApp, request).value

      val view = mockedApp.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }

  }

}
