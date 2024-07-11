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
import cats.data.EitherT
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, put, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.FrontendAppConfig
import generators.Generators
import helpers.WireMockServerHandler
import models.IdentifierType._
import models.enrolment.{SubscriptionInfo, Verifier}
import models.error.ApiError
import models.error.ApiError.{ServiceUnavailableError, UnableToCreateEnrolmentError}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxEnrolmentsConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  "TaxEnrolmentsConnector" - {

    lazy val application: Application = new GuiceApplicationBuilder()
      .configure(
        conf = "microservice.services.tax-enrolments.port" -> server.port()
      )
      .build()

    lazy val connector: TaxEnrolmentsConnector = application.injector.instanceOf[TaxEnrolmentsConnector]

    lazy val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

    lazy val enrolmentKey = config.enrolmentKey

    "createEnrolment" - {

      "must return status as 204 for successful Tax Enrolment call" in {
        forAll(validPostCodes, validSubscriptionID, validAbroadFlag) {
          (postCode, subID, abroadFlag) =>
            val enrolmentInfo = SubscriptionInfo(postCode = Some(postCode), abroadFlag = Some(abroadFlag), id = subID)

            stubResponseForPutRequest(s"/tax-enrolments/service/$enrolmentKey/enrolment", NO_CONTENT)

            val result: EitherT[Future, ApiError, Int] = connector.createEnrolment(enrolmentInfo)
            result.value.futureValue mustBe Right(NO_CONTENT)
        }
      }

      "must return status as 400 and BadRequest error" in {
        forAll(validPostCodes, validSubscriptionID, validAbroadFlag) {
          (postCode, subID, abroadFlag) =>
            val enrolmentInfo = SubscriptionInfo(postCode = Some(postCode), abroadFlag = Some(abroadFlag), id = subID)
            stubResponseForPutRequest(s"/tax-enrolments/service/$enrolmentKey/enrolment", BAD_REQUEST)

            val result = connector.createEnrolment(enrolmentInfo)
            result.value.futureValue mustBe Left(UnableToCreateEnrolmentError)
        }
      }

      "must return status ServiceUnavailable Error" in {
        forAll(validPostCodes, validSubscriptionID, validAbroadFlag) {
          (postCode, subID, abroadFlag) =>
            val enrolmentInfo = SubscriptionInfo(postCode = Some(postCode), abroadFlag = Some(abroadFlag), id = subID)
            stubResponseForPutRequest(s"/tax-enrolments/service/$enrolmentKey/enrolment", INTERNAL_SERVER_ERROR)

            val result = connector.createEnrolment(enrolmentInfo)
            result.value.futureValue mustBe Left(ServiceUnavailableError)
        }
      }
    }

    "createEnrolmentRequest" - {
      "must return correct EnrolmentRequest when abroadFlag provided as verifier" in {
        forAll(validSubscriptionID, validAbroadFlag) {
          (subID, abroadFlag) =>
            val enrolmentInfo = SubscriptionInfo(abroadFlag = Some(abroadFlag), id = subID)

            val expectedVerifiers = Seq(Verifier(ABROADFLAG, enrolmentInfo.abroadFlag.get))

            enrolmentInfo.convertToEnrolmentRequest.verifiers mustBe expectedVerifiers
        }
      }

      "must return correct EnrolmentRequest when postCode provided as verifier" in {
        forAll(validPostCodes, validSubscriptionID) {
          (postCode, subID) =>
            val enrolmentInfo = SubscriptionInfo(postCode = Some(postCode), id = subID)

            val expectedVerifiers = Seq(Verifier(POSTCODE, enrolmentInfo.postCode.get))

            enrolmentInfo.convertToEnrolmentRequest.verifiers mustBe expectedVerifiers
        }
      }
    }
  }

  private def stubResponseForPutRequest(expectedUrl: String, expectedStatus: Int): StubMapping =
    server.stubFor(
      put(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )

}
