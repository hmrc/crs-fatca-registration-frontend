/*
 * Copyright 2024 HM Revenue & Customs
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

package models.subscription.request

import models.subscription.response.DisplaySubscriptionResponse
import models.{IdentifierType, UserAnswers}
import pages.changeContactDetails.{OrganisationHaveSecondContactPage, OrganisationSecondContactNamePage}
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import utils.UserAnswersHelper

case class UpdateSubscriptionRequest(
  idType: String,
  idNumber: String,
  tradingName: Option[String],
  gbUser: Boolean,
  primaryContact: ContactInformation,
  secondaryContact: Option[ContactInformation]
)

object UpdateSubscriptionRequest extends UserAnswersHelper with Logging {
  implicit lazy val writes: Writes[UpdateSubscriptionRequest] = Json.writes[UpdateSubscriptionRequest]

  // ORGANISATION
  def convertToRequestOrg(displaySubscriptionResponse: DisplaySubscriptionResponse, userAnswers: UserAnswers): Option[UpdateSubscriptionRequest] = {
    val response = displaySubscriptionResponse.success

    val primaryContact = getOrgContactInformation[ChangeOrganisationPrimaryContactDetailsPages](
      response.crfaSubscriptionDetails.primaryContact.contactInformation,
      userAnswers
    )

    val secondaryContact = (userAnswers.get(OrganisationHaveSecondContactPage), userAnswers.get(OrganisationSecondContactNamePage)) match {
      case (Some(true), Some(name)) =>
        getOrgContactInformation[ChangeOrganisationSecondaryContactDetailsPages](
          OrganisationDetails(name),
          userAnswers
        )
      case _ => None
    }

    primaryContact map {
      primaryContact =>
        UpdateSubscriptionRequest(
          IdentifierType.ZCRF,
          displaySubscriptionResponse.subscriptionId.value,
          response.crfaSubscriptionDetails.tradingName,
          response.crfaSubscriptionDetails.gbUser,
          primaryContact,
          secondaryContact
        )
    }
  }

  def getOrgContactInformation[T <: ContactTypePage](
    contactType: ContactType,
    userAnswers: UserAnswers
  )(implicit contactTypePage: T): Option[ContactInformation] = {
    val contactTypeInfo = (contactType, userAnswers.get(contactTypePage.contactNamePage)) match {
      case (_: OrganisationDetails, Some(organisationContactName)) =>
        OrganisationDetails(organisationContactName)
      case _ =>
        val errorMessage = s"Contact name [${contactTypePage.contactNamePage}] is missing from userAnswers"
        logger.warn(errorMessage)
        throw new IllegalStateException(errorMessage)
    }

    for {
      email           <- userAnswers.get(contactTypePage.contactEmailPage)
      havePhoneNumber <- userAnswers.get(contactTypePage.havePhoneNumberPage)
    } yield {
      val phoneNumber = if (havePhoneNumber) userAnswers.get(contactTypePage.contactPhoneNumberPage) else None
      ContactInformation(contactTypeInfo, email, phoneNumber)
    }
  }

  // INDIVIDUAL
  def convertToRequestInd(displaySubscriptionResponse: DisplaySubscriptionResponse, userAnswers: UserAnswers): Option[UpdateSubscriptionRequest] = {
    val response = displaySubscriptionResponse.success

    val individualContact = getIndContactInformation[ChangeIndividualContactDetailsPages](
      response.crfaSubscriptionDetails.primaryContact.contactInformation,
      userAnswers
    )

    individualContact map {
      contact =>
        UpdateSubscriptionRequest(
          IdentifierType.ZCRF,
          displaySubscriptionResponse.subscriptionId.value,
          response.crfaSubscriptionDetails.tradingName,
          response.crfaSubscriptionDetails.gbUser,
          contact,
          None
        )
    }
  }

  def getIndContactInformation[T <: ContactTypePage](
    contactType: ContactType,
    userAnswers: UserAnswers
  )(implicit contactTypePage: T): Option[ContactInformation] =
    for {
      email           <- userAnswers.get(contactTypePage.contactEmailPage)
      havePhoneNumber <- userAnswers.get(contactTypePage.havePhoneNumberPage)
    } yield {
      val phoneNumber = if (havePhoneNumber) userAnswers.get(contactTypePage.contactPhoneNumberPage) else None
      ContactInformation(contactType, email, phoneNumber)
    }

}
