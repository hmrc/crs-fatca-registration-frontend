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

import models.{ReporterType, UserAnswers}
import pages._
import pages.changeContactDetails._
import play.api.libs.json.Reads

sealed trait IndividualAnswersValidator {
  self: CheckYourAnswersValidator =>

  private def checkIndChangePhoneAnswers: Seq[Page] = (userAnswers.get(IndividualHavePhonePage) match {
    case Some(true)  => checkPage(IndividualPhonePage)
    case Some(false) => None
    case _           => Some(IndividualHavePhonePage)
  }).toSeq

  private[utils] def checkIndChangeContactDetailsMissingAnswers: Seq[Page] = checkPage(IndividualEmailPage).toSeq ++ checkIndChangePhoneAnswers

  private def checkIndPhoneAnswers: Seq[Page] = (userAnswers.get(IndContactHavePhonePage) match {
    case Some(true)  => checkPage(IndContactPhonePage)
    case Some(false) => None
    case _           => Some(IndContactHavePhonePage)
  }).toSeq

  private[utils] def checkIndividualContactDetailsMissingAnswers: Seq[Page] = checkPage(IndContactEmailPage).toSeq ++ checkIndPhoneAnswers

  private def checkIndividualAddressMissingAnswers: Seq[Page] = (userAnswers.get(IndWhereDoYouLivePage) match {
    case Some(true) => any(checkPage(IndWhatIsYourPostcodePage), checkPage(IndUKAddressWithoutIdPage))
        .orElse(
          any(
            checkPage(IndSelectAddressPage),
            checkPage(IndUKAddressWithoutIdPage)
          ).map(
            _ => IndWhatIsYourPostcodePage
          )
        )
    case Some(false) => checkPage(IndNonUKAddressWithoutIdPage)
    case _           => Some(IndWhereDoYouLivePage)
  }).toSeq

  private def checkIndividualWithoutIdMissingAnswers: Seq[Page] = Seq(
    checkPage(IndWhatIsYourNamePage),
    checkPage(DateOfBirthWithoutIdPage)
  ).flatten ++ checkIndividualAddressMissingAnswers ++ checkIndividualContactDetailsMissingAnswers

  private def checkIndividualWithIdMissingAnswers: Seq[Page] = Seq(
    checkPage(IndWhatIsYourNINumberPage),
    checkPage(IndContactNamePage),
    checkPage(IndDateOfBirthPage),
    checkPage(RegistrationInfoPage)
  ).flatten ++ checkIndividualContactDetailsMissingAnswers

  def checkIndividualMissingAnswers: Seq[Page] = userAnswers.get(IndDoYouHaveNINumberPage) match {
    case Some(false) => checkIndividualWithoutIdMissingAnswers
    case Some(true)  => checkIndividualWithIdMissingAnswers
    case _           => Seq(IndDoYouHaveNINumberPage)
  }

}

sealed trait OrgAnswersValidator {
  self: CheckYourAnswersValidator with IndividualAnswersValidator =>

  private def checkOrgChangePhoneMissingAnswers: Seq[Page] = (userAnswers.get(OrganisationContactHavePhonePage) match {
    case Some(true)  => checkPage(OrganisationContactPhonePage)
    case Some(false) => None
    case _           => Some(OrganisationContactHavePhonePage)
  }).toSeq

  private def checkOrgChangeSecPhoneMissingAnswers: Seq[Page] = (userAnswers.get(OrganisationSecondContactHavePhonePage) match {
    case Some(true)  => checkPage(OrganisationSecondContactPhonePage)
    case Some(false) => None
    case _           => Some(OrganisationSecondContactHavePhonePage)
  }).toSeq

  private def checkOrgChangeFirstContactDetailsMissingAnswers: Seq[Page] = Seq(
    checkPage(OrganisationContactNamePage),
    checkPage(OrganisationContactEmailPage)
  ).flatten ++ checkOrgChangePhoneMissingAnswers

  private def checkOrgChangeSecContactDetailsMissingAnswers: Seq[Page] =
    userAnswers.get(OrganisationHaveSecondContactPage) match {
      case Some(true) => Seq(
          checkPage(OrganisationSecondContactNamePage),
          checkPage(OrganisationSecondContactEmailPage)
        ).flatten ++ checkOrgChangeSecPhoneMissingAnswers
      case Some(false) => Seq.empty
      case _           => Seq(OrganisationHaveSecondContactPage)
    }

  private[utils] def checkChangeContactDetailsMissingAnswers = checkOrgChangeFirstContactDetailsMissingAnswers ++ checkOrgChangeSecContactDetailsMissingAnswers

  private def checkOrgPhoneMissingAnswers: Seq[Page] = (userAnswers.get(ContactHavePhonePage) match {
    case Some(true)  => checkPage(ContactPhonePage)
    case Some(false) => None
    case _           => Some(ContactHavePhonePage)
  }).toSeq

