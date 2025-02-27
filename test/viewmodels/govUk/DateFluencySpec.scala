/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.govUk

import forms.mappings.Mappings
import models.DateHelper.today
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.all._

import java.time.LocalDate

class DateFluencySpec extends AnyFreeSpec with Matchers with Mappings with OptionValues {

  ".apply" - {

    implicit val messages: Messages = stubMessages()

    val fieldset = FieldsetViewModel(LegendViewModel("foo"))

    val form: Form[LocalDate] =
      Form(
        "value" -> localDate(
          invalidKey = "indDateOfBirth.error.invalid",
          notRealDateKey = "indDateOfBirth.error.notRealDate",
          allRequiredKey = "indDateOfBirth.error.required.all",
          dayRequiredKey = "indDateOfBirth.error.required.day",
          monthRequiredKey = "indDateOfBirth.error.required.month",
          yearRequiredKey = "indDateOfBirth.error.required.year",
          dayAndMonthRequiredKey = "indDateOfBirth.error.required.dayAndMonth",
          dayAndYearRequiredKey = "indDateOfBirth.error.required.dayAndYear",
          monthAndYearRequiredKey = "indDateOfBirth.error.required.monthAndYear",
          futureDateKey = "indDateOfBirth.error.futureDate",
          pastDateKey = "indDateOfBirth.error.pastDate",
          maxDate = today,
          minDate = LocalDate.of(1900, 1, 1)
        )
      )

    val errorClass = "govuk-input--error"

    "must highlight all fields when there is an error, but the error does not specify any individual day/month/year field" in {

      val boundForm = form.bind(Map.empty[String, String])

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.forall(_.classes.contains(errorClass)) mustEqual true
    }

    "must highlight the day field when the error is that a day is missing" in {

      val boundForm = form.bind(Map(
        "value.month" -> "1",
        "value.year"  -> "2000"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must include(errorClass)
      result.items.find(_.id == "value.month").value.classes must not include errorClass
      result.items.find(_.id == "value.year").value.classes must not include errorClass
    }

    "must highlight the day and month fields when the error is that a day and month are both missing" in {

      val boundForm = form.bind(Map(
        "value.year" -> "2000"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must include(errorClass)
      result.items.find(_.id == "value.month").value.classes must include(errorClass)
      result.items.find(_.id == "value.year").value.classes must not include errorClass
    }

    "must highlight the day and year fields when the error is that a day and year are both missing" in {

      val boundForm = form.bind(Map(
        "value.month" -> "1"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must include(errorClass)
      result.items.find(_.id == "value.month").value.classes must not include errorClass
      result.items.find(_.id == "value.year").value.classes must include(errorClass)
    }

    "must highlight the month field when the error is that a month is missing" in {

      val boundForm = form.bind(Map(
        "value.day"  -> "1",
        "value.year" -> "2000"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must not include errorClass
      result.items.find(_.id == "value.month").value.classes must include(errorClass)
      result.items.find(_.id == "value.year").value.classes must not include errorClass
    }

    "must highlight the month and year fields when the error is that a month and year are both missing" in {

      val boundForm = form.bind(Map(
        "value.day" -> "1"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must not include errorClass
      result.items.find(_.id == "value.month").value.classes must include(errorClass)
      result.items.find(_.id == "value.year").value.classes must include(errorClass)
    }

    "must highlight the year field when the error is that a year is missing" in {

      val boundForm = form.bind(Map(
        "value.day"   -> "1",
        "value.month" -> "1"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must not include errorClass
      result.items.find(_.id == "value.month").value.classes must not include errorClass
      result.items.find(_.id == "value.year").value.classes must include(errorClass)
    }

    "must not highlight any fields when there is not an error" in {

      val boundForm = form.bind(Map(
        "value.day"   -> "1",
        "value.month" -> "1",
        "value.year"  -> "2000"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.forall(_.classes.contains(errorClass)) mustEqual false
    }

    "must not highlight day field when it is not a real date" in {

      val boundForm = form.bind(Map(
        "value.day"   -> "32",
        "value.month" -> "1",
        "value.year"  -> "2000"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must include(errorClass)
      result.items.find(_.id == "value.month").value.classes must not include errorClass
      result.items.find(_.id == "value.year").value.classes must not include errorClass
    }

    "must not highlight month field when it is not a real date" in {

      val boundForm = form.bind(Map(
        "value.day"   -> "10",
        "value.month" -> "13",
        "value.year"  -> "2000"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.find(_.id == "value.day").value.classes must not include errorClass
      result.items.find(_.id == "value.month").value.classes must include(errorClass)
      result.items.find(_.id == "value.year").value.classes must not include errorClass
    }

    "must highlight all fields when there are two values for not real date" in {

      val boundForm = form.bind(Map(
        "value.day"   -> "32",
        "value.month" -> "13",
        "value.year"  -> "2000"
      ))

      val result = DateViewModel(boundForm("value"), fieldset)

      result.items.forall(_.classes.contains(errorClass)) mustEqual true
    }
  }

}
