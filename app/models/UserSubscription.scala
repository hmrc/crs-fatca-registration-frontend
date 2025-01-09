/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class UserSubscription(
  id: String,
  subscriptionID: SubscriptionID,
  lastUpdated: Instant = Instant.now
)

object UserSubscription {

  implicit def format(encryptionEnabled: Boolean)(implicit crypto: Encrypter with Decrypter): OFormat[UserSubscription] =
    if (encryptionEnabled) encryptedFormat else plainFormat

  private def plainFormat: OFormat[UserSubscription] = {
    val reads: Reads[UserSubscription] =
      (
        (__ \ "_id").read[String] and
          (__ \ "subscriptionID").read[SubscriptionID] and
          (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(UserSubscription.apply _)

    val writes: OWrites[UserSubscription] =
      (
        (__ \ "_id").write[String] and
          (__ \ "subscriptionID").write[SubscriptionID] and
          (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(unlift(UserSubscription.unapply))

    OFormat(reads, writes)
  }

  private def encryptedFormat(implicit crypto: Encrypter with Decrypter): OFormat[UserSubscription] = {

    implicit val sensitiveFormat: Format[SensitiveString] =
      JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

    val encryptedReads: Reads[UserSubscription] =
      (
        (__ \ "_id").read[String] and
          (__ \ "subscriptionID").read[SensitiveString] and
          (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(
        (id, subscriptionID, lastUpdated) =>
          UserSubscription(id, SubscriptionID(subscriptionID.decryptedValue), lastUpdated)
      )

    val encryptedWrites: OWrites[UserSubscription] =
      (
        (__ \ "_id").write[String] and
          (__ \ "subscriptionID").write[SensitiveString] and
          (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(
        userSubscription => (userSubscription.id, SensitiveString(userSubscription.subscriptionID.value), userSubscription.lastUpdated)
      )

    OFormat(encryptedReads, encryptedWrites)
  }

}
