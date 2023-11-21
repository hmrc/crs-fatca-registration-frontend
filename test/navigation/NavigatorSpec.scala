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

import base.SpecBase
import controllers.individual.routes._
import controllers.organisation.routes._
import controllers.routes
import generators.{Generators, UserAnswersGenerator}
import helpers.JsonFixtures.utr
import models.ReporterType.{Individual, LimitedCompany, LimitedPartnership, Partnership, Sole, UnincorporatedAssociation}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

import java.time.LocalDate

class NavigatorSpec extends SpecBase with TableDrivenPropertyChecks with Generators with UserAnswersGenerator {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go from ReporterTypePage to IndDoYouHaveNINumberPage for Individual reporter" in {

        val userAnswers = emptyUserAnswers.set(ReporterTypePage, Individual).success.value
        navigator.nextPage(ReporterTypePage, NormalMode, userAnswers) mustBe IndDoYouHaveNINumberController.onPageLoad(NormalMode)
      }

      val nonIndividualReporterTypes = TableDrivenPropertyChecks.Table(
        "nonIndividualReporterTypes",
        ReporterType.values.filter(_ != ReporterType.Individual): _*
      )

      forAll(nonIndividualReporterTypes) {
        reporterType =>
          s"must go from ReporterTypePage to RegisteredAddressInUKPage for $reporterType reporter" in {

            val userAnswers = emptyUserAnswers.set(ReporterTypePage, reporterType).success.value
            navigator.nextPage(ReporterTypePage, NormalMode, userAnswers) mustBe RegisteredAddressInUKController.onPageLoad(NormalMode)
          }
      }

      "must go from ReporterTypePage to JourneyRecoveryPage when reporterType is not answered" in {

        navigator.nextPage(ReporterTypePage, NormalMode, emptyUserAnswers) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from RegisteredAddressInUKPage to WhatIsYourUTRPage when user answers yes" in {
        val userAnswers = emptyUserAnswers.set(RegisteredAddressInUKPage, true).success.value
        navigator.nextPage(RegisteredAddressInUKPage, NormalMode, userAnswers) mustBe controllers.organisation.routes.WhatIsYourUTRController
          .onPageLoad(NormalMode)
      }

      "must go from RegisteredAddressInUKPage to DoYouHaveUniqueTaxPayerReferencePage when user answers no" in {

        val userAnswers = emptyUserAnswers.set(RegisteredAddressInUKPage, false).success.value
        navigator.nextPage(
          RegisteredAddressInUKPage,
          NormalMode,
          userAnswers
        ) mustBe controllers.routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(NormalMode)
      }

      "must go from DoYouHaveUniqueTaxPayerReferencePage to WhatIsYourUTRPage when user has a UTR" in {
        val userAnswers = emptyUserAnswers.set(DoYouHaveUniqueTaxPayerReferencePage, true).success.value
        navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, userAnswers) mustBe controllers.organisation.routes.WhatIsYourUTRController
          .onPageLoad(NormalMode)
      }

      "must go from DoYouHaveUniqueTaxPayerReferencePage to IndDoYouHaveNINumberPage for Individual reporter with no UTR" in {

        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.Individual)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value

        navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, userAnswers) mustBe IndDoYouHaveNINumberController.onPageLoad(NormalMode)
      }

      forAll(nonIndividualReporterTypes) {
        reporterType =>
          s"must go from DoYouHaveUniqueTaxPayerReferencePage to BusinessNameWithoutIDPage for $reporterType reporter with no UTR" in {

            val userAnswers = emptyUserAnswers
              .set(ReporterTypePage, reporterType)
              .success
              .value
              .set(DoYouHaveUniqueTaxPayerReferencePage, false)
              .success
              .value

            navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, userAnswers) mustBe BusinessNameWithoutIDController.onPageLoad(NormalMode)
          }
      }

      "must go from DoYouHaveUniqueTaxPayerReferencePage to JourneyRecoveryPage when DoYouHaveUniqueTaxPayerReference and ReporterType are not answered" in {
        navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, emptyUserAnswers) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from DoYouHaveUniqueTaxPayerReferencePage to JourneyRecoveryPage when ReporterType is not answered" in {

        val userAnswers = emptyUserAnswers.set(DoYouHaveUniqueTaxPayerReferencePage, false).success.value

        navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, userAnswers) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      forAll(nonIndividualReporterTypes) {
        reporterType =>
          s"must go from DoYouHaveUniqueTaxPayerReferencePage to JourneyRecoveryPage when only reporterType is answered as $reporterType" in {

            val userAnswers = emptyUserAnswers.set(ReporterTypePage, reporterType).success.value

            navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, userAnswers) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
          }
      }

      "must go from BusinessNameWithoutIDPage to HaveTradingNamePage" in {

        navigator.nextPage(BusinessNameWithoutIDPage, NormalMode, emptyUserAnswers) mustBe HaveTradingNameController.onPageLoad(NormalMode)
      }

      "must go from HaveTradingNamePage to BusinessTradingNameWithoutIDPage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(HaveTradingNamePage, true).success.value
        navigator.nextPage(HaveTradingNamePage, NormalMode, userAnswers) mustBe BusinessTradingNameWithoutIDController.onPageLoad(NormalMode)
      }

      "must go from HaveTradingNamePage to BusinessAddressWithoutIDPage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(HaveTradingNamePage, false).success.value
        navigator.nextPage(HaveTradingNamePage, NormalMode, userAnswers) mustBe NonUKBusinessAddressWithoutIDController.onPageLoad(NormalMode)
      }

      "must gro from BusinessTradingNameWithoutIDPage to BusinessAddressWithoutIDPage" in {

        navigator.nextPage(BusinessTradingNameWithoutIDPage, NormalMode, emptyUserAnswers) mustBe NonUKBusinessAddressWithoutIDController.onPageLoad(NormalMode)
      }

      "must go from YourContactDetailsPage to ContactNamePage" in {

        navigator.nextPage(YourContactDetailsPage, NormalMode, UserAnswers("id")) mustBe ContactNameController.onPageLoad(NormalMode)
      }

      "must go from ContactNamePage to ContactEmailPage" in {

        navigator.nextPage(ContactNamePage, NormalMode, emptyUserAnswers) mustBe ContactEmailController.onPageLoad(NormalMode)
      }

      "must go from ContactEmailPage to ContactHavePhonePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(ContactEmailPage, "test@email").success.value
        navigator.nextPage(ContactEmailPage, NormalMode, userAnswers) mustBe ContactHavePhoneController.onPageLoad(NormalMode)
      }

      "must go from ContactHavePhonePage to ContactPhonePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(ContactHavePhonePage, true).success.value
        navigator.nextPage(ContactHavePhonePage, NormalMode, userAnswers) mustBe ContactPhoneController.onPageLoad(NormalMode)
      }

      "must go from ContactHavePhonePage to CheckYourAnswersPage when user answers no" in {

        val userAnswers = emptyUserAnswers.set(ContactHavePhonePage, false).success.value
        navigator.nextPage(ContactHavePhonePage, NormalMode, userAnswers) mustBe controllers.routes.CheckYourAnswersController.onPageLoad
      }

      "must go from ContactPhonePage to HaveSecondContactPage" in {

        navigator.nextPage(ContactPhonePage, NormalMode, emptyUserAnswers) mustBe HaveSecondContactController.onPageLoad(NormalMode)
      }

      "must go from SecondContactNamePage to SecondContactEmailPage" in {

        val userAnswers = emptyUserAnswers.set(SecondContactNamePage, "value").success.value
        navigator.nextPage(SecondContactNamePage, NormalMode, userAnswers) mustBe SecondContactEmailController.onPageLoad(NormalMode)
      }

      "must go from SecondContactEmailPage to SecondContactHavePhonePage" in {

        navigator.nextPage(SecondContactEmailPage, NormalMode, emptyUserAnswers) mustBe SecondContactHavePhoneController.onPageLoad(NormalMode)
      }

      "must go from SecondContactHavePhonePage to SecondContactPhonePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(SecondContactHavePhonePage, true).success.value
        navigator.nextPage(SecondContactHavePhonePage, NormalMode, userAnswers) mustBe SecondContactPhoneController.onPageLoad(NormalMode)
      }

      "must go from SecondContactHavePhonePage to CheckYourAnswersPage when user answers no" in {

        val userAnswers = emptyUserAnswers.set(SecondContactHavePhonePage, false).success.value
        navigator.nextPage(SecondContactHavePhonePage, NormalMode, userAnswers) mustBe controllers.routes.CheckYourAnswersController.onPageLoad
      }

      "must go from SecondContactPhonePage to CheckYourAnswersPage" in {

        val userAnswers = emptyUserAnswers.set(SecondContactPhonePage, "123456789").success.value
        navigator.nextPage(SecondContactPhonePage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad
      }
      "must go from IndContactEmailPage to IndContactHavePhonePage" in {

        val userAnswers = emptyUserAnswers.set(IndContactEmailPage, "test@email").success.value
        navigator.nextPage(IndContactEmailPage, NormalMode, userAnswers) mustBe
          controllers.individual.routes.IndContactHavePhoneController.onPageLoad(NormalMode)
      }

      "must go from IndContactPhonePage to CheckYourAnswersPage" in {

        val userAnswers = emptyUserAnswers.set(IndContactPhonePage, "123456789").success.value
        navigator.nextPage(IndContactPhonePage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "must go from IndDateOfBirthPage to IndIdentityConfirmedPage" in {

        val userAnswers = emptyUserAnswers.set(IndDateOfBirthPage, LocalDate.now()).success.value
        navigator.nextPage(IndDateOfBirthPage, NormalMode, userAnswers) mustBe IndIdentityConfirmedController.onPageLoad
      }

      "must go from IsThisYourBusinessPage" - {

        val nonOrgReporters = Table(
          "nonOrgReporter",
          Seq(Sole, Individual): _*
        )

        "to IndContactEmailPage when Yes is selected for Sole reporter type" in {
          ScalaCheckPropertyChecks.forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ReporterTypePage, Sole)
                  .success
                  .value
                  .set(IsThisYourBusinessPage, true)
                  .success
                  .value

              navigator
                .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
                .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode))
          }
        }

        forAll(nonOrgReporters) {
          nonOrgReporter =>
            s"to DifferentBusinessPage when No is selected for an auto-matched $nonOrgReporter reporter type" in {
              ScalaCheckPropertyChecks.forAll(arbitrary[UserAnswers]) {
                answers =>
                  val updatedAnswers =
                    answers
                      .set(ReporterTypePage, nonOrgReporter)
                      .success
                      .value
                      .set(IsThisYourBusinessPage, false)
                      .success
                      .value
                      .set(AutoMatchedUTRPage, utr)
                      .success
                      .value

                  navigator
                    .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
                    .mustBe(controllers.organisation.routes.DifferentBusinessController.onPageLoad())
              }
            }
        }

        val orgReporters = Table(
          "orgReporters",
          Seq(LimitedCompany, Partnership, LimitedPartnership, UnincorporatedAssociation): _*
        )

        forAll(orgReporters) {
          orgReporter =>
            s"to YourContactDetailsPage when Yes is selected for $orgReporter reporter type" in {
              ScalaCheckPropertyChecks.forAll(arbitrary[UserAnswers]) {
                answers =>
                  val updatedAnswers =
                    answers
                      .set(ReporterTypePage, orgReporter)
                      .success
                      .value
                      .set(IsThisYourBusinessPage, true)
                      .success
                      .value

                  navigator
                    .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
                    .mustBe(routes.YourContactDetailsController.onPageLoad())
              }
            }
        }

        forAll(orgReporters) {
          orgReporter =>
            s"to DifferentBusinessPage when Yes is selected for an auto-matched $orgReporter reporter type" in {
              ScalaCheckPropertyChecks.forAll(arbitrary[UserAnswers]) {
                answers =>
                  val updatedAnswers =
                    answers
                      .set(ReporterTypePage, orgReporter)
                      .success
                      .value
                      .set(IsThisYourBusinessPage, false)
                      .success
                      .value
                      .set(AutoMatchedUTRPage, utr)
                      .success
                      .value

                  navigator
                    .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
                    .mustBe(controllers.organisation.routes.DifferentBusinessController.onPageLoad())
              }
            }
        }

        "to YourContactDetailsPage when there is no ReporterType and Yes is selected" in {
          ScalaCheckPropertyChecks.forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .remove(ReporterTypePage)
                  .success
                  .value
                  .set(IsThisYourBusinessPage, true)
                  .success
                  .value

              navigator
                .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
                .mustBe(routes.YourContactDetailsController.onPageLoad())
          }
        }

        "to BusinessNotIdentifiedPage when No is selected" in {
          ScalaCheckPropertyChecks.forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ReporterTypePage, LimitedCompany)
                  .success
                  .value
                  .set(IsThisYourBusinessPage, false)
                  .success
                  .value

              navigator
                .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
                .mustBe(controllers.organisation.routes.BusinessNotIdentifiedController.onPageLoad())
          }
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }

}
