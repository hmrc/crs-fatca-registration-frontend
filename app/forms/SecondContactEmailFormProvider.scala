package forms

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import utils.RegExConstants

class SecondContactEmailFormProvider @Inject() extends Mappings {

  private val maxLength = 35

  def apply(): Form[String] =
    Form(
      "value" -> validatedText(
        "secondContactEmail.error.required",
        "secondContactEmail.error.invalid",
        "secondContactEmail.error.length",
        RegExConstants.emailRegex,
        maxLength
      )
    )
}
