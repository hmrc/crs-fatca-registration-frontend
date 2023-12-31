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
import play.api.data.{FieldMapping, Mapping}
import play.api.data.Forms.of
import models.Enumerable

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.wholeNumber",
                    nonNumericKey: String = "error.nonNumeric",
                    args: Seq[String] = Seq.empty
  ): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def boolean(requiredKey: String = "error.required", invalidKey: String = "error.boolean", args: Seq[String] = Seq.empty): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def validatedTextMaxLength(requiredKey: String, lengthKey: String, maxLength: Int): FieldMapping[String] =
    of(textMaxLengthFormatter(requiredKey, lengthKey, maxLength))

  protected def enumerable[A](requiredKey: String = "error.required", invalidKey: String = "error.invalid", args: Seq[String] = Seq.empty)(implicit
    ev: Enumerable[A]
  ): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def requiredRegexOnlyText(requiredKey: String, invalidKey: String, regex: String): FieldMapping[String] =
    of(requiredRegexOnly(requiredKey, invalidKey, regex))

  protected def localDate(
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
  ): FieldMapping[LocalDate] =
    of(
      new LocalDateFormatter(
        invalidKey,
        notRealDateKey,
        allRequiredKey,
        dayRequiredKey,
        monthRequiredKey,
        yearRequiredKey,
        dayAndMonthRequiredKey,
        dayAndYearRequiredKey,
        monthAndYearRequiredKey,
        futureDateKey,
        pastDateKey,
        maxDate,
        minDate,
        args
      )
    )

  protected def validatedText(
    requiredKey: String,
    invalidKey: String,
    lengthKey: String,
    regex: String,
    maxLength: Int,
    minLength: Int = 1,
    msgArg: String = ""
  ): FieldMapping[String] =
    of(validatedTextFormatter(requiredKey, invalidKey, lengthKey, regex, maxLength, minLength, msgArg))

  protected def validatedOptionalText(invalidKey: String, lengthKey: String, regex: String, length: Int): FieldMapping[Option[String]] =
    of(validatedOptionalTextFormatter(invalidKey, lengthKey, regex, length))

  protected def addressPostcode(requiredKey: String,
                                lengthKey: String,
                                invalidKey: String,
                                regex: String,
                                invalidCharKey: String,
                                InvalidCharRegex: String
  ): FieldMapping[Option[String]] =
    of(addressPostcodeFormatter(requiredKey, lengthKey, invalidKey, regex, invalidCharKey, InvalidCharRegex))

  protected def mandatoryPostcode(requiredKey: String,
                                  lengthKey: String,
                                  invalidKey: String,
                                  regex: String,
                                  invalidCharKey: String,
                                  InvalidCharRegex: String
  ): Mapping[String] =
    of(mandatoryPostcodeFormatter(requiredKey, lengthKey, invalidKey, regex, invalidCharKey, InvalidCharRegex))

  protected def optionalPostcode(requiredKey: String,
                                 lengthKey: String,
                                 invalidKey: String,
                                 regex: String,
                                 countryFieldName: String
  ): FieldMapping[Option[String]] =
    of(optionalPostcodeFormatter(requiredKey, lengthKey, invalidKey, regex, countryFieldName))

  protected def validatedUTR(requiredKey: String, invalidKey: String, invalidFormatKey: String, regex: String, msgArg: String = ""): FieldMapping[String] =
    of(validatedUtrFormatter(requiredKey, invalidKey, invalidFormatKey, regex, msgArg))

}
