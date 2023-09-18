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

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.data.Form

class IndDateOfBirthFormProviderSpec extends DateBehaviours {

  val form = new IndDateOfBirthFormProvider()()

  ".value" - {

    val minDate = LocalDate.of(1900, 1, 1)

    val validData = datesBetween(
      min = minDate,
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value.day", "indDateOfBirth.error.required.all")

    "must return a FormError when month is missing" in {
      val key  = "value"
      val date = LocalDate.now()

      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> "",
        s"$key.year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors.size mustBe 1
      result.errors.head.message mustBe "indDateOfBirth.error.required.month"
      result.errors.head.args mustBe Seq("month")
    }

    "must return a FormError when month is invalid" in {
      val key  = "value"
      val date = LocalDate.now()

      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> "a",
        s"$key.year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors.size mustBe 1
      result.errors.head.message mustBe "indDateOfBirth.error.invalid"
      result.errors.head.args mustBe Seq("month")
    }

    "must not allow a date later than today" in {
      val key  = "value"
      val date = LocalDate.now().plusYears(1)
      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> date.getMonthValue.toString,
        s"$key.year"  -> date.getYear.toString
      )

      val result: Form[LocalDate] = form.bind(data)

      result.errors.size mustBe 1
      result.errors.head.message mustBe "indDateOfBirth.error.futureDate"
    }

    "must not allow a date earlier than 01/01/1900" in {
      val key  = "value"
      val date = minDate.minusDays(1)
      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> date.getMonthValue.toString,
        s"$key.year"  -> date.getYear.toString
      )

      val result: Form[LocalDate] = form.bind(data)

      result.errors.size mustBe 1
      result.errors.head.message mustBe "indDateOfBirth.error.pastDate"
    }

  }

}
