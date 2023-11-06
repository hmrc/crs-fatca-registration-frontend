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
import models.IndWhatIsYourName
import utils.RegexConstants

class IndWhatIsYourNameFormProvider @Inject() extends Mappings with RegexConstants {

  private val maxLength = 35

  def apply(): Form[IndWhatIsYourName] = Form(
    mapping(
      "firstName" -> validatedText(
        "indWhatIsYourName.error.firstName.required",
        "indWhatIsYourName.error.firstName.invalid",
        "indWhatIsYourName.error.firstName.length",
        individualNameRegex,
        maxLength
      ),
      "lastName" -> validatedText(
        "indWhatIsYourName.error.lastName.required",
        "indWhatIsYourName.error.lastName.invalid",
        "indWhatIsYourName.error.lastName.length",
        individualNameRegex,
        maxLength
      )
    )(IndWhatIsYourName.apply)(IndWhatIsYourName.unapply)
  )

}