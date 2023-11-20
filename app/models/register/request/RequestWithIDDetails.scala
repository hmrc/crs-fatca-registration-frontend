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

package models.register.request

import models.Name
import models.matching.{AutoMatchedRegistrationRequest, RegistrationRequest}
import models.register.request
import models.register.request.details.{PartnerDetails, WithIDIndividual, WithIDOrganisation}
import play.api.libs.json.{__, Json, OWrites, Reads}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class RequestWithIDDetails(
  IDType: String,
  IDNumber: String,
  requiresNameMatch: Boolean,
  isAnAgent: Boolean,
  partnerDetails: Option[PartnerDetails] = None
)

object RequestWithIDDetails {
  val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

  implicit lazy val requestWithIDDetailsReads: Reads[RequestWithIDDetails] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "IDType").read[String] and
        (__ \ "IDNumber").read[String] and
        (__ \ "requiresNameMatch").read[Boolean] and
        (__ \ "isAnAgent").read[Boolean] and
        (__ \ "individual").readNullable[WithIDIndividual] and
        (__ \ "organisation").readNullable[WithIDOrganisation]
    )(
      (idType, idNumber, requiresNameMatch, isAnAgent, individual, organisation) =>
        (individual, organisation) match {
          case (Some(_), Some(_)) => throw new Exception("Request details cannot have both and organisation or individual element")
          case (Some(ind), _)     => request.RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, Option(ind))
          case (_, Some(org))     => request.RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, Option(org))
          case (None, None)       => request.RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent)
        }
    )
  }

  implicit lazy val requestWithIDDetailsWrites: OWrites[RequestWithIDDetails] = OWrites[RequestWithIDDetails] {
    case RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, Some(individual @ WithIDIndividual(_, _, _, _))) =>
      Json.obj(
        "IDType"            -> idType,
        "IDNumber"          -> idNumber,
        "requiresNameMatch" -> requiresNameMatch,
        "isAnAgent"         -> isAnAgent,
        "individual"        -> individual
      )
    case RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, Some(organisation @ WithIDOrganisation(_, _))) =>
      Json.obj(
        "IDType"            -> idType,
        "IDNumber"          -> idNumber,
        "requiresNameMatch" -> requiresNameMatch,
        "isAnAgent"         -> isAnAgent,
        "organisation"      -> organisation
      )
    case RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, None) =>
      Json.obj(
        "IDType"            -> idType,
        "IDNumber"          -> idNumber,
        "requiresNameMatch" -> requiresNameMatch,
        "isAnAgent"         -> isAnAgent
      )
  }

  def apply(name: Name, dob: Option[LocalDate], identifierName: String, identifierValue: String): RequestWithIDDetails =
    RequestWithIDDetails(
      identifierName,
      identifierValue,
      requiresNameMatch = true,
      isAnAgent = false,
      Option(WithIDIndividual(name.firstName, None, name.lastName, dob.map(_.format(dateFormat))))
    )

  def apply(registrationRequest: RegistrationRequest): RequestWithIDDetails =
    RequestWithIDDetails(
      registrationRequest.identifierType,
      registrationRequest.identifier,
      requiresNameMatch = true,
      isAnAgent = false,
      Option(WithIDOrganisation(registrationRequest.name, registrationRequest.businessType.map(_.code).getOrElse("")))
    )

  def apply(registrationRequest: AutoMatchedRegistrationRequest): RequestWithIDDetails =
    RequestWithIDDetails(
      registrationRequest.identifierType,
      registrationRequest.identifier,
      requiresNameMatch = false,
      isAnAgent = false,
      partnerDetails = None
    )

}
