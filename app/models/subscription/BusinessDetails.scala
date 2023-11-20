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

package models.subscription

import models.register.response.RegistrationWithIDResponse
import models.register.response.details.{IndividualResponse, OrganisationResponse}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class BusinessDetails(name: String, address: BusinessAddress)

object BusinessDetails {

  implicit lazy val reads: Reads[BusinessDetails] = (
    (JsPath \ "organisation" \ "organisationName").read[String] and
      (JsPath \ "address").read[BusinessAddress]
  )(BusinessDetails.apply _)

  implicit lazy val writes: Writes[BusinessDetails] = Json.writes[BusinessDetails]

  def fromRegistrationMatch(payload: RegistrationWithIDResponse): Option[BusinessDetails] = {
    val addressExtracted =
      payload.registerWithIDResponse.responseDetail.map(_.address)

    val nameExtracted: Option[String] =
      payload.registerWithIDResponse.responseDetail.map {
        _.partnerDetails match {
          case IndividualResponse(firstName, _, lastName, _)   => s"$firstName $lastName"
          case OrganisationResponse(organisationName, _, _, _) => organisationName
        }
      }

    for {
      address <- addressExtracted
      name    <- nameExtracted
    } yield BusinessDetails(
      name,
      BusinessAddress.fromAddressResponse(address)
    )
  }

}
