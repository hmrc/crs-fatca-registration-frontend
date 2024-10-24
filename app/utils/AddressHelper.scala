/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.{Address, AddressLookup}

object AddressHelper {

  def formatAddress(address: AddressLookup): String = {
    val lines = Seq(
      address.addressLine1,
      address.addressLine2,
      address.addressLine3,
      address.addressLine4,
      Option(address.town),
      Option(address.postcode),
      address.county,
      address.country.map(_.description)
    ).flatten
    lines.mkString(", ")
  }

  def formatAddress(address: Address): String = {
    val lines = Seq(address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4, address.postCode, address.country.description)
      .collect {
        case s: String => s
        case Some(s)   => s
      }
    lines.mkString(", ")
  }

}
