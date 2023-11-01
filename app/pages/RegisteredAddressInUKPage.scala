package pages

import play.api.libs.json.JsPath

case object RegisteredAddressInUKPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "registeredAddressInUK"
}
