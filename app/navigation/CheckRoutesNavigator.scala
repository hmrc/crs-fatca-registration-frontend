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

package navigation

import controllers.individual.routes._
import controllers.organisation.routes._
import controllers.routes._
import models.ReporterType.{Individual, Sole}
import models._
import pages._
import pages.changeContactDetails._
import play.api.Logging
import play.api.libs.json.Reads
import play.api.mvc.Call

trait CheckRoutesNavigator extends Logging {

  val checkRoutes: Page => UserAnswers => Call = {
    case ReporterTypePage                     => whatAreYouReportingAsRoutes()
    case RegisteredAddressInUKPage            => registeredAddressInUKRoutes()
    case DoYouHaveUniqueTaxPayerReferencePage => doYouHaveUniqueTaxPayerReferenceRoutes()
    case WhatIsYourUTRPage                    => whatIsYourUTRRoutes()
    case WhatIsYourNamePage                   => _ => IsThisYourBusinessController.onPageLoad(CheckMode)
    case BusinessNamePage                     => _ => IsThisYourBusinessController.onPageLoad(CheckMode)
    case IsThisYourBusinessPage               => isThisYourBusinessRoutes()
    case BusinessNameWithoutIDPage            => businessNameWithoutIDRoutes()
    case HaveTradingNamePage                  => haveTradingNameRoutes()
    case BusinessTradingNameWithoutIDPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          NonUKBusinessAddressWithoutIDPage,
          NonUKBusinessAddressWithoutIDController.onPageLoad(CheckMode)
        )
    case NonUKBusinessAddressWithoutIDPage => businessAddressWithoutIdRoutes()
    case IndDoYouHaveNINumberPage          => doYouHaveNINORoutes()
    case IndWhatIsYourNINumberPage         => whatIsYourNINumberRoutes()
    case IndContactNamePage                => contactNameRoutes()
    case IndWhatIsYourNamePage             => whatIsYourNameRoutes()
    case IndDateOfBirthPage                => whatIsYourDateOfBirthRoutes()
    case RegistrationInfoPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          IndContactEmailPage,
          IndContactEmailController.onPageLoad(NormalMode)
        )
    case DateOfBirthWithoutIdPage => whatIsYourDateOfBirthRoutes()
    case IndWhereDoYouLivePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IndWhereDoYouLivePage,
          IndWhatIsYourPostcodeController.onPageLoad(CheckMode),
          IndNonUKAddressWithoutIdController.onPageLoad(CheckMode)
        )
    case IndNonUKAddressWithoutIdPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          IndContactEmailPage,
          IndContactEmailController.onPageLoad(NormalMode)
        )
    case IndWhatIsYourPostcodePage => addressLookupRoutes()
    case IndSelectAddressPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          IndContactEmailPage,
          IndContactEmailController.onPageLoad(NormalMode)
        )
    case IsThisYourAddressPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisYourAddressPage,
          checkNextPageForValueThenRoute(
            userAnswers,
            IndContactEmailPage,
            IndContactEmailController.onPageLoad(NormalMode)
          ),
          IndUKAddressWithoutIdController.onPageLoad(CheckMode)
        )
    case IndUKAddressWithoutIdPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          IndContactEmailPage,
          IndContactEmailController.onPageLoad(NormalMode)
        )
    case IndContactHavePhonePage => indContactHavePhoneCheckModeNavigation
    case IndContactPhonePage     => _ => CheckYourAnswersController.onPageLoad()
    case YourContactDetailsPage  => _ => ContactNameController.onPageLoad(CheckMode)
    case ContactHavePhonePage    => contactHavePhoneCheckModeNavigation
    case ContactPhonePage        => _ => CheckYourAnswersController.onPageLoad()
    case HaveSecondContactPage =>
      userAnswers =>
        yesNoNavigate(
          userAnswers.get(HaveSecondContactPage).exists(_ != true) || userAnswers.get(SecondContactNamePage).isDefined,
          CheckYourAnswersController.onPageLoad(),
          SecondContactNameController.onPageLoad(CheckMode)
        )
    case SecondContactNamePage =>
      userAnswers =>
        yesNoNavigate(
          userAnswers.get(SecondContactEmailPage).isDefined,
          CheckYourAnswersController.onPageLoad(),
          SecondContactEmailController.onPageLoad(CheckMode)
        )
    case SecondContactEmailPage => userAnswers =>
        yesNoNavigate(
          userAnswers.get(SecondContactHavePhonePage).isDefined,
          CheckYourAnswersController.onPageLoad(),
          SecondContactHavePhoneController.onPageLoad(CheckMode)
        )
    case SecondContactHavePhonePage => secondContactHavePhoneCheckModeNavigation
    case SecondContactPhonePage     => _ => CheckYourAnswersController.onPageLoad()

    case OrganisationContactNamePage      => _ => controllers.changeContactDetails.routes.OrganisationContactEmailController.onPageLoad(CheckMode)
    case OrganisationContactEmailPage     => _ => controllers.changeContactDetails.routes.OrganisationContactHavePhoneController.onPageLoad(CheckMode)
    case OrganisationContactHavePhonePage => orgContactHavePhoneRoutes()
    case OrganisationContactPhonePage     => _ => controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
    case OrganisationHaveSecondContactPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          OrganisationHaveSecondContactPage,
          controllers.changeContactDetails.routes.OrganisationSecondContactNameController.onPageLoad(CheckMode),
          controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
        )
    case OrganisationSecondContactNamePage  => _ => controllers.changeContactDetails.routes.OrganisationSecondContactEmailController.onPageLoad(CheckMode)
    case OrganisationSecondContactEmailPage => _ => controllers.changeContactDetails.routes.OrganisationSecondContactHavePhoneController.onPageLoad(CheckMode)
    case OrganisationSecondContactHavePhonePage => orgSecondContactHavePhoneRoutes()
    case OrganisationSecondContactPhonePage     => _ => controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()

    case IndividualEmailPage     => _ => controllers.changeContactDetails.routes.IndividualHavePhoneController.onPageLoad(CheckMode)
    case IndividualHavePhonePage => indContactHavePhoneRoutes()
    case IndividualPhonePage     => _ => controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad()

    case _ => _ => CheckYourAnswersController.onPageLoad()
  }

  private def addressLookupRoutes()(ua: UserAnswers): Call =
    ua.get(AddressLookupPage) match {
      case Some(value) if value.length == 1 => IndIsThisYourAddressController.onPageLoad(CheckMode)
      case _                                => IndSelectAddressController.onPageLoad(CheckMode)
    }

  private def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(JourneyRecoveryController.onPageLoad())

  private def yesNoNavigate(check: => Boolean, yesCall: => Call, noCall: => Call): Call = if (check) yesCall else noCall

  private def indContactHavePhoneRoutes()(ua: UserAnswers): Call =
    yesNoPage(
      ua,
      IndividualHavePhonePage,
      if (ua.get(IndividualPhonePage).isDefined) {
        controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad()
      } else { controllers.changeContactDetails.routes.IndividualPhoneController.onPageLoad(CheckMode) },
      controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad()
    )

  private def orgContactHavePhoneRoutes()(ua: UserAnswers): Call = yesNoPage(
    ua,
    OrganisationContactHavePhonePage,
    if (ua.get(OrganisationContactPhonePage).isDefined) {
      controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
    } else { controllers.changeContactDetails.routes.OrganisationContactPhoneController.onPageLoad(CheckMode) },
    controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
  )

  private def orgSecondContactHavePhoneRoutes()(ua: UserAnswers): Call = yesNoPage(
    ua,
    OrganisationSecondContactHavePhonePage,
    if (ua.get(OrganisationSecondContactPhonePage).isDefined) {
      controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
    } else { controllers.changeContactDetails.routes.OrganisationSecondContactPhoneController.onPageLoad(CheckMode) },
    controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
  )

  private def whatAreYouReportingAsRoutes()(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Individual) => checkRoutes(IndDoYouHaveNINumberPage)(ua)

      case Some(_) => RegisteredAddressInUKController.onPageLoad(CheckMode)
      case None    => JourneyRecoveryController.onPageLoad()
    }

  private def registeredAddressInUKRoutes()(ua: UserAnswers): Call =
    ua.get(RegisteredAddressInUKPage) match {
      case Some(true)  => WhatIsYourUTRController.onPageLoad(CheckMode)
      case Some(false) => DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode)
      case _           => RegisteredAddressInUKController.onPageLoad(CheckMode)
    }

  private def doYouHaveUniqueTaxPayerReferenceRoutes()(ua: UserAnswers): Call =
    (ua.get(DoYouHaveUniqueTaxPayerReferencePage), ua.get(ReporterTypePage)) match {
      case (Some(true), _)           => WhatIsYourUTRController.onPageLoad(CheckMode)
      case (Some(false), Some(Sole)) => IndDoYouHaveNINumberController.onPageLoad(CheckMode)
      case (Some(false), Some(_))    => checkRoutes(BusinessNameWithoutIDPage)(ua)
      case _                         => DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode)
    }

  private def businessAddressWithoutIdRoutes()(ua: UserAnswers): Call = {
    val nextPageBasedOnRepType = ua.get(ReporterTypePage) match {
      case Some(Sole) => checkNextPageForValueThenRoute(
          ua,
          IndContactEmailPage,
          IndContactEmailController.onPageLoad(NormalMode)
        )
      case Some(_) => checkNextPageForValueThenRoute(
          ua,
          ContactNamePage,
          YourContactDetailsController.onPageLoad(CheckMode)
        )
      case _ =>
        JourneyRecoveryController.onPageLoad()
    }
    ua.get(NonUKBusinessAddressWithoutIDPage) match {
      case Some(_) => nextPageBasedOnRepType
      case _       => NonUKBusinessAddressWithoutIDController.onPageLoad(CheckMode)
    }
  }

  private def isThisYourBusinessRoutes()(ua: UserAnswers): Call =
    (ua.get(IsThisYourBusinessPage), ua.get(ReporterTypePage), ua.get(AutoMatchedUTRPage).isDefined) match {
      case (Some(true), Some(Sole), _) =>
        checkNextPageForValueThenRoute(
          ua,
          IndContactEmailPage,
          IndContactEmailController.onPageLoad(NormalMode)
        )
      case (Some(true), _, _) =>
        checkNextPageForValueThenRoute(ua, ContactNamePage, YourContactDetailsController.onPageLoad(CheckMode))
      case (Some(false), _, true)       => DifferentBusinessController.onPageLoad()
      case (Some(false), Some(Sole), _) => SoleTraderNotIdentifiedController.onPageLoad
      case _                            => BusinessNotIdentifiedController.onPageLoad()
    }

  private def whatIsYourUTRRoutes()(ua: UserAnswers): Call = {
    val resolveRouteIfUtrExists = ua.get(ReporterTypePage) match {
      case Some(Sole) => WhatIsYourNameController.onPageLoad(CheckMode)
      case _          => BusinessNameController.onPageLoad(CheckMode)
    }
    ua.get(WhatIsYourUTRPage) match {
      case Some(_) => resolveRouteIfUtrExists
      case _       => WhatIsYourUTRController.onPageLoad(CheckMode)
    }
  }

  private def doYouHaveNINORoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        checkNextPageForValueThenRoute(ua, IndWhatIsYourNINumberPage, IndWhatIsYourNINumberController.onPageLoad(CheckMode))
      case Some(false) =>
        checkNextPageForValueThenRoute(ua, IndWhatIsYourNamePage, IndWhatIsYourNameController.onPageLoad(CheckMode))
      case None => IndDoYouHaveNINumberController.onPageLoad(CheckMode)
    }

  private def whatIsYourNINumberRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        IndContactNameController.onPageLoad(CheckMode)
      case _ =>
        logger.warn("Have NI Number answer not found or false when routing from WhatIsYourNINumberPage")
        JourneyRecoveryController.onPageLoad()
    }

  private def contactNameRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        IndDateOfBirthController.onPageLoad(CheckMode)

      case _ =>
        logger.warn("Have NI Number answer not found or false when routing from IndContactNamePage")
        JourneyRecoveryController.onPageLoad()
    }

  private def businessNameWithoutIDRoutes()(ua: UserAnswers): Call =
    ua.get(BusinessNameWithoutIDPage) match {
      case Some(_) =>
        haveTradingNameRoutes(fromBusinessName = true)(ua)
      case _ =>
        BusinessNameWithoutIDController.onPageLoad(CheckMode)
    }

  private def haveTradingNameRoutes(fromBusinessName: Boolean = false)(ua: UserAnswers): Call =
    ua.get(HaveTradingNamePage) match {
      case Some(true) =>
        if (fromBusinessName) {
          businessTradingNameWithoutIDRoutes()(ua)
        } else {
          checkNextPageForValueThenRoute(
            ua,
            BusinessTradingNameWithoutIDPage,
            BusinessTradingNameWithoutIDController.onPageLoad(CheckMode),
            CheckYourAnswersController.onPageLoad()
          )
        }
      case Some(false) =>
        businessAddressWithoutIdRoutes()(ua)
      case _ =>
        HaveTradingNameController.onPageLoad(CheckMode)
    }

  private def businessTradingNameWithoutIDRoutes()(ua: UserAnswers): Call =
    ua.get(BusinessTradingNameWithoutIDPage) match {
      case Some(_) =>
        businessAddressWithoutIdRoutes()(ua)
      case _ =>
        BusinessTradingNameWithoutIDController.onPageLoad(CheckMode)
    }

  private def whatIsYourDateOfBirthRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        IndIdentityConfirmedController.onPageLoad(CheckMode)
      case Some(false) =>
        checkNextPageForValueThenRoute(
          ua,
          IndWhereDoYouLivePage,
          IndWhereDoYouLiveController.onPageLoad(CheckMode)
        )
      case _ =>
        logger.warn("NI Number answer not found when routing from DateOfBirthPage")
        JourneyRecoveryController.onPageLoad()
    }

  private def whatIsYourNameRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        IndIdentityConfirmedController.onPageLoad(CheckMode)
      case Some(false) =>
        checkNextPageForValueThenRoute(
          ua,
          DateOfBirthWithoutIdPage,
          IndDateOfBirthWithoutIdController.onPageLoad(CheckMode)
        )
      case _ =>
        logger.warn("Have NI Number answer not found when routing from IndWhatIsYourNamePage")
        JourneyRecoveryController.onPageLoad()
    }

  private def checkNextPageForValueThenRoute[A](
    userAnswers: UserAnswers,
    page: QuestionPage[A],
    callWhenNotAnswered: Call,
    callWhenAlreadyAnswered: Call = CheckYourAnswersController.onPageLoad()
  )(implicit rds: Reads[A]): Call = {
    val answerExists = userAnswers.get(page).fold(false)(
      _ => true
    )
    if (answerExists) {
      callWhenAlreadyAnswered
    } else {
      callWhenNotAnswered
    }
  }

  private def contactHavePhoneCheckModeNavigation(userAnswers: UserAnswers): Call =
    userAnswers.get(ContactHavePhonePage) match {
      case Some(true) =>
        userAnswers.get(ContactPhonePage) match {
          case Some(_) =>
            CheckYourAnswersController.onPageLoad()
          case None =>
            ContactPhoneController.onPageLoad(CheckMode)
        }
      case Some(false) =>
        CheckYourAnswersController.onPageLoad()
      case None =>
        JourneyRecoveryController.onPageLoad()
    }

  private def secondContactHavePhoneCheckModeNavigation(userAnswers: UserAnswers): Call =
    userAnswers.get(SecondContactHavePhonePage) match {
      case Some(true) =>
        userAnswers.get(SecondContactPhonePage) match {
          case Some(_) =>
            CheckYourAnswersController.onPageLoad()
          case None =>
            SecondContactPhoneController.onPageLoad(CheckMode)
        }
      case Some(false) =>
        CheckYourAnswersController.onPageLoad()
      case None =>
        JourneyRecoveryController.onPageLoad()
    }

  private def indContactHavePhoneCheckModeNavigation(userAnswers: UserAnswers): Call =
    userAnswers.get(IndContactHavePhonePage) match {
      case Some(true) =>
        userAnswers.get(IndContactPhonePage) match {
          case Some(_) =>
            CheckYourAnswersController.onPageLoad()
          case None =>
            IndContactPhoneController.onPageLoad(CheckMode)
        }
      case Some(false) =>
        CheckYourAnswersController.onPageLoad()
      case None =>
        JourneyRecoveryController.onPageLoad()
    }

}
