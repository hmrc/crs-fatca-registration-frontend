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

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.SubscriptionConnector
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.SafeId
import models.subscription.request._
import models.subscription.response.{
  DisplayResponseDetail,
  DisplaySubscriptionResponse,
  IndividualRegistrationType,
  OrganisationRegistrationType,
  RegistrationType
}
import models.{Name, SubscriptionID, UserAnswers}
import pages.changeContactDetails.{OrganisationHaveSecondContactPage, OrganisationSecondContactNamePage}
import play.api.Logging
import repositories.SubscriptionRepository
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SubscriptionService @Inject() (val subscriptionConnector: SubscriptionConnector, val subscriptionRepository: SubscriptionRepository) extends Logging {

  def updateOrgContactDetails(
    subscriptionId: SubscriptionID,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    updateContactDetails(subscriptionId, userAnswers, UpdateSubscriptionRequest.convertToRequestOrg)
      .map(_.isDefined)

  def updateIndContactDetails(
    subscriptionId: SubscriptionID,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    updateContactDetails(subscriptionId, userAnswers, UpdateSubscriptionRequest.convertToRequestInd)
      .map(_.isDefined)

  def updateContactDetailsWithAuditData(
    subscriptionId: SubscriptionID,
    userAnswers: UserAnswers,
    registrationType: RegistrationType
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SuccessfulContactDetailsUpdate]] = {
    val buildRequest: (DisplaySubscriptionResponse, UserAnswers) => Option[UpdateSubscriptionRequest] = registrationType match {
      case OrganisationRegistrationType => UpdateSubscriptionRequest.convertToRequestOrg
      case IndividualRegistrationType   => UpdateSubscriptionRequest.convertToRequestInd
    }

    updateContactDetails(subscriptionId, userAnswers, buildRequest)
  }

  def checkAndCreateSubscription(
    safeId: SafeId,
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, SubscriptionID]] =
    getSubscription(safeId) flatMap {
      case Some(subscriptionId) =>
        EitherT.rightT(subscriptionId).value
      case _ =>
        (CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, affinityGroup) match {
          case Some(subscriptionRequest) =>
            subscriptionConnector.createSubscription(subscriptionRequest)
          case _ =>
            EitherT.leftT(MandatoryInformationMissingError())
        }).value
    }

  def getSubscription(safeId: SafeId)(implicit
    ec: ExecutionContext
  ): Future[Option[SubscriptionID]] =
    subscriptionRepository.get(safeId.value).map(_.map(_.subscriptionID))

  def getSubscription(subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[DisplaySubscriptionResponse]] =
    subscriptionConnector.readSubscription(ReadSubscriptionRequest(subscriptionId.value))

  // INDIVIDUAL
  def checkIfIndContactDetailsHasChanged(
    subscriptionResponseFromBackend: DisplaySubscriptionResponse,
    userAnswers: UserAnswers
  ): Option[Boolean] = {
    val subscriptionDetailFromBackend = subscriptionResponseFromBackend.success
    for {
      primaryContactDetailsFromUserAnswers <- getIndividualContactDetailsFromUserAnswers(userAnswers, subscriptionDetailFromBackend)
      primaryContactHasChanged = !primaryContactDetailsFromUserAnswers.equals(subscriptionDetailFromBackend.crfaSubscriptionDetails.primaryContact)
    } yield primaryContactHasChanged
  }

  def populateUserAnswersFromIndSubscription(userAnswers: UserAnswers, responseDetail: DisplayResponseDetail): Option[UserAnswers] =
    for {
      contactSet: UserAnswers <-
        populateUserAnswersFromIndContactInfo[ChangeIndividualContactDetailsPages](userAnswers, responseDetail.crfaSubscriptionDetails.primaryContact)
    } yield contactSet

  private def populateUserAnswersFromIndContactInfo[T <: ContactTypePage](
    userAnswers: UserAnswers,
    contactInfo: ContactInformation
  )(implicit contactTypePage: T): Option[UserAnswers] = {
    val userAnswersWithContactInfoPopulated = for {
      emailSet       <- userAnswers.set(contactTypePage.contactEmailPage, contactInfo.email)
      phoneNumberSet <- emailSet.set(contactTypePage.contactPhoneNumberPage, contactInfo.phone.getOrElse("Not providedd"))
      updatedAnswers <- phoneNumberSet.set(contactTypePage.havePhoneNumberPage, contactInfo.phone.exists(_.nonEmpty))
    } yield updatedAnswers

    userAnswersWithContactInfoPopulated.toOption
  }

  private def getIndividualContactDetailsFromUserAnswers(
    userAnswers: UserAnswers,
    subscriptionDetailFromBackend: DisplayResponseDetail
  ): Option[ContactInformation] =
    getContactDetailsFromUserAnswers[ChangeIndividualContactDetailsPages](
      userAnswers,
      subscriptionDetailFromBackend.crfaSubscriptionDetails.primaryContact.contactInformation
    )

  // ORGANISATION

  def checkIfOrgContactDetailsHasChanged(
    subscriptionResponseFromBackend: DisplaySubscriptionResponse,
    userAnswers: UserAnswers
  ): Option[Boolean] = {
    val subscriptionDetailFromBackend = subscriptionResponseFromBackend.success

    for {
      primaryContactDetailsFromUserAnswers <- getOrgPrimaryContactDetailsFromUserAnswers(userAnswers, subscriptionDetailFromBackend)
      secondContactDetailsFromUserAnswers = getOrgSecondContactDetailsFromUserAnswers(userAnswers, subscriptionDetailFromBackend)
      primaryContactHasChanged            = !primaryContactDetailsFromUserAnswers.equals(subscriptionDetailFromBackend.crfaSubscriptionDetails.primaryContact)
      secondContactHasChanged             = !secondContactDetailsFromUserAnswers.equals(subscriptionDetailFromBackend.crfaSubscriptionDetails.secondaryContact)
    } yield primaryContactHasChanged || secondContactHasChanged
  }

  def populateUserAnswersFromOrgSubscription(userAnswers: UserAnswers, responseDetail: DisplayResponseDetail): Option[UserAnswers] =
    for {
      primaryContactSet <-
        populateUserAnswersFromOrgContactInfo[ChangeOrganisationPrimaryContactDetailsPages](userAnswers, responseDetail.crfaSubscriptionDetails.primaryContact)
      hasSecondContact = responseDetail.crfaSubscriptionDetails.secondaryContact.nonEmpty
      haveSecondContactSet <- primaryContactSet.set(OrganisationHaveSecondContactPage, hasSecondContact).toOption
      secondaryContactSet <- responseDetail.crfaSubscriptionDetails.secondaryContact
        .flatMap(
          secondContact =>
            populateUserAnswersFromOrgContactInfo[ChangeOrganisationSecondaryContactDetailsPages](
              haveSecondContactSet,
              secondContact
            )
        )
        .orElse(Option(haveSecondContactSet))
    } yield secondaryContactSet

  private def populateUserAnswersFromOrgContactInfo[T <: ContactTypePage](
    userAnswers: UserAnswers,
    contactInfo: ContactInformation
  )(implicit contactTypePage: T): Option[UserAnswers] = {

    val userAnswersWithContactInfoPopulated = for {
      contactNameSet <- setContactName(userAnswers, contactInfo)
      emailSet       <- contactNameSet.set(contactTypePage.contactEmailPage, contactInfo.email)
      phoneNumberSet <- emailSet.set(contactTypePage.contactPhoneNumberPage, contactInfo.phone.getOrElse("Not provided"))
      updatedAnswers <- phoneNumberSet.set(contactTypePage.havePhoneNumberPage, contactInfo.phone.exists(_.nonEmpty))
    } yield updatedAnswers

    userAnswersWithContactInfoPopulated.toOption
  }

  private def getOrgPrimaryContactDetailsFromUserAnswers(
    userAnswers: UserAnswers,
    subscriptionDetailFromBackend: DisplayResponseDetail
  ): Option[ContactInformation] =
    getContactDetailsFromUserAnswers[ChangeOrganisationPrimaryContactDetailsPages](
      userAnswers,
      subscriptionDetailFromBackend.crfaSubscriptionDetails.primaryContact.contactInformation
    )

  private def getOrgSecondContactDetailsFromUserAnswers(
    userAnswers: UserAnswers,
    subscription: DisplayResponseDetail
  ): Option[ContactInformation] =
    userAnswers.get(OrganisationHaveSecondContactPage) match {
      case Some(true) =>
        getContactType(userAnswers, subscription)
          .flatMap(getContactDetailsFromUserAnswers[ChangeOrganisationSecondaryContactDetailsPages](userAnswers, _))
      case _ => None
    }

  private def getContactDetailsFromUserAnswers[T <: ContactTypePage](
    userAnswers: UserAnswers,
    contactType: ContactType
  )(implicit contactTypePage: T): Option[ContactInformation] = {

    val updatedContactType: ContactType = userAnswers.get(contactTypePage.contactNamePage) match {
      case Some(name) => OrganisationDetails(name)
      case _          => contactType
    }

    for {
      email           <- userAnswers.get(contactTypePage.contactEmailPage)
      havePhoneNumber <- userAnswers.get(contactTypePage.havePhoneNumberPage)
    } yield {
      val phoneNumber = if (havePhoneNumber) userAnswers.get(contactTypePage.contactPhoneNumberPage) else None
      ContactInformation(updatedContactType, email, phoneNumber)
    }
  }

  private def getContactType(userAnswers: UserAnswers, subscription: DisplayResponseDetail): Option[ContactType] =
    (subscription.crfaSubscriptionDetails.secondaryContact, userAnswers.get(OrganisationSecondContactNamePage)) match {
      case (_, Some(name)) =>
        Option(OrganisationDetails(name))
      case (Some(contactInformation), _) =>
        Option(contactInformation.contactInformation)
      case _ => None
    }

  private def setContactName[T <: ContactTypePage](
    userAnswers: UserAnswers,
    contactInfo: ContactInformation
  )(implicit contactTypePage: T): Try[UserAnswers] =
    contactInfo.contactInformation match {
      case organisation: OrganisationDetails =>
        userAnswers.set(contactTypePage.contactNamePage, organisation.name)
      case individual: IndividualDetails =>
        val individualName = Name(individual.firstName, individual.lastName).fullName
        userAnswers.set(contactTypePage.contactNamePage, individualName)
    }

  private def updateContactDetails(
    subscriptionId: SubscriptionID,
    userAnswers: UserAnswers,
    buildRequest: (DisplaySubscriptionResponse, UserAnswers) => Option[UpdateSubscriptionRequest]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SuccessfulContactDetailsUpdate]] =
    getSubscription(subscriptionId).flatMap {
      case Some(original) =>
        buildRequest(original, userAnswers) match {
          case Some(updated) =>
            subscriptionConnector.updateSubscription(updated).map {
              case true  => Some(SuccessfulContactDetailsUpdate(original, updated))
              case false => None
            }
          case None =>
            logger.warn("updateContactDetails: failed to convert userAnswers to RequestDetailForUpdate")
            Future.successful(None)
        }
      case None =>
        logger.warn("updateContactDetails: readSubscription call failed to fetch data")
        Future.successful(None)
    }

  case class SuccessfulContactDetailsUpdate(
    original: DisplaySubscriptionResponse,
    updated: UpdateSubscriptionRequest
  )

}