  private def checkOrgSecPhoneMissingAnswers: Seq[Page] = (userAnswers.get(SecondContactHavePhonePage) match {
    case Some(true)  => checkPage(SecondContactPhonePage)
    case Some(false) => None
    case _           => Some(SecondContactHavePhonePage)
  }).toSeq

  private def checkOrgContactDetailsMissingAnswers: Seq[Page] = Seq(
    checkPage(ContactNamePage),
    checkPage(ContactEmailPage)
  ).flatten ++ checkOrgPhoneMissingAnswers

  private def checkOrgSecContactDetailsMissingAnswers: Seq[Page] =
    userAnswers.get(HaveSecondContactPage) match {
      case Some(true) => Seq(
          checkPage(SecondContactNamePage),
          checkPage(SecondContactEmailPage)
        ).flatten ++ checkOrgSecPhoneMissingAnswers
      case Some(false) => Seq.empty
      case _           => Seq(HaveSecondContactPage)
    }

  private def checkContactDetailsMissingAnswers: Seq[Page] = reporterType match {
    case Some(ReporterType.Individual) | Some(ReporterType.Sole) => checkIndividualContactDetailsMissingAnswers
    case _                                                       => checkOrgContactDetailsMissingAnswers ++ checkOrgSecContactDetailsMissingAnswers
  }

  private def checkOrgWithoutIdMissingAnswers: Seq[Page] = Seq(
    checkPage(BusinessNameWithoutIDPage),
    checkPage(HaveTradingNamePage),
    userAnswers.get(HaveTradingNamePage) match {
      case Some(true) => checkPage(BusinessTradingNameWithoutIDPage)
      case _          => None
    },
    checkPage(NonUKBusinessAddressWithoutIDPage)
  ).flatten ++ checkContactDetailsMissingAnswers

  private def checkOrgWithIdMissingAnswers: Seq[Page] = Seq(
    checkPage(WhatIsYourUTRPage),
    if (isAutoMatchedUtr) {
      None
    } else {
      if (reporterType.contains(ReporterType.Sole)) checkPage(WhatIsYourNamePage) else checkPage(BusinessNamePage)
    },
    checkPageWithAnswer(IsThisYourBusinessPage, true),
    checkPage(RegistrationInfoPage)
  ).flatten ++ checkContactDetailsMissingAnswers

  def checkOrgMissingAnswers: Seq[Page] = (isAutoMatchedUtr, userAnswers.get(RegisteredAddressInUKPage)) match {
    case (true, _)       => checkOrgWithIdMissingAnswers
    case (_, Some(true)) => checkOrgWithIdMissingAnswers
    case (_, Some(false)) => userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage) match {
        case Some(true) => checkOrgWithIdMissingAnswers
        case Some(false) => reporterType match {
            case Some(ReporterType.Individual) | Some(ReporterType.Sole) => checkIndividualMissingAnswers
            case _                                                       => checkOrgWithoutIdMissingAnswers
          }
        case _ => Seq(DoYouHaveUniqueTaxPayerReferencePage)
      }
    case _ => Seq(RegisteredAddressInUKPage)
  }

}

class CheckYourAnswersValidator(val userAnswers: UserAnswers) extends IndividualAnswersValidator with OrgAnswersValidator {

  private[utils] val reporterType     = userAnswers.get(ReporterTypePage)
  private[utils] val isAutoMatchedUtr = userAnswers.get(AutoMatchedUTRPage).isDefined

  private[utils] def checkPage[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[Page] =
    userAnswers.get(page) match {
      case None => Some(page)
      case _    => None
    }

  private[utils] def checkPageWithAnswer[A](page: QuestionPage[A], answer: A)(implicit rds: Reads[A]): Option[Page] =
    userAnswers.get(page) match {
      case None                   => Some(page)
      case Some(a) if a != answer => Some(page)
      case _                      => None
    }

  private[utils] def any(checkPages: Option[Page]*): Option[Page] = checkPages.find(_.isEmpty).getOrElse(checkPages.last)

  def validate: Seq[Page] =
    (reporterType, isAutoMatchedUtr) match {
      case (Some(ReporterType.Individual), _) => checkIndividualMissingAnswers
      case (Some(_), _) | (_, true)           => checkOrgMissingAnswers
      case _                                  => Seq(ReporterTypePage)
    }

  def validateIndChangeContactDetails: Seq[Page] = checkIndChangeContactDetailsMissingAnswers
  def validateOrgChangeContactDetails: Seq[Page] = checkChangeContactDetailsMissingAnswers

}

object CheckYourAnswersValidator {
  def apply(userAnswers: UserAnswers): CheckYourAnswersValidator = new CheckYourAnswersValidator(userAnswers)
}
