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

package repositories

import config.FrontendAppConfig
import models.UserSubscription
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import play.api.libs.json.Format
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig,
  clock: Clock
)(implicit ec: ExecutionContext, crypto: Encrypter with Decrypter)
    extends PlayMongoRepository[UserSubscription](
      collectionName = "user-subscription",
      mongoComponent = mongoComponent,
      domainFormat = UserSubscription.format(appConfig.mongoEncryptionEnabled),
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("subscriptionLastUpdatedIdx")
            .expireAfter(appConfig.subscriptionTtl, java.util.concurrent.TimeUnit.SECONDS)
        )
      )
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: String): Bson = Filters.equal("_id", id)

  def keepAlive(id: String): Future[Boolean] =
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.set("lastUpdated", Instant.now(clock))
      )
      .toFuture()
      .map(
        _ => true
      )

  def get(id: String): Future[Option[UserSubscription]] =
    keepAlive(id).flatMap {
      _ =>
        collection
          .find(byId(id))
          .headOption()
    }

  def set(subscription: UserSubscription): Future[Boolean] = {

    val updatedSubscription = subscription copy (lastUpdated = Instant.now(clock))

    collection
      .replaceOne(
        filter = byId(updatedSubscription.id),
        replacement = updatedSubscription,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(
        _ => true
      )
  }

  def clear(id: String): Future[Boolean] =
    collection
      .deleteOne(byId(id))
      .toFuture()
      .map(
        _ => true
      )

}
