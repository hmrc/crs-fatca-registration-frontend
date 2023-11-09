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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class WhatIsYourUTRFormProviderSpec extends StringFieldBehaviours {

  val requiredKey      = "whatIsYourUTR.error.required"
  val invalidFormatKey = "whatIsYourUTR.error.invalidFormat"
  val invalidKey       = "whatIsYourUTR.error.invalid"
  val fixedLength      = 10

  val form = new WhatIsYourUTRFormProvider()("")

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validUtr
    )

    behave like fieldWithFixedLengthNumeric(
      form,
      fieldName,
      fixedLength,
      lengthError = FormError(fieldName, invalidFormatKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidKey)
    )
  }

}
