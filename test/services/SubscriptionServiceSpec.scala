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
import com.softwaremill.quicklens.*
import connectors.SubscriptionConnector
import generators.ModelGenerators
import helpers.JsonFixtures.*
import models.error.ApiError
import models.error.ApiError.*
import models.subscription.request.*
import models.subscription.response.{DisplaySubscriptionResponse, IndividualRegistrationType, OrganisationRegistrationType}
import models.{Address, Country, Name, ReporterType, SubscriptionID, UserAnswers, UserSubscription}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.MockitoSugar.reset
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*
import pages.changeContactDetails.*
import play.api.inject.bind
import repositories.SubscriptionRepository
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val mockSubscriptionConnector: SubscriptionConnector   = mock[SubscriptionConnector]
  val mockSubscriptionRepository: SubscriptionRepository = mock[SubscriptionRepository]

  private val indPages          = ContactTypePage.individualContactDetailsPages
  private val orgPrimaryPages   = ContactTypePage.primaryContactDetailsPages
  private val orgSecondaryPages = ContactTypePage.secondaryContactDetailsPages

  private val application = applicationBuilder()
    .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
    .overrides(bind[SubscriptionRepository].toInstance(mockSubscriptionRepository))

  override def beforeEach(): Unit = {
    reset(mockSubscriptionConnector)
    reset(mockSubscriptionRepository)
    super.beforeEach()
  }

  val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

  "SubscriptionService" - {

    "checkAndCreateSubscription" - {

      "must return 'SubscriptionID' on creating subscription" in {
        val subscriptionID                                      = SubscriptionID("id")
        val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

        when(mockSubscriptionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

        val address = Address("", None, "", None, None, Country("GB", "United Kingdom"))
        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.Individual).success.value
          .set(IndDoYouHaveNINumberPage, false).success.value
          .set(IndWhatIsYourNamePage, name).success.value
          .set(IndContactEmailPage, TestEmail).success.value
          .set(IndContactHavePhonePage, false).success.value
          .set(IndUKAddressWithoutIdPage, address).success.value

        val result = service.checkAndCreateSubscription(safeId, userAnswers, AffinityGroup.Individual)
        result.futureValue mustBe Right(SubscriptionID("id"))

        verify(mockSubscriptionRepository, times(1)).get(any())
        verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
      }

      "must return 'SubscriptionID' on creating subscription when a second contact exists" in {
        val subscriptionID                                      = SubscriptionID("id")
        val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

        when(mockSubscriptionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

        val address = Address("", None, "", None, None, Country("GB", "United Kingdom"))
        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.Individual).success.value
          .set(IndDoYouHaveNINumberPage, false).success.value
          .set(IndWhatIsYourNamePage, name).success.value
          .set(IndContactEmailPage, TestEmail).success.value
          .set(ContactHavePhonePage, false).success.value
          .set(IndUKAddressWithoutIdPage, address).success.value

        val result = service.checkAndCreateSubscription(safeId, userAnswers, AffinityGroup.Individual)
        result.futureValue mustBe Right(SubscriptionID("id"))

        verify(mockSubscriptionRepository, times(1)).get(any())
        verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
      }

      "must return 'SubscriptionID' when a subscription already exists" in {
        forAll(arbitrary[DisplaySubscriptionResponse]) {
          subscription =>
            when(mockSubscriptionRepository.get(any())).thenReturn(Future.successful(Some(UserSubscription(safeId.value, subscription.subscriptionId))))

            val result = service.checkAndCreateSubscription(safeId, emptyUserAnswers, AffinityGroup.Individual)
            result.futureValue.value mustBe subscription.subscriptionId
        }
      }

      "must return MandatoryInformationMissingError when UserAnswers is empty" in {
        val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Left(MandatoryInformationMissingError()))

        when(mockSubscriptionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

        val result = service.checkAndCreateSubscription(safeId, UserAnswers("id"), AffinityGroup.Individual)

        result.futureValue mustBe Left(MandatoryInformationMissingError())
      }

      "must return error when it fails to create subscription" in {
        val errors = Seq(NotFoundError, BadRequestError, DuplicateSubmissionError, UnableToCreateEMTPSubscriptionError)
        for (error <- errors) {
          val userAnswers = UserAnswers("id")
            .set(DoYouHaveUniqueTaxPayerReferencePage, true).success.value
            .set(ContactEmailPage, TestEmail).success.value
            .set(ContactNamePage, s"$FirstName $LastName").success.value
            .set(ContactHavePhonePage, false).success.value
            .set(HaveSecondContactPage, false).success.value

          val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Left(error))
          when(mockSubscriptionRepository.get(any())).thenReturn(Future.successful(None))
          when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

          val result = service.checkAndCreateSubscription(safeId, userAnswers, AffinityGroup.Organisation)

          result.futureValue mustBe Left(error)
        }
      }
    }

    "getSubscription" - {

      "by SafeId" - {
        "must return subscription details for valid input" in {
          forAll(arbitrary[SubscriptionID]) {
            subscription =>
              when(mockSubscriptionRepository.get(any())).thenReturn(Future.successful(Some(UserSubscription(safeId.value, subscription))))
              val result = service.getSubscription(safeId)
              result.futureValue mustBe Some(subscription)
          }
        }

        "must return 'None' when repository returns None" in {
          when(mockSubscriptionRepository.get(any())).thenReturn(Future.successful(None))
          val result = service.getSubscription(safeId)
          result.futureValue mustBe None
        }
      }

      "by SubscriptionID" - {
        "must return DisplaySubscriptionResponse when connector finds subscription" in {
          forAll(arbitrary[DisplaySubscriptionResponse]) {
            subscriptionResponse =>
              when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
                .thenReturn(Future.successful(Some(subscriptionResponse)))

              val result = service.getSubscription(subscriptionId)
              result.futureValue mustBe Some(subscriptionResponse)
          }
        }

        "must return None when connector returns None" in {
          when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
            .thenReturn(Future.successful(None))

          val result = service.getSubscription(subscriptionId)
          result.futureValue mustBe None
        }
      }
    }

    "updateOrgContactDetails" - {

      "must return true when primary contact details are updated" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, organisationDetails, contactPhoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(organisationDetails)
              .modify(_.success.crfaSubscriptionDetails.secondaryContact).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscription.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, contactPhoneNumber)
              .withPage(OrganisationHaveSecondContactPage, false)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))
            when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(true))

            service.updateOrgContactDetails(subscriptionId, userAnswers).futureValue mustBe true
        }
      }

      "must return false when unable to update contact details" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails]) {
          (subscription, organisationDetails) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation)
              .setTo(organisationDetails)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscription.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationHaveSecondContactPage, false)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))
            when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(false))

            service.updateOrgContactDetails(subscriptionId, userAnswers).futureValue mustBe false
        }
      }

      "must return false when userAnswers cannot be converted to an organisation update request" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails]) {
          (subscription, organisationDetails) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation)
              .setTo(organisationDetails)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))

            service.updateOrgContactDetails(subscriptionId, userAnswers).futureValue mustBe false
        }
      }

      "must return false when the connector returns None when retrieving the subscription" in {
        when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(None))

        service.updateOrgContactDetails(subscriptionId, emptyUserAnswers).futureValue mustBe false
      }
    }

    "updateIndContactDetails" - {

      "must return true when individual contact details are updated with phone number" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, individualDetails, contactPhoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.success.crfaSubscriptionDetails.secondaryContact).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(indPages.contactEmailPage, subscription.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(indPages.havePhoneNumberPage, true)
              .withPage(indPages.contactPhoneNumberPage, contactPhoneNumber)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))
            when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(true))

            service.updateIndContactDetails(subscriptionId, userAnswers).futureValue mustBe true
        }
      }

      "must return false when connector fails to update individual contact details" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails]) {
          (subscription, individualDetails) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.success.crfaSubscriptionDetails.secondaryContact).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(indPages.contactEmailPage, subscription.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(indPages.havePhoneNumberPage, false)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))
            when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(false))

            service.updateIndContactDetails(subscriptionId, userAnswers).futureValue mustBe false
        }
      }

      "must return false when userAnswers cannot be converted to an individual update request" in {
        forAll(arbitrary[DisplaySubscriptionResponse]) {
          subscriptionResponse =>
            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))

            service.updateIndContactDetails(subscriptionId, emptyUserAnswers).futureValue mustBe false
        }
      }

      "must return false when readSubscription returns None" in {
        when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(None))

        service.updateIndContactDetails(subscriptionId, emptyUserAnswers).futureValue mustBe false
      }
    }

    "updateContactDetailsWithAuditData" - {

      "must return Some(SuccessfulContactDetailsUpdate) for OrganisationRegistrationType" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, organisationDetails, contactPhoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(organisationDetails)
              .modify(_.success.crfaSubscriptionDetails.secondaryContact).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscription.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, contactPhoneNumber)
              .withPage(OrganisationHaveSecondContactPage, false)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))
            when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(true))

            val result = service.updateContactDetailsWithAuditData(subscriptionId, userAnswers, OrganisationRegistrationType).futureValue
            result mustBe defined
            result.value.original mustBe subscriptionResponse
            result.value.updated.secondaryContact mustBe None
        }
      }

      "must return Some(SuccessfulContactDetailsUpdate) for IndividualRegistrationType" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, individualDetails, contactPhoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.success.crfaSubscriptionDetails.secondaryContact).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(indPages.contactEmailPage, subscription.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(indPages.havePhoneNumberPage, true)
              .withPage(indPages.contactPhoneNumberPage, contactPhoneNumber)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))
            when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(true))

            val result = service.updateContactDetailsWithAuditData(subscriptionId, userAnswers, IndividualRegistrationType).futureValue
            result mustBe defined
            result.value.original mustBe subscriptionResponse
            result.value.updated.secondaryContact mustBe None
        }
      }

      "must return None when update fails" in {
        when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(None))

        service.updateContactDetailsWithAuditData(subscriptionId, emptyUserAnswers, IndividualRegistrationType).futureValue mustBe None
      }
    }

    "checkIfIndContactDetailsHasChanged" - {

      "must return Some(false) when no change is made to individual contact details (with phone)" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, individualDetails, phoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.success.crfaSubscriptionDetails.primaryContact.phone).setTo(Some(phoneNumber))

            val userAnswers = emptyUserAnswers
              .withPage(indPages.contactEmailPage, subscriptionResponse.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(indPages.havePhoneNumberPage, true)
              .withPage(indPages.contactPhoneNumberPage, phoneNumber)

            service.checkIfIndContactDetailsHasChanged(subscriptionResponse, userAnswers) mustBe Some(false)
        }
      }

      "must return Some(false) when no change is made to individual contact details (without phone)" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails]) {
          (subscription, individualDetails) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.success.crfaSubscriptionDetails.primaryContact.phone).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(indPages.contactEmailPage, subscriptionResponse.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(indPages.havePhoneNumberPage, false)

            service.checkIfIndContactDetailsHasChanged(subscriptionResponse, userAnswers) mustBe Some(false)
        }
      }

      "must return Some(true) when individual contact details have changed" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails], emailMatchingRegexAndLength(emailRegex, EmailLength)) {
          (subscription, individualDetails, newEmail) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.success.crfaSubscriptionDetails.primaryContact.phone).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(indPages.contactEmailPage, newEmail)
              .withPage(indPages.havePhoneNumberPage, false)

            service.checkIfIndContactDetailsHasChanged(subscriptionResponse, userAnswers) mustBe Some(true)
        }
      }

      "must return None when required individual userAnswers are missing" in {
        forAll(arbitrary[DisplaySubscriptionResponse]) {
          subscriptionResponse =>
            service.checkIfIndContactDetailsHasChanged(subscriptionResponse, emptyUserAnswers) mustBe None
        }
      }
    }

    "populateUserAnswersFromIndSubscription" - {

      "must populate userAnswers when phone number is provided" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, individualDetails, phoneNumber) =>
            val responseDetail = subscription.success
              .modify(_.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.crfaSubscriptionDetails.primaryContact.phone).setTo(Some(phoneNumber))

            val result = service.populateUserAnswersFromIndSubscription(emptyUserAnswers, responseDetail).value

            result.get(indPages.contactEmailPage) mustBe Some(responseDetail.crfaSubscriptionDetails.primaryContact.email)
            result.get(indPages.contactPhoneNumberPage) mustBe Some(phoneNumber)
            result.get(indPages.havePhoneNumberPage) mustBe Some(true)
        }
      }

      "must populate userAnswers when phone number is None" ignore { // TODO Fix as part of GLSD-23953
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[IndividualDetails]) {
          (subscription, individualDetails) =>
            val responseDetail = subscription.success
              .modify(_.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(individualDetails)
              .modify(_.crfaSubscriptionDetails.primaryContact.phone).setTo(None)

            val result = service.populateUserAnswersFromIndSubscription(emptyUserAnswers, responseDetail).value

            result.get(indPages.contactEmailPage) mustBe Some(responseDetail.crfaSubscriptionDetails.primaryContact.email)
            result.get(indPages.contactPhoneNumberPage) mustBe Some("Not provided")
            result.get(indPages.havePhoneNumberPage) mustBe Some(false)
        }
      }
    }

    "populateUserAnswersFromOrgSubscription" - {

      "must populate primary contact only when secondaryContact is None" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, organisationDetails, phoneNumber) =>
            val responseDetail = subscription.success
              .modify(_.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(organisationDetails)
              .modify(_.crfaSubscriptionDetails.primaryContact.phone).setTo(Some(phoneNumber))
              .modify(_.crfaSubscriptionDetails.secondaryContact).setTo(None)

            val result = service.populateUserAnswersFromOrgSubscription(emptyUserAnswers, responseDetail).value

            result.get(orgPrimaryPages.contactNamePage) mustBe Some(organisationDetails.name)
            result.get(orgPrimaryPages.contactEmailPage) mustBe Some(responseDetail.crfaSubscriptionDetails.primaryContact.email)
            result.get(orgPrimaryPages.contactPhoneNumberPage) mustBe Some(phoneNumber)
            result.get(orgPrimaryPages.havePhoneNumberPage) mustBe Some(true)
            result.get(OrganisationHaveSecondContactPage) mustBe Some(false)
            result.get(OrganisationSecondContactNamePage) mustBe None
        }
      }

      "must populate primary and secondary contacts when secondaryContact is present (including IndividualDetails)" ignore {
        // TODO Fix as part of GLSD-23953
        forAll(
          arbitrary[DisplaySubscriptionResponse],
          arbitrary[OrganisationDetails],
          arbitrary[IndividualDetails],
          emailMatchingRegexAndLength(emailRegex, EmailLength)
        ) {
          (subscription, primaryOrgDetails, secondIndDetails, secondEmail) =>
            val responseDetail = subscription.success
              .modify(_.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(primaryOrgDetails)
              .modify(_.crfaSubscriptionDetails.primaryContact.phone).setTo(None)
              .modify(_.crfaSubscriptionDetails.secondaryContact)
              .setTo(Some(ContactInformation(secondIndDetails, secondEmail, None)))

            val result = service.populateUserAnswersFromOrgSubscription(emptyUserAnswers, responseDetail).value

            result.get(orgPrimaryPages.contactNamePage) mustBe Some(primaryOrgDetails.name)
            result.get(orgPrimaryPages.havePhoneNumberPage) mustBe Some(false)
            result.get(orgPrimaryPages.contactPhoneNumberPage) mustBe Some("Not provided")
            result.get(OrganisationHaveSecondContactPage) mustBe Some(true)
            result.get(orgSecondaryPages.contactNamePage) mustBe Some(Name(secondIndDetails.firstName, secondIndDetails.lastName).fullName)
            result.get(orgSecondaryPages.contactEmailPage) mustBe Some(secondEmail)
            result.get(orgSecondaryPages.havePhoneNumberPage) mustBe Some(false)
            result.get(orgSecondaryPages.contactPhoneNumberPage) mustBe Some("Not provided")
        }
      }
    }

    "checkIfOrgContactDetailsHasChanged" - {

      "must return false when no change is made to primary contact details" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, organisationDetails, phoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation)
              .setTo(organisationDetails)
              .modify(_.success.crfaSubscriptionDetails.primaryContact.phone)
              .setTo(Some(phoneNumber))
              .modify(_.success.crfaSubscriptionDetails.secondaryContact)
              .setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, organisationDetails.name)
              .withPage(OrganisationContactEmailPage, subscriptionResponse.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, phoneNumber)
              .withPage(OrganisationHaveSecondContactPage, false)

            val result = service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, userAnswers)

            result mustBe Some(false)
        }
      }

      "must return false when secondary contact exists in subscription and second contact name is not overwritten in userAnswers" in {
        forAll(
          arbitrary[DisplaySubscriptionResponse],
          arbitrary[OrganisationDetails],
          arbitrary[OrganisationDetails],
          validPhoneNumber(PhoneNumberLength),
          emailMatchingRegexAndLength(emailRegex, EmailLength)
        ) {
          (subscription, primaryOrg, secondOrg, phoneNumber, secondEmail) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation).setTo(primaryOrg)
              .modify(_.success.crfaSubscriptionDetails.primaryContact.phone).setTo(Some(phoneNumber))
              .modify(_.success.crfaSubscriptionDetails.secondaryContact)
              .setTo(Some(ContactInformation(secondOrg, secondEmail, None)))

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, primaryOrg.name)
              .withPage(OrganisationContactEmailPage, subscriptionResponse.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, phoneNumber)
              .withPage(OrganisationHaveSecondContactPage, true)
              .withPage(OrganisationSecondContactEmailPage, secondEmail)
              .withPage(OrganisationSecondContactHavePhonePage, false)

            service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, userAnswers) mustBe Some(false)
        }
      }

      "must return true when primary contact details are changed for organisation" in {
        forAll(
          arbitrary[DisplaySubscriptionResponse],
          arbitrary[OrganisationDetails],
          validPhoneNumber(PhoneNumberLength),
          emailMatchingRegexAndLength(emailRegex, EmailLength),
          validPhoneNumber(PhoneNumberLength),
          arbitrary[String]
        ) {
          (subscription, organisationDetails, contactPhoneNumber, secondContactEmail, secondContactPhoneNumber, secondContactName) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation)
              .setTo(organisationDetails)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscription.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, contactPhoneNumber)
              .withPage(OrganisationHaveSecondContactPage, true)
              .withPage(OrganisationSecondContactNamePage, secondContactName)
              .withPage(OrganisationSecondContactEmailPage, secondContactEmail)
              .withPage(OrganisationSecondContactHavePhonePage, true)
              .withPage(OrganisationSecondContactPhonePage, secondContactPhoneNumber)

            val result = service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, userAnswers)

            result mustBe Some(true)
        }
      }

      "must return true when a secondary contact is added" in {
        forAll(
          arbitrary[DisplaySubscriptionResponse],
          validPhoneNumber(PhoneNumberLength),
          emailMatchingRegexAndLength(emailRegex, EmailLength),
          validPhoneNumber(PhoneNumberLength),
          arbitrary[String]
        ) {
          (subscription, contactPhoneNumber, secondContactEmail, secondContactPhoneNumber, secondContactName) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation)
              .setTo(OrganisationDetails(name.fullName))
              .modify(_.success.crfaSubscriptionDetails.primaryContact.phone)
              .setTo(Some(contactPhoneNumber))
              .modify(_.success.crfaSubscriptionDetails.secondaryContact)
              .setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscriptionResponse.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, contactPhoneNumber)
              .withPage(OrganisationHaveSecondContactPage, true)
              .withPage(OrganisationSecondContactNamePage, secondContactName)
              .withPage(OrganisationSecondContactEmailPage, secondContactEmail)
              .withPage(OrganisationSecondContactHavePhonePage, true)
              .withPage(OrganisationSecondContactPhonePage, secondContactPhoneNumber)

            val result = service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, userAnswers)

            result mustBe Some(true)
        }
      }

      "must return true when secondary contact is removed" in {
        forAll(
          arbitrary[DisplaySubscriptionResponse],
          emailMatchingRegexAndLength(emailRegex, EmailLength),
          validPhoneNumber(PhoneNumberLength),
          emailMatchingRegexAndLength(emailRegex, EmailLength)
        ) {
          (subscription, contactEmail, contactPhoneNumber, secondContactEmail) =>
            val subscriptionResponse = subscription
              .modify(_.success.crfaSubscriptionDetails.primaryContact.contactInformation)
              .setTo(OrganisationDetails(name.fullName))
              .modify(_.success.crfaSubscriptionDetails.primaryContact.email)
              .setTo(contactEmail)
              .modify(_.success.crfaSubscriptionDetails.primaryContact.phone)
              .setTo(Some(contactPhoneNumber))
              .modify(_.success.crfaSubscriptionDetails.secondaryContact)
              .setTo(Some(ContactInformation(OrganisationDetails(name.fullName), secondContactEmail, phone = None)))

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscriptionResponse.success.crfaSubscriptionDetails.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, contactPhoneNumber)
              .withPage(OrganisationHaveSecondContactPage, false)

            val result = service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, userAnswers)

            result mustBe Some(true)
        }
      }

      "must return None when required organisation userAnswers are missing" in {
        forAll(arbitrary[DisplaySubscriptionResponse]) {
          subscriptionResponse =>
            service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, emptyUserAnswers) mustBe None
        }
      }
    }
  }

}
