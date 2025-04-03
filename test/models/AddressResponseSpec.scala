/*
 * Copyright 2025 HM Revenue & Customs
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
import models.register.response.details.AddressResponse

class AddressResponseSpec extends SpecBase {

  "asList should correctly format addressResponse without showing UK Country description" in {
    val address = AddressResponse("100 Parliament Street", None, None, Some("London"), Some("SW1A 2BQ"), "GB", Some(Country("GB", "United Kingdom", None)))

    address.asList mustBe List("100 Parliament Street", "London", "SW1A  2BQ")
  }

  "asList should correctly format addressResponse" in {
    val address = AddressResponse("value 1", Some("value 2"), Some("value 3"), Some("value 4"), Some("XX9 9XX"), "DE", Some(Country("DE", "Denmark", None)))

    address.asList mustBe List("value 1", "value 2", "value 3", "value 4", "XX9  9XX", "Denmark")
  }

}
