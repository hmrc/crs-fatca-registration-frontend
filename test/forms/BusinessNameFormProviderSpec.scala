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

class BusinessNameFormProviderSpec extends StringFieldBehaviours {

  val selectedReporterType = "llp"
  val requiredKey          = "businessName.error.required.llp"
  val lengthKey            = "businessName.error.length.llp"
  val invalidKey           = "businessName.error.invalid.llp"
  val maxLength            = 105

  val form = new BusinessNameFormProvider()(selectedReporterType)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringMatchingRegexAndLength(orgNameRegex, maxLength)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      invalidString = "@@",
      error = FormError(fieldName, invalidKey)
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
