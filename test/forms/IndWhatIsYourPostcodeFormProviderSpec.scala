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

class IndWhatIsYourPostcodeFormProviderSpec extends StringFieldBehaviours {

  val form = new IndWhatIsYourPostcodeFormProvider()()

  ".postCode" - {

    val fieldName      = "postCode"
    val requiredKey    = "indWhatIsYourPostcode.error.required"
    val lengthKey      = "indWhatIsYourPostcode.error.length"
    val invalidKey     = "indWhatIsYourPostcode.error.invalid"
    val invalidCharKey = "indWhatIsYourPostcode.error.chars"

    val postCodeMaxLength = 10


    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPostCodes
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = postCodeMaxLength,
      lengthError = FormError(fieldName, lengthKey)
    )


    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "xx9 9xx9",
      FormError(fieldName, invalidKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "!#2",
      FormError(fieldName, invalidCharKey),
      Some("chars")
    )
  }

}
