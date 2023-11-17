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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.SubscriptionID
import models.enrolment.GroupIds
import models.error.ApiError.{EnrolmentExistsError, MalformedError}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentStoreProxyConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.enrolment-store-proxy.port" -> server.port()
    )
    .build()

  lazy val connector: EnrolmentStoreProxyConnector = application.injector.instanceOf[EnrolmentStoreProxyConnector]
  val enrolmentStoreProxyUrl                       = "/enrolment-store-proxy/enrolment-store/enrolments"
  val enrolmentStoreProxy200Url                    = "/enrolment-store-proxy/enrolment-store/enrolments/HMRC-FATCA-ORG~FATCAID~xxx200/groups"
  val enrolmentStoreProxy204Url                    = "/enrolment-store-proxy/enrolment-store/enrolments/HMRC-FATCA-ORG~FATCAID~xxx204/groups"

  val enrolmentStoreProxyResponseJson: String =
    """{
      |  "principalGroupIds": [
      |    "ABCEDEFGI1234567",
      |    "ABCEDEFGI1234568"
      |  ],
      |  "delegatedGroupIds": [
      |    "ABCEDEFGI1234567",
      |    "ABCEDEFGI1234568"
      |  ]
      |}""".stripMargin

  val enrolmentStoreProxyResponseNoPrincipalIdJson: String =
    """{
      |  "principalGroupIds": []
      |}""".stripMargin

  "EnrolmentStoreProxyConnector" - {
    "when calling enrolmentStatus" - {

      "return 200 and a enrolmentStatus response when already enrolment exists" in {
        val subscriptionID = SubscriptionID("xxx200")
        val groupIds       = Json.parse(enrolmentStoreProxyResponseJson).as[GroupIds]
        stubResponse(enrolmentStoreProxy200Url, OK, enrolmentStoreProxyResponseJson)
        val result = connector.enrolmentStatus(subscriptionID)
        result.value.futureValue mustBe Left(EnrolmentExistsError(groupIds))
      }

      "return 204 and a enrolmentStatus response when no enrolment exists" in {
        val subscriptionID = SubscriptionID("xxx204")
        stubResponse(enrolmentStoreProxy204Url, NO_CONTENT, "")

        val result = connector.enrolmentStatus(subscriptionID)
        result.value.futureValue mustBe Right(())
      }

      "return 204 enrolmentStatus response when principalGroupId is empty seq" in {
        val subscriptionID = SubscriptionID("xxx204")
        stubResponse(enrolmentStoreProxy204Url, OK, enrolmentStoreProxyResponseNoPrincipalIdJson)
        val result = connector.enrolmentStatus(subscriptionID)
        result.value.futureValue mustBe Right(())
      }

      "return 404 and a enrolmentStatus response when invalid or malfromed URL" in {
        val subscriptionID = SubscriptionID("xxx404")
        stubResponse(enrolmentStoreProxy204Url, NOT_FOUND, "")

        val result = connector.enrolmentStatus(subscriptionID)
        result.value.futureValue mustBe Left(MalformedError(NOT_FOUND))
      }

    }

  }

  private def stubResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      get(urlEqualTo(expectedEndpoint))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

}
