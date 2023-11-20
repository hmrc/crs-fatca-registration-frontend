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

package services

import base.SpecBase
import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.{EnrolmentStoreProxyConnector, TaxEnrolmentsConnector}
import helpers.JsonFixtures._
import models.enrolment.GroupIds
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, UnableToCreateEnrolmentError}
import models.matching.IndRegistrationInfo
import models.{Address, Country, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, when}
import pages._
import play.api.http.Status.NO_CONTENT
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxEnrolmentServiceSpec extends SpecBase {

  val mockTaxEnrolmentsConnector: TaxEnrolmentsConnector             = mock[TaxEnrolmentsConnector]
  val mockEnrolmentStoreProxyConnector: EnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]

  private val application: GuiceApplicationBuilder =
    applicationBuilder()
      .overrides(bind[TaxEnrolmentsConnector].toInstance(mockTaxEnrolmentsConnector))
      .overrides(bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector))

  val service: TaxEnrolmentService = application.injector.instanceOf[TaxEnrolmentService]

  override def beforeEach(): Unit = {
    reset(mockTaxEnrolmentsConnector, mockEnrolmentStoreProxyConnector)
    super.beforeEach()
  }

  "TaxEnrolmentService" - {
    "must create a subscriptionModel from userAnswers and call the taxEnrolmentsConnector returning with a Successful NO_CONTENT" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Right(NO_CONTENT))

      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(response)
      when(mockEnrolmentStoreProxyConnector.enrolmentStatus(any())(any(), any())).thenReturn(EitherT.fromEither[Future](Right(())))

      val subscriptionID = SubscriptionID("id")
      val address        = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(IndDoYouHaveNINumberPage, false)
        .success
        .value
        .set(IndContactNamePage, individualContactName)
        .success
        .value
        .set(ContactEmailPage, TestEmail)
        .success
        .value
        .set(NonUKBusinessAddressWithoutIDPage, address)
        .success
        .value
        .set(RegistrationInfoPage, IndRegistrationInfo(safeId))
        .success
        .value

      val result = service.checkAndCreateEnrolment(safeId, userAnswers, subscriptionID)

      result.futureValue mustBe Right(NO_CONTENT)
    }

    "must return BAD_REQUEST when 400 is received from taxEnrolments" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Left(UnableToCreateEnrolmentError))

      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(response)
      when(mockEnrolmentStoreProxyConnector.enrolmentStatus(any())(any(), any())).thenReturn(EitherT.fromEither[Future](Right(())))

      val subscriptionID = SubscriptionID("id")
      val address        = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(IndDoYouHaveNINumberPage, false)
        .success
        .value
        .set(IndContactNamePage, individualContactName)
        .success
        .value
        .set(ContactEmailPage, TestEmail)
        .success
        .value
        .set(NonUKBusinessAddressWithoutIDPage, address)
        .success
        .value
        .set(RegistrationInfoPage, IndRegistrationInfo(safeId))
        .success
        .value

      val result = service.checkAndCreateEnrolment(safeId, userAnswers, subscriptionID)

      result.futureValue mustBe Left(UnableToCreateEnrolmentError)
    }

    "must return EnrolmentExistsError when there is already an enrolment" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Left(UnableToCreateEnrolmentError))
      val groupIds                                 = GroupIds(Seq("groupId"), Seq.empty)

      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(response)
      when(mockEnrolmentStoreProxyConnector.enrolmentStatus(any())(any(), any()))
        .thenReturn(EitherT.fromEither[Future](Left(EnrolmentExistsError(groupIds))))

      val subscriptionID = SubscriptionID("id")
      val address        = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(IndDoYouHaveNINumberPage, false)
        .success
        .value
        .set(IndContactNamePage, individualContactName)
        .success
        .value
        .set(ContactEmailPage, TestEmail)
        .success
        .value
        .set(NonUKBusinessAddressWithoutIDPage, address)
        .success
        .value
        .set(RegistrationInfoPage, IndRegistrationInfo(safeId))
        .success
        .value

      val result = service.checkAndCreateEnrolment(safeId, userAnswers, subscriptionID)

      result.futureValue mustBe Left(EnrolmentExistsError(groupIds))
    }
  }

}
