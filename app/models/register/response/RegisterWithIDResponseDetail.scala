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

package models.register.response

import models.matching.SafeId
import models.register.response.details.{AddressResponse, IndividualResponse, OrganisationResponse, PartnerDetailsResponse}
import models.shared.ContactDetails
import play.api.libs.json.{__, Json, Reads, Writes}

case class RegisterWithIDResponseDetail(
  SAFEID: SafeId,
  ARN: Option[String],
  isEditable: Boolean,
  isAnAgent: Boolean,
  isAnASAgent: Option[Boolean],
  isAnIndividual: Boolean,
  partnerDetails: PartnerDetailsResponse,
  address: AddressResponse,
  contactDetails: ContactDetails
)

object RegisterWithIDResponseDetail {

  implicit lazy val responseDetailsWrites: Writes[RegisterWithIDResponseDetail] = Writes[RegisterWithIDResponseDetail] {
    case RegisterWithIDResponseDetail(safeid,
                                      arn,
                                      isEditable,
                                      isAnAgent,
                                      isAnASAgent,
                                      isAnIndividual,
                                      individual @ IndividualResponse(_, _, _, _),
                                      address,
                                      contactDetails
        ) =>
      Json.obj(
        "SAFEID"         -> safeid.value,
        "ARN"            -> arn,
        "isEditable"     -> isEditable,
        "isAnAgent"      -> isAnAgent,
        "isAnASAgent"    -> isAnASAgent,
        "isAnIndividual" -> isAnIndividual,
        "individual"     -> individual,
        "address"        -> address,
        "contactDetails" -> contactDetails
      )

    case RegisterWithIDResponseDetail(safeid,
                                      arn,
                                      isEditable,
                                      isAnAgent,
                                      isAnASAgent,
                                      isAnIndividual,
                                      organisation @ OrganisationResponse(_, _, _, _),
                                      address,
                                      contactDetails
        ) =>
      Json.obj(
        "SAFEID"         -> safeid.value,
        "ARN"            -> arn,
        "isEditable"     -> isEditable,
        "isAnAgent"      -> isAnAgent,
        "isAnASAgent"    -> isAnASAgent,
        "isAnIndividual" -> isAnIndividual,
        "organisation"   -> organisation,
        "address"        -> address,
        "contactDetails" -> contactDetails
      )
  }

  implicit lazy val responseDetailsReads: Reads[RegisterWithIDResponseDetail] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "SAFEID").read[SafeId] and
        (__ \ "ARN").readNullable[String] and
        (__ \ "isEditable").read[Boolean] and
        (__ \ "isAnAgent").read[Boolean] and
        (__ \ "isAnASAgent").readNullable[Boolean] and
        (__ \ "isAnIndividual").read[Boolean] and
        (__ \ "individual").readNullable[IndividualResponse] and
        (__ \ "organisation").readNullable[OrganisationResponse] and
        (__ \ "address").read[AddressResponse] and
        (__ \ "contactDetails").read[ContactDetails]
    )(
      (safeid, arn, isEditable, isAnAgent, isAnASAgent, isAnIndividual, individual, organisation, address, contactDetails) =>
        (individual, organisation) match {
          case (Some(_), Some(_)) => throw new Exception("Response details cannot have both and organisation or individual element")
          case (Some(ind), _)     => RegisterWithIDResponseDetail(safeid, arn, isEditable, isAnAgent, isAnASAgent, isAnIndividual, ind, address, contactDetails)
          case (_, Some(org))     => RegisterWithIDResponseDetail(safeid, arn, isEditable, isAnAgent, isAnASAgent, isAnIndividual, org, address, contactDetails)
          case (None, None)       => throw new Exception("Response Details must have either an organisation or individual element")
        }
    )
  }

}
