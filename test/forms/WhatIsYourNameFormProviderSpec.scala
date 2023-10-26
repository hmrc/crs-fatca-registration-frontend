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

class WhatIsYourNameFormProviderSpec extends StringFieldBehaviours {

  val form = new WhatIsYourNameFormProvider()()

//  ".firstName" - {
//
//    val fieldName   = "firstName"
//    val requiredKey = "whatIsYourName.error.firstName.required"
//    val lengthKey   = "whatIsYourName.error.firstName.length"
//    val maxLength   = 35
//
//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      stringsWithMaxLength(maxLength)
//    )
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )
//
//    behave like mandatoryField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, requiredKey)
//    )
//  }
//
//  ".lastName" - {
//
//    val fieldName   = "lastName"
//    val requiredKey = "whatIsYourName.error.lastName.required"
//    val lengthKey   = "whatIsYourName.error.lastName.length"
//    val maxLength   = 35
//
//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      stringsWithMaxLength(maxLength)
//    )
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )
//
//    behave like mandatoryField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, requiredKey)
//    )
//  }

  ".firstName" - {

    val fieldName   = "firstName"
    val requiredKey = "whatIsYourName.error.firstName.required"
    val invalidKey  = "whatIsYourName.error.firstName.invalid"
    val lengthKey   = "whatIsYourName.error.firstName.length"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringMatchingRegexAndLength(individualNameRegex, maxLength)
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

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jfhf-\\^' `&%",
      FormError(fieldName, invalidKey)
    )
  }

  ".lastName" - {

    val fieldName   = "lastName"
    val requiredKey = "whatIsYourName.error.lastName.required"
    val invalidKey  = "whatIsYourName.error.lastName.invalid"
    val lengthKey   = "whatIsYourName.error.lastName.length"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringMatchingRegexAndLength(individualNameRegex, maxLength)
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

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jfhf-\\^' `&%",
      FormError(fieldName, invalidKey)
    )
  }

}
