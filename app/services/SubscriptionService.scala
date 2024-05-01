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
import models.subscription.response.{DisplayResponseDetail, DisplaySubscriptionResponse}
import models.{IdentifierType, Name, SubscriptionID, UserAnswers}
import pages.changeContactDetails.{OrganisationHaveSecondContactPage, OrganisationSecondContactNamePage}
import play.api.Logging
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SubscriptionService @Inject() (val subscriptionConnector: SubscriptionConnector) extends Logging {

  def updateContactDetails(
    subscriptionId: SubscriptionID,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    getSubscription(subscriptionId).flatMap {
      case Some(displaySubscriptionResponse) =>
        UpdateSubscriptionRequest.convertToRequest(displaySubscriptionResponse, userAnswers) match {
          case Some(updateSubscriptionRequest) => subscriptionConnector.updateSubscription(updateSubscriptionRequest)
          case _ =>
            logger.warn("updateContactDetails: failed to convert userAnswers to RequestDetailForUpdate")
            Future.successful(false)
        }
      case _ =>
        logger.warn("updateContactDetails: readSubscription call failed to fetch data")
        Future.successful(false)
    }

  def checkAndCreateSubscription(
    safeId: SafeId,
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, SubscriptionID]] =
    getSubscription(safeId) flatMap {
      case Some(displaySubscriptionResponse) =>
        EitherT.rightT(displaySubscriptionResponse.subscriptionId).value
      case _ =>
        (CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, affinityGroup) match {
          case Some(subscriptionRequest) =>
            subscriptionConnector.createSubscription(subscriptionRequest)
          case _ =>
            EitherT.leftT(MandatoryInformationMissingError())
        }).value
    }

  def getSubscription(safeId: SafeId)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[DisplaySubscriptionResponse]] =
    subscriptionConnector.readSubscription(ReadSubscriptionRequest(IdentifierType.SAFEID, safeId.value))

  def getSubscription(subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[DisplaySubscriptionResponse]] =
    subscriptionConnector.readSubscription(ReadSubscriptionRequest(IdentifierType.FATCAID, subscriptionId.value))

  // INDIVIDUAL
  def checkIfIndContactDetailsHasChanged(
    subscriptionResponseFromBackend: DisplaySubscriptionResponse,
    userAnswers: UserAnswers
  ): Option[Boolean] = {
    val subscriptionDetailFromBackend = subscriptionResponseFromBackend.success
    for {
      primaryContactDetailsFromUserAnswers <- getIndividualContactDetailsFromUserAnswers(userAnswers, subscriptionDetailFromBackend)
      primaryContactHasChanged = !primaryContactDetailsFromUserAnswers.equals(subscriptionDetailFromBackend.primaryContact)
    } yield primaryContactHasChanged
  }

  def populateUserAnswersFromIndSubscription(userAnswers: UserAnswers, responseDetail: DisplayResponseDetail): Option[UserAnswers] =
    for {
      contactSet: UserAnswers <-
        populateUserAnswersFromIndContactInfo[ChangeIndividualContactDetailsPages](userAnswers, responseDetail.primaryContact)
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
      subscriptionDetailFromBackend.primaryContact.contactInformation
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
      primaryContactHasChanged            = !primaryContactDetailsFromUserAnswers.equals(subscriptionDetailFromBackend.primaryContact)
      secondContactHasChanged             = !secondContactDetailsFromUserAnswers.equals(subscriptionDetailFromBackend.secondaryContact)
    } yield primaryContactHasChanged || secondContactHasChanged
  }

  def populateUserAnswersFromOrgSubscription(userAnswers: UserAnswers, responseDetail: DisplayResponseDetail): Option[UserAnswers] =
    for {
      primaryContactSet <- populateUserAnswersFromOrgContactInfo[ChangeOrganisationPrimaryContactDetailsPages](userAnswers, responseDetail.primaryContact)
      hasSecondContact = responseDetail.secondaryContact.nonEmpty
      haveSecondContactSet <- primaryContactSet.set(OrganisationHaveSecondContactPage, hasSecondContact).toOption
      secondaryContactSet <- responseDetail.secondaryContact
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
      subscriptionDetailFromBackend.primaryContact.contactInformation
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
    (subscription.secondaryContact, userAnswers.get(OrganisationSecondContactNamePage)) match {
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
      case _ => Try(userAnswers)
    }

}
