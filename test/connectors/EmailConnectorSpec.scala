/*
 * Copyright 2026 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, postRequestedFor, urlEqualTo, verify}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.ModelGenerators
import helpers.WireMockServerHandler
import models.email.{EmailNotSent, EmailSent, EmailStatus, SendEmailRequest}
import models.error.ApiError
import models.error.ApiError.InternalServerError
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailConnectorSpec extends SpecBase with WireMockServerHandler with ModelGenerators with ScalaCheckPropertyChecks {

  private val emailEndpoint = "/hmrc/email"

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(
      conf =
        "microservice.services.email.host" -> "localhost",
      "microservice.services.email.port" -> server.port()
    )
    .build()

  lazy val connector: EmailConnector = application.injector.instanceOf[EmailConnector]

  implicit override val hc: HeaderCarrier = HeaderCarrier()

  private val testEmailAddress   = "test@example.com"
  private val testTemplateName   = "crs_fatca_registration_submitted"
  private val testTemplateParams = Map("name" -> "Jane Doe")

  "EmailConnector" - {
    "sendEmail" - {
      "must return Right(EmailSent) when the email service responds with ACCEPTED" in {
        stubPostResponse(ACCEPTED, "")

        val expectedPayload = Json.toJson(
          SendEmailRequest(
            to = List(testEmailAddress),
            templateId = testTemplateName,
            parameters = testTemplateParams,
            force = false
          )
        )

        val result: EitherT[Future, ApiError, EmailStatus] =
          connector.sendEmail(testEmailAddress, testTemplateName, testTemplateParams)

        result.value.futureValue mustBe Right(EmailSent)

        server.verify(
          postRequestedFor(urlEqualTo(emailEndpoint))
            .withRequestBody(equalToJson(expectedPayload.toString()))
        )
      }

      "must return Right(EmailNotSent) when the email service responds with a non-ACCEPTED status" in {
        val errorStatuses = Seq(BAD_REQUEST, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE)

        for (status <- errorStatuses) {
          server.resetAll()
          stubPostResponse(status, "")

          val result = connector.sendEmail(testEmailAddress, testTemplateName, testTemplateParams)
          result.value.futureValue mustBe Right(EmailNotSent)
        }
      }

      "must return Left(InternalServerError) when connecting to the email service fails" in {
        server.stop()

        val result = connector.sendEmail(testEmailAddress, testTemplateName, testTemplateParams)
        result.value.futureValue mustBe Left(InternalServerError)

        server.start()
      }
    }
  }

  private def stubPostResponse(expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(emailEndpoint))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

}
