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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class CreateRegistrationAuditRequestSpec
    extends AnyFreeSpec
    with Matchers {

  "CreateRegistrationAuditRequest" - {

    "must write to JSON" in {

      val request =
        CreateRegistrationAuditRequest(
          affinityType = "Organisation",
          registeringAs = "Organisation",
          registrationType = "OrgWithID",
          idType = "UTR",
          idValue = "1234567890",
          tradingName = Some("Trading Name"),
          businessName = Some("Business Name"),
          addressLine1 = Some("1 Test Street"),
          addressLine2 = Some("Test Area"),
          city = Some("London"),
          region = Some("Greater London"),
          postcode = Some("AA1 1AA"),
          country = Some("GB"),
          uprn = Some("123456789"),
          dateOfBirth = None,
          firstContactName = "First Contact",
          firstContactEmail = "first@example.com",
          firstContactTelephone = Some("07123456789"),
          secondContactName = Some("Second Contact"),
          secondContactEmail = Some("second@example.com"),
          secondContactTelephone = Some("07987654321"),
          fatcaId = "FATCA123456"
        )

      val json =
        Json.toJson(request)

      (json \ "affinityType").as[String] mustBe "Organisation"
      (json \ "registeringAs").as[String] mustBe "Organisation"
      (json \ "registrationType").as[String] mustBe "OrgWithID"
      (json \ "idType").as[String] mustBe "UTR"
      (json \ "idValue").as[String] mustBe "1234567890"

      (json \ "tradingName").as[String] mustBe "Trading Name"
      (json \ "businessName").as[String] mustBe "Business Name"

      (json \ "addressLine1").as[String] mustBe "1 Test Street"
      (json \ "addressLine2").as[String] mustBe "Test Area"
      (json \ "city").as[String] mustBe "London"
      (json \ "region").as[String] mustBe "Greater London"
      (json \ "postcode").as[String] mustBe "AA1 1AA"
      (json \ "country").as[String] mustBe "GB"

      (json \ "firstContactName").as[String] mustBe "First Contact"
      (json \ "firstContactEmail").as[String] mustBe "first@example.com"

      (json \ "fatcaId").as[String] mustBe "FATCA123456"
    }
  }

}
