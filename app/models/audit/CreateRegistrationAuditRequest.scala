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

package models.audit

import play.api.libs.json.{Json, OWrites}

case class CreateRegistrationAuditRequest(
  affinityType: String,
  registeringAs: String,
  registrationType: String,
  idType: String,
  idValue: String,
  tradingName: Option[String],
  businessName: Option[String],
  addressLine1: Option[String],
  addressLine2: Option[String],
  city: Option[String],
  region: Option[String],
  postcode: Option[String],
  country: Option[String],
  uprn: Option[String],
  dateOfBirth: Option[String],
  firstContactName: String,
  firstContactEmail: String,
  firstContactTelephone: Option[String],
  secondContactName: Option[String],
  secondContactEmail: Option[String],
  secondContactTelephone: Option[String],
  fatcaId: String
)

object CreateRegistrationAuditRequest {

  implicit val writes: OWrites[CreateRegistrationAuditRequest] =
    Json.writes[CreateRegistrationAuditRequest]

}
