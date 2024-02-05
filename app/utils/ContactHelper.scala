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

package utils

import models.UserAnswers
import pages.changeContactDetails.OrganisationContactNamePage
import pages.changeContactDetails.OrganisationSecondContactNamePage
import pages.{ContactNamePage, SecondContactNamePage}
import play.api.i18n.Messages

trait ContactHelper {

  def getFirstContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(ContactNamePage)
      .fold(messages("default.firstContact.name"))(
        contactName => contactName
      )

  def getSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(SecondContactNamePage)
      .fold(messages("default.secondContact.name"))(
        contactName => contactName
      )

  def getContactName(ua: UserAnswers)(implicit messages: Messages): String =
    ua.get(OrganisationContactNamePage).getOrElse(messages("default.firstContact.name"))

  def getOrganisationSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(OrganisationSecondContactNamePage)
      .fold(messages("default.secondContact.name"))(
        contactName => contactName
      )

}
