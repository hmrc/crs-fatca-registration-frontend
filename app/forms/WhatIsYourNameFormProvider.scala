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
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.WhatIsYourName
import utils.RegexConstants

class WhatIsYourNameFormProvider @Inject() extends Mappings with RegexConstants {

  private val maxLength = 35

  def apply(): Form[WhatIsYourName] = Form(
    mapping(
      "firstName" -> validatedText(
        "whatIsYourName.error.firstName.required",
        "whatIsYourName.error.firstName.invalid",
        "whatIsYourName.error.firstName.length",
        individualNameRegex,
        maxLength
      ),
      "lastName" -> validatedText(
        "whatIsYourName.error.lastName.required",
        "whatIsYourName.error.lastName.invalid",
        "whatIsYourName.error.lastName.length",
        individualNameRegex,
        maxLength
      )
    )(WhatIsYourName.apply)(WhatIsYourName.unapply)
  )

}
