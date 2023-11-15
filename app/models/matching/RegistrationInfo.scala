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

package models.matching

import models.register.response.details.AddressResponse
import play.api.libs.json._

sealed trait RegistrationInfo

object RegistrationInfo {
  implicit val format: OFormat[RegistrationInfo] = Json.format[RegistrationInfo]
}

case class OrgRegistrationInfo(safeId: SafeId, name: String, address: AddressResponse) extends RegistrationInfo

object OrgRegistrationInfo {

  implicit val format: OFormat[OrgRegistrationInfo] = Json.format[OrgRegistrationInfo]
}

case class IndRegistrationInfo(safeId: SafeId) extends RegistrationInfo

object IndRegistrationInfo {

  implicit val format: OFormat[IndRegistrationInfo] = Json.format[IndRegistrationInfo]
}
