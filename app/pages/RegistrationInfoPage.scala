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

package pages

import models.matching.RegistrationInfo
import play.api.libs.json.JsPath
import models.UserAnswers
import scala.util.Try

case object RegistrationInfoPage extends QuestionPage[RegistrationInfo] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "registrationInfo"

  override def cleanup(value: Option[RegistrationInfo], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(IdMatchInfoPage)
      case None    => super.cleanup(value, userAnswers)
    }

}
