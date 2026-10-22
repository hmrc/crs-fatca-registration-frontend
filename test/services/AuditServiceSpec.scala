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
import models.audit.{CreateRegistrationAuditRequest, UpdateContactDetailsAuditRequest}
import models.subscription.request.{ContactInformation, IndividualDetails, OrganisationDetails, UpdateSubscriptionRequest}
import models.subscription.response.DisplaySubscriptionResponse
import models.{Address, Country, Name, ReporterType, SubscriptionID, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => mEq}
import org.mockito.MockitoSugar.{never, reset, verify, when}
import org.scalacheck.Arbitrary
import pages._
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
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

  // Keep the fixtures focused on the fields that the audit builders read.
  private def individualContact(
    email: String,
    phone: Option[String]
  ): ContactInformation =
    ContactInformation(
      IndividualDetails("John", "Smith"),
      email,
      phone
    )

  private def organisationContact(
    name: String,
    email: String,
    phone: Option[String]
  ): ContactInformation =
    ContactInformation(
      OrganisationDetails(name),
      email,
      phone
    )

  private def originalSubscription(
    primary: ContactInformation,
    secondary: Option[ContactInformation] = None
  ): DisplaySubscriptionResponse = {
    // No need to build an entire subscription response: the builder only reads these contacts.
    val response = org.mockito.Mockito.mock(
      classOf[DisplaySubscriptionResponse],
      org.mockito.Mockito.RETURNS_DEEP_STUBS
    )

    when(response.success.crfaSubscriptionDetails.primaryContact).thenReturn(primary)
    when(response.success.crfaSubscriptionDetails.secondaryContact).thenReturn(secondary)
    response
  }

  private def updatedSubscription(
    primary: ContactInformation,
    secondary: Option[ContactInformation] = None
  ): UpdateSubscriptionRequest = {
    val request = mock[UpdateSubscriptionRequest]
    when(request.primaryContact).thenReturn(primary)
    when(request.secondaryContact).thenReturn(secondary)
    request
  }

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
          ReporterType.Individual.toString

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
          ReporterType.Individual.toString

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
          ReporterType.LimitedCompany.toString

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
          ReporterType.LimitedCompany.toString

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
          ReporterType.LimitedCompany.toString

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

      "must send OrgWithID for a UK sole trader with a UTR" in {

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
              ReporterType.Sole
            )
            .withPage(
              RegisteredAddressInUKPage,
              true
            )
            .withPage(
              DoYouHaveUniqueTaxPayerReferencePage,
              true
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
            affinityGroup = AffinityGroup.Individual
          )

        result.affinityType mustBe
          AffinityGroup.Individual.toString

        result.registeringAs mustBe
          ReporterType.Sole.toString

        result.registrationType mustBe
          "OrgWithID"

        result.idType mustBe
          "UTR"

        result.idValue mustBe
          utr.uniqueTaxPayerReference

        result.firstContactName mustBe
          organisationContactName

        result.firstContactEmail mustBe
          organisationContactEmail

        result.firstContactTelephone mustBe
          Some(organisationContactPhone)
      }

      "must send IndividualWithID for a UK sole trader without a UTR who has a NINO" in {

        val userAnswers =
          emptyUserAnswers
            .withPage(
              ReporterTypePage,
              ReporterType.Sole
            )
            .withPage(
              RegisteredAddressInUKPage,
              true
            )
            .withPage(
              DoYouHaveUniqueTaxPayerReferencePage,
              false
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

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Organisation
          )

        result.affinityType mustBe
          AffinityGroup.Organisation.toString

        result.registeringAs mustBe
          ReporterType.Sole.toString

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
      }

      "must send IndividualWithoutID for a non-UK sole trader without a NINO" in {

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
          emptyUserAnswers
            .withPage(
              ReporterTypePage,
              ReporterType.Sole
            )
            .withPage(
              RegisteredAddressInUKPage,
              false
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
              false
            )
            .withPage(
              IndNonUKAddressWithoutIdPage,
              address
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

        val result =
          sendAndCapture(
            userAnswers = userAnswers,
            affinityGroup = AffinityGroup.Organisation
          )

        result.affinityType mustBe
          AffinityGroup.Organisation.toString

        result.registeringAs mustBe
          ReporterType.Sole.toString

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

    "buildIndividualUpdateContactDetails" - {

      "must include all current individual contact details when only the email changes" in {
        val original = originalSubscription(individualContact("old@test.com", Some(individualPhone)))
        val updated  = updatedSubscription(individualContact("new@test.com", Some(individualPhone)))

        val audit = service.buildIndividualUpdateContactDetails(
          original,
          updated,
          subscriptionId,
          AffinityGroup.Individual
        ).value

        audit mustBe UpdateContactDetailsAuditRequest(
          fatcaId = subscriptionId.value,
          affinityType = AffinityGroup.Individual.toString,
          email = Some("new@test.com"),
          telephone = Some(individualPhone)
        )
      }

      "must audit removal of the phone while omitting it from the payload" in {
        val original = originalSubscription(individualContact(individualEmail, Some(individualPhone)))
        val updated  = updatedSubscription(individualContact(individualEmail, None))

        val audit = service.buildIndividualUpdateContactDetails(
          original,
          updated,
          subscriptionId,
          AffinityGroup.Individual
        ).value

        audit.email mustBe Some(individualEmail)
        audit.telephone mustBe None
        (Json.toJson(audit) \ "telephone").toOption mustBe None
        (Json.toJson(audit) \ "email").as[String] mustBe individualEmail
      }

      "must not build an audit when individual contact details have not changed" in {
        val contact = individualContact(individualEmail, Some(individualPhone))

        service.buildIndividualUpdateContactDetails(
          originalSubscription(contact),
          updatedSubscription(contact),
          subscriptionId,
          AffinityGroup.Individual
        ) mustBe None
      }
    }

    "buildOrganisationUpdateContactDetails" - {

      "must include all first and second contact fields even if only one value changed" in {
        val primaryBefore = organisationContact("First Contact", "old@test.com", Some("11111"))
        val primaryAfter  = organisationContact("First Contact", "new@test.com", Some("11111"))
        val second        = organisationContact("Second Contact", "second@test.com", Some("22222"))

        val audit = service.buildOrganisationUpdateContactDetails(
          originalSubscription(primaryBefore, Some(second)),
          updatedSubscription(primaryAfter, Some(second)),
          subscriptionId,
          AffinityGroup.Organisation
        ).value

        audit mustBe UpdateContactDetailsAuditRequest(
          fatcaId = subscriptionId.value,
          affinityType = AffinityGroup.Organisation.toString,
          firstContactName = Some("First Contact"),
          firstContactEmail = Some("new@test.com"),
          firstContactTelephone = Some("11111"),
          secondContactName = Some("Second Contact"),
          secondContactEmail = Some("second@test.com"),
          secondContactTelephone = Some("22222")
        )
      }

      "must omit all second contact fields when the entire contact is removed" in {
        val first  = organisationContact("First Contact", "first@test.com", Some("11111"))
        val second = organisationContact("Second Contact", "second@test.com", Some("22222"))

        val audit = service.buildOrganisationUpdateContactDetails(
          originalSubscription(first, Some(second)),
          updatedSubscription(first, None),
          subscriptionId,
          AffinityGroup.Organisation
        ).value

        audit.firstContactName mustBe Some("First Contact")
        audit.firstContactEmail mustBe Some("first@test.com")
        audit.secondContactName mustBe None
        audit.secondContactEmail mustBe None
        audit.secondContactTelephone mustBe None

        val json = Json.toJson(audit)
        (json \ "secondContactName").toOption mustBe None
        (json \ "secondContactEmail").toOption mustBe None
        (json \ "secondContactTelephone").toOption mustBe None
      }

      "must retain secondary name and email when its telephone is removed" in {
        val first        = organisationContact("First Contact", "first@test.com", None)
        val secondBefore = organisationContact("Second Contact", "second@test.com", Some("22222"))
        val secondAfter  = organisationContact("Second Contact", "second@test.com", None)

        val audit = service.buildOrganisationUpdateContactDetails(
          originalSubscription(first, Some(secondBefore)),
          updatedSubscription(first, Some(secondAfter)),
          subscriptionId,
          AffinityGroup.Organisation
        ).value

        audit.secondContactName mustBe Some("Second Contact")
        audit.secondContactEmail mustBe Some("second@test.com")
        audit.secondContactTelephone mustBe None
        (Json.toJson(audit) \ "secondContactTelephone").toOption mustBe None
      }

      "must include a newly added secondary contact" in {
        val first  = organisationContact("First Contact", "first@test.com", None)
        val second = organisationContact("Second Contact", "second@test.com", Some("22222"))

        val audit = service.buildOrganisationUpdateContactDetails(
          originalSubscription(first, None),
          updatedSubscription(first, Some(second)),
          subscriptionId,
          AffinityGroup.Organisation
        ).value

        audit.secondContactName mustBe Some("Second Contact")
        audit.secondContactEmail mustBe Some("second@test.com")
        audit.secondContactTelephone mustBe Some("22222")
      }

      "must use the authenticated Agent affinity rather than hardcoding Organisation" in {
        val original = organisationContact("First Contact", "old@test.com", None)
        val updated  = organisationContact("First Contact", "new@test.com", None)

        val audit = service.buildOrganisationUpdateContactDetails(
          originalSubscription(original),
          updatedSubscription(updated),
          subscriptionId,
          AffinityGroup.Agent
        ).value

        audit.affinityType mustBe AffinityGroup.Agent.toString
      }

      "must not build an audit when organisation contact details have not changed" in {
        val first  = organisationContact("First Contact", "first@test.com", Some("11111"))
        val second = organisationContact("Second Contact", "second@test.com", None)

        service.buildOrganisationUpdateContactDetails(
          originalSubscription(first, Some(second)),
          updatedSubscription(first, Some(second)),
          subscriptionId,
          AffinityGroup.Organisation
        ) mustBe None
      }
    }

    "sendUpdateContactDetails" - {

      val auditRequest = UpdateContactDetailsAuditRequest(
        fatcaId = subscriptionId.value,
        affinityType = AffinityGroup.Organisation.toString,
        firstContactEmail = Some("new@test.com")
      )

      "must send the audit request to the connector successfully" in {
        when(
          mockAuditConnector.sendUpdateContactDetails(any())(any(), any())
        ).thenReturn(Future.successful(HttpResponse(status = NO_CONTENT, body = "")))

        service.sendUpdateContactDetails(auditRequest).futureValue mustBe AuditSent

        verify(mockAuditConnector).sendUpdateContactDetails(mEq(auditRequest))(any(), any())
      }

      "must return AuditFailed when the connector fails" in {
        when(
          mockAuditConnector.sendUpdateContactDetails(any())(any(), any())
        ).thenReturn(Future.failed(new RuntimeException("Audit endpoint unavailable")))

        service.sendUpdateContactDetails(auditRequest).futureValue mustBe AuditFailed

        verify(mockAuditConnector).sendUpdateContactDetails(mEq(auditRequest))(any(), any())
      }
    }
  }

}
