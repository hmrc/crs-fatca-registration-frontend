package forms

import forms.behaviours.OptionFieldBehaviours
import models.IndSelectAddress
import play.api.data.FormError

class IndSelectAddressFormProviderSpec extends OptionFieldBehaviours {

  val form = new IndSelectAddressFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "indSelectAddress.error.required"

    behave like optionsField[IndSelectAddress](
      form,
      fieldName,
      validValues = IndSelectAddress.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
