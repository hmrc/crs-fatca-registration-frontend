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

import base.TestValues
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import config.FrontendAppConfig
import models.audit.{CreateRegistrationAuditRequest, UpdateContactDetailsAuditRequest}
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.ExecutionContext.Implicits.global

class AuditConnectorSpec
    extends AnyFreeSpec
    with Matchers
    with BeforeAndAfterAll with TestValues {

  private val wireMockServer =
    new WireMockServer(options().dynamicPort())

  implicit val hc: HeaderCarrier =
    HeaderCarrier()

  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  private val createAuditRequest =
    CreateRegistrationAuditRequest(
      affinityType = "Organisation",
      registeringAs = "Organisation",
      registrationType = "OrgWithID",
      idType = "UTR",
      idValue = "1234567890",
      tradingName = Some("Trading Name"),
      businessName = Some("Business Name"),
      addressLine1 = Some("1 Test Street"),
      addressLine2 = Some("Test Area"),
      city = Some("London"),
      region = Some("Greater London"),
      postcode = Some("AA1 1AA"),
      country = Some("GB"),
      uprn = Some("123456789"),
      dateOfBirth = None,
      firstContactName = "First Contact",
      firstContactEmail = "first@example.com",
      firstContactTelephone = Some("07123456789"),
      secondContactName = Some("Second Contact"),
      secondContactEmail = Some("second@example.com"),
      secondContactTelephone = Some("07987654321"),
      fatcaId = "FATCA123456"
    )

  private val updateContactDetailsAuditRequest =
    UpdateContactDetailsAuditRequest(
      fatcaId = subscriptionId.value,
      affinityType = AffinityGroup.Individual.toString,
      email = Some("new@test.com")
    )

  "sendCreateRegistration" - {

    "must POST the CreateRegistration audit request to the backend" in {

      wireMockServer.stubFor(
        post(urlEqualTo("/audit/create-registration"))
          .willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
          )
      )

      val application =
        new GuiceApplicationBuilder()
          .build()

      running(application) {

        val mockConfig =
          mock[FrontendAppConfig]

        when(mockConfig.businessMatchingUrl)
          .thenReturn(wireMockServer.baseUrl())

        val http =
          application.injector.instanceOf[HttpClientV2]

        val connector =
          new AuditConnector(
            http = http,
            config = mockConfig
          )

        val result =
          await(
            connector.sendCreateRegistration(createAuditRequest)
          )

        result.status mustBe NO_CONTENT

        wireMockServer.verify(
          postRequestedFor(
            urlEqualTo("/audit/create-registration")
          ).withRequestBody(
            equalToJson(
              Json.stringify(
                Json.toJson(createAuditRequest)
              )
            )
          )
        )
      }
    }

    "must fail when the backend returns an error response" in {

      wireMockServer.stubFor(
        post(urlEqualTo("/audit/create-registration"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val application =
        new GuiceApplicationBuilder()
          .build()

      running(application) {

        val mockConfig =
          mock[FrontendAppConfig]

        when(mockConfig.businessMatchingUrl)
          .thenReturn(wireMockServer.baseUrl())

        val connector =
          new AuditConnector(
            http = application.injector.instanceOf[HttpClientV2],
            config = mockConfig
          )

        val exception =
          intercept[UpstreamErrorResponse] {
            await(
              connector.sendCreateRegistration(createAuditRequest)
            )
          }

        exception.statusCode mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "sendUpdateContactDetails" - {

    val updateContactDetailsAuditUrl = "/audit/update-contact-details"

    "must POST the CreateRegistration audit request to the backend" in {

      wireMockServer.stubFor(
        post(urlEqualTo(updateContactDetailsAuditUrl))
          .willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
          )
      )

      val application =
        new GuiceApplicationBuilder()
          .build()

      running(application) {

        val mockConfig =
          mock[FrontendAppConfig]

        when(mockConfig.businessMatchingUrl)
          .thenReturn(wireMockServer.baseUrl())

        val http =
          application.injector.instanceOf[HttpClientV2]

        val connector =
          new AuditConnector(
            http = http,
            config = mockConfig
          )

        val result =
          await(
            connector.sendUpdateContactDetails(updateContactDetailsAuditRequest)
          )

        result.status mustBe NO_CONTENT

        wireMockServer.verify(
          postRequestedFor(
            urlEqualTo(updateContactDetailsAuditUrl)
          ).withRequestBody(
            equalToJson(
              Json.stringify(
                Json.toJson(updateContactDetailsAuditRequest)
              )
            )
          )
        )
      }
    }

    "must fail when the backend returns an error response" in {

      wireMockServer.stubFor(
        post(urlEqualTo(updateContactDetailsAuditUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val application =
        new GuiceApplicationBuilder()
          .build()

      running(application) {

        val mockConfig =
          mock[FrontendAppConfig]

        when(mockConfig.businessMatchingUrl)
          .thenReturn(wireMockServer.baseUrl())

        val connector =
          new AuditConnector(
            http = application.injector.instanceOf[HttpClientV2],
            config = mockConfig
          )

        val exception =
          intercept[UpstreamErrorResponse] {
            await(
              connector.sendUpdateContactDetails(updateContactDetailsAuditRequest)
            )
          }

        exception.statusCode mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
