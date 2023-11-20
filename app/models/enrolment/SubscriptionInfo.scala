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
import models.ReporterType.{LimitedCompany, LimitedPartnership, Partnership, Sole, UnincorporatedAssociation}
import models.error.ApiError
import models.matching.SafeId
import models.{SubscriptionID, UserAnswers}
import pages._
import play.api.libs.json.{Json, OFormat}

case class SubscriptionInfo(safeID: String,
                            saUtr: Option[String] = None,
                            ctUtr: Option[String] = None,
                            nino: Option[String] = None,
                            nonUkPostcode: Option[String] = None,
                            id: String
) {

  def convertToEnrolmentRequest: EnrolmentRequest =
    EnrolmentRequest(identifiers = Seq(Identifier(FATCAID, id)), verifiers = buildVerifiers)

  def buildVerifiers: Seq[Verifier] = {

    val mandatoryVerifiers = Seq(Verifier(SAFEID, safeID))

    mandatoryVerifiers ++
      buildOptionalVerifier(saUtr, SAUTR) ++
      buildOptionalVerifier(ctUtr, CTUTR) ++
      buildOptionalVerifier(nino, NINO) ++
      buildOptionalVerifier(nonUkPostcode, NonUKPostalCode)

  }

  def buildOptionalVerifier(optionalInfo: Option[String], key: String): Seq[Verifier] =
    optionalInfo
      .map(
        info => Verifier(key, info)
      )
      .toSeq

}

object SubscriptionInfo {
  implicit val format: OFormat[SubscriptionInfo] = Json.format[SubscriptionInfo]

  def createSubscriptionInfo(safeId: SafeId, userAnswers: UserAnswers, subscriptionId: SubscriptionID): Either[ApiError, SubscriptionInfo] =
    Right(
      SubscriptionInfo(
        safeID = safeId.value,
        saUtr = getSaUtrIfProvided(userAnswers),
        ctUtr = getCtUtrIfProvided(userAnswers),
        nino = getNinoIfProvided(userAnswers),
        nonUkPostcode = getNonUkPostCodeIfProvided(userAnswers),
        id = subscriptionId.value
      )
    )

  private def getNinoIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(IndWhatIsYourNINumberPage) match {
      case Some(nino) => Some(nino)
      case _          => None
    }

  private def getSaUtrIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(ReporterTypePage) match {
      case Some(Partnership) | Some(Sole) | Some(LimitedPartnership) => userAnswers.get(WhatIsYourUTRPage).map(_.uniqueTaxPayerReference)
      case _                                                         => None
    }

  private def getCtUtrIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(ReporterTypePage) match {
      case Some(LimitedCompany) | Some(UnincorporatedAssociation) => userAnswers.get(WhatIsYourUTRPage).map(_.uniqueTaxPayerReference)
      case _                                                      => None
    }

  private def getNonUkPostCodeIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(NonUKBusinessAddressWithoutIDPage) match {
      case Some(address) => address.postCode
      case _             => None
    }

}
