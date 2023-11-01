package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ReporterType

object ReporterType extends Enumerable.Implicits {

  case object WhatWillYouReportAs extends WithName("what will you report as?") with ReporterType
  case object Option2 extends WithName("option2") with ReporterType

  val values: Seq[ReporterType] = Seq(
    WhatWillYouReportAs, Option2
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"reporterType.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[ReporterType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
