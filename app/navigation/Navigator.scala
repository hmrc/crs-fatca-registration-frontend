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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import models.ReporterType.{Individual, Sole}
import pages._
import models._
import play.api.Logging

@Singleton
class Navigator @Inject() () extends Logging {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ReporterTypePage => whatAreYouReportingAs(NormalMode)
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
    // business without ID pages
    case BusinessNameWithoutIDPage => _ => controllers.organisation.routes.HaveTradingNameController.onPageLoad(NormalMode)
    case HaveTradingNamePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveTradingNamePage,
          controllers.organisation.routes.BusinessTradingNameWithoutIDController.onPageLoad(NormalMode),
          controllers.organisation.routes.BusinessAddressWithoutIDController.onPageLoad(NormalMode)
        )
    case BusinessTradingNameWithoutIDPage =>
      _ => controllers.organisation.routes.BusinessAddressWithoutIDController.onPageLoad(NormalMode)
    case BusinessAddressWithoutIDPage => businessAddressWithoutIdRouting(NormalMode)
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
          controllers.routes.CheckYourAnswersController.onPageLoad
        )
    case ContactPhonePage => _ => controllers.organisation.routes.HaveSecondContactController.onPageLoad(NormalMode)
    // org second contact details pages
    case HaveSecondContactPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveSecondContactPage,
          controllers.organisation.routes.SecondContactNameController.onPageLoad(NormalMode),
          controllers.routes.CheckYourAnswersController.onPageLoad
        )
    case SecondContactNamePage  => _ => controllers.organisation.routes.SecondContactEmailController.onPageLoad(NormalMode)
    case SecondContactEmailPage => _ => controllers.organisation.routes.SecondContactHavePhoneController.onPageLoad(NormalMode)
    case SecondContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SecondContactHavePhonePage,
          controllers.organisation.routes.SecondContactPhoneController.onPageLoad(NormalMode),
          controllers.routes.CheckYourAnswersController.onPageLoad
        )
    case SecondContactPhonePage => _ => controllers.routes.CheckYourAnswersController.onPageLoad

    case IndContactEmailPage     => _ => controllers.individual.routes.IndContactHavePhoneController.onPageLoad(NormalMode)
    case IndContactHavePhonePage => _ => controllers.individual.routes.IndContactPhoneController.onPageLoad(NormalMode)
    case IndContactPhonePage     => _ => controllers.routes.CheckYourAnswersController.onPageLoad
    case IndDateOfBirthPage      => _ => controllers.individual.routes.IndIdentityConfirmedController.onPageLoad()
    case IndWhatIsYourPostcodePage =>
      ua => addressLookupNavigation(NormalMode)(ua)

    case _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case ReporterTypePage => whatAreYouReportingAs(CheckMode)
    case RegisteredAddressInUKPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          RegisteredAddressInUKPage,
          controllers.organisation.routes.WhatIsYourUTRController.onPageLoad(CheckMode),
          controllers.routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode)
        )
    case DoYouHaveUniqueTaxPayerReferencePage => doYouHaveUniqueTaxPayerReference(CheckMode)
    case WhatIsYourUTRPage                    => isSoleProprietor(CheckMode)
    case _                                    => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private def addressLookupNavigation(mode: Mode)(ua: UserAnswers): Call =
    ua.get(AddressLookupPage) match {
      case Some(value) if value.length == 1 => controllers.individual.routes.IndIsThisYourAddressController.onPageLoad(mode)
      case _                                => controllers.individual.routes.IndWhatIsYourPostcodeController.onPageLoad(mode)
    }

  private def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def whatAreYouReportingAs(mode: Mode)(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Individual) => controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(mode)
      case Some(_)          => controllers.organisation.routes.RegisteredAddressInUKController.onPageLoad(mode)
      case None             => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(DoYouHaveUniqueTaxPayerReferencePage), ua.get(ReporterTypePage)) match {
      case (Some(true), _)                 => controllers.organisation.routes.WhatIsYourUTRController.onPageLoad(mode)
      case (Some(false), Some(Individual)) => controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(mode)
      case (Some(false), Some(_))          => controllers.organisation.routes.BusinessNameWithoutIDController.onPageLoad(mode)
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
      case Some(Sole) | Some(Individual) => controllers.individual.routes.IndContactEmailController.onPageLoad(mode)
      case Some(_)                       => controllers.routes.YourContactDetailsController.onPageLoad()
      case _ =>
        logger.warn("ReporterType answer not found when routing from ReporterTypePage")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def isSoleProprietor(mode: Mode)(ua: UserAnswers): Call =
    ua.get(ReporterTypePage) match {
      case Some(Sole) => controllers.individual.routes.IndWhatIsYourNameController.onPageLoad(mode)
      case _          => controllers.organisation.routes.BusinessNameController.onPageLoad(mode)
    }

}
