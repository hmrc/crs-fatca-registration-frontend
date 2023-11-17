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
import connectors.RegistrationConnector
import helpers.JsonFixtures._
import helpers.RegisterHelper._
import models.IdentifierType.{NINO, UTR}
import models.Name
import models.ReporterType.LimitedCompany
import models.error.ApiError
import models.error.ApiError.NotFoundError
import models.matching._
import models.register.request.RegisterWithID
import models.register.response.RegistrationWithIDResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.MockitoSugar.when
import play.api.inject.bind

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessMatchingWithIdServiceSpec extends SpecBase {

  val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private val application = applicationBuilder()
    .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))

  val service: BusinessMatchingWithIdService = application.injector.instanceOf[BusinessMatchingWithIdService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    super.beforeEach()
  }

  val dob: LocalDate = LocalDate.now

  private val name = Name(FirstName, LastName)

  "BusinessMatchingWithIdService" - {

    "sendIndividualRegistrationInformation" - {

      "must return matching information when both safeId and subscriptionId can be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Right(registrationWithIDIndividualResponse))

        when(mockRegistrationConnector.withIndividualNino(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendIndividualRegistrationInformation(RegisterWithID(name, Some(LocalDate.now()), NINO, TestNiNumber))

        result.futureValue mustBe Right(IndRegistrationInfo(safeId))
      }

      "must return an error when when safeId or subscriptionId can't be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Left(NotFoundError))

        when(mockRegistrationConnector.withIndividualNino(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendIndividualRegistrationInformation(RegisterWithID(name, Some(LocalDate.now()), NINO, TestNiNumber))

        result.futureValue mustBe Left(NotFoundError)
      }
    }

    "sendBusinessRegistrationInformation" - {

      "must return matching information when both safeId and subscriptionId can be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Right(registrationWithIDOrganisationResponse))

        when(mockRegistrationConnector.withOrganisationUtr(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendBusinessRegistrationInformation(
            RegisterWithID(RegistrationRequest(UTR, utr.uniqueTaxPayerReference, OrgName, Some(LimitedCompany)))
          )

        result.futureValue mustBe Right(
          OrgRegistrationInfo(safeId, OrgName, addressResponse)
        )
      }

      "must return an error when when safeId or subscriptionId can't be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Left(NotFoundError))

        when(mockRegistrationConnector.withOrganisationUtr(any())(any(), any())).thenReturn(response)

        val registerWithID =
          RegisterWithID(RegistrationRequest(UTR, utr.uniqueTaxPayerReference, OrgName, Some(LimitedCompany)))
        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendBusinessRegistrationInformation(registerWithID)

        result.futureValue mustBe Left(NotFoundError)
      }
    }
  }

}
