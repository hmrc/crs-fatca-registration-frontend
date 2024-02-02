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

package forms.changeContactDetails

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import utils.RegexConstants

class OrganisationSecondContactNameFormProviderSpec extends StringFieldBehaviours with RegexConstants {

  val requiredKey = "organisationSecondContactName.error.required"
  val invalidKey  = "organisationSecondContactName.error.invalid"
  val lengthKey   = "organisationSecondContactName.error.length"
  val maxLength   = 35

  val form = new OrganisationSecondContactNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringMatchingRegexAndLength(orgNameRegex, maxLength)
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like mandatoryField(
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
