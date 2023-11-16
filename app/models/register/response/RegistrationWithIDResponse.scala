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
import models.register.response.details.{AddressResponse, IndividualResponse, OrganisationResponse}
import play.api.libs.json.{Format, Json}

case class RegistrationWithIDResponse(registerWithIDResponse: RegisterWithIDResponse) {

  private val responseDetail = registerWithIDResponse.responseDetail

  val safeId: Option[SafeId] = responseDetail.map(_.SAFEID)

  val name: Option[String] = responseDetail.map(_.partnerDetails) collect {
    case organisation: OrganisationResponse => organisation.organisationName
    case individual: IndividualResponse     => s"${individual.firstName} ${individual.lastName}"
  }

  val address: Option[AddressResponse] = responseDetail.map(_.address)
}

object RegistrationWithIDResponse {
  implicit val format: Format[RegistrationWithIDResponse] = Json.format[RegistrationWithIDResponse]
}
