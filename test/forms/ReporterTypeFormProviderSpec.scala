package forms

import forms.behaviours.OptionFieldBehaviours
import models.ReporterType
import play.api.data.FormError

class ReporterTypeFormProviderSpec extends OptionFieldBehaviours {

  val form = new ReporterTypeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "reporterType.error.required"

    behave like optionsField[ReporterType](
      form,
      fieldName,
      validValues  = ReporterType.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
