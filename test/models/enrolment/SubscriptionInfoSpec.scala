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

package models.enrolment

import generators.ModelGenerators
import helpers.JsonFixtures.UserAnswersId
import models.{Address, SubscriptionID, UserAnswers}
import models.matching.OrgRegistrationInfo
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{NonUKBusinessAddressWithoutIDPage, RegistrationInfoPage}
import play.api.libs.json.Json

import java.time.{Clock, Instant, ZoneId}

class SubscriptionInfoSpec extends AnyFreeSpec with Matchers with ModelGenerators with ScalaCheckPropertyChecks {

  def emptyUserAnswers: UserAnswers = UserAnswers(
    UserAnswersId,
    Json.obj(),
    Instant.now(Clock.fixed(Instant.parse("2021-11-14T14:23:34.312535Z"), ZoneId.of("UTC")))
  )

  "SubscriptionInfo" - {
    "contain postCode for org registration info" in {
      forAll {
        (registrationInfo: OrgRegistrationInfo, subscriptionId: SubscriptionID) =>
          val userAnswers      = emptyUserAnswers.set(RegistrationInfoPage, registrationInfo).success.value
          val subscriptionInfo = SubscriptionInfo(userAnswers, subscriptionId)

          subscriptionInfo.postCode mustBe registrationInfo.address.postalCode
      }
    }

    "contain postCode for org non-uk business address" in {
      forAll {
        (address: Address, subscriptionId: SubscriptionID) =>
          val userAnswers      = emptyUserAnswers.set(NonUKBusinessAddressWithoutIDPage, address).success.value
          val subscriptionInfo = SubscriptionInfo(userAnswers, subscriptionId)

          subscriptionInfo.postCode mustBe address.postCode
      }
    }

    "contain abroadFlag as N for org registration info" in {
      forAll {
        (registrationInfo: OrgRegistrationInfo, subscriptionId: SubscriptionID) =>
          val userAnswers      = emptyUserAnswers.set(RegistrationInfoPage, registrationInfo).success.value
          val subscriptionInfo = SubscriptionInfo(userAnswers, subscriptionId)

          subscriptionInfo.abroadFlag mustBe Some("N")
      }
    }

    "contain abroadFlag as Y for org non-uk business address" in {
      forAll {
        (address: Address, subscriptionId: SubscriptionID) =>
          val userAnswers      = emptyUserAnswers.set(NonUKBusinessAddressWithoutIDPage, address).success.value
          val subscriptionInfo = SubscriptionInfo(userAnswers, subscriptionId)

          subscriptionInfo.abroadFlag mustBe Some("Y")
      }
    }
  }

}
