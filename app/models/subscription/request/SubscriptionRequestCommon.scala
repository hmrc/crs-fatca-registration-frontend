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

import models.Regime
import play.api.libs.json.{Json, OFormat}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class SubscriptionRequestCommon(regime: String,
                                     receiptDate: String,
                                     acknowledgementReference: String,
                                     originatingSystem: String,
                                     requestParameters: Option[Seq[RequestParameter]],
                                     conversationID: Option[String] = None
)

object SubscriptionRequestCommon {
  implicit val format: OFormat[SubscriptionRequestCommon] = Json.format[SubscriptionRequestCommon]

  private val mdtp = "MDTP"

  def createSubscriptionRequestCommon(): SubscriptionRequestCommon = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    // Generate a 32 chars UUID without hyphens
    val acknowledgementReference = UUID.randomUUID().toString.replace("-", "")

    SubscriptionRequestCommon(
      regime = Regime.CRSFATCA.toString,
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = acknowledgementReference,
      originatingSystem = mdtp,
      requestParameters = None
    )
  }

}
