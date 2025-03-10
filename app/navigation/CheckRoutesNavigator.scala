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

trait CheckRoutesNavigator extends Logging {

  val checkRoutes: Page => UserAnswers => Call = {
    case ReporterTypePage => whatAreYouReportingAs()
    case RegisteredAddressInUKPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          RegisteredAddressInUKPage,
          controllers.organisation.routes.WhatIsYourUTRController.onPageLoad(CheckMode),
          controllers.routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode)
        )
    case DoYouHaveUniqueTaxPayerReferencePage => doYouHaveUniqueTaxPayerReference()
    case WhatIsYourUTRPage                    => isSoleProprietor()
    case WhatIsYourNamePage                   => _ => controllers.organisation.routes.IsThisYourBusinessController.onPageLoad(CheckMode)
    case BusinessNamePage                     => _ => controllers.organisation.routes.IsThisYourBusinessController.onPageLoad(CheckMode)
    case IsThisYourBusinessPage               => isThisYourBusiness()
    case BusinessNameWithoutIDPage            => _ => routes.CheckYourAnswersController.onPageLoad()
    case HaveTradingNamePage => userAnswers =>
        yesNoPage(
          userAnswers,
          HaveTradingNamePage,
          controllers.organisation.routes.BusinessTradingNameWithoutIDController.onPageLoad(CheckMode),
          checkNextPageForValueThenRoute(
            userAnswers,
            page = NonUKBusinessAddressWithoutIDPage,
            callWhenNotAnswered = controllers.organisation.routes.NonUKBusinessAddressWithoutIDController.onPageLoad(CheckMode)
          )
        )
    case BusinessTradingNameWithoutIDPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          NonUKBusinessAddressWithoutIDPage,
          controllers.organisation.routes.NonUKBusinessAddressWithoutIDController.onPageLoad(CheckMode)
        )
    case NonUKBusinessAddressWithoutIDPage => businessAddressWithoutIdRouting()
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
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
    case DateOfBirthWithoutIdPage => whatIsYourDateOfBirthRoutes()
    case IndWhereDoYouLivePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IndWhereDoYouLivePage,
          controllers.individual.routes.IndWhatIsYourPostcodeController.onPageLoad(CheckMode),
          controllers.individual.routes.IndNonUKAddressWithoutIdController.onPageLoad(CheckMode)
        )
    case IndNonUKAddressWithoutIdPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          IndContactEmailPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
    case IndWhatIsYourPostcodePage => addressLookupNavigation()
    case IndSelectAddressPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          IndContactEmailPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
    case IsThisYourAddressPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisYourAddressPage,
          checkNextPageForValueThenRoute(
            userAnswers,
            IndContactEmailPage,
            controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
          ),
          controllers.individual.routes.IndUKAddressWithoutIdController.onPageLoad(CheckMode)
        )
    case IndUKAddressWithoutIdPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(
          userAnswers,
          IndContactEmailPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
    case IndContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IndContactHavePhonePage,
          controllers.individual.routes.IndContactPhoneController.onPageLoad(CheckMode),
          controllers.routes.CheckYourAnswersController.onPageLoad()
        )
    case IndContactPhonePage => _ => controllers.routes.CheckYourAnswersController.onPageLoad()
    case ContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          ContactHavePhonePage,
          controllers.organisation.routes.ContactPhoneController.onPageLoad(CheckMode),
          controllers.routes.CheckYourAnswersController.onPageLoad()
        )
    case ContactPhonePage => _ => controllers.routes.CheckYourAnswersController.onPageLoad()
    case HaveSecondContactPage =>
      userAnswers =>
        yesNoNavigate(
          userAnswers.get(HaveSecondContactPage).exists(_ != true) || userAnswers.get(SecondContactNamePage).isDefined,
          controllers.routes.CheckYourAnswersController.onPageLoad(),
          controllers.organisation.routes.SecondContactNameController.onPageLoad(CheckMode)
        )
    case SecondContactNamePage =>
      userAnswers =>
        yesNoNavigate(
          userAnswers.get(SecondContactEmailPage).isDefined,
          controllers.routes.CheckYourAnswersController.onPageLoad(),
          controllers.organisation.routes.SecondContactEmailController.onPageLoad(CheckMode)
        )
    case SecondContactEmailPage => userAnswers =>
        yesNoNavigate(
          userAnswers.get(SecondContactHavePhonePage).isDefined,
          controllers.routes.CheckYourAnswersController.onPageLoad(),
          controllers.organisation.routes.SecondContactHavePhoneController.onPageLoad(CheckMode)
        )
    case SecondContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SecondContactHavePhonePage,
          controllers.organisation.routes.SecondContactPhoneController.onPageLoad(CheckMode),
          controllers.routes.CheckYourAnswersController.onPageLoad()
        )
    case SecondContactPhonePage       => _ => controllers.routes.CheckYourAnswersController.onPageLoad()
    case OrganisationContactNamePage  => _ => controllers.changeContactDetails.routes.OrganisationContactEmailController.onPageLoad(CheckMode)
    case OrganisationContactEmailPage => _ => controllers.changeContactDetails.routes.OrganisationContactHavePhoneController.onPageLoad(CheckMode)
    case OrganisationContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          OrganisationContactHavePhonePage,
          controllers.changeContactDetails.routes.OrganisationContactPhoneController.onPageLoad(CheckMode),
          controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
        )
    case OrganisationContactPhonePage => _ => controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
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
    case OrganisationSecondContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          OrganisationSecondContactHavePhonePage,
          controllers.changeContactDetails.routes.OrganisationSecondContactPhoneController.onPageLoad(CheckMode),
          controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()
        )
    case OrganisationSecondContactPhonePage => _ => controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()

    case IndividualEmailPage => _ => controllers.changeContactDetails.routes.IndividualHavePhoneController.onPageLoad(CheckMode)
    case IndividualHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IndividualHavePhonePage,
          controllers.changeContactDetails.routes.IndividualPhoneController.onPageLoad(CheckMode),
          controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad()
        )
    case IndividualPhonePage => _ => controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad()

    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def addressLookupNavigation()(ua: UserAnswers): Call =
    ua.get(AddressLookupPage) match {
      case Some(value) if value.length == 1 => controllers.individual.routes.IndIsThisYourAddressController.onPageLoad(CheckMode)
      case _                                => controllers.individual.routes.IndSelectAddressController.onPageLoad(CheckMode)
    }

  private def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def yesNoNavigate(check: => Boolean, yesCall: => Call, noCall: => Call): Call = if (check) yesCall else noCall

  private def whatAreYouReportingAs()(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Individual) =>
        checkNextPageForValueThenRoute(
          ua,
          IndDoYouHaveNINumberPage,
          controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(CheckMode)
        )
      case Some(_) =>
        checkNextPageForValueThenRoute(
          ua,
          RegisteredAddressInUKPage,
          controllers.organisation.routes.RegisteredAddressInUKController.onPageLoad(CheckMode)
        )
      case None => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def doYouHaveUniqueTaxPayerReference()(ua: UserAnswers): Call =
    (ua.get(DoYouHaveUniqueTaxPayerReferencePage), ua.get(ReporterTypePage)) match {
      case (Some(true), _)           => controllers.organisation.routes.WhatIsYourUTRController.onPageLoad(CheckMode)
      case (Some(false), Some(Sole)) => controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(CheckMode)
      case (Some(false), Some(_)) =>
        checkNextPageForValueThenRoute(ua, BusinessNameWithoutIDPage, controllers.organisation.routes.BusinessNameWithoutIDController.onPageLoad(CheckMode))
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

  private def businessAddressWithoutIdRouting()(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Sole) => checkNextPageForValueThenRoute(
          ua,
          IndContactEmailPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
      case Some(_) => checkNextPageForValueThenRoute(
          ua,
          ContactNamePage,
          routes.YourContactDetailsController.onPageLoad()
        )
      case _ =>
        logger.warn(s"ReporterType answer not found when routing from NonUKBusinessAddressWithoutIDPage in mode $CheckMode")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def isThisYourBusiness()(ua: UserAnswers): Call =
    (ua.get(IsThisYourBusinessPage), ua.get(ReporterTypePage), ua.get(AutoMatchedUTRPage).isDefined) match {
      case (Some(true), Some(Sole), _) =>
        checkNextPageForValueThenRoute(
          ua,
          IndContactEmailPage,
          controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)
        )
      case (Some(true), _, _) =>
        checkNextPageForValueThenRoute(ua, ContactNamePage, routes.YourContactDetailsController.onPageLoad())
      case (Some(false), _, true)       => controllers.organisation.routes.DifferentBusinessController.onPageLoad()
      case (Some(false), Some(Sole), _) => controllers.routes.SoleTraderNotIdentifiedController.onPageLoad
      case _                            => controllers.organisation.routes.BusinessNotIdentifiedController.onPageLoad()
    }

  private def isSoleProprietor()(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Sole) => controllers.organisation.routes.WhatIsYourNameController.onPageLoad(CheckMode)
      case _          => controllers.organisation.routes.BusinessNameController.onPageLoad(CheckMode)
    }

  private def doYouHaveNINORoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        checkNextPageForValueThenRoute(ua, IndWhatIsYourNINumberPage, controllers.individual.routes.IndWhatIsYourNINumberController.onPageLoad(CheckMode))
      case Some(false) =>
        checkNextPageForValueThenRoute(ua, IndWhatIsYourNamePage, controllers.individual.routes.IndWhatIsYourNameController.onPageLoad(CheckMode))
      case _ =>
        logger.warn("NI Number answer not found when routing from IndDoYouHaveNINumberPage")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def whatIsYourNINumberRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        controllers.individual.routes.IndContactNameController.onPageLoad(CheckMode)
      case _ =>
        logger.warn("Have NI Number answer not found or false when routing from WhatIsYourNINumberPage")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def contactNameRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        controllers.individual.routes.IndDateOfBirthController.onPageLoad(CheckMode)

      case _ =>
        logger.warn("Have NI Number answer not found or false when routing from IndContactNamePage")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def whatIsYourDateOfBirthRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(CheckMode)
      case Some(false) =>
        checkNextPageForValueThenRoute(
          ua,
          IndWhereDoYouLivePage,
          controllers.individual.routes.IndWhereDoYouLiveController.onPageLoad(CheckMode)
        )
      case _ =>
        logger.warn("NI Number answer not found when routing from DateOfBirthPage")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def whatIsYourNameRoutes()(ua: UserAnswers): Call =
    ua.get(IndDoYouHaveNINumberPage) match {
      case Some(true) =>
        controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(CheckMode)
      case Some(false) =>
        checkNextPageForValueThenRoute(
          ua,
          DateOfBirthWithoutIdPage,
          controllers.individual.routes.IndDateOfBirthWithoutIdController.onPageLoad(CheckMode)
        )
      case _ =>
        logger.warn("Have NI Number answer not found when routing from IndWhatIsYourNamePage")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def checkNextPageForValueThenRoute[A](
    userAnswers: UserAnswers,
    page: QuestionPage[A],
    callWhenNotAnswered: Call,
    callWhenAlreadyAnswered: Call = routes.CheckYourAnswersController.onPageLoad()
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

}
