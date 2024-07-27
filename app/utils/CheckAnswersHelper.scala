/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.UserAnswers
import pages.{Page, QuestionPage}
import play.api.libs.json.Reads

class CheckAnswersHelper(private val userAnswers: UserAnswers) {

  def checkPage[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[Page] =
    userAnswers.get(page) match {
      case None => Some(page)
      case _    => None
    }

  def checkPage[A](page: QuestionPage[A], answer: A)(implicit rds: Reads[A]): Option[Page] =
    userAnswers.get(page) match {
      case Some(`answer`) => None
      case _              => Some(page)
    }

  def any(checkPages: Option[Page]*): Option[Page] = checkPages.find(_.isEmpty).getOrElse(checkPages.last)

}

object CheckAnswersHelper {
  def apply(userAnswers: UserAnswers): CheckAnswersHelper = new CheckAnswersHelper(userAnswers)
}
