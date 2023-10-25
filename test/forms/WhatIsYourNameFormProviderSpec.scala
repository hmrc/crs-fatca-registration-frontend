package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class WhatIsYourNameFormProviderSpec extends StringFieldBehaviours {

  val form = new WhatIsYourNameFormProvider()()

  ".firstName" - {

    val fieldName = "firstName"
    val requiredKey = "whatIsYourName.error.firstName.required"
    val lengthKey = "whatIsYourName.error.firstName.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".lastName" - {

    val fieldName = "lastName"
    val requiredKey = "whatIsYourName.error.lastName.required"
    val lengthKey = "whatIsYourName.error.lastName.length"
    val maxLength = 3535

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
