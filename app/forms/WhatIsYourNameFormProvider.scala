package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class WhatIsYourNameFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("whatIsYourName.error.required")
        .verifying(maxLength(100, "whatIsYourName.error.length"))
    )
}
