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

import connectors.AuditConnector
import models.ReporterType.{Individual, LimitedCompany, LimitedPartnership, Partnership, Sole, UnincorporatedAssociation}
import models.audit.AuditResult.{AuditFailed, AuditNotSent, AuditSent}
import models.subscription.request.{ContactInformation, OrganisationDetails, UpdateSubscriptionRequest}
import models.subscription.response.DisplaySubscriptionResponse
import models.audit.{AuditResult, CreateRegistrationAuditRequest, UpdateContactDetailsAuditRequest}
import models.{Address, ReporterType, SubscriptionID, UserAnswers}
import pages._
import pages.changeContactDetails.{OrganisationSecondContactEmailPage, OrganisationSecondContactNamePage, OrganisationSecondContactPhonePage}
import play.api.Logging
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject() (
  auditConnector: AuditConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  private case class RegistrationDetails(
    registrationType: String,
    idType: String,
    idValue: String
  )

  def sendCreateRegistration(
    userAnswers: UserAnswers,
    subscriptionId: SubscriptionID,
    affinityGroup: AffinityGroup
  )(implicit hc: HeaderCarrier): Future[AuditResult] =
    buildCreateRegistration(
      userAnswers = userAnswers,
      subscriptionId = subscriptionId,
      affinityGroup = affinityGroup
    ) match {
      case Some(auditRequest) =>
        auditConnector
          .sendCreateRegistration(auditRequest)
          .map(
            _ => AuditSent
          )
          .recover {
            case exception =>
              logger.error(
                "Failed to send CreateRegistration audit event",
                exception
              )

              AuditFailed
          }

      case None =>
        logger.error(
          "CreateRegistration audit was not sent because required audit information was missing"
        )

        Future.successful(
          AuditNotSent
        )
    }

  def sendUpdateContactDetails(
    auditRequest: UpdateContactDetailsAuditRequest
  )(implicit hc: HeaderCarrier): Future[AuditResult] =
    auditConnector
      .sendUpdateContactDetails(auditRequest)
      .map(
        _ => AuditSent
      )
      .recover {
        case exception =>
          logger.error(
            "Failed to send UpdateContactDetails audit event",
            exception
          )

          AuditFailed
      }

  def buildOrganisationUpdateContactDetails(
    original: DisplaySubscriptionResponse,
    updated: UpdateSubscriptionRequest,
    subscriptionId: SubscriptionID,
    affinityGroup: AffinityGroup
  ): Option[UpdateContactDetailsAuditRequest] = {

    val originalDetails  = original.success.crfaSubscriptionDetails
    val updatedPrimary   = updated.primaryContact
    val updatedSecondary = updated.secondaryContact

    val hasChanges =
      originalDetails.primaryContact != updatedPrimary ||
        originalDetails.secondaryContact != updatedSecondary

    Option.when(hasChanges) {
      UpdateContactDetailsAuditRequest(
        fatcaId = subscriptionId.value,
        affinityType = affinityGroup.toString,
        firstContactName = organisationContactName(updatedPrimary),
        firstContactEmail = Some(updatedPrimary.email),
        firstContactTelephone = updatedPrimary.phone,
        secondContactName = updatedSecondary.flatMap(organisationContactName),
        secondContactEmail = updatedSecondary.map(_.email),
        secondContactTelephone = updatedSecondary.flatMap(_.phone)
      )
    }
  }

  def buildIndividualUpdateContactDetails(
    original: DisplaySubscriptionResponse,
    updated: UpdateSubscriptionRequest,
    subscriptionId: SubscriptionID,
    affinityGroup: AffinityGroup
  ): Option[UpdateContactDetailsAuditRequest] = {

    val originalContact = original.success.crfaSubscriptionDetails.primaryContact
    val updatedContact  = updated.primaryContact

    Option.when(originalContact != updatedContact) {
      UpdateContactDetailsAuditRequest(
        fatcaId = subscriptionId.value,
        affinityType = affinityGroup.toString,
        email = Some(updatedContact.email),
        telephone = updatedContact.phone
      )
    }
  }

  private def buildCreateRegistration(
    userAnswers: UserAnswers,
    subscriptionId: SubscriptionID,
    affinityGroup: AffinityGroup
  ): Option[CreateRegistrationAuditRequest] =
    for {
      reporterType <-
        userAnswers.get(ReporterTypePage)

      registrationDetails <-
        extractRegistrationDetails(
          userAnswers = userAnswers,
          reporterType = reporterType
        )

      isOrganisationJourney =
        isOrganisationRegistration(
          registrationDetails
        )

      address =
        extractAddress(
          userAnswers = userAnswers,
          registrationType = registrationDetails.registrationType
        )

      firstContactName <-
        extractFirstContactName(
          userAnswers = userAnswers,
          isOrganisationJourney = isOrganisationJourney
        )

      firstContactEmail <-
        extractFirstContactEmail(
          userAnswers = userAnswers,
          isOrganisationJourney = isOrganisationJourney
        )
    } yield createAuditRequest(
      userAnswers = userAnswers,
      subscriptionId = subscriptionId,
      affinityGroup = affinityGroup,
      reporterType = reporterType,
      registrationDetails = registrationDetails,
      isOrganisationJourney = isOrganisationJourney,
      address = address,
      firstContactName = firstContactName,
      firstContactEmail = firstContactEmail
    )

  private def createAuditRequest(
    userAnswers: UserAnswers,
    subscriptionId: SubscriptionID,
    affinityGroup: AffinityGroup,
    reporterType: ReporterType,
    registrationDetails: RegistrationDetails,
    isOrganisationJourney: Boolean,
    address: Option[Address],
    firstContactName: String,
    firstContactEmail: String
  ): CreateRegistrationAuditRequest =
    CreateRegistrationAuditRequest(
      affinityType = affinityGroup.toString,
      registeringAs = extractRegisteringAs(reporterType),
      registrationType = registrationDetails.registrationType,
      idType = registrationDetails.idType,
      idValue = registrationDetails.idValue,
      tradingName = extractTradingName(userAnswers),
      businessName = extractBusinessName(userAnswers),
      addressLine1 = address.map(_.addressLine1).flatMap(optionalNonEmpty),
      addressLine2 = address.flatMap(_.addressLine2).flatMap(optionalNonEmpty),
      city = address.map(_.addressLine3).flatMap(optionalNonEmpty),
      region = address.flatMap(_.addressLine4).flatMap(optionalNonEmpty),
      postcode = address.flatMap(_.postCode).flatMap(optionalNonEmpty),
      country = address.map(_.country.code).flatMap(optionalNonEmpty),
      uprn = extractUprn(userAnswers = userAnswers, registrationType = registrationDetails.registrationType),
      dateOfBirth = extractDateOfBirth(userAnswers),
      firstContactName = firstContactName,
      firstContactEmail = firstContactEmail,
      firstContactTelephone = extractFirstContactTelephone(userAnswers = userAnswers, isOrganisationJourney = isOrganisationJourney),
      secondContactName = extractSecondContactName(userAnswers),
      secondContactEmail = extractSecondContactEmail(userAnswers),
      secondContactTelephone = extractSecondContactTelephone(userAnswers),
      fatcaId = subscriptionId.value
    )

  private def extractRegisteringAs(
    reporterType: ReporterType
  ): String =
    reporterType.toString

  private def extractRegistrationDetails(
    userAnswers: UserAnswers,
    reporterType: ReporterType
  ): Option[RegistrationDetails] =
    reporterType match {
      case Individual =>
        Some(
          extractIndividualRegistrationDetails(
            userAnswers
          )
        )

      case Sole =>
        extractSoleTraderRegistrationDetails(
          userAnswers
        )

      case LimitedCompany |
          Partnership |
          LimitedPartnership |
          UnincorporatedAssociation =>
        Some(
          extractOrganisationRegistrationDetails(
            userAnswers
          )
        )
    }

  private def extractIndividualRegistrationDetails(
    userAnswers: UserAnswers
  ): RegistrationDetails =
    extractNino(userAnswers) match {
      case Some(value) =>
        RegistrationDetails(
          registrationType = "IndividualWithID",
          idType = "NINO",
          idValue = value
        )

      case None =>
        RegistrationDetails(
          registrationType = "IndividualWithoutID",
          idType = "NotProvided",
          idValue = "NotProvided"
        )
    }

  private def extractSoleTraderRegistrationDetails(
    userAnswers: UserAnswers
  ): Option[RegistrationDetails] = {

    val registeredInUK =
      userAnswers.get(RegisteredAddressInUKPage)

    val hasUtr =
      userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage)

    (registeredInUK, hasUtr) match {
      case (Some(true), Some(true)) =>
        extractUtr(userAnswers).map {
          value =>
            RegistrationDetails(
              registrationType = "OrgWithID",
              idType = "UTR",
              idValue = value
            )
        }

      case (Some(true), Some(false)) =>
        Some(
          extractIndividualRegistrationDetails(
            userAnswers
          )
        )

      case (Some(false), _) =>
        Some(
          extractIndividualRegistrationDetails(
            userAnswers
          )
        )

      case _ =>
        None
    }
  }

  private def extractOrganisationRegistrationDetails(
    userAnswers: UserAnswers
  ): RegistrationDetails = {

    val autoMatchedUtr =
      userAnswers
        .get(AutoMatchedUTRPage)
        .map(_.uniqueTaxPayerReference)
        .flatMap(optionalNonEmpty)

    val utr =
      extractUtr(userAnswers)

    (autoMatchedUtr, utr) match {
      case (Some(value), _) =>
        RegistrationDetails(
          registrationType = "CTAutomatched",
          idType = "UTR",
          idValue = value
        )

      case (None, Some(value)) =>
        RegistrationDetails(
          registrationType = "OrgWithID",
          idType = "UTR",
          idValue = value
        )

      case _ =>
        RegistrationDetails(
          registrationType = "OrgWithoutID",
          idType = "NotProvided",
          idValue = "NotProvided"
        )
    }
  }

  private def isOrganisationRegistration(
    registrationDetails: RegistrationDetails
  ): Boolean =
    registrationDetails.registrationType match {
      case "OrgWithID" |
          "OrgWithoutID" |
          "CTAutomatched" =>
        true

      case _ =>
        false
    }

  private def extractNino(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(IndWhatIsYourNINumberPage)
      .map(_.nino)
      .flatMap(optionalNonEmpty)

  private def extractUtr(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(WhatIsYourUTRPage)
      .map(_.uniqueTaxPayerReference)
      .flatMap(optionalNonEmpty)

  private def extractTradingName(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(BusinessTradingNameWithoutIDPage)
      .flatMap(optionalNonEmpty)

  private def extractBusinessName(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(BusinessNameWithoutIDPage)
      .orElse(
        userAnswers.get(BusinessNamePage)
      )
      .flatMap(optionalNonEmpty)

  private def extractDateOfBirth(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(IndDateOfBirthPage)
      .orElse(
        userAnswers.get(DateOfBirthWithoutIdPage)
      )
      .map(_.toString)
      .flatMap(optionalNonEmpty)

  private def extractFirstContactName(
    userAnswers: UserAnswers,
    isOrganisationJourney: Boolean
  ): Option[String] =
    if (isOrganisationJourney) {
      userAnswers
        .get(ContactNamePage)
        .flatMap(optionalNonEmpty)
    } else {
      userAnswers
        .get(IndWhatIsYourNamePage)
        .orElse(
          userAnswers.get(WhatIsYourNamePage)
        )
        .map(_.fullName)
        .flatMap(optionalNonEmpty)
    }

  private def extractFirstContactEmail(
    userAnswers: UserAnswers,
    isOrganisationJourney: Boolean
  ): Option[String] =
    if (isOrganisationJourney) {
      userAnswers
        .get(ContactEmailPage)
        .flatMap(optionalNonEmpty)
    } else {
      userAnswers
        .get(IndContactEmailPage)
        .flatMap(optionalNonEmpty)
    }

  private def extractFirstContactTelephone(
    userAnswers: UserAnswers,
    isOrganisationJourney: Boolean
  ): Option[String] =
    if (isOrganisationJourney) {
      userAnswers
        .get(ContactPhonePage)
        .flatMap(optionalNonEmpty)
    } else {
      userAnswers
        .get(IndContactPhonePage)
        .flatMap(optionalNonEmpty)
    }

  private def extractSecondContactName(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(OrganisationSecondContactNamePage)
      .orElse(
        userAnswers.get(SecondContactNamePage)
      )
      .flatMap(optionalNonEmpty)

  private def extractSecondContactEmail(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(OrganisationSecondContactEmailPage)
      .orElse(
        userAnswers.get(SecondContactEmailPage)
      )
      .flatMap(optionalNonEmpty)

  private def extractSecondContactTelephone(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(OrganisationSecondContactPhonePage)
      .orElse(
        userAnswers.get(SecondContactPhonePage)
      )
      .flatMap(optionalNonEmpty)

  private def extractAddress(
    userAnswers: UserAnswers,
    registrationType: String
  ): Option[Address] =
    registrationType match {
      case "IndividualWithoutID" =>
        extractIndividualAddress(userAnswers)

      case "OrgWithoutID" =>
        extractBusinessAddress(userAnswers)

      case _ =>
        None
    }

  private def extractBusinessAddress(
    userAnswers: UserAnswers
  ): Option[Address] =
    userAnswers
      .get(NonUKBusinessAddressWithoutIDPage)

  private def extractIndividualAddress(
    userAnswers: UserAnswers
  ): Option[Address] =
    userAnswers
      .get(IndWhereDoYouLivePage) match {
      case Some(true) =>
        userAnswers
          .get(IndSelectedAddressLookupPage)
          .flatMap(_.toAddress)
          .orElse {
            userAnswers
              .get(IndUKAddressWithoutIdPage)
          }

      case _ =>
        userAnswers
          .get(IndNonUKAddressWithoutIdPage)
    }

  private def extractUprn(
    userAnswers: UserAnswers,
    registrationType: String
  ): Option[String] =
    if (registrationType == "IndividualWithoutID") {
      userAnswers
        .get(IndSelectedAddressLookupPage)
        .map(_.uprn.toString)
    } else {
      None
    }

  private def optionalNonEmpty(
    value: String
  ): Option[String] =
    Option(value)
      .map(_.trim)
      .filter(_.nonEmpty)

  private def organisationContactName(
    contact: ContactInformation
  ): Option[String] =
    contact.contactInformation match {
      case OrganisationDetails(name) => Some(name)
      case _                         => None
    }

}
