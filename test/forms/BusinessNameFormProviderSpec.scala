package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class BusinessNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "businessName.error.required"
  val lengthKey = "businessName.error.length"
  val maxLength = 100

  val form = new BusinessNameFormProvider()()

  ".value" - {

    val fieldName = "value"

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
