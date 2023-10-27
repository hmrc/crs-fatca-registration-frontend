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

class WhatIsYourNINumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey      = "whatIsYourNINumber.error.required"
  val invalidFormatKey = "whatIsYourNINumber.error.format.invalid"
  val invalidKey       = "whatIsYourNINumber.error.invalid"
  val lengthKey        = "whatIsYourNINumber.error.length"
  val maxLength        = 9

  val form = new IndWhatIsYourNINumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validNino
    )

    behave like fieldWithMaxLengthAndInvalid(
      form,
      fieldName,
      maxLength = maxLength,
      errors = Seq(
        FormError(fieldName, invalidFormatKey, Seq(ninoFormatRegex))
      )
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      invalidString = "QQ 12 34 56 C",
      error = FormError(fieldName, invalidKey, Seq(ninoRegex))
    )
  }

}
