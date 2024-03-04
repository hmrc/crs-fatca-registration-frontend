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

package models.subscription.request

import models.matching.SafeId
import models.{IdentifierType, UserAnswers}
import pages.{BusinessTradingNameWithoutIDPage, IndUKAddressWithoutIdPage, NonUKBusinessAddressWithoutIDPage}
import play.api.libs.json.{Json, OFormat}
import utils.UserAnswersHelper

case class CreateSubscriptionRequest(idType: String,
                                     idNumber: String,
                                     tradingName: Option[String],
                                     gbUser: Boolean,
                                     primaryContact: ContactInformation,
                                     secondaryContact: Option[ContactInformation]
)

object CreateSubscriptionRequest extends UserAnswersHelper {

  implicit val format: OFormat[CreateSubscriptionRequest] = Json.format[CreateSubscriptionRequest]
  private val idType: String                              = IdentifierType.SAFE

  def convertTo(safeId: SafeId, userAnswers: UserAnswers): Option[CreateSubscriptionRequest] = {
    for {
      primaryContact <- ContactInformation.convertToPrimary(userAnswers)
    } yield ContactInformation.convertToSecondary(userAnswers) match {
      case Right(value) =>
        Some(
          CreateSubscriptionRequest(
            idType = idType,
            idNumber = safeId.value,
            tradingName = userAnswers.get(BusinessTradingNameWithoutIDPage),
            gbUser = isGBUser(userAnswers),
            primaryContact = primaryContact,
            secondaryContact = value
          )
        )
      case _ => None
    }
  }.flatten

  private def isGBUser(userAnswers: UserAnswers): Boolean =
    if (userAnswers.get(NonUKBusinessAddressWithoutIDPage).exists(_.isOtherCountry) || userAnswers.get(IndUKAddressWithoutIdPage).exists(_.isOtherCountry)) {
      false
    } else {
      true
    }

}
