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

package controllers

import base.{ControllerMockFixtures, SpecBase}
import helpers.JsonFixtures._
import models.enrolment.GroupIds
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, MandatoryInformationMissingError}
import models.matching.OrgRegistrationInfo
import models.register.response.details.AddressResponse
import models.requests.DataRequest
import models.{ReporterType, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import play.api.inject.bind
import play.api.mvc.{AnyContent, Result}
import play.api.test.Helpers._
import services.TaxEnrolmentService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ControllerHelperSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach {

  val mockTaxEnrolmentService: TaxEnrolmentService = mock[TaxEnrolmentService]

  override def beforeEach(): Unit =
    reset(mockTaxEnrolmentService)

  private val application = applicationBuilder()
    .overrides(bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentService))
    .build()

  val controller: ControllerHelper = application.injector.instanceOf[ControllerHelper]

  val subscriptionId: SubscriptionID = SubscriptionID("ABC123")

  val userAnswers: UserAnswers = emptyUserAnswers
    .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
    .withPage(ContactNamePage, "")
    .withPage(ContactEmailPage, TestEmail)

  "ControllerHelper" - {
    "updateSubscriptionIdAndCreateEnrolment update the subscription ID in user answers and create an enrolment" in {

      val affinityGroup: AffinityGroup         = AffinityGroup.Individual
      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, UserAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(1)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.RegistrationConfirmationController.onPageLoad().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any())(any(), any())
    }

    "Redirect to Individual already registered when tax enrolments returns EnrolmentExists error for Affinity Group Individual" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Individual

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, UserAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(EnrolmentExistsError(mock[GroupIds]))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any())(any(), any())
    }

    "Redirect to Individual already registered when tax enrolments returns EnrolmentExists error for Affinity Group Agent" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Agent

      val dataRequest: DataRequest[AnyContent] =
        DataRequest(fakeRequest, UserAnswersId, affinityGroup, userAnswers.withPage(ReporterTypePage, ReporterType.Individual))

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(EnrolmentExistsError(mock[GroupIds]))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any())(any(), any())
    }

    "Redirect to Business already registered with ID when tax enrolments returns EnrolmentExists error" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Organisation
      val addressResponse              = AddressResponse("line1", None, None, None, None, "UK")
      val userAnswers2 = userAnswers
        .withPage(RegistrationInfoPage, OrgRegistrationInfo(safeId, name = "", address = addressResponse))
      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, UserAnswersId, affinityGroup, userAnswers2)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(EnrolmentExistsError(mock[GroupIds]))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.PreRegisteredController.onPageLoad(withId = true).url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any())(any(), any())
    }

    "Redirect to Business already registered without ID when tax enrolments returns EnrolmentExists error" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Organisation

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, UserAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(EnrolmentExistsError(mock[GroupIds]))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.PreRegisteredController.onPageLoad(withId = false).url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any())(any(), any())
    }

    "Redirect to SomeInformation is missing controller" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Organisation

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, UserAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(MandatoryInformationMissingError("Error"))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.InformationMissingController.onPageLoad().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any())(any(), any())
    }

    "Return service unavailable for other errors" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Organisation

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, UserAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(ApiError.ServiceUnavailableError)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SERVICE_UNAVAILABLE

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any())(any(), any())
    }
  }

}
