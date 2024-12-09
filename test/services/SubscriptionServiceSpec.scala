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
import connectors.SubscriptionConnector
import generators.ModelGenerators
import helpers.JsonFixtures._
import models.error.ApiError
import models.error.ApiError._
import models.subscription.request.{ContactInformation, OrganisationDetails, ReadSubscriptionRequest}
import models.subscription.response.DisplaySubscriptionResponse
import models.{Address, Country, ReporterType, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.MockitoSugar.reset
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.inject.bind
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import com.softwaremill.quicklens._
import org.scalacheck.Arbitrary.arbitrary
import pages.changeContactDetails._

class SubscriptionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  private val application = applicationBuilder()
    .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))

  override def beforeEach(): Unit = {
    reset(mockSubscriptionConnector)
    super.beforeEach()
  }

  val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

  "SubscriptionService" - {
    "must return 'SubscriptionID' on creating subscription" in {
      val subscriptionID                                      = SubscriptionID("id")
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
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

      verify(mockSubscriptionConnector, times(1)).readSubscription(any())(any(), any())
      verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
    }

    "must return 'SubscriptionID' on creating subscription when a second contact exists" in {
      val subscriptionID                                      = SubscriptionID("id")
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
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

      verify(mockSubscriptionConnector, times(1)).readSubscription(any())(any(), any())
      verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
    }

    "must return 'SubscriptionID' when a subscription already exists" in {

      forAll(arbitrary[DisplaySubscriptionResponse]) {
        subscription =>
          when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(Some(subscription)))

          val result = service.checkAndCreateSubscription(safeId, emptyUserAnswers, AffinityGroup.Individual)
          result.futureValue.value mustBe subscription.subscriptionId
      }
    }

    "must return MandatoryInformationMissingError when UserAnswers is empty" in {
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Left(MandatoryInformationMissingError()))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
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
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

        val result = service.checkAndCreateSubscription(safeId, userAnswers, AffinityGroup.Organisation)

        result.futureValue mustBe Left(error)
      }
    }

    "getSubscription" - {

      "must return subscription details for valid input" in {
        forAll(arbitrary[DisplaySubscriptionResponse]) {
          subscription =>
            when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(Some(subscription)))
            val result = service.getSubscription(safeId)
            result.futureValue mustBe Some(subscription)
        }
      }

      "must return 'None' for any failures of exceptions" in {
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
        val result = service.getSubscription(safeId)
        result.futureValue mustBe None
      }
    }

    "updateContactDetails" - {

      "must return true when primary contact details are updated" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, organisationDetails, contactPhoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.primaryContact.contactInformation).setTo(organisationDetails)
              .modify(_.success.secondaryContact).setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscription.success.primaryContact.email)
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
              .modify(_.success.primaryContact.contactInformation)
              .setTo(organisationDetails)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscription.success.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationHaveSecondContactPage, false)

            when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(Some(subscriptionResponse)))
            when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
              .thenReturn(Future.successful(false))

            service.updateOrgContactDetails(subscriptionId, userAnswers).futureValue mustBe false
        }
      }

      "must return false when the connector returns None when retrieving the subscription" in {
        when(mockSubscriptionConnector.readSubscription(any[ReadSubscriptionRequest])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(None))

        service.updateOrgContactDetails(subscriptionId, emptyUserAnswers).futureValue mustBe false
      }
    }

    "checkIfContactDetailsHasChanged" - {

      "must return false when no change is made to primary contact details" in {
        forAll(arbitrary[DisplaySubscriptionResponse], arbitrary[OrganisationDetails], validPhoneNumber(PhoneNumberLength)) {
          (subscription, organisationDetails, phoneNumber) =>
            val subscriptionResponse = subscription
              .modify(_.success.primaryContact.contactInformation)
              .setTo(organisationDetails)
              .modify(_.success.primaryContact.phone)
              .setTo(Some(phoneNumber))
              .modify(_.success.secondaryContact)
              .setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, organisationDetails.name)
              .withPage(OrganisationContactEmailPage, subscriptionResponse.success.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, phoneNumber)
              .withPage(OrganisationHaveSecondContactPage, false)

            val result = service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, userAnswers)

            result mustBe Some(false)
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
              .modify(_.success.primaryContact.contactInformation)
              .setTo(organisationDetails)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscription.success.primaryContact.email)
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
              .modify(_.success.primaryContact.contactInformation)
              .setTo(OrganisationDetails(name.fullName))
              .modify(_.success.primaryContact.phone)
              .setTo(Some(contactPhoneNumber))
              .modify(_.success.secondaryContact)
              .setTo(None)

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscriptionResponse.success.primaryContact.email)
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
              .modify(_.success.primaryContact.contactInformation)
              .setTo(OrganisationDetails(name.fullName))
              .modify(_.success.primaryContact.email)
              .setTo(contactEmail)
              .modify(_.success.primaryContact.phone)
              .setTo(Some(contactPhoneNumber))
              .modify(_.success.secondaryContact)
              .setTo(Some(ContactInformation(OrganisationDetails(name.fullName), secondContactEmail, phone = None)))

            val userAnswers = emptyUserAnswers
              .withPage(OrganisationContactNamePage, name.fullName)
              .withPage(OrganisationContactEmailPage, subscriptionResponse.success.primaryContact.email)
              .withPage(OrganisationContactHavePhonePage, true)
              .withPage(OrganisationContactPhonePage, contactPhoneNumber)
              .withPage(OrganisationHaveSecondContactPage, false)

            val result = service.checkIfOrgContactDetailsHasChanged(subscriptionResponse, userAnswers)

            result mustBe Some(true)
        }
      }
    }
  }

}
