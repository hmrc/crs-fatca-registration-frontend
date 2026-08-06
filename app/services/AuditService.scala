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
import models.audit.CreateRegistrationAuditRequest
import models.matching.OrgRegistrationInfo
import models.{Address, Country, SubscriptionID, UserAnswers}
import pages._
import pages.changeContactDetails.{OrganisationSecondContactEmailPage, OrganisationSecondContactNamePage, OrganisationSecondContactPhonePage}
import play.api.Logging
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswersHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject() (
  auditConnector: AuditConnector
)(implicit ec: ExecutionContext)
    extends Logging
    with UserAnswersHelper {

  private case class RegistrationDetails(
    registrationType: String,
    idType: String,
    idValue: String
  )
//TODO - worth noting this event will only fire if all required fields are found for the audit, of course optional fields being ommitted will cause no issue
  // and the audit failing will not impact the users journey at all - I have put some simple logging in for now

  def sendCreateRegistration(
    userAnswers: UserAnswers,
    subscriptionId: SubscriptionID,
    affinityGroup: AffinityGroup
  )(implicit hc: HeaderCarrier): Future[Unit] =
    buildCreateRegistration(
      userAnswers = userAnswers,
      subscriptionId = subscriptionId,
      affinityGroup = affinityGroup
    ) match {
      case Some(auditRequest) =>
        auditConnector
          .sendCreateRegistration(auditRequest)
          .map(
            _ => ()
          )
          .recover {
            case exception =>
              logger.error(
                "Failed to send CreateRegistration audit event",
                exception
              )
              ()
          }

      case None =>
        logger.error(
          "CreateRegistration audit was not sent because required audit information was missing"
        )
        Future.successful(())
    }

  private def buildCreateRegistration(
    userAnswers: UserAnswers,
    subscriptionId: SubscriptionID,
    affinityGroup: AffinityGroup
  ): Option[CreateRegistrationAuditRequest] = {

    val isBusiness = isRegisteringAsBusiness(userAnswers)

    val registrationDetails =
      extractRegistrationDetails(userAnswers, affinityGroup)

    for {
      address           <- extractAddress(userAnswers)
      firstContactName  <- extractFirstContactName(userAnswers, isBusiness)
      firstContactEmail <- extractFirstContactEmail(userAnswers, isBusiness)
    } yield CreateRegistrationAuditRequest(
      affinityType = affinityGroup.toString,
      registeringAs = if (isBusiness) "Organisation" else "Individual",
      registrationType = registrationDetails.registrationType,
      idType = registrationDetails.idType,
      idValue = registrationDetails.idValue,
      tradingName = extractTradingName(userAnswers),
      businessName = extractBusinessName(userAnswers),
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      city = address.addressLine3,
      region = address.addressLine4,
      postcode = address.postCode,
      country = address.country.description,
      uprn = None,
      dateOfBirth = extractDateOfBirth(userAnswers),
      firstContactName = firstContactName,
      firstContactEmail = firstContactEmail,
      firstContactTelephone =
        extractFirstContactTelephone(userAnswers, isBusiness),
      secondContactName = extractSecondContactName(userAnswers),
      secondContactEmail = extractSecondContactEmail(userAnswers),
      secondContactTelephone = extractSecondContactTelephone(userAnswers),
      fatcaId = subscriptionId.value
    )
  }

  private def extractRegistrationDetails(
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): RegistrationDetails = {

    val autoMatchedUtr =
      userAnswers
        .get(AutoMatchedUTRPage)
        .map(_.uniqueTaxPayerReference)
        .flatMap(optionalNonEmpty)

    val nino = extractNino(userAnswers)
    val utr  = extractUtr(userAnswers)
//TODO - debated making registrationType its own ADT, may still do that just to make the modeling a little clearer

    (autoMatchedUtr, nino, utr) match {
      case (Some(value), _, _) =>
        RegistrationDetails(
          registrationType = "CTAutomatched",
          idType = "UTR",
          idValue = value
        )

      case (None, Some(value), _) =>
        RegistrationDetails(
          registrationType = "IndividualWithID",
          idType = "NINO",
          idValue = value
        )

      case (None, None, Some(value)) =>
        RegistrationDetails(
          registrationType = "OrgWithID",
          idType = "UTR",
          idValue = value
        )

      case (None, None, None) if affinityGroup == AffinityGroup.Individual =>
        RegistrationDetails(
          registrationType = "IndividualWithoutID",
          idType = "NotProvided",
          idValue = "NotProvided"
        )

      case _ =>
        RegistrationDetails(
          registrationType = "OrgWithoutID",
          idType = "NotProvided",
          idValue = "NotProvided"
        )
    }
  }
//TODO - this wall of extract functions I debated moving out of the service but I felt clicking between files may have been harder to digest
  // There are plenty of 'gets' here which may seem risky but, we can determine that the user would of entered at least *one* of these pages or had the data pulled from somewhere by the time of CYA completion.

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
      .orElse(userAnswers.get(BusinessNamePage))
      .flatMap(optionalNonEmpty)

  private def extractBusinessName(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(BusinessNameWithoutIDPage)
      .orElse {
        userAnswers
          .get(RegistrationInfoPage)
          .collect {
            case OrgRegistrationInfo(_, name, _) =>
              name
          }
      }
      .flatMap(optionalNonEmpty)

  private def extractDateOfBirth(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(IndDateOfBirthPage)
      .orElse(userAnswers.get(DateOfBirthWithoutIdPage))
      .map(_.toString)
      .flatMap(optionalNonEmpty)

  private def extractFirstContactName(
    userAnswers: UserAnswers,
    isBusiness: Boolean
  ): Option[String] =
    if (isBusiness) {
      userAnswers
        .get(ContactNamePage)
        .flatMap(optionalNonEmpty)
    } else {
      userAnswers
        .get(IndWhatIsYourNamePage)
        .orElse(userAnswers.get(WhatIsYourNamePage))
        .map(_.fullName)
        .flatMap(optionalNonEmpty)
    }

  private def extractFirstContactEmail(
    userAnswers: UserAnswers,
    isBusiness: Boolean
  ): Option[String] =
    if (isBusiness) {
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
    isBusiness: Boolean
  ): Option[String] =
    if (isBusiness) {
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
      .orElse(userAnswers.get(SecondContactNamePage))
      .flatMap(optionalNonEmpty)

  private def extractSecondContactEmail(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(OrganisationSecondContactEmailPage)
      .orElse(userAnswers.get(SecondContactEmailPage))
      .flatMap(optionalNonEmpty)

  private def extractSecondContactTelephone(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers
      .get(OrganisationSecondContactPhonePage)
      .orElse(userAnswers.get(SecondContactPhonePage))
      .flatMap(optionalNonEmpty)

  private def extractAddress(
    userAnswers: UserAnswers
  ): Option[Address] =
    if (isRegisteringAsBusiness(userAnswers)) {
      extractBusinessAddress(userAnswers)
    } else {
      extractIndividualAddress(userAnswers)
    }

//TODO - So this extracts the business address, the only snag i've found is that when the address
//  is a matched one, it is returned as this type AddressResponse instead of Address, meaning what values are optional differ, mainly addressline3 and the country - welcome any suggestions here

  private def extractBusinessAddress(
    userAnswers: UserAnswers
  ): Option[Address] =
    userAnswers
      .get(NonUKBusinessAddressWithoutIDPage)
      .orElse {
        userAnswers
          .get(RegistrationInfoPage)
          .collect {
            case OrgRegistrationInfo(_, _, address) =>
              Address(
                addressLine1 = address.addressLine1,
                addressLine2 = address.addressLine2,
                addressLine3 = address.addressLine3.getOrElse(""),
                addressLine4 = address.addressLine4,
                postCode = address.postalCode,
                country = address.country.getOrElse(
                  Country(
                    code = address.countryCode,
                    description = ""
                  )
                )
              )
          }
      }

  private def extractIndividualAddress(
    userAnswers: UserAnswers
  ): Option[Address] =
    userAnswers.get(IndWhereDoYouLivePage) match {
      case Some(true) =>
        userAnswers
          .get(IndSelectedAddressLookupPage)
          .flatMap(_.toAddress)
          .orElse {
            userAnswers.get(IndUKAddressWithoutIdPage)
          }

      case _ =>
        userAnswers.get(IndNonUKAddressWithoutIdPage)
    }

  // TODO - purpose of this helper is to make sure no unintentional empty strings ("") or empty quotes slip through as a valid string entry rather then None
  private def optionalNonEmpty(
    value: String
  ): Option[String] =
    Option(value)
      .map(_.trim)
      .filter(_.nonEmpty)

}
