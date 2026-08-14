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

package services

import base.SpecBase
import connectors.AuditConnector
import generators.{ModelGenerators, UserAnswersGenerator}
import models.audit.AuditResult.{AuditFailed, AuditNotSent, AuditSent}
import models.audit.CreateRegistrationAuditRequest
import models.{Address, Country, Name, ReporterType, SubscriptionID, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{never, reset, verify, when}
import org.scalacheck.Arbitrary
import pages._
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec
    extends SpecBase
    with ModelGenerators
    with UserAnswersGenerator {

  private val mockAuditConnector: AuditConnector =
    mock[AuditConnector]

  private val service =
    new AuditService(mockAuditConnector)

  private val subscriptionId =
    SubscriptionID("FATCA123456")

  private val individualName =
    Name("John", "Smith")

  private val individualDateOfBirth =
    LocalDate.of(1996, 3, 8)

  private val individualEmail =
    "individual@test.com"

  private val individualPhone =
    "441234567898"

  private val organisationContactName =
    "Test Contact"

  private val organisationContactEmail =
    "contact@test.com"

  private val organisationContactPhone =
    "441234567899"

  override def beforeEach(): Unit = {
    reset(mockAuditConnector)
    super.beforeEach()
  }

  private def sampleUserAnswers(
    arbitrary: Arbitrary[UserAnswers]
  ): UserAnswers =
    arbitrary.arbitrary.sample.getOrElse {
      fail("Unable to generate UserAnswers for AuditServiceSpec")
    }

  private def sendAndCapture(
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): CreateRegistrationAuditRequest = {

    when(
      mockAuditConnector
        .sendCreateRegistration(any())(any(), any())
    ).thenReturn(
      Future.successful(
        HttpResponse(
          status = NO_CONTENT,
          body = ""
        )
      )
    )

    service
      .sendCreateRegistration(
        userAnswers = userAnswers,
        subscriptionId = subscriptionId,
        affinityGroup = affinityGroup
      )
      .futureValue mustBe AuditSent

    val captor =
      ArgumentCaptor.forClass(
        classOf[CreateRegistrationAuditRequest]
      )

    verify(mockAuditConnector)
      .sendCreateRegistration(
        captor.capture()
      )(any(), any())

    captor.getValue
  }

  private def individualWithIdUserAnswers: UserAnswers =
    emptyUserAnswers
      .withPage(
        ReporterTypePage,
        ReporterType.Individual
      )
      .withPage(
        IndDoYouHaveNINumberPage,
        true
      )
      .withPage(
        IndWhatIsYourNINumberPage,
        Nino("CC123456C")
      )
      .withPage(
        IndWhatIsYourNamePage,
        individualName
      )
      .withPage(
        IndDateOfBirthPage,
        individualDateOfBirth
      )
      .withPage(
        IndContactEmailPage,
        individualEmail
      )
      .withPage(
        IndContactHavePhonePage,
        true
      )
      .withPage(
        IndContactPhonePage,
        individualPhone
      )

  private def individualWithoutIdUserAnswers(
    address: Address,
    livesInUK: Boolean
  ): UserAnswers = {

    val userAnswers =
      emptyUserAnswers
        .withPage(
          ReporterTypePage,
          ReporterType.Individual
        )
        .withPage(
          IndDoYouHaveNINumberPage,
          false
        )
        .withPage(
          WhatIsYourNamePage,
          individualName
        )
        .withPage(
          DateOfBirthWithoutIdPage,
          individualDateOfBirth
        )
        .withPage(
          IndWhereDoYouLivePage,
          livesInUK
        )
        .withPage(
          IndContactEmailPage,
          individualEmail
        )
        .withPage(
          IndContactHavePhonePage,
          true
        )
        .withPage(
          IndContactPhonePage,
          individualPhone
        )

    if (livesInUK) {
      userAnswers.withPage(
        IndUKAddressWithoutIdPage,
        address
      )
    } else {
      userAnswers.withPage(
        IndNonUKAddressWithoutIdPage,
        address
      )
    }
  }

  private def organisationWithoutIdUserAnswers(
    address: Address
  ): UserAnswers =
    emptyUserAnswers
      .withPage(
        ReporterTypePage,
        ReporterType.LimitedCompany
      )
      .withPage(
        BusinessNameWithoutIDPage,
        "Test Business"
      )
      .withPage(
        BusinessTradingNameWithoutIDPage,
        "Test Trading Name"
      )
      .withPage(
        NonUKBusinessAddressWithoutIDPage,
        address
      )
      .withPage(
        ContactNamePage,
        organisationContactName
      )
      .withPage(
        ContactEmailPage,
        organisationContactEmail
      )
      .withPage(
        ContactPhonePage,
        organisationContactPhone
      )

  "AuditService" - {

    "sendCreateRegistration" - {

      "must send the correct audit event for an individual with ID" in {

        val userAnswers =
          individualWithIdUserAnswers

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Individual
          )

        result.affinityType mustBe
          AffinityGroup.Individual.toString

        result.registeringAs mustBe
          "Individual"

        result.registrationType mustBe
          "IndividualWithID"

        result.idType mustBe
          "NINO"

        result.idValue mustBe
          "CC123456C"

        result.firstContactName mustBe
          individualName.fullName

        result.firstContactEmail mustBe
          individualEmail

        result.firstContactTelephone mustBe
          Some(individualPhone)

        result.dateOfBirth mustBe
          Some("1996-03-08")

        result.addressLine1 mustBe None
        result.addressLine2 mustBe None
        result.city mustBe None
        result.region mustBe None
        result.postcode mustBe None
        result.country mustBe None

        result.fatcaId mustBe
          subscriptionId.value
      }

      "must send the correct audit event for an individual without ID" in {

        val address =
          Address(
            addressLine1 = "1 Test Street",
            addressLine2 = Some("Test Area"),
            addressLine3 = "London",
            addressLine4 = None,
            postCode = Some("AA1 1AA"),
            country = Country.GB
          )

        val userAnswers =
          individualWithoutIdUserAnswers(
            address = address,
            livesInUK = true
          )

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Individual
          )

        result.affinityType mustBe
          AffinityGroup.Individual.toString

        result.registeringAs mustBe
          "Individual"

        result.registrationType mustBe
          "IndividualWithoutID"

        result.idType mustBe
          "NotProvided"

        result.idValue mustBe
          "NotProvided"

        result.firstContactName mustBe
          individualName.fullName

        result.firstContactEmail mustBe
          individualEmail

        result.firstContactTelephone mustBe
          Some(individualPhone)

        result.dateOfBirth mustBe
          Some("1996-03-08")

        result.addressLine1 mustBe
          Some("1 Test Street")

        result.addressLine2 mustBe
          Some("Test Area")

        result.city mustBe
          Some("London")

        result.region mustBe
          None

        result.postcode mustBe
          Some("AA1 1AA")

        result.country mustBe
          Some("GB")

        result.fatcaId mustBe
          subscriptionId.value
      }

      "must send the correct audit event for an organisation with ID" in {

        val generatedUserAnswers =
          sampleUserAnswers(orgWithId)

        val utr =
          generatedUserAnswers
            .get(WhatIsYourUTRPage)
            .value

        val userAnswers =
          emptyUserAnswers
            .withPage(
              ReporterTypePage,
              ReporterType.LimitedCompany
            )
            .withPage(
              WhatIsYourUTRPage,
              utr
            )
            .withPage(
              ContactNamePage,
              organisationContactName
            )
            .withPage(
              ContactEmailPage,
              organisationContactEmail
            )
            .withPage(
              ContactPhonePage,
              organisationContactPhone
            )

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Organisation
          )

        result.affinityType mustBe
          AffinityGroup.Organisation.toString

        result.registeringAs mustBe
          "Organisation"

        result.registrationType mustBe
          "OrgWithID"

        result.idType mustBe
          "UTR"

        result.idValue mustBe
          utr.uniqueTaxPayerReference

        result.businessName mustBe
          None

        result.firstContactName mustBe
          organisationContactName

        result.firstContactEmail mustBe
          organisationContactEmail

        result.firstContactTelephone mustBe
          Some(organisationContactPhone)

        result.addressLine1 mustBe None
        result.addressLine2 mustBe None
        result.city mustBe None
        result.region mustBe None
        result.postcode mustBe None
        result.country mustBe None

        result.fatcaId mustBe
          subscriptionId.value
      }

      "must send the correct audit event for an organisation without ID" in {

        val address =
          Address(
            addressLine1 = "1 Business Street",
            addressLine2 = Some("Business Area"),
            addressLine3 = "Paris",
            addressLine4 = Some("Ile-de-France"),
            postCode = Some("75001"),
            country = Country(
              code = "FR",
              description = "France"
            )
          )

        val userAnswers =
          organisationWithoutIdUserAnswers(address)

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Organisation
          )

        result.affinityType mustBe
          AffinityGroup.Organisation.toString

        result.registeringAs mustBe
          "Organisation"

        result.registrationType mustBe
          "OrgWithoutID"

        result.idType mustBe
          "NotProvided"

        result.idValue mustBe
          "NotProvided"

        result.businessName mustBe
          Some("Test Business")

        result.tradingName mustBe
          Some("Test Trading Name")

        result.addressLine1 mustBe
          Some("1 Business Street")

        result.addressLine2 mustBe
          Some("Business Area")

        result.city mustBe
          Some("Paris")

        result.region mustBe
          Some("Ile-de-France")

        result.postcode mustBe
          Some("75001")

        result.country mustBe
          Some("FR")

        result.firstContactName mustBe
          organisationContactName

        result.firstContactEmail mustBe
          organisationContactEmail

        result.firstContactTelephone mustBe
          Some(organisationContactPhone)

        result.fatcaId mustBe
          subscriptionId.value
      }

      "must send CTAutomatched when an auto matched UTR is present" in {

        val generatedUserAnswers =
          sampleUserAnswers(orgWithId)

        val utr =
          generatedUserAnswers
            .get(WhatIsYourUTRPage)
            .value

        val userAnswers =
          emptyUserAnswers
            .withPage(
              ReporterTypePage,
              ReporterType.LimitedCompany
            )
            .withPage(
              AutoMatchedUTRPage,
              utr
            )
            .withPage(
              ContactNamePage,
              organisationContactName
            )
            .withPage(
              ContactEmailPage,
              organisationContactEmail
            )
            .withPage(
              ContactPhonePage,
              organisationContactPhone
            )

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Organisation
          )

        result.affinityType mustBe
          AffinityGroup.Organisation.toString

        result.registeringAs mustBe
          "Organisation"

        result.registrationType mustBe
          "CTAutomatched"

        result.idType mustBe
          "UTR"

        result.idValue mustBe
          utr.uniqueTaxPayerReference

        result.addressLine1 mustBe None
        result.addressLine2 mustBe None
        result.city mustBe None
        result.region mustBe None
        result.postcode mustBe None
        result.country mustBe None

        result.fatcaId mustBe
          subscriptionId.value
      }

      "must populate an individual UK manually entered address" in {

        val address =
          Address(
            addressLine1 = "1 Audit Street",
            addressLine2 = Some("Audit Area"),
            addressLine3 = "London",
            addressLine4 = Some("Greater London"),
            postCode = Some("AA1 1AA"),
            country = Country.GB
          )

        val userAnswers =
          individualWithoutIdUserAnswers(
            address = address,
            livesInUK = true
          )

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Individual
          )

        result.addressLine1 mustBe
          Some("1 Audit Street")

        result.addressLine2 mustBe
          Some("Audit Area")

        result.city mustBe
          Some("London")

        result.region mustBe
          Some("Greater London")

        result.postcode mustBe
          Some("AA1 1AA")

        result.country mustBe
          Some("GB")
      }

      "must populate an individual non-UK address" in {

        val address =
          Address(
            addressLine1 = "1 Rue Audit",
            addressLine2 = None,
            addressLine3 = "Paris",
            addressLine4 = None,
            postCode = Some("75001"),
            country = Country(
              code = "FR",
              description = "France"
            )
          )

        val userAnswers =
          individualWithoutIdUserAnswers(
            address = address,
            livesInUK = false
          )

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Individual
          )

        result.addressLine1 mustBe
          Some("1 Rue Audit")

        result.addressLine2 mustBe
          None

        result.city mustBe
          Some("Paris")

        result.region mustBe
          None

        result.postcode mustBe
          Some("75001")

        result.country mustBe
          Some("FR")
      }

      "must not fail when the audit connector fails" in {

        val address =
          Address(
            addressLine1 = "1 Business Street",
            addressLine2 = None,
            addressLine3 = "Paris",
            addressLine4 = None,
            postCode = Some("75001"),
            country = Country(
              code = "FR",
              description = "France"
            )
          )

        val userAnswers =
          organisationWithoutIdUserAnswers(address)

        when(
          mockAuditConnector
            .sendCreateRegistration(any())(any(), any())
        ).thenReturn(
          Future.failed(
            new RuntimeException(
              "Audit endpoint unavailable"
            )
          )
        )

        val result =
          service.sendCreateRegistration(
            userAnswers = userAnswers,
            subscriptionId = subscriptionId,
            affinityGroup = AffinityGroup.Organisation
          )

        result.futureValue mustBe AuditFailed

        verify(mockAuditConnector)
          .sendCreateRegistration(
            any()
          )(any(), any())
      }

      "must not call the connector when required audit information is missing" in {

        val result =
          service.sendCreateRegistration(
            userAnswers = emptyUserAnswers,
            subscriptionId = subscriptionId,
            affinityGroup = AffinityGroup.Individual
          )

        result.futureValue mustBe AuditNotSent

        verify(
          mockAuditConnector,
          never
        ).sendCreateRegistration(
          any()
        )(any(), any())
      }

      "must ignore empty optional string values" in {

        val address =
          Address(
            addressLine1 = "1 Business Street",
            addressLine2 = None,
            addressLine3 = "Paris",
            addressLine4 = None,
            postCode = Some("75001"),
            country = Country(
              code = "FR",
              description = "France"
            )
          )

        val userAnswers =
          organisationWithoutIdUserAnswers(address)
            .withPage(
              BusinessTradingNameWithoutIDPage,
              "   "
            )
            .withPage(
              ContactPhonePage,
              "   "
            )

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Organisation
          )

        result.tradingName mustBe None

        result.firstContactTelephone mustBe None
      }
    }
  }

}
