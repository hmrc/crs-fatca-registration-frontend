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

package models.enrolment

import models.IdentifierType._
import models.matching.OrgRegistrationInfo
import models.{Address, SubscriptionID, UserAnswers}
import pages._
import play.api.libs.json.{Json, OFormat}

case class SubscriptionInfo(postCode: Option[String] = None, abroadFlag: Option[String] = None, id: String) {

  def convertToEnrolmentRequest: EnrolmentRequest =
    EnrolmentRequest(identifiers = Seq(Identifier(FATCAID, id)), verifiers = buildVerifiers)

  private def buildVerifiers: Seq[Verifier] =
    buildOptionalVerifier(postCode, POSTCODE) ++
      buildOptionalVerifier(abroadFlag, ABROADFLAG)

  private def buildOptionalVerifier(optionalInfo: Option[String], key: String): Seq[Verifier] =
    optionalInfo
      .map(
        info => Verifier(key, info)
      )
      .toSeq

}

object SubscriptionInfo {
  implicit val format: OFormat[SubscriptionInfo] = Json.format[SubscriptionInfo]

  def apply(userAnswers: UserAnswers, subscriptionId: SubscriptionID): SubscriptionInfo =
    SubscriptionInfo(
      postCode = getPostCodeIfProvided(userAnswers),
      abroadFlag = getAbroadFlagIfProvided(userAnswers),
      id = subscriptionId.value
    )

  private def getPostCodeIfProvided(userAnswers: UserAnswers): Option[String] =
    (userAnswers.get(RegistrationInfoPage), getAddress(userAnswers)) match {
      case (Some(OrgRegistrationInfo(_, _, address)), _) => address.postalCode
      case (_, Some(address))                            => address.postCode
      case _                                             => None
    }

  private def getAbroadFlagIfProvided(userAnswers: UserAnswers): Option[String] =
    (userAnswers.get(RegistrationInfoPage), getAddress(userAnswers)) match {
      case (Some(OrgRegistrationInfo(_, _, _)), _) => Some("N")
      case (_, Some(address)) if address.isGB      => Some("N")
      case (_, Some(address)) if !address.isGB     => Some("Y")
      case _                                       => None
    }

  private def getAddress(userAnswers: UserAnswers): Option[Address] =
    userAnswers.get(IndUKAddressWithoutIdPage) orElse
      userAnswers.get(IndNonUKAddressWithoutIdPage) orElse
      userAnswers.get(NonUKBusinessAddressWithoutIDPage) orElse
      userAnswers.get(IndSelectedAddressLookupPage).flatMap(_.toAddress)

}
