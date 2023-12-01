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
import connectors.SubscriptionConnector
import helpers.JsonFixtures._
import models.error.ApiError
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, MandatoryInformationMissingError, NotFoundError, UnableToCreateEMTPSubscriptionError}
import models.{Address, Country, ReporterType, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.MockitoSugar.reset
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  private val application = applicationBuilder()
    .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))

  override def beforeEach(): Unit = {
    reset(mockSubscriptionConnector)
    super.beforeEach()
  }

  val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

  "SubscriptionService" - {
    "must return 'SubscriptionID' on creating subscription" in {
      val subscriptionID                                      = SubscriptionID("id")
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

      val address = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(IndDoYouHaveNINumberPage, false)
        .success
        .value
        .set(IndContactNamePage, name)
        .success
        .value
        .set(IndContactEmailPage, TestEmail)
        .success
        .value
        .set(IndContactHavePhonePage, false)
        .success
        .value
        .set(IndUKAddressWithoutIdPage, address)
        .success
        .value

      val result = service.checkAndCreateSubscription(safeId, userAnswers)
      result.futureValue mustBe Right(SubscriptionID("id"))

      verify(mockSubscriptionConnector, times(1)).readSubscription(any())(any(), any())
      verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
    }

    "must return 'SubscriptionID' on creating subscription when a second contact exists" in {
      val subscriptionID                                      = SubscriptionID("id")
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

      val address = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(IndDoYouHaveNINumberPage, false)
        .success
        .value
        .set(IndContactNamePage, name)
        .success
        .value
        .set(IndContactEmailPage, TestEmail)
        .success
        .value
        .set(ContactHavePhonePage, false)
        .success
        .value
        .set(IndUKAddressWithoutIdPage, address)
        .success
        .value
        .set(HaveSecondContactPage, true)
        .success
        .value
        .set(SecondContactHavePhonePage, false)
        .success
        .value

      val result = service.checkAndCreateSubscription(safeId, userAnswers)
      result.futureValue mustBe Right(SubscriptionID("id"))

      verify(mockSubscriptionConnector, times(1)).readSubscription(any())(any(), any())
      verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
    }

    "must return 'SubscriptionID' when a subscription already exists" in {
      val subscriptionID = SubscriptionID("id")

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(Some(subscriptionID)))

      val result = service.checkAndCreateSubscription(safeId, emptyUserAnswers)
      result.futureValue mustBe Right(subscriptionID)
    }

    "must return MandatoryInformationMissingError when UserAnswers is empty" in {
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Left(MandatoryInformationMissingError()))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

      val result = service.checkAndCreateSubscription(safeId, UserAnswers("id"))

      result.futureValue mustBe Left(MandatoryInformationMissingError())
    }

    "must return error when it fails to create subscription" in {
      val errors = Seq(NotFoundError, BadRequestError, DuplicateSubmissionError, UnableToCreateEMTPSubscriptionError)
      for (error <- errors) {
        val userAnswers = UserAnswers("id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value
          .set(ContactNamePage, s"$FirstName $LastName")
          .success
          .value
          .set(ContactHavePhonePage, false)
          .success
          .value
          .set(HaveSecondContactPage, false)
          .success
          .value

        val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Left(error))
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

        val result = service.checkAndCreateSubscription(safeId, userAnswers)

        result.futureValue mustBe Left(error)
      }
    }

    "getDisplaySubscriptionId" - {

      "must return 'SubscriptionID' for valid input" in {
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("id"))))
        val result = service.getDisplaySubscriptionId(safeId)
        result.futureValue mustBe Some(SubscriptionID("id"))
      }

      "must return 'None' for any failures of exceptions" in {
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
        val result = service.getDisplaySubscriptionId(safeId)
        result.futureValue mustBe None
      }
    }
  }

}
