package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class BusinessNameFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("businessName.error.required")
        .verifying(maxLength(100, "businessName.error.length"))
    )
}
