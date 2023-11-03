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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ReporterType {
  val code: String
}

object ReporterType extends Enumerable.Implicits {

  case object Sole extends WithName("sole") with ReporterType {
    val code = "0000"
  }

  case object Partnership extends WithName("partnership") with ReporterType {
    val code = "0001"
  }

  case object LimitedPartnership extends WithName("limitedPartnership") with ReporterType {
    val code = "0002"
  }

  case object LimitedCompany extends WithName("limited") with ReporterType {
    val code = "0003"
  }

  case object UnincorporatedAssociation extends WithName("unincorporatedAssociation") with ReporterType {
    val code = "0004"
  }

  case object Individual extends WithName("individual") with ReporterType {
    val code = "N/A"
  }

  val values: Seq[ReporterType] = Seq(
    LimitedCompany,
    Partnership,
    LimitedPartnership,
    UnincorporatedAssociation,
    Sole,
    Individual
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"reporterType.${value.toString}")),
        value = Some(value.toString),
        id = if (index == 0) Some(s"value") else Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[ReporterType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

}
