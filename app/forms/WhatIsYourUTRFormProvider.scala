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

import javax.inject.Inject
import forms.mappings.Mappings
import models.UniqueTaxpayerReference
import play.api.data.Form
import play.api.data.Forms.mapping
import utils.RegexConstants

class WhatIsYourUTRFormProvider @Inject() extends Mappings with RegexConstants {

  def apply(taxType: String): Form[UniqueTaxpayerReference] =
    Form(
      mapping("value" -> validatedUTR("whatIsYourUTR.error.required", "whatIsYourUTR.error.invalid", "whatIsYourUTR.error.invalidFormat", utrRegex, taxType))(
        UniqueTaxpayerReference.apply
      )(
        UniqueTaxpayerReference.unapply
      )
    )

}