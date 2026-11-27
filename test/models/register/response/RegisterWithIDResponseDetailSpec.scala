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

package models.register.response

import base.SpecBase
import play.api.libs.json.Json

class RegisterWithIDResponseDetailSpec extends SpecBase {

  private val baseJson = Json.obj(
    "SAFEID"         -> "XE0000123456789",
    "ARN"            -> "AARN1234567",
    "isEditable"     -> true,
    "isAnAgent"      -> false,
    "isAnASAgent"    -> false,
    "isAnIndividual" -> true,
    "address" -> Json.obj(
      "addressLine1" -> "100 SuttonStreet",
      "addressLine2" -> "Wokingham",
      "postalCode"   -> "AB1 2CD",
      "countryCode"  -> "GB"
    ),
    "contactDetails" -> Json.obj(
      "phoneNumber"  -> "01332752856",
      "emailAddress" -> "test@example.com"
    )
  )

  private val individualJson = Json.obj(
    "firstName"   -> "John",
    "lastName"    -> "Doe",
    "dateOfBirth" -> "1990-01-01"
  )

  private val organisationJson = Json.obj(
    "organisationName" -> "Test Organisation Ltd",
    "isAGroup"         -> false,
    "organisationType" -> "0001"
  )

  "RegisterWithIDResponseDetail" - {
    "must throw an Exception when JSON contains both individual and organisation elements" in {
      val json = baseJson ++ Json.obj(
        "individual"   -> individualJson,
        "organisation" -> organisationJson
      )

      val result = intercept[Exception] {
        json.as[RegisterWithIDResponseDetail]
      }

      result.getMessage mustBe "Response details cannot have both and organisation or individual element"
    }

    "must throw an Exception when JSON contains neither individual nor organisation element" in {
      val result = intercept[Exception] {
        baseJson.as[RegisterWithIDResponseDetail]
      }

      result.getMessage mustBe "Response Details must have either an organisation or individual element"
    }
  }

}
