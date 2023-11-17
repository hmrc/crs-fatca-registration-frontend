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

package models.register.request.details

import models.Address
import play.api.libs.json.{__, Json, OWrites, Reads}

case class AddressRequest(addressLine1: String,
                          addressLine2: Option[String],
                          addressLine3: String,
                          addressLine4: Option[String],
                          postalCode: Option[String],
                          countryCode: String
)

object AddressRequest {

  def apply(address: Address): AddressRequest =
    AddressRequest(
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      postalCode = address.postCode,
      countryCode = address.country.code
    )

  implicit lazy val writes: OWrites[AddressRequest] = OWrites[AddressRequest] {
    address =>
      Json.obj(
        "addressLine1" -> address.addressLine1,
        "addressLine2" -> address.addressLine2,
        "addressLine3" -> address.addressLine3,
        "addressLine4" -> address.addressLine4,
        "postalCode"   -> address.postalCode,
        "countryCode"  -> address.countryCode
      )
  }

  implicit lazy val reads: Reads[AddressRequest] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "addressLine1").read[String] and
        (__ \ "addressLine2").readNullable[String] and
        (__ \ "addressLine3").read[String] and
        (__ \ "addressLine4").readNullable[String] and
        (__ \ "postalCode").readNullable[String] and
        (__ \ "countryCode").read[String]
    )(
      (a1, a2, a3, a4, pc, cc) => AddressRequest(a1, a2, a3, a4, pc, cc)
    )
  }

}
