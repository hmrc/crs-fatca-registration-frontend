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

package models.subscription.request

import models.UserAnswers
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import pages._
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import utils.UserAnswersHelper

import scala.language.implicitConversions

sealed trait ContactType

object ContactType {

  implicit lazy val reads: Reads[ContactType] = {

    implicit class ReadsWithContravariantOr[A](a: Reads[A]) {
      def or[B >: A](b: Reads[B]): Reads[B] =
        a.map[B](identity).orElse(b)
    }

    implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
      a.map(identity)

    OrganisationDetails.reads or
      IndividualDetails.reads
  }

  implicit val writes: Writes[ContactType] = Writes[ContactType] {
    case o: OrganisationDetails => Json.toJson(o)
    case i: IndividualDetails   => Json.toJson(i)
  }

}

case class OrganisationDetails(organisationName: String) extends ContactType

object OrganisationDetails {

  implicit lazy val reads: Reads[OrganisationDetails] = {
    import play.api.libs.functional.syntax._
    (__ \ "organisation" \ "organisationName").read[String] fmap OrganisationDetails.apply
  }

  implicit val writes: Writes[OrganisationDetails] =
    (__ \ "organisation" \ "organisationName").write[String] contramap unlift(OrganisationDetails.unapply)

  def convertTo(contactName: Option[String]): Option[OrganisationDetails] =
    contactName.map(OrganisationDetails(_))

}

case class IndividualDetails(firstName: String, middleName: Option[String], lastName: String) extends ContactType

object IndividualDetails {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[IndividualDetails] =
    (
      (__ \ "individual" \ "firstName").read[String] and
        (__ \ "individual" \ "middleName").readNullable[String] and
        (__ \ "individual" \ "lastName").read[String]
    )(IndividualDetails.apply _)

  implicit val writes: OWrites[IndividualDetails] =
    ((__ \ "individual" \ "firstName").write[String] and
      (__ \ "individual" \ "middleName").writeNullable[String] and
      (__ \ "individual" \ "lastName").write[String])(unlift(IndividualDetails.unapply))

  def convertTo(userAnswers: UserAnswers): Option[IndividualDetails] =
    (userAnswers.get(IndWhatIsYourNamePage), userAnswers.get(IndContactNamePage), userAnswers.get(WhatIsYourNamePage)) match {
      case (Some(name), _, _)                  => Some(IndividualDetails(name.firstName, None, name.lastName))
      case (_, Some(individualContactName), _) => Some(IndividualDetails(individualContactName.firstName, None, individualContactName.lastName))
      case (_, _, Some(soleTraderName))        => Some(IndividualDetails(soleTraderName.firstName, None, soleTraderName.lastName))
      case _                                   => None
    }

}

case class ContactInformation(contactInformation: ContactType, email: String, phone: Option[String], mobile: Option[String])

object ContactInformation extends UserAnswersHelper {

  implicit lazy val reads: Reads[ContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.read[ContactType] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
    )(ContactInformation.apply _)
  }

  implicit lazy val writes: OWrites[ContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.write[ContactType] and
        (__ \ "email").write[String] and
        (__ \ "phone").writeNullable[String] and
        (__ \ "mobile").writeNullable[String]
    )(unlift(ContactInformation.unapply))
  }

  def convertToPrimary(userAnswers: UserAnswers): Option[ContactInformation] = {

    lazy val buildBusinessContact =
      (for {
        businessEmail       <- userAnswers.get(ContactEmailPage)
        businessContactInfo <- OrganisationDetails.convertTo(userAnswers.get(ContactNamePage))
      } yield Some(
        ContactInformation(contactInformation = businessContactInfo, email = businessEmail, phone = userAnswers.get(ContactPhonePage), mobile = None)
      )).flatten

    lazy val buildIndividualContact =
      (for {
        individualEmail       <- userAnswers.get(IndContactEmailPage)
        individualContactInfo <- IndividualDetails.convertTo(userAnswers)
      } yield Some(
        ContactInformation(contactInformation = individualContactInfo, email = individualEmail, phone = userAnswers.get(IndContactPhonePage), mobile = None)
      )).flatten

    if (isRegisteringAsBusiness(userAnswers)) {
      buildBusinessContact
    } else {
      buildIndividualContact
    }
  }

  def convertToSecondary(userAnswers: UserAnswers): Either[ApiError, Option[ContactInformation]] = {
    lazy val buildSecondContact =
      for {
        orgDetails     <- OrganisationDetails.convertTo(userAnswers.get(SecondContactNamePage))
        secondaryEmail <- userAnswers.get(SecondContactEmailPage)
      } yield ContactInformation(contactInformation = orgDetails, email = secondaryEmail, phone = userAnswers.get(SecondContactPhonePage), mobile = None)

    if (isRegisteringAsBusiness(userAnswers)) {
      val secondContactHavePhonePage = userAnswers.get(SecondContactHavePhonePage)
      val secondContactPage          = userAnswers.get(HaveSecondContactPage)

      (secondContactPage, secondContactHavePhonePage) match {
        case (Some(false), _)      => Right(None)
        case (Some(true), Some(_)) => Right(buildSecondContact)
        case (Some(true), None)    => Left(MandatoryInformationMissingError("Have Second Contact Phone not answered"))
        case (_, _)                => Left(MandatoryInformationMissingError("Have Second Contact Information not answered"))
      }
    } else {
      Right(None)
    }
  }

}
