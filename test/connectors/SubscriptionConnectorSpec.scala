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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.SubscriptionID
import models.error.ApiError
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, NotFoundError, ServiceUnavailableError, UnableToCreateEMTPSubscriptionError}
import models.subscription.request.{CreateSubscriptionRequest, ReadSubscriptionRequest}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  private val seqOfCodes                    = Seq(400, 404, 403, 500, 501, 502, 503, 504)
  lazy val connector: SubscriptionConnector = application.injector.instanceOf[SubscriptionConnector]
  private val subscriptionUrl               = "/crs-fatca-registration/subscription"
  private val errorCodes: Gen[Int]          = Gen.oneOf(seqOfCodes)

  "SubscriptionConnector" - {
    "readSubscription" - {
      "must return SubscriptionID for individual subscription" in {
        val request          = arbitrary[ReadSubscriptionRequest].sample.value
        val expectedResponse = Some(SubscriptionID("Subscription 123"))

        val subscriptionResponse = Json.obj(
          "success" -> Json.obj(
            "processingDate" -> "2020-01-01T00:00:00Z",
            "subscriptionId" -> "Subscription 123",
            "gbUser"         -> true,
            "primaryContact" -> Json.obj(
              "individual" -> Json.obj(
                "firstName" -> "Primary",
                "lastName"  -> "Contact"
              ),
              "email"  -> "test1@example.com",
              "phone"  -> "0191 498 0001",
              "mobile" -> "0770 090 0001"
            ),
            "secondaryContact" -> Json.obj(
              "individual" -> Json.obj(
                "firstName" -> "Secondary",
                "lastName"  -> "Contact"
              ),
              "email"  -> "test2@example.com",
              "phone"  -> "0191 498 0002",
              "mobile" -> "0770 090 0002"
            )
          )
        )

        stubPostResponse("/read-subscription", OK, subscriptionResponse.toString())

        val result: Future[Option[SubscriptionID]] = connector.readSubscription(request)
        result.futureValue mustBe expectedResponse
      }

      "must return SubscriptionID for organisation subscription" in {
        val request          = arbitrary[ReadSubscriptionRequest].sample.value
        val expectedResponse = Some(SubscriptionID("Subscription 123"))

        val subscriptionResponse = Json.obj(
          "success" -> Json.obj(
            "processingDate" -> "2020-01-01T00:00:00Z",
            "subscriptionId" -> "Subscription 123",
            "tradingName"    -> "Test Business",
            "gbUser"         -> true,
            "primaryContact" -> Json.obj(
              "organisation" -> Json.obj(
                "name" -> "Department 1"
              ),
              "email"  -> "test1@example.com",
              "phone"  -> "0191 498 0001",
              "mobile" -> "0770 090 0001"
            ),
            "secondaryContact" -> Json.obj(
              "organisation" -> Json.obj(
                "name" -> "Department 1"
              ),
              "email"  -> "test2@example.com",
              "phone"  -> "0191 498 0002",
              "mobile" -> "0770 090 0002"
            )
          )
        )

        stubPostResponse("/read-subscription", OK, subscriptionResponse.toString())

        val result: Future[Option[SubscriptionID]] = connector.readSubscription(request)
        result.futureValue mustBe expectedResponse
      }

      "must return None when read subscription fails" in {
        val request = arbitrary[ReadSubscriptionRequest].sample.value

        val subscriptionResponse = Json.obj(
          "errorDetail" -> Json.obj(
            "errorCode"    -> "001",
            "errorMessage" -> "Regime Missing or Invalid",
            "source"       -> "ETMP",
            "sourceFaultDetail" -> Json.obj(
              "detail" -> Json.arr("Regime missing or invalid")
            ),
            "timestamp"     -> "2023-08-31T13:00:21.655Z",
            "correlationId" -> "d60de98c-f499-47f5-b2d6-e80966e8d19e"
          )
        )

        stubPostResponse("/read-subscription", OK, subscriptionResponse.toString())

        val result = connector.readSubscription(request)
        result.futureValue mustBe None
      }
    }

    "createSubscription" - {
      "must return SubscriptionResponse for valid input request" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value
        val expectedResponse          = SubscriptionID("Subscription 123")

        val subscriptionResponse = Json.obj(
          "success" -> Json.obj(
            "processingDate" -> "2020-01-01T00:00:00Z",
            "subscriptionId" -> "Subscription 123"
          )
        )

        stubPostResponse("/create-subscription", OK, subscriptionResponse.toString())

        val result = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Right(expectedResponse)
      }

      "must return UnableToCreateEMTPSubscriptionError for invalid response" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value

        val subscriptionResponse = Json.obj(
          "failure" -> Json.obj(
            "processingDate" -> "2020-01-01T00:00:00Z",
            "subscriptionId" -> "Subscription 123"
          )
        )

        stubPostResponse("/create-subscription", OK, subscriptionResponse.toString())

        val result: EitherT[Future, ApiError, SubscriptionID] = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Left(UnableToCreateEMTPSubscriptionError)
      }

      "must return NotFoundError for not found response" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value

        stubPostResponse("/create-subscription", NOT_FOUND, "")

        val result: EitherT[Future, ApiError, SubscriptionID] = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Left(NotFoundError)
      }

      "must return BadRequestError for bad request response" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value

        stubPostResponse("/create-subscription", BAD_REQUEST, "")

        val result: EitherT[Future, ApiError, SubscriptionID] = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Left(BadRequestError)
      }

      "must return ServiceUnavailableError for service unavailable response" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value

        stubPostResponse("/create-subscription", SERVICE_UNAVAILABLE, "")

        val result: EitherT[Future, ApiError, SubscriptionID] = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Left(ServiceUnavailableError)
      }

      "must return DuplicateSubmissionError when tried to submit the same request" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value

        val subscriptionErrorResponse: String =
          s"""
             | {
             | "errorDetail": {
             |    "timestamp" : "2021-03-11T08:20:44Z",
             |    "correlationId": "c181e730-2386-4359-8ee0-f911d6e5f3bc",
             |    "errorCode": "409",
             |    "errorMessage": "Duplicate submission",
             |    "source": "Back End",
             |    "sourceFaultDetail": {
             |      "detail": [
             |        "Duplicate submission"
             |      ]
             |    }
             |  }
             |  }
             |""".stripMargin

        stubPostResponse("/create-subscription", CONFLICT, subscriptionErrorResponse)

        val result = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Left(DuplicateSubmissionError)
      }

      "must return UnableToCreateEMTPSubscriptionError when submission to backend fails" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value
        val errorCode                 = errorCodes.sample.value

        val subscriptionErrorResponse: String =
          s"""
               | {
               | "errorDetail": {
               |    "timestamp": "2016-08-16T18:15:41Z",
               |    "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
               |    "errorCode": "$errorCode",
               |    "errorMessage": "Internal error",
               |    "source": "Internal error"
               |  }
               |  }
               |""".stripMargin

        stubPostResponse("/create-subscription", errorCode, subscriptionErrorResponse)

        val result = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Left(getApiError(errorCode))
      }
    }

  }

  def getApiError(status: Int): ApiError =
    status match {
      case NOT_FOUND =>
        NotFoundError
      case BAD_REQUEST =>
        BadRequestError
      case SERVICE_UNAVAILABLE =>
        ServiceUnavailableError
      case _ =>
        UnableToCreateEMTPSubscriptionError
    }

  private def stubPostResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$subscriptionUrl$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

}
