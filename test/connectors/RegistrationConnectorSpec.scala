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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.JsonFixtures._
import helpers.RegisterHelper._
import helpers.WireMockServerHandler
import models.IdentifierType.{NINO, UTR}
import models.{Name, Regime}
import models.ReporterType.Partnership
import models.error.ApiError
import models.error.ApiError.{NotFoundError, ServiceUnavailableError}
import models.matching.SafeId
import models.register.request._
import models.register.request.details.{Individual, NoIdOrganisation, WithIDIndividual, WithIDOrganisation}
import models.shared.Parameters
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]
  val registrationUrl                       = "/crs-fatca-registration/registration"

  val requestCommon: RequestCommon =
    RequestCommon(
      "2016-08-16T15:55:30Z",
      Regime.CRFA.toString,
      "ec031b045855445e96f98a569ds56cd2",
      Some(Seq(Parameters("REGIME", Regime.CRFA.toString)))
    )

  val registrationWithIndividualIDPayload: RegisterWithID = RegisterWithID(
    RegisterWithIDRequest(
      requestCommon,
      RequestWithIDDetails(
        NINO,
        TestNiNumber,
        requiresNameMatch = true,
        isAnAgent = false,
        Option(WithIDIndividual(FirstName, None, LastName, Some(TestDate)))
      )
    )
  )

  val registrationWithOrganisationIDPayload: RegisterWithID = RegisterWithID(
    RegisterWithIDRequest(
      requestCommon,
      RequestWithIDDetails(
        UTR,
        utr.uniqueTaxPayerReference,
        requiresNameMatch = true,
        isAnAgent = false,
        Option(WithIDOrganisation(OrgName, Partnership.code))
      )
    )
  )

  val registrationWithoutOrganisationIDPayload: RegisterWithoutID = RegisterWithoutID(
    RegisterWithoutIDRequest(
      requestCommon,
      RequestWithoutIDDetails(Some(NoIdOrganisation(OrgName)), None, addressRequest, contactDetails, None)
    )
  )

  val registrationWithoutIndividualIDPayload: RegisterWithoutID = RegisterWithoutID(
    RegisterWithoutIDRequest(
      requestCommon,
      RequestWithoutIDDetails(None, Some(Individual(Name(FirstName, LastName), LocalDate.parse(TestDate, formatter))), addressRequest, contactDetails, None)
    )
  )

  "RegistrationConnector" - {

    "when calling withIndividualNino" - {

      "return 200 and a registration response when individual is matched by nino" in {

        stubResponse("/individual/nino", OK, withIDIndividualResponse)

        val result = connector.withIndividualNino(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Right(registrationWithIDIndividualResponse)
      }

      "return 404 and NotFoundError when there is no match" in {

        stubResponse("/individual/nino", NOT_FOUND, withIDIndividualResponse)

        val result = connector.withIndividualNino(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Left(NotFoundError)
      }

      "return 503 and ServiceUnavailableError when remote is unavailable " in {

        stubResponse("/individual/nino", SERVICE_UNAVAILABLE, withIDIndividualResponse)

        val result = connector.withIndividualNino(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Left(ServiceUnavailableError)
      }

    }

    "when calling withOrganisationUtr" - {

      "return 200 and a registration response when organisation is matched by utr" in {

        stubResponse("/organisation/utr", OK, withIDOrganisationResponse)

        val result = connector.withOrganisationUtr(registrationWithOrganisationIDPayload)
        result.value.futureValue mustBe Right(registrationWithIDOrganisationResponse)
      }

      "return 404 and NotFoundError when there is no match" in {

        stubResponse("/organisation/utr", NOT_FOUND, withIDOrganisationResponse)

        val result = connector.withOrganisationUtr(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Left(NotFoundError)
      }

      "return 503 and ServiceUnavailableError when remote is unavailable " in {

        stubResponse("/organisation/utr", SERVICE_UNAVAILABLE, withIDOrganisationResponse)

        val result = connector.withOrganisationUtr(registrationWithOrganisationIDPayload)
        result.value.futureValue mustBe Left(ServiceUnavailableError)
      }
    }

    "when calling withIndividualNoId" - {

      "return 200 and a registration response when individual has no id" in {

        stubResponse("/individual/noId", OK, withoutIDResponse)

        val result: Future[Either[ApiError, SafeId]] = connector.withIndividualNoId(registrationWithoutIndividualIDPayload)
        result.futureValue mustBe Right(safeId)
      }

      "return 404 and NotFoundError when there is no match" in {

        stubResponse("/individual/noId", NOT_FOUND, withoutIDResponse)

        val result = connector.withIndividualNoId(registrationWithoutIndividualIDPayload)
        result.futureValue mustBe Left(NotFoundError)
      }

      "return 503 and ServiceUnavailableError when remote is unavailable " in {

        stubResponse("/individual/noId", SERVICE_UNAVAILABLE, withoutIDResponse)

        val result = connector.withIndividualNoId(registrationWithoutIndividualIDPayload)
        result.futureValue mustBe Left(ServiceUnavailableError)
      }
    }

    "when calling withOrganisationNoId" - {

      "return 200 and a registration response when organisation has no id" in {

        stubResponse("/organisation/noId", OK, withoutIDResponse)

        val result = connector.withOrganisationNoId(registrationWithoutOrganisationIDPayload)
        result.futureValue mustBe Right(safeId)
      }

      "return 404 and NotFoundError when there is no match" in {

        stubResponse("/organisation/noId", NOT_FOUND, withoutIDResponse)

        val result = connector.withOrganisationNoId(registrationWithoutOrganisationIDPayload)
        result.futureValue mustBe Left(NotFoundError)
      }

      "return 503 and ServiceUnavailableError when remote is unavailable " in {

        stubResponse("/organisation/noId", SERVICE_UNAVAILABLE, withoutIDResponse)

        val result = connector.withOrganisationNoId(registrationWithoutOrganisationIDPayload)
        result.futureValue mustBe Left(ServiceUnavailableError)
      }
    }
  }

  private def stubResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$registrationUrl$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

}
