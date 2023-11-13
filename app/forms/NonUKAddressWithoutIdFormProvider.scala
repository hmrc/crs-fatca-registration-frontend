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

package forms

import models.Address
import forms.mappings.Mappings
import models.Country
import play.api.data.Form
import play.api.data.Forms._
import utils.RegexConstants

import javax.inject.Inject

class NonUKAddressWithoutIdFormProvider @Inject() extends Mappings with RegexConstants {

  val addressLineLength = 35

  def apply(countryList: Seq[Country]): Form[Address] = Form(
    mapping(
      "addressLine1" -> validatedText(
        "addressWithoutId.error.addressLine1.required",
        "addressWithoutId.error.addressLine1.invalid",
        "addressWithoutId.error.addressLine1.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine2" -> validatedOptionalText("addressWithoutId.error.addressLine2.invalid",
                                              "addressWithoutId.error.addressLine2.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "addressLine3" -> validatedText(
        "addressWithoutId.error.addressLine3.required",
        "addressWithoutId.error.addressLine3.invalid",
        "addressWithoutId.error.addressLine3.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine4" -> validatedOptionalText("addressWithoutId.error.addressLine4.invalid",
                                              "addressWithoutId.error.addressLine4.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "postCode" -> optionalPostcode(
        "addressWithoutId.error.postcode.required",
        "addressWithoutId.error.postcode.length",
        "addressWithoutId.error.postcode.invalid",
        regexPostcode,
        "country"
      ),
      "country" -> text("addressWithoutId.error.country.required")
        .verifying("addressWithoutId.error.country.required", value => countryList.exists(_.code == value))
        .transform[Country](value => countryList.find(_.code == value).get, _.code)
    )(Address.apply)(Address.unapply)
  )

}
