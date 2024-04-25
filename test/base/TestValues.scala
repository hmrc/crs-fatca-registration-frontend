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

package base

import models.matching.SafeId
import models.{IndContactName, Name, SubscriptionID, UniqueTaxpayerReference}

trait TestValues {

  val UserAnswersId       = "id"
  val UtrValue            = "1234567890"
  private val SafeIdValue = "XE0000123456789"

  private val SubscriptionIdValue = "XA0000123456789"

  val OrgName      = "Some Test Org"
  val TestNiNumber = "CC123456C"
  val TestEmail    = "test@test.com"

  val TestPhoneNumber       = "0188899999"
  val TestFaxNumber         = "0987654323"
  val TestMobilePhoneNumber = "07321012345"
  val TestPostCode          = "ZZ1 1ZZ"
  val TestDate              = "1999-12-20"

  val FirstName  = "Fred"
  val LastName   = "Flintstone"
  val MiddleName = "Flint"

  val IndividualKey   = "individual"
  val OrganisationKey = "organisation"

  val utr: UniqueTaxpayerReference = UniqueTaxpayerReference(UtrValue)
  val safeId: SafeId               = SafeId(SafeIdValue)

  val subscriptionId: SubscriptionID = SubscriptionID(SubscriptionIdValue)

  val name: Name = Name(FirstName, LastName)

  val individualContactName: IndContactName = IndContactName(FirstName, LastName)

}
