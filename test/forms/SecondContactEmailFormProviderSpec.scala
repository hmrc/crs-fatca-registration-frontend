package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SecondContactEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "secondContactEmail.error.required"
  val lengthKey = "secondContactEmail.error.length"
  val maxLength = 132

  val form = new SecondContactEmailFormProvider()()

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
