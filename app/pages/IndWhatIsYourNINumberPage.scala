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

import models.UserAnswers
import play.api.libs.json.JsPath
import uk.gov.hmrc.domain.Nino

import scala.util.Try

case object IndWhatIsYourNINumberPage extends QuestionPage[Nino] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "indWhatIsYourNINumber"

  override def cleanup(value: Option[Nino], userAnswers: UserAnswers): Try[UserAnswers] = {
    val oldNino: Option[Nino] = userAnswers.get(IdMatchInfoPage).flatMap(_.nino)
    val hasChanged: Boolean   = oldNino != value

    if (hasChanged) {
      userAnswers.remove(RegistrationInfoPage)
    } else {
      super.cleanup(value, userAnswers)
    }
  }

}
