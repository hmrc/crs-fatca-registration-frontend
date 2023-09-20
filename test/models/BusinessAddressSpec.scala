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

package models

import base.SpecBase
import models.organisation.BusinessAddress
import play.api.libs.json.{JsValue, Json}

class BusinessAddressSpec extends SpecBase {

  val addressResponse: AddressResponse = AddressResponse(
    "line1",
    Some("line2"),
    Some("line3"),
    Some("line4"),
    Some("NE98 1ZZ"),
    "GB"
  )

  val businessAddress: BusinessAddress = BusinessAddress(
    "line1",
    Some("line2"),
    Some("line3"),
    Some("line4"),
    "NE98 1ZZ",
    "GB"
  )

  "BusinessAddress" - {

    "must serialise BusinessAddress" in {
      val json: JsValue =
        Json.parse(
          """
            |{"addressLine1":"line1","addressLine2":"line2","addressLine3": "line3","addressLine4":"line4", "postCode": "NE98 1ZZ","countryCode":"GB"}""".stripMargin
        )

      Json.toJson(businessAddress) mustBe json
    }

    "must de-serialise BusinessAddress" in {
      val json: JsValue =
        Json.parse(
          """
            |{"addressLine1":"line1","addressLine2":"line2","addressLine3": "line3","addressLine4":"line4", "postalCode": "NE98 1ZZ","countryCode":"GB"}""".stripMargin
        )

      json.as[BusinessAddress] mustBe businessAddress
    }

    "fromAddressResponse must create a BusinessAddress from an AddressResponse" in {
      BusinessAddress.fromAddressResponse(addressResponse) mustEqual businessAddress
    }
  }

}
