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
import pages._
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.auth.core.AffinityGroup
import utils.UserAnswersHelper

case class CreateSubscriptionRequest(idType: String,
                                     idNumber: String,
                                     tradingName: Option[String],
                                     gbUser: Boolean,
                                     primaryContact: ContactInformation,
                                     secondaryContact: Option[ContactInformation]
)

object CreateSubscriptionRequest extends UserAnswersHelper {

  implicit val reads: Reads[CreateSubscriptionRequest]   = Json.reads[CreateSubscriptionRequest]
  implicit val writes: Writes[CreateSubscriptionRequest] = Json.writes[CreateSubscriptionRequest]

  def buildSubscriptionRequest(safeId: SafeId, userAnswers: UserAnswers, affinityGroup: AffinityGroup): Option[CreateSubscriptionRequest] = {
    for {
      primaryContact <- ContactInformation.convertToPrimary(userAnswers)
    } yield ContactInformation.convertToSecondary(userAnswers, affinityGroup) match {
      case Right(value) =>
        Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
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

  def isGBUser(userAnswers: UserAnswers): Boolean = {
    val businessHasUtr              = userAnswers.get(WhatIsYourUTRPage).exists(_.uniqueTaxPayerReference.trim.nonEmpty)
    val individualHasNino           = userAnswers.get(IndDoYouHaveNINumberPage).getOrElse(false)
    val individualAddressLookupIsGb = userAnswers.get(IndSelectedAddressLookupPage).nonEmpty
    val individualManualAddressIsGb = userAnswers.get(IndUKAddressWithoutIdPage).exists(_.isGB)

    businessHasUtr || individualHasNino || individualAddressLookupIsGb || individualManualAddressIsGb
  }

}
