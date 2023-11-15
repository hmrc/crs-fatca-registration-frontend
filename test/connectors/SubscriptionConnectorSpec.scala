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
import models.subscription.request.{CreateSubscriptionRequest, DisplaySubscriptionRequest}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder

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
      "must return SubscriptionID for valid input request for CRS-FATCA" in {
        val displaySubscriptionRequest = arbitrary[DisplaySubscriptionRequest].sample.value
        val expectedResponse           = SubscriptionID("subscriptionID")

        val subscriptionResponse: String =
          s"""
             |{
             | "displaySubscriptionResponse": {
             |   "responseCommon": {
             |     "status": "OK",
             |     "processingDate": "2020-09-23T16:12:11Z"
             |   },
             |   "responseDetail": {
             |      "subscriptionID": "subscriptionID"
             |   }
             | }
             |}""".stripMargin

        stubPostResponse("/read-subscription", OK, subscriptionResponse)

        val result: Future[Option[SubscriptionID]] = connector.readSubscription(displaySubscriptionRequest)
        result.futureValue.value mustBe expectedResponse
      }

      "must return None for invalid json response" in {
        val displaySubscriptionRequest = arbitrary[DisplaySubscriptionRequest].sample.value

        val subscriptionResponse: String =
          s"""
             |{
             | "displaySubscriptionResponse": {
             |   "responseCommon": {
             |     "status": "OK",
             |     "processingDate": "2020-09-23T16:12:11Z"
             |   },
             |   "responseDetail": {}
             | }
             |}""".stripMargin

        stubPostResponse("/read-subscription", OK, subscriptionResponse)

        val result = connector.readSubscription(displaySubscriptionRequest)
        result.futureValue mustBe None
      }

      "must return None when read subscription fails" in {
        val displaySubscriptionRequest = arbitrary[DisplaySubscriptionRequest].sample.value
        val errorCode                  = errorCodes.sample.value

        val subscriptionErrorResponse: String =
          s"""
                 | "errorDetail": {
                 |    "timestamp": "2016-08-16T18:15:41Z",
                 |    "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
                 |    "errorCode": "$errorCode",
                 |    "errorMessage": "Internal error",
                 |    "source": "Internal error"
                 |  }
                 |""".stripMargin

        stubPostResponse("/read-subscription", errorCode, subscriptionErrorResponse)

        val result = connector.readSubscription(displaySubscriptionRequest)
        result.futureValue mustBe None
      }
    }

    "createSubscription" - {
      "must return SubscriptionResponse for valid input request" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value
        val expectedResponse          = SubscriptionID("subscriptionID")

        val subscriptionResponse: String =
          s"""
             |{
             | "createSubscriptionResponse": {
             |   "responseCommon": {
             |     "status": "OK",
             |     "processingDate": "2020-09-23T16:12:11Z"
             |   },
             |   "responseDetail": {
             |      "subscriptionID": "subscriptionID"
             |   }
             | }
             |}""".stripMargin

        stubPostResponse("/create-subscription", OK, subscriptionResponse)

        val result = connector.createSubscription(createSubscriptionRequest)
        result.value.futureValue mustBe Right(expectedResponse)
      }

      "must return UnableToCreateEMTPSubscriptionError for invalid response" in {
        val createSubscriptionRequest = arbitrary[CreateSubscriptionRequest].sample.value

        val subscriptionResponse: String =
          s"""
             |{
             | "createSubscriptionResponse": {
             |   "responseCommon": {
             |     "status": "OK",
             |     "processingDate": "2020-09-23T16:12:11Z"
             |   },
             |   "responseDetail1": {
             |      "subscriptionID": "subscriptionID"
             |   }
             | }
             |}""".stripMargin

        stubPostResponse("/create-subscription", OK, subscriptionResponse)

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
