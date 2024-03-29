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
import helpers.JsonFixtures._
import models.ReporterType.{Individual, LimitedCompany, LimitedPartnership, Partnership, Sole, UnincorporatedAssociation}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.domain.Nino

import java.time.{Clock, LocalDate}

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

      "must go from DoYouHaveNINumberPage to WhatIsYourNINumberPage for Individual reporter if have Nino" in {

        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, true)
          .success
          .value
        navigator.nextPage(IndDoYouHaveNINumberPage, NormalMode, userAnswers) mustBe IndWhatIsYourNINumberController.onPageLoad(NormalMode)
      }

      "must go from IndDoYouHaveNINumberPage to IndContactNamePage for Individual reporter if no Nino" in {

        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, false)
          .success
          .value
        navigator.nextPage(IndDoYouHaveNINumberPage, NormalMode, userAnswers) mustBe IndWhatIsYourNameController.onPageLoad(NormalMode)

      }

      "must go from whatIsNINumberPage to WhatIsYourNamePage if have Nino" in {

        val userAnswers = emptyUserAnswers
          .set(IndWhatIsYourNINumberPage, Nino("CC123456C"))
          .success
          .value
        navigator.nextPage(IndWhatIsYourNINumberPage, NormalMode, userAnswers) mustBe IndContactNameController.onPageLoad(NormalMode)
      }

      "must go from IndContactNamePage to IndDateOfBirthPage" in {

        val userAnswers = emptyUserAnswers
          .set(IndContactNamePage, Name(FirstName, LastName))
          .success
          .value

        navigator.nextPage(IndContactNamePage, NormalMode, userAnswers) mustBe IndDateOfBirthController.onPageLoad(NormalMode)
      }

      "must go from IndDateOfBirthPage to IndIdentityConfirmedPage if Nino" in {

        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, true)
          .success
          .value

        navigator.nextPage(IndDateOfBirthPage, NormalMode, userAnswers) mustBe IndIdentityConfirmedController.onPageLoad(NormalMode)
      }

      "must go from IndDateOfBirthPage to IndWhereDoYouLivePage if no Nino" in {

        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, false)
          .success
          .value

        navigator.nextPage(DateOfBirthWithoutIdPage, NormalMode, userAnswers) mustBe IndWhereDoYouLiveController.onPageLoad(NormalMode)
      }

      val organisationReporterTypes = TableDrivenPropertyChecks.Table(
        "orgReporterTypes",
        ReporterType.orgReporterTypes: _*
      )

      forAll(organisationReporterTypes) {
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

      forAll(Table("reporterType", ReporterType.values: _*)) {
        reporterType =>
          s"must go from DoYouHaveUniqueTaxPayerReferencePage to WhatIsYourUTRPage for $reporterType reporter having a UTR" in {
            val userAnswers = emptyUserAnswers
              .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
              .withPage(ReporterTypePage, reporterType)

            navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, userAnswers) mustBe controllers.organisation.routes.WhatIsYourUTRController
              .onPageLoad(NormalMode)
          }
      }

      forAll(Table("nonOrgReporterTypes", ReporterType.nonOrgReporterTypes: _*)) {
        reporterType =>
          s"must go from DoYouHaveUniqueTaxPayerReferencePage to IndDoYouHaveNINumberPage for $reporterType reporter with no UTR" in {

            val userAnswers = emptyUserAnswers
              .withPage(ReporterTypePage, reporterType)
              .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)

            navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, userAnswers) mustBe IndDoYouHaveNINumberController.onPageLoad(NormalMode)
          }
      }

      forAll(organisationReporterTypes) {
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

      forAll(organisationReporterTypes) {
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

      "must go from ContactHavePhonePage to HaveSecondContactPage when user answers no" in {

        val userAnswers = emptyUserAnswers.set(ContactHavePhonePage, false).success.value
        navigator.nextPage(ContactHavePhonePage, NormalMode, userAnswers) mustBe HaveSecondContactController.onPageLoad(NormalMode)
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

      "must go from IndDoYouHaveNINumberPage to IndWhatIsYourNamePage when user answers No" in {
        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, false)
          .success
          .value

        navigator
          .nextPage(IndDoYouHaveNINumberPage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndWhatIsYourNameController.onPageLoad(NormalMode))
      }

      "must go from IndDoYouHaveNINumberPage to IndWhatIsYourNINumberPage when user answers Yes" in {
        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, true)
          .success
          .value

        navigator
          .nextPage(IndDoYouHaveNINumberPage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndWhatIsYourNINumberController.onPageLoad(NormalMode))
      }

      "must go from IndWhatIsYourNamePage to IndDateOfBirthWithoutIdPage" in {
        navigator
          .nextPage(IndWhatIsYourNamePage, NormalMode, emptyUserAnswers)
          .mustBe(controllers.individual.routes.IndDateOfBirthWithoutIdController.onPageLoad(NormalMode))
      }

      "must go from DateOfBirthWithoutIdPage to IndWhereDoYouLivePage for user without Id" in {
        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, false)
          .success
          .value
          .set(DateOfBirthWithoutIdPage, LocalDate.now(Clock.systemUTC()))
          .success
          .value

        navigator
          .nextPage(DateOfBirthWithoutIdPage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndWhereDoYouLiveController.onPageLoad(NormalMode))
      }

      "must go from DateOfBirthWithoutIdPage to IndIdentityConfirmedPage for user with Id" in {
        val userAnswers = emptyUserAnswers
          .set(IndDoYouHaveNINumberPage, true)
          .success
          .value
          .set(DateOfBirthWithoutIdPage, LocalDate.now(Clock.systemUTC()))
          .success
          .value

        navigator
          .nextPage(DateOfBirthWithoutIdPage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(NormalMode))
      }

      "must go from DateOfBirthWithoutIdPage to JourneyRecoveryPage when NI Number answer not found" in {
        val userAnswers = emptyUserAnswers
          .set(DateOfBirthWithoutIdPage, LocalDate.now(Clock.systemUTC()))
          .success
          .value

        navigator
          .nextPage(DateOfBirthWithoutIdPage, NormalMode, userAnswers)
          .mustBe(routes.JourneyRecoveryController.onPageLoad())
      }

      "must go from IndWhereDoYouLivePage to IndWhatIsYourPostcodePage when user answers Yes" in {
        val userAnswers = emptyUserAnswers
          .set(IndWhereDoYouLivePage, true)
          .success
          .value

        navigator
          .nextPage(IndWhereDoYouLivePage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndWhatIsYourPostcodeController.onPageLoad(NormalMode))
      }

      "must go from IndWhereDoYouLivePage to IndNonUKAddressWithoutIdPage when user answers No" in {
        val userAnswers = emptyUserAnswers
          .set(IndWhereDoYouLivePage, false)
          .success
          .value

        navigator
          .nextPage(IndWhereDoYouLivePage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndNonUKAddressWithoutIdController.onPageLoad(NormalMode))
      }

      "must go from IndNonUKAddressWithoutIdPage to IndContactEmailPage" in {
        navigator
          .nextPage(IndNonUKAddressWithoutIdPage, NormalMode, emptyUserAnswers)
          .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode))
      }

      "must go from IndWhatIsYourPostcodePage to IndIsThisYourAddressPage when there is only one matching address" in {
        ScalaCheckPropertyChecks.forAll(arbitrary[models.AddressLookup]) {
          addressLookup =>
            val userAnswers = emptyUserAnswers
              .set(AddressLookupPage, Seq(addressLookup))
              .success
              .value

            navigator
              .nextPage(IndWhatIsYourPostcodePage, NormalMode, userAnswers)
              .mustBe(controllers.individual.routes.IndIsThisYourAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from IndWhatIsYourPostcodePage to IndSelectAddressPage when there is more than one matching address" in {
        ScalaCheckPropertyChecks.forAll(arbitrary[models.AddressLookup]) {
          addressLookup =>
            val userAnswers = emptyUserAnswers
              .set(AddressLookupPage, Seq(addressLookup, addressLookup))
              .success
              .value

            navigator
              .nextPage(IndWhatIsYourPostcodePage, NormalMode, userAnswers)
              .mustBe(controllers.individual.routes.IndSelectAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from IsThisYourAddressPage to IndContactEmailPage when user answers Yes" in {
        val userAnswers = emptyUserAnswers
          .set(IsThisYourAddressPage, true)
          .success
          .value

        navigator
          .nextPage(IsThisYourAddressPage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode))
      }

      "must go from IsThisYourAddressPage to IndUKAddressWithoutIdPage when user answers No" in {
        val userAnswers = emptyUserAnswers
          .set(IsThisYourAddressPage, false)
          .success
          .value

        navigator
          .nextPage(IsThisYourAddressPage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndUKAddressWithoutIdController.onPageLoad(NormalMode))
      }

      "must go from IndSelectAddressPage to IndContactEmailPage" in {
        navigator
          .nextPage(IndSelectAddressPage, NormalMode, emptyUserAnswers)
          .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode))
      }

      "must go from IndContactHavePhonePage to IndContactPhonePage when user answers Yes" in {
        val userAnswers = emptyUserAnswers
          .set(IndContactHavePhonePage, true)
          .success
          .value

        navigator
          .nextPage(IndContactHavePhonePage, NormalMode, userAnswers)
          .mustBe(controllers.individual.routes.IndContactPhoneController.onPageLoad(NormalMode))
      }

      "must go from IndContactHavePhonePage to CheckYourAnswersPage when user answers No" in {
        val userAnswers = emptyUserAnswers
          .set(IndContactHavePhonePage, false)
          .success
          .value

        navigator
          .nextPage(IndContactHavePhonePage, NormalMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "must go from IndWhereDoYouLivePage to IndWhatIsYourPostcodePage when user answers Yes" in {
        val userAnswers = emptyUserAnswers
          .set(IndWhereDoYouLivePage, true)
          .success
          .value

        navigator
          .nextPage(IndWhereDoYouLivePage, CheckMode, userAnswers)
          .mustBe(controllers.individual.routes.IndWhatIsYourPostcodeController.onPageLoad(CheckMode))
      }

      "must go from IndWhereDoYouLivePage to IndNonUKAddressWithoutIdPage when user answers No" in {
        val userAnswers = emptyUserAnswers
          .set(IndWhereDoYouLivePage, false)
          .success
          .value

        navigator
          .nextPage(IndWhereDoYouLivePage, CheckMode, userAnswers)
          .mustBe(controllers.individual.routes.IndNonUKAddressWithoutIdController.onPageLoad(CheckMode))
      }

      "must go from IndNonUKAddressWithoutIdPage to CheckYourAnswersPage" in {
        navigator
          .nextPage(IndNonUKAddressWithoutIdPage, CheckMode, emptyUserAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IndWhatIsYourPostcodePage to IsThisYourAddressPage when there is only one matching address" in {
        ScalaCheckPropertyChecks.forAll(arbitrary[models.AddressLookup]) {
          addressLookup =>
            val userAnswers = emptyUserAnswers
              .set(AddressLookupPage, Seq(addressLookup))
              .success
              .value

            navigator
              .nextPage(IndWhatIsYourPostcodePage, CheckMode, userAnswers)
              .mustBe(controllers.individual.routes.IndIsThisYourAddressController.onPageLoad(CheckMode))
        }
      }

      "must go from IndWhatIsYourPostcodePage to IndSelectAddressPage when there is more than one matching address" in {
        ScalaCheckPropertyChecks.forAll(arbitrary[models.AddressLookup]) {
          addressLookup =>
            val userAnswers = emptyUserAnswers
              .set(AddressLookupPage, Seq(addressLookup, addressLookup))
              .success
              .value

            navigator
              .nextPage(IndWhatIsYourPostcodePage, CheckMode, userAnswers)
              .mustBe(controllers.individual.routes.IndSelectAddressController.onPageLoad(CheckMode))
        }
      }

      "must go from IsThisYourAddressPage to CheckYourAnswersPage when user answers Yes" in {
        val userAnswers = emptyUserAnswers
          .set(IsThisYourAddressPage, true)
          .success
          .value

        navigator
          .nextPage(IsThisYourAddressPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IsThisYourAddressPage to IndUKAddressWithoutIdPage when user answers No" in {
        val userAnswers = emptyUserAnswers
          .set(IsThisYourAddressPage, false)
          .success
          .value

        navigator
          .nextPage(IsThisYourAddressPage, CheckMode, userAnswers)
          .mustBe(controllers.individual.routes.IndUKAddressWithoutIdController.onPageLoad(CheckMode))
      }

      "must go from IndUKAddressWithoutIdPage to CheckYourAnswersPage" in {
        navigator
          .nextPage(IndUKAddressWithoutIdPage, CheckMode, emptyUserAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from NonUKBusinessAddressWithoutIDPage to CheckYourAnswersPage" in {
        navigator
          .nextPage(NonUKBusinessAddressWithoutIDPage, CheckMode, emptyUserAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IndWhatIsYourNINumberPage to IndContactNamePage when IndDoYouHaveNINumberPage is true but no IndContactNamePage" in {
        navigator
          .nextPage(IndWhatIsYourNINumberPage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, true))
          .mustBe(controllers.individual.routes.IndContactNameController.onPageLoad(CheckMode))
      }

      "must go from IndWhatIsYourNINumberPage to CheckYourAnswersPage when IndDoYouHaveNINumberPage is true and IndContactNamePage exists" in {
        ScalaCheckPropertyChecks.forAll(arbitrary[Name]) {
          name =>
            val answers = emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, true).withPage(IndContactNamePage, name)
            navigator
              .nextPage(IndWhatIsYourNINumberPage, CheckMode, answers)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "must go from IndWhatIsYourNINumberPage to JourneyRecoveryPage when there is no IndDoYouHaveNINumberPage" in {
        navigator
          .nextPage(IndWhatIsYourNINumberPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

      "must go from IndWhatIsYourNINumberPage to JourneyRecoveryPage when IndDoYouHaveNINumberPage is false" in {
        navigator
          .nextPage(IndWhatIsYourNINumberPage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, false))
          .mustBe(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

      "must go from IndContactNamePage to IndDateOfBirthPage when IndDoYouHaveNINumberPage is true but no IndDateOfBirthPage" in {
        navigator
          .nextPage(IndContactNamePage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, true))
          .mustBe(controllers.individual.routes.IndDateOfBirthController.onPageLoad(CheckMode))
      }

      "must go from IndContactNamePage to IndDateOfBirthPage when CheckYourAnswersPage is true and IndDateOfBirthPage exists" in {
        val answers = emptyUserAnswers
          .withPage(IndDoYouHaveNINumberPage, true)
          .withPage(IndDateOfBirthPage, LocalDate.now())

        navigator
          .nextPage(IndContactNamePage, CheckMode, answers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IndContactNamePage to JourneyRecoveryPage when IndDoYouHaveNINumberPage is false" in {
        navigator
          .nextPage(IndContactNamePage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, false))
          .mustBe(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

      "must go from IndContactNamePage to JourneyRecoveryPage when IndDoYouHaveNINumberPage is missing" in {
        navigator
          .nextPage(IndContactNamePage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

      "must go from IndWhatIsYourNamePage to JourneyRecoveryPage when IndDoYouHaveNINumberPage is missing" in {
        navigator
          .nextPage(IndWhatIsYourNamePage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

      "must go from IndWhatIsYourNamePage to IndIdentityConfirmedPage when IndDoYouHaveNINumberPage is true" in {
        navigator
          .nextPage(IndWhatIsYourNamePage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, true))
          .mustBe(controllers.individual.routes.IndIdentityConfirmedController.onPageLoad(CheckMode))
      }

      "must go from IndWhatIsYourNamePage to IndDateOfBirthWithoutIdPage when IndDoYouHaveNINumberPage is false but no DateOfBirthWithoutIdPage" in {
        navigator
          .nextPage(IndWhatIsYourNamePage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, false))
          .mustBe(controllers.individual.routes.IndDateOfBirthWithoutIdController.onPageLoad(CheckMode))
      }

      "must go from IndWhatIsYourNamePage to CheckYourAnswersPage when IndDoYouHaveNINumberPage is false and DateOfBirthWithoutIdPage exists" in {
        val answers = emptyUserAnswers
          .withPage(IndDoYouHaveNINumberPage, false)
          .withPage(DateOfBirthWithoutIdPage, LocalDate.now())

        navigator
          .nextPage(IndWhatIsYourNamePage, CheckMode, answers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad())
      }
    }
  }

}
