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

import controllers.routes
import models.ReporterType.{Individual, Sole}
import models._
import pages._
import pages.changeContactDetails._
import play.api.Logging
import play.api.libs.json.Reads
import play.api.mvc.Call

trait NormalRoutesNavigator extends Logging {

  val normalRoutes: Page => UserAnswers => Call = {
    case IndividualEmailPage     => _ => controllers.changeContactDetails.routes.IndividualHavePhoneController.onPageLoad(NormalMode)
    case IndividualHavePhonePage => _ => controllers.changeContactDetails.routes.IndividualPhoneController.onPageLoad(NormalMode)
    case IndividualPhonePage     => _ => controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad()
    case IsThisYourBusinessPage  => isThisYourBusiness(NormalMode)
    case ReporterTypePage        => whatAreYouReportingAs(NormalMode)
    case RegisteredAddressInUKPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          RegisteredAddressInUKPage,
          controllers.organisation.routes.WhatIsYourUTRController.onPageLoad(NormalMode),
          controllers.routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(NormalMode)
        )
    case DoYouHaveUniqueTaxPayerReferencePage => doYouHaveUniqueTaxPayerReference(NormalMode)
    case WhatIsYourUTRPage                    => isSoleProprietor(NormalMode)
    case WhatIsYourNamePage =>
      _ => controllers.organisation.routes.IsThisYourBusinessController.onPageLoad(NormalMode)
    case BusinessNamePage =>
      _ => controllers.organisation.routes.IsThisYourBusinessController.onPageLoad(NormalMode)
    // business without ID pages
    case BusinessNameWithoutIDPage => _ => controllers.organisation.routes.HaveTradingNameController.onPageLoad(NormalMode)
    case HaveTradingNamePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveTradingNamePage,
          controllers.organisation.routes.BusinessTradingNameWithoutIDController.onPageLoad(NormalMode),
          controllers.organisation.routes.NonUKBusinessAddressWithoutIDController.onPageLoad(NormalMode)
        )
    case BusinessTradingNameWithoutIDPage =>
      _ => controllers.organisation.routes.NonUKBusinessAddressWithoutIDController.onPageLoad(NormalMode)
    case NonUKBusinessAddressWithoutIDPage => businessAddressWithoutIdRouting(NormalMode)
    // org first contact details pages
    case YourContactDetailsPage => _ => controllers.organisation.routes.ContactNameController.onPageLoad(NormalMode)
    case ContactNamePage        => _ => controllers.organisation.routes.ContactEmailController.onPageLoad(NormalMode)
    case ContactEmailPage       => _ => controllers.organisation.routes.ContactHavePhoneController.onPageLoad(NormalMode)
    case ContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          ContactHavePhonePage,
          controllers.organisation.routes.ContactPhoneController.onPageLoad(NormalMode),
          controllers.organisation.routes.HaveSecondContactController.onPageLoad(NormalMode)
        )
    case ContactPhonePage => _ => controllers.organisation.routes.HaveSecondContactController.onPageLoad(NormalMode)
    // org second contact details pages
    case HaveSecondContactPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveSecondContactPage,
          controllers.organisation.routes.SecondContactNameController.onPageLoad(NormalMode),
          controllers.routes.CheckYourAnswersController.onPageLoad()
        )
    case SecondContactNamePage  => _ => controllers.organisation.routes.SecondContactEmailController.onPageLoad(NormalMode)
    case SecondContactEmailPage => _ => controllers.organisation.routes.SecondContactHavePhoneController.onPageLoad(NormalMode)
    case SecondContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SecondContactHavePhonePage,
          controllers.organisation.routes.SecondContactPhoneController.onPageLoad(NormalMode),
          controllers.routes.CheckYourAnswersController.onPageLoad()
        )
    case SecondContactPhonePage => _ => controllers.routes.CheckYourAnswersController.onPageLoad()

    // individual
    case IndDoYouHaveNINumberPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IndDoYouHaveNINumberPage,
          controllers.individual.routes.IndWhatIsYourNINumberController.onPageLoad(NormalMode),
          controllers.individual.routes.IndWhatIsYourNameController.onPageLoad(NormalMode)
        )
    case IndWhatIsYourNINumberPage => _ => controllers.individual.routes.IndContactNameController.onPageLoad(NormalMode)
    case IndContactNamePage        => _ => controllers.individual.routes.IndDateOfBirthController.onPageLoad(NormalMode)
    case IndWhatIsYourNamePage     => _ => controllers.individual.routes.IndDateOfBirthWithoutIdController.onPageLoad(NormalMode)
    case DateOfBirthWithoutIdPage  => whatIsYourDateOfBirthRoutes(NormalMode)
    case RegistrationInfoPage      => _ => controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
    case IndWhereDoYouLivePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IndWhereDoYouLivePage,
          controllers.individual.routes.IndWhatIsYourPostcodeController.onPageLoad(NormalMode),
          controllers.individual.routes.IndNonUKAddressWithoutIdController.onPageLoad(NormalMode)
        )
    case IndUKAddressWithoutIdPage    => _ => controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
    case IndNonUKAddressWithoutIdPage => _ => controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
    case IndWhatIsYourPostcodePage    => addressLookupNavigation(NormalMode)
    case IsThisYourAddressPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisYourAddressPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode),
          controllers.individual.routes.IndUKAddressWithoutIdController.onPageLoad(NormalMode)
        )

    case IndSelectAddressPage => _ => controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
    case IndContactEmailPage  => _ => controllers.individual.routes.IndContactHavePhoneController.onPageLoad(NormalMode)
    case IndContactPhonePage  => _ => controllers.routes.CheckYourAnswersController.onPageLoad()
    case IndDateOfBirthPage   => whatIsYourDateOfBirthRoutes(NormalMode)
    case IndContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IndContactHavePhonePage,
          controllers.individual.routes.IndContactPhoneController.onPageLoad(NormalMode),
          controllers.routes.CheckYourAnswersController.onPageLoad()
        )

    case _ => _ => routes.IndexController.onPageLoad
  }

  private def addressLookupNavigation(mode: Mode)(ua: UserAnswers): Call =
    ua.get(AddressLookupPage) match {
      case Some(value) if value.length == 1 => controllers.individual.routes.IndIsThisYourAddressController.onPageLoad(mode)
      case _                                => controllers.individual.routes.IndSelectAddressController.onPageLoad(mode)
    }

  private def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def whatAreYouReportingAs(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(ReporterTypePage), mode) match {
      case (Some(Individual), _) => controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(mode)
      case (Some(_), _)          => controllers.organisation.routes.RegisteredAddressInUKController.onPageLoad(mode)
      case (None, _)             => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(DoYouHaveUniqueTaxPayerReferencePage), ua.get(ReporterTypePage)) match {
      case (Some(true), _)           => controllers.organisation.routes.WhatIsYourUTRController.onPageLoad(mode)
      case (Some(false), Some(Sole)) => controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(mode)
      case (Some(false), Some(_))    => controllers.organisation.routes.BusinessNameWithoutIDController.onPageLoad(mode)
      case (None, Some(_)) =>
        logger.warn("DoYouHaveUniqueTaxPayerReference answer not found when routing from DoYouHaveUniqueTaxPayerReferencePage")
        routes.JourneyRecoveryController.onPageLoad()
      case (Some(_), None) =>
        logger.warn("ReporterType answer not found when routing from DoYouHaveUniqueTaxPayerReferencePage")
        routes.JourneyRecoveryController.onPageLoad()
      case (None, None) =>
        logger.warn("DoYouHaveUniqueTaxPayerReference and ReporterType answers not found when routing from DoYouHaveUniqueTaxPayerReferencePage")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def businessAddressWithoutIdRouting(mode: Mode)(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Sole) => checkNextPageForValueThenRoute(
          mode,
          ua,
          IndContactEmailPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
      case Some(_) => checkNextPageForValueThenRoute(
          mode,
          ua,
          ContactNamePage,
          routes.YourContactDetailsController.onPageLoad(NormalMode)
        )
      case _ =>
        logger.warn(s"ReporterType answer not found when routing from NonUKBusinessAddressWithoutIDPage in mode $mode")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def isThisYourBusiness(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(IsThisYourBusinessPage), ua.get(ReporterTypePage), ua.get(AutoMatchedUTRPage).isDefined) match {
      case (Some(true), Some(Sole), _) =>
        checkNextPageForValueThenRoute(
          mode,
          ua,
          IndContactEmailPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
      case (Some(true), _, _) =>
        checkNextPageForValueThenRoute(mode, ua, ContactNamePage, routes.YourContactDetailsController.onPageLoad(NormalMode))
      case (Some(false), _, true)       => controllers.organisation.routes.DifferentBusinessController.onPageLoad()
      case (Some(false), Some(Sole), _) => controllers.routes.SoleTraderNotIdentifiedController.onPageLoad
      case _                            => controllers.organisation.routes.BusinessNotIdentifiedController.onPageLoad()
    }

  private def isSoleProprietor(mode: Mode)(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Sole) => controllers.organisation.routes.WhatIsYourNameController.onPageLoad(mode)
      case _          => controllers.organisation.routes.BusinessNameController.onPageLoad(mode)
    }

  private def whatIsYourDateOfBirthRoutes(mode: Mode)(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(mode)
      case Some(false) =>
        checkNextPageForValueThenRoute(
          mode,
          ua,
          IndWhereDoYouLivePage,
          controllers.individual.routes.IndWhereDoYouLiveController.onPageLoad(mode)
        )
      case _ =>
        logger.warn("NI Number answer not found when routing from DateOfBirthPage")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def checkNextPageForValueThenRoute[A](
    mode: Mode,
    userAnswers: UserAnswers,
    page: QuestionPage[A],
    callWhenNotAnswered: Call,
    callWhenAlreadyAnswered: Call = routes.CheckYourAnswersController.onPageLoad()
  )(implicit rds: Reads[A]): Call = {
    val answerExists = mode.equals(CheckMode) && userAnswers.get(page).fold(false)(
      _ => true
    )
    if (answerExists) {
      callWhenAlreadyAnswered
    } else {
      callWhenNotAnswered
    }
  }

}
