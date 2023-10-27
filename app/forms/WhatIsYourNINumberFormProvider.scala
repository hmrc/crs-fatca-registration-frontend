/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import javax.inject.Inject
import forms.mappings.{Mappings, StopOnFirstFail}
import play.api.data.Form
import utils.RegexConstants

class WhatIsYourNINumberFormProvider @Inject() extends Mappings with RegexConstants {

  private val maxLength = 9

  private def removeWhitespace(string: String): String = string.split("\\s+").mkString

  def apply(): Form[String] =
    Form(
      "value" -> text("whatIsYourNINumber.error.required")
        .transform[String](nino => removeWhitespace(nino.toUpperCase), nino => nino)
        .verifying(
          StopOnFirstFail[String](
            regexp(ninoFormatRegex, "whatIsYourNINumber.error.format.invalid"),
            regexp(ninoRegex, "whatIsYourNINumber.error.invalid"),
            maxLength(maxLength, "whatIsYourNINumber.error.length")
          )
        )
    )

}
