package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.ReporterType

class ReporterTypeFormProvider @Inject() extends Mappings {

  def apply(): Form[ReporterType] =
    Form(
      "value" -> enumerable[ReporterType]("reporterType.error.required")
    )
}
