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

import java.time.{LocalDate, Month, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.data.FormError

class IndDateOfBirthFormProviderSpec extends DateBehaviours {

  val form = new IndDateOfBirthFormProvider()()

  private val fieldName = "value"

  ".value" - {

    val now          = LocalDate.now(ZoneOffset.UTC)
    val tooEarlyDate = LocalDate.of(1900, Month.JANUARY, 1)
    val testDate     = LocalDate.of(2000, 1, 1)

    val validData = datesBetween(
      min = testDate,
      max = now
    )

    behave like dateField(form, fieldName, validData)

    behave like mandatoryDateField(form, fieldName, "indDateOfBirth.error.required.all")

    behave like dateFieldWithMax(
      form,
      fieldName,
      now,
      FormError(fieldName, "indDateOfBirth.error.futureDate")
    )

    behave like dateFieldWithMin(
      form,
      fieldName,
      tooEarlyDate,
      FormError(fieldName, "indDateOfBirth.error.tooEarlyDate")
    )
  }

}
