package pages

import models.ReporterType
import play.api.libs.json.JsPath

case object ReporterTypePage extends QuestionPage[ReporterType] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "reporterType"
}
