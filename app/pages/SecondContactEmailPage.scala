package pages

import play.api.libs.json.JsPath

case object SecondContactEmailPage extends QuestionPage[String] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "secondContactEmail"
}
