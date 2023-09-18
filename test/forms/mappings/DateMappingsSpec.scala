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

package forms.mappings

import base.SpecBase

import java.time.{LocalDate, Month}
import generators.Generators
import models.DateHelper.today
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

class DateMappingsSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with Mappings {

  private val minDate: LocalDate = LocalDate.of(1900, Month.JANUARY, 1)

  private val form = Form(
    "value" -> localDate(
      allRequiredKey = "error.required.all",
      dayRequiredKey = "error.required.day",
      monthRequiredKey = "error.required.month",
      yearRequiredKey = "error.required.year",
      dayAndMonthRequiredKey = "error.required.dayAndMonth",
      dayAndYearRequiredKey = "error.required.dayAndYear",
      monthAndYearRequiredKey = "error.required.monthAndYear",
      notRealDateKey = "error.notRealDate",
      futureDateKey = "error.future",
      pastDateKey = "error.tooEarlyDate",
      invalidKey = "error.invalid",
      maxDate = today,
      minDate = minDate
    )
  )

  private val validData = datesBetween(
    min = minDate,
    max = today
  )

  val invalidField: Gen[String] = Gen.alphaStr.suchThat(_.nonEmpty)

  val missingField: Gen[Option[String]] = Gen.option(Gen.const(""))

  "must bind valid data" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
    }
  }

  "must fail to bind an empty date" in {

    val result = form.bind(Map.empty[String, String])

    result.errors must contain only FormError("value.day", "error.required.all", List.empty)
  }

  "must fail to bind a date with a missing day" in {

    forAll(validData -> "valid date", missingField -> "missing field") {
      (date, field) =>
        val initialData = Map(
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val data = field.fold(initialData) {
          value =>
            initialData + ("value.day" -> value)
        }

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.required.day", List("day"))
    }
  }

  "must fail to bind a date with an invalid day" in {

    forAll(validData -> "valid date", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> field,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain(
          FormError("value.day", "error.invalid", List("day"))
        )
    }
  }

  "must fail to bind a date with a missing month" in {

    forAll(validData -> "valid date", missingField -> "missing field") {
      (date, field) =>
        val initialData = Map(
          "value.day"  -> date.getDayOfMonth.toString,
          "value.year" -> date.getYear.toString
        )

        val data = field.fold(initialData) {
          value =>
            initialData + ("value.month" -> value)
        }

        val result = form.bind(data)

        result.errors must contain only FormError("value.month", "error.required.month", List("month"))
    }
  }

  "must fail to bind a date with an invalid month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> field,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain(
          FormError("value.month", "error.invalid", List("month"))
        )
    }
  }

  "must fail to bind a date with a missing year" in {

    forAll(validData -> "valid date", missingField -> "missing field") {
      (date, field) =>
        val initialData = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString
        )

        val data = field.fold(initialData) {
          value =>
            initialData + ("value.year" -> value)
        }

        val result = form.bind(data)

        result.errors must contain only FormError("value.year", "error.required.year", List("year"))
    }
  }

  "must fail to bind a date with an invalid year" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> field
        )

        val result = form.bind(data)

        result.errors must contain(
          FormError("value.year", "error.invalid", List("year"))
        )
    }
  }

  "must fail to bind a date with a missing day and month" in {

    forAll(validData -> "valid date", missingField -> "missing day", missingField -> "missing month") {
      (date, dayOpt, monthOpt) =>
        val day = dayOpt.fold(Map.empty[String, String]) {
          value =>
            Map("value.day" -> value)
        }

        val month = monthOpt.fold(Map.empty[String, String]) {
          value =>
            Map("value.month" -> value)
        }

        val data: Map[String, String] = Map(
          "value.year" -> date.getYear.toString
        ) ++ day ++ month

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.required.dayAndMonth", List("day", "month"))
    }
  }

  "must fail to bind a date with a missing day and year" in {

    forAll(validData -> "valid date", missingField -> "missing day", missingField -> "missing year") {
      (date, dayOpt, yearOpt) =>
        val day = dayOpt.fold(Map.empty[String, String]) {
          value =>
            Map("value.day" -> value)
        }

        val year = yearOpt.fold(Map.empty[String, String]) {
          value =>
            Map("value.year" -> value)
        }

        val data: Map[String, String] = Map(
          "value.month" -> date.getMonthValue.toString
        ) ++ day ++ year

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.required.dayAndYear", List("day", "year"))
    }
  }

  "must fail to bind a date with a missing month and year" in {

    forAll(validData -> "valid date", missingField -> "missing month", missingField -> "missing year") {
      (date, monthOpt, yearOpt) =>
        val month = monthOpt.fold(Map.empty[String, String]) {
          value =>
            Map("value.month" -> value)
        }

        val year = yearOpt.fold(Map.empty[String, String]) {
          value =>
            Map("value.year" -> value)
        }

        val data: Map[String, String] = Map(
          "value.day" -> date.getDayOfMonth.toString
        ) ++ month ++ year

        val result = form.bind(data)

        result.errors must contain only FormError("value.month", "error.required.monthAndYear", List("month", "year"))
    }
  }

  "must fail to bind an invalid day and month" in {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid month") {
      (date, day, month) =>
        val data = Map(
          "value.day"   -> day,
          "value.month" -> month,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.invalid", List.empty)
    }
  }

  "must fail to bind an invalid day and year" in {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid year") {
      (date, day, year) =>
        val data = Map(
          "value.day"   -> day,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.invalid", List.empty)
    }
  }

  "must fail to bind an invalid month and year" in {

    forAll(validData -> "valid date", invalidField -> "invalid month", invalidField -> "invalid year") {
      (date, month, year) =>
        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> month,
          "value.year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.invalid", List.empty)
    }
  }

  "must fail to bind an invalid day, month and year" in {

    forAll(invalidField -> "valid day", invalidField -> "invalid month", invalidField -> "invalid year") {
      (day, month, year) =>
        val data = Map(
          "value.day"   -> day,
          "value.month" -> month,
          "value.year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.invalid", List.empty)
    }
  }

  "must fail to bind an invalid date" in {

    val data = Map(
      "value.day"   -> "30",
      "value.month" -> "2",
      "value.year"  -> "2018"
    )

    val result = form.bind(data)

    result.errors must contain(
      FormError("value", "error.notRealDate", List.empty)
    )
  }

  "must unbind a date" in {

    forAll(validData -> "valid date") {
      date =>
        val filledForm = form.fill(date)

        filledForm("value.day").value.value mustEqual date.getDayOfMonth.toString
        filledForm("value.month").value.value mustEqual date.getMonthValue.toString
        filledForm("value.year").value.value mustEqual date.getYear.toString
    }
  }

}
