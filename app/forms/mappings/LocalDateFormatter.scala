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

import java.time.LocalDate
import models.DateHelper.formatDateToString
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
  invalidKey: String,
  notRealDateKey: String,
  allRequiredKey: String,
  dayRequiredKey: String,
  monthRequiredKey: String,
  yearRequiredKey: String,
  dayAndMonthRequiredKey: String,
  dayAndYearRequiredKey: String,
  monthAndYearRequiredKey: String,
  futureDateKey: String,
  pastDateKey: String,
  maxDate: LocalDate,
  minDate: LocalDate,
  args: Seq[String] = Seq.empty
) extends Formatter[LocalDate]
    with Formatters {

  private val fieldKeys: List[String] = List("day", "month", "year")

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        val errorArgs = getErrorArgs(day, month)

        val finalArgs = if (errorArgs.isEmpty) Seq("day", "month", "year") else errorArgs

        Left(Seq(FormError(key, notRealDateKey, finalArgs)))
    }

  private def getErrorArgs(day: Int, month: Int): Seq[String] = {
    val isDayError   = if (day < 1 || day > 31) true else false
    val isMonthError = if (month < 1 || month > 12) true else false

    (isDayError, isMonthError) match {
      case (true, false) => Seq("day")
      case (false, true) => Seq("month")
      case (_, _)        => Seq()
    }
  }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      day   <- int.bind(s"$key.day", data)
      month <- int.bind(s"$key.month", data)
      year  <- int.bind(s"$key.year", data)
      date  <- toDate(key, day, month, year)
    } yield date
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    val cleanedData = data.map {
      case (k, v) => k -> v.replaceAll("[\\s-]", "")
    }

    val fields = fieldKeys
      .map(
        field => field -> cleanedData.get(s"$key.$field").filter(_.nonEmpty)
      )
      .toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 3 => noMissingField(key, cleanedData)
      case 2 => singleFieldMissing(key, missingFields)
      case 1 => twoFieldsMissing(key, missingFields)
      case _ => Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  private def noMissingField(key: String, data: Map[String, String]) =
    formatDate(key, data).left
      .map(_.map(
        e => e.copy(key = key, args = e.args ++ args)
      ))
      .flatMap {
        case validDate if validDate.isAfter(maxDate) =>
          Left(List(FormError(key, futureDateKey, List(formatDateToString(maxDate)) ++ args)))
        case validDate if validDate.isBefore(minDate) =>
          Left(List(FormError(key, pastDateKey, args)))
        case validDate => Right(validDate)
      }

  private def twoFieldsMissing(key: String, missingFields: => List[String]) =
    if (!missingFields.exists(_.toLowerCase.contains("day"))) {
      Left(List(FormError(key, monthAndYearRequiredKey, missingFields ++ args)))
    } else if (!missingFields.exists(_.toLowerCase.contains("month"))) {
      Left(List(FormError(key, dayAndYearRequiredKey, missingFields ++ args)))
    } else {
      Left(List(FormError(key, dayAndMonthRequiredKey, missingFields ++ args)))
    }

  private def singleFieldMissing(key: String, missingFields: => List[String]) =
    if (missingFields.exists(_.toLowerCase.contains("day"))) {
      Left(List(FormError(key, dayRequiredKey, missingFields ++ args)))
    } else if (missingFields.exists(_.toLowerCase.contains("month"))) {
      Left(List(FormError(key, monthRequiredKey, missingFields ++ args)))
    } else {
      Left(List(FormError(key, yearRequiredKey, missingFields ++ args)))
    }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day"   -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )

}
