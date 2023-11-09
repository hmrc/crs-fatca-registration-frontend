package forms

import forms.behaviours.OptionFieldBehaviours
import models.IndWhereDoYouLive
import play.api.data.FormError

class IndWhereDoYouLiveFormProviderSpec extends OptionFieldBehaviours {

  val form = new IndWhereDoYouLiveFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "indWhereDoYouLive.error.required"

    behave like optionsField[IndWhereDoYouLive](
      form,
      fieldName,
      validValues = IndWhereDoYouLive.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
