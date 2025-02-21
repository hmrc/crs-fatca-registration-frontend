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
import models.ReporterType.{Individual, LimitedCompany, Sole}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.changeContactDetails._

import java.time.LocalDate

class CheckRouteNavigatorSpec extends SpecBase with TableDrivenPropertyChecks with Generators with UserAnswersGenerator {

  val navigator = new Navigator

  "Navigator" - {

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "must go from ReporterTypePage" - {
        val ua = emptyUserAnswers.withPage(ReporterTypePage, Individual)
        "to Check Your Answers if Individual is unchanged" in {
          val answers = ua.withPage(IndDoYouHaveNINumberPage, true)
          navigator.nextPage(ReporterTypePage, CheckMode, answers) mustBe routes.CheckYourAnswersController.onPageLoad
        }
        "to IndDoYouHaveNINumber if reportYpe changed to Individual" in {
          navigator.nextPage(ReporterTypePage, CheckMode, ua) mustBe controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(CheckMode)
        }
      }

      "must go from WhatIsYourUTRPage to WhatIsYourNamePage for a Sole Trader" in {
        val answers = emptyUserAnswers.withPage(ReporterTypePage, Sole)

        navigator
          .nextPage(WhatIsYourUTRPage, CheckMode, answers)
          .mustBe(controllers.organisation.routes.WhatIsYourNameController.onPageLoad(CheckMode))
      }

      "must go from WhatIsYourUTRPage to BusinessNamePage for any other reporter type" in {
        val answers = emptyUserAnswers.withPage(ReporterTypePage, LimitedCompany)

        navigator
          .nextPage(WhatIsYourUTRPage, CheckMode, answers)
          .mustBe(controllers.organisation.routes.BusinessNameController.onPageLoad(CheckMode))
      }

      "must go from WhatIsYourNamePage to IsThisYourBusinessPage" in {
        navigator
          .nextPage(WhatIsYourNamePage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.organisation.routes.IsThisYourBusinessController.onPageLoad(CheckMode))
      }

      "must go from DoYouHaveUniqueTaxPayerReferencePage to" - {
        "WhatIsYourUtrPage when user says Yes" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, LimitedCompany)
            .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)

          navigator
            .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, userAnswers)
            .mustBe(controllers.organisation.routes.WhatIsYourUTRController.onPageLoad(CheckMode))
        }

        "BusinessNameWithoutIDPage when org user says No" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, LimitedCompany)
            .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)

          navigator
            .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, userAnswers)
            .mustBe(controllers.organisation.routes.BusinessNameWithoutIDController.onPageLoad(CheckMode))
        }

        "IndDoYouHaveNINumberPage when sole trader says No" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, Sole)
            .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)

          navigator
            .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, userAnswers)
            .mustBe(controllers.individual.routes.IndDoYouHaveNINumberController.onPageLoad(CheckMode))
        }
      }

      "must go from BusinessNameWithoutIDPage to HaveTradingNamePage" in {
        navigator
          .nextPage(BusinessNameWithoutIDPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.organisation.routes.HaveTradingNameController.onPageLoad(CheckMode))
      }

      "must go from HaveTradingNamePage to BusinessTradingNameWithoutID when user says Yes" in {
        val userAnswers = emptyUserAnswers.withPage(HaveTradingNamePage, true)
        navigator
          .nextPage(HaveTradingNamePage, CheckMode, userAnswers)
          .mustBe(controllers.organisation.routes.BusinessTradingNameWithoutIDController.onPageLoad(CheckMode))
      }

      "must go from HaveTradingNamePage to NonUKBusinessAddressWithoutIDPage when user says No and NonUKBusinessAddressWithoutIDPage is not populated" in {
        val userAnswers = emptyUserAnswers.withPage(HaveTradingNamePage, false)
        navigator
          .nextPage(HaveTradingNamePage, CheckMode, userAnswers)
          .mustBe(controllers.organisation.routes.NonUKBusinessAddressWithoutIDController.onPageLoad(CheckMode))
      }
      "must go from HaveTradingNamePage to CheckYourAnswersController when user says No and NonUKBusinessAddressWithoutIDPage is populated" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveTradingNamePage, false)
          .withPage(NonUKBusinessAddressWithoutIDPage, arbitrary[Address].sample.value)
        navigator
          .nextPage(HaveTradingNamePage, CheckMode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from BusinessTradingNameWithoutIDPage to CheckYourAnswersPage if there is an address" in {
        val userAnswers = emptyUserAnswers
          .set(NonUKBusinessAddressWithoutIDPage, arbitrary[Address].sample.value)
          .success
          .value

        navigator
          .nextPage(BusinessTradingNameWithoutIDPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from BusinessTradingNameWithoutIDPage to NonUKBusinessAddressWithoutIDPage if there is no address" in {
        navigator
          .nextPage(BusinessTradingNameWithoutIDPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.organisation.routes.NonUKBusinessAddressWithoutIDController.onPageLoad(CheckMode))
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

      "must go from IndNonUKAddressWithoutIdPage to CheckYourAnswersPage if there is a contact email" in {
        val userAnswers = emptyUserAnswers
          .set(IndContactEmailPage, arbitrary[String].sample.value)
          .success
          .value

        navigator
          .nextPage(IndNonUKAddressWithoutIdPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IndNonUKAddressWithoutIdPage to IndContactEmailPage if there is no contact email" in {
        navigator
          .nextPage(IndNonUKAddressWithoutIdPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)) 
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

      "must go from IndSelectAddressPage to CheckYourAnswersPage when there is a contact email" in {
        val userAnswers = emptyUserAnswers
          .withPage(IndContactEmailPage, arbitrary[String].sample.value)

        navigator
          .nextPage(IndSelectAddressPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IndSelectAddressPage to IndContactEmailPage when there is no contact email" in {
        navigator
          .nextPage(IndSelectAddressPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)) 
      }

      "must go from IsThisYourAddressPage" - {

        "to IndUKAddressWithoutIdPage when user answers No" in {
          val userAnswers = emptyUserAnswers
            .set(IsThisYourAddressPage, false)
            .success
            .value

          navigator
            .nextPage(IsThisYourAddressPage, CheckMode, userAnswers)
            .mustBe(controllers.individual.routes.IndUKAddressWithoutIdController.onPageLoad(CheckMode))
        }

        "to CheckYourAnswersPage when user answers Yes and there is a contact email" in {
          val userAnswers = emptyUserAnswers
            .withPage(IndContactEmailPage, arbitrary[String].sample.value)
            .withPage(IsThisYourAddressPage, true)

          navigator
            .nextPage(IsThisYourAddressPage, CheckMode, userAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }

        "to CheckYourAnswersPage when user answers Yes and there is no contact email" in {
          val userAnswers = emptyUserAnswers
            .withPage(IsThisYourAddressPage, true)

          navigator
            .nextPage(IsThisYourAddressPage, CheckMode, userAnswers)
            .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)) 
        }

      }

      "must go from IndUKAddressWithoutIdPage to CheckYourAnswersPage if there is a contact email" in {
        val userAnswers = emptyUserAnswers
          .set(IndContactEmailPage, arbitrary[String].sample.value)
          .success
          .value

        navigator
          .nextPage(IndUKAddressWithoutIdPage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IndUKAddressWithoutIdPage to IndContactEmailPage if there is no contact email" in {
        navigator
          .nextPage(IndUKAddressWithoutIdPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)) 
      }

      "must go from NonUKBusinessAddressWithoutIDPage" - {

        "to JourneyRecoveryPage if there is no reporter type" in {
          navigator
            .nextPage(NonUKBusinessAddressWithoutIDPage, CheckMode, emptyUserAnswers)
            .mustBe(routes.JourneyRecoveryController.onPageLoad())
        }

        "to CheckYourAnswersPage if there is a contact email for a Sole reporter type" in {
          val answers = emptyUserAnswers
            .withPage(ReporterTypePage, Sole)
            .withPage(IndContactEmailPage, arbitrary[String].sample.value)

          navigator
            .nextPage(NonUKBusinessAddressWithoutIDPage, CheckMode, answers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }

        "to IndContactEmailPage if there is no contact email for a Sole reporter type" in {
          val answers = emptyUserAnswers
            .withPage(ReporterTypePage, Sole)

          navigator
            .nextPage(NonUKBusinessAddressWithoutIDPage, CheckMode, answers)
            .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)) 
        }

        "to CheckYourAnswersPage if there is a contact name for any other reporter type" in {
          val answers = emptyUserAnswers
            .withPage(ReporterTypePage, LimitedCompany)
            .withPage(ContactNamePage, arbitrary[String].sample.value)

          navigator
            .nextPage(NonUKBusinessAddressWithoutIDPage, CheckMode, answers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }

        "to YourContactDetailsPage if there is no contact name for any other reporter type" in {
          val answers = emptyUserAnswers
            .withPage(ReporterTypePage, LimitedCompany)

          navigator
            .nextPage(NonUKBusinessAddressWithoutIDPage, CheckMode, answers)
            .mustBe(routes.YourContactDetailsController.onPageLoad())
        }

      }

      "must go from IndContactHavePhonePage to IndContactPhonePage when user answers Yes" in {
        val userAnswers = emptyUserAnswers
          .set(IndContactHavePhonePage, true)
          .success
          .value

        navigator
          .nextPage(IndContactHavePhonePage, CheckMode, userAnswers)
          .mustBe(controllers.individual.routes.IndContactPhoneController.onPageLoad(CheckMode))
      }

      "must go from IndContactHavePhonePage to CheckYourAnswersPage when user answers No" in {
        val userAnswers = emptyUserAnswers.withPage(IndContactHavePhonePage, false)

        navigator
          .nextPage(IndContactHavePhonePage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from ContactHavePhonePage to ContactPhonePage when user answers Yes" in {
        val userAnswers = emptyUserAnswers.withPage(ContactHavePhonePage, true)

        navigator
          .nextPage(ContactHavePhonePage, CheckMode, userAnswers)
          .mustBe(controllers.organisation.routes.ContactPhoneController.onPageLoad(CheckMode))
      }

      "must go from ContactHavePhonePage to CheckYourAnswersPage when user answers No" in {
        val userAnswers = emptyUserAnswers.withPage(ContactHavePhonePage, false)

        navigator
          .nextPage(ContactHavePhonePage, CheckMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from ContactPhonePage to CheckYourAnswersPage" in {
        navigator
          .nextPage(ContactPhonePage, CheckMode, emptyUserAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from HaveSecondContactPage to SecondContactNamePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.withPage(HaveSecondContactPage, true)
        navigator.nextPage(HaveSecondContactPage, CheckMode, userAnswers) mustBe SecondContactNameController.onPageLoad(CheckMode)
      }

      "must go from HaveSecondContactPage to CheckYourAnswersPage when user answers yes and contact name is already found" in {

        val userAnswers = emptyUserAnswers.withPage(SecondContactNamePage, "Test").withPage(HaveSecondContactPage, true)
        navigator.nextPage(HaveSecondContactPage, CheckMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from HaveSecondContactPage to CheckYourAnswersPage when user answers no" in {

        val userAnswers = emptyUserAnswers.withPage(HaveSecondContactPage, false)
        navigator.nextPage(HaveSecondContactPage, CheckMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from SecondContactNamePage to SecondContactEmailPage" in {

        val userAnswers = emptyUserAnswers.withPage(SecondContactNamePage, "value")
        navigator.nextPage(SecondContactNamePage, CheckMode, userAnswers) mustBe SecondContactEmailController.onPageLoad(CheckMode)
      }

      "must go from SecondContactEmailPage to SecondContactHavePhonePage" in {

        navigator.nextPage(SecondContactEmailPage, CheckMode, emptyUserAnswers) mustBe SecondContactHavePhoneController.onPageLoad(CheckMode)
      }

      "must go from SecondContactHavePhonePage to SecondContactPhonePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.withPage(SecondContactHavePhonePage, true)
        navigator.nextPage(SecondContactHavePhonePage, CheckMode, userAnswers) mustBe SecondContactPhoneController.onPageLoad(CheckMode)
      }

      "must go from SecondContactHavePhonePage to CheckYourAnswersPage when user answers no" in {

        val userAnswers = emptyUserAnswers.withPage(SecondContactHavePhonePage, false)
        navigator.nextPage(SecondContactHavePhonePage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from SecondContactPhonePage to CheckYourAnswersPage" in {
        navigator
          .nextPage(SecondContactPhonePage, CheckMode, emptyUserAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from IndWhatIsYourNINumberPage to IndContactNamePage when IndDoYouHaveNINumberPage is true" in {
        navigator
          .nextPage(IndWhatIsYourNINumberPage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, true))
          .mustBe(controllers.individual.routes.IndContactNameController.onPageLoad(CheckMode))
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

      "must go from IndContactNamePage to IndDateOfBirthPage when IndDoYouHaveNINumberPage is true" in {
        navigator
          .nextPage(IndContactNamePage, CheckMode, emptyUserAnswers.withPage(IndDoYouHaveNINumberPage, true))
          .mustBe(controllers.individual.routes.IndDateOfBirthController.onPageLoad(CheckMode))
      }

      "must go from IndDateOfBirthPage to IndIdentityConfirmedPage if Nino" in {

        val userAnswers = emptyUserAnswers
          .withPage(IndDoYouHaveNINumberPage, true)

        navigator.nextPage(IndDateOfBirthPage, NormalMode, userAnswers) mustBe IndIdentityConfirmedController.onPageLoad(NormalMode) 
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

      "must go from BusinessNamePage to IsThisYourBusinessController" in {
        navigator
          .nextPage(BusinessNamePage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.organisation.routes.IsThisYourBusinessController.onPageLoad(CheckMode))
      }

      "must go from IsThisYourBusinessPage" - {

        "to IndContactEmailPage when Yes is selected for a Sole Trader" in {
          val answers = emptyUserAnswers
            .set(ReporterTypePage, Sole).success.value
            .set(IsThisYourBusinessPage, true).success.value

          navigator
            .nextPage(IsThisYourBusinessPage, CheckMode, answers)
            .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)) 
        }

        "to SoleTraderNotIdentifiedPage when No is selected for a Sole Trader" in {
          val answers = emptyUserAnswers
            .set(ReporterTypePage, Sole).success.value
            .set(IsThisYourBusinessPage, false).success.value

          navigator
            .nextPage(IsThisYourBusinessPage, CheckMode, answers)
            .mustBe(controllers.routes.SoleTraderNotIdentifiedController.onPageLoad)
        }

        "to DifferentBusinessPage when No is selected for a auto-matched Sole Trader" in {
          val answers = emptyUserAnswers
            .set(AutoMatchedUTRPage, utr).success.value
            .set(ReporterTypePage, Sole).success.value
            .set(IsThisYourBusinessPage, false).success.value

          navigator
            .nextPage(IsThisYourBusinessPage, CheckMode, answers)
            .mustBe(controllers.organisation.routes.DifferentBusinessController.onPageLoad())
        }

        val organisationReporterTypes = TableDrivenPropertyChecks.Table(
          "orgReporterTypes",
          ReporterType.orgReporterTypes: _*
        )

        forAll(organisationReporterTypes) {
          reporterType =>
            s"to YourContactDetailsPage when Yes is selected for a $reporterType and there is no contact name" in {
              val answers = emptyUserAnswers
                .set(ReporterTypePage, reporterType).success.value
                .set(IsThisYourBusinessPage, true).success.value

              navigator
                .nextPage(IsThisYourBusinessPage, CheckMode, answers)
                .mustBe(routes.YourContactDetailsController.onPageLoad())
            }

            s"to CheckYourAnswersPage when Yes is selected for a $reporterType and there is a contact name" in {
              val answers = emptyUserAnswers
                .set(ReporterTypePage, reporterType).success.value
                .set(IsThisYourBusinessPage, true).success.value
                .set(ContactNamePage, arbitrary[String].sample.value).success.value

              navigator
                .nextPage(IsThisYourBusinessPage, CheckMode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
            }

            s"to BusinessNotIdentifiedPage when No is selected for a $reporterType" in {
              val answers = emptyUserAnswers
                .set(ReporterTypePage, reporterType).success.value
                .set(IsThisYourBusinessPage, false).success.value

              navigator
                .nextPage(IsThisYourBusinessPage, CheckMode, answers)
                .mustBe(controllers.organisation.routes.BusinessNotIdentifiedController.onPageLoad())
            }

            s"to DifferentBusinessPage when No is selected for a auto-matched $reporterType" in {
              val answers = emptyUserAnswers
                .set(AutoMatchedUTRPage, utr).success.value
                .set(ReporterTypePage, reporterType).success.value
                .set(IsThisYourBusinessPage, false).success.value

              navigator
                .nextPage(IsThisYourBusinessPage, CheckMode, answers)
                .mustBe(controllers.organisation.routes.DifferentBusinessController.onPageLoad())
            }
        }

        "to YourContactDetailsPage when there is no ReporterType and Yes is selected" in {
          val answers = emptyUserAnswers
            .set(IsThisYourBusinessPage, true).success.value

          navigator
            .nextPage(IsThisYourBusinessPage, CheckMode, answers)
            .mustBe(routes.YourContactDetailsController.onPageLoad())
        }

      }

      "must go from RegistrationInfoPage to CheckYourAnswersPage if there is a contact email" in {
        val answers = emptyUserAnswers.withPage(IndContactEmailPage, arbitrary[String].sample.value)
        navigator
          .nextPage(RegistrationInfoPage, CheckMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from RegistrationInfoPage to IndContactEmailPage if there is no contact email" in {
        navigator
          .nextPage(RegistrationInfoPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.individual.routes.IndContactEmailController.onPageLoad(NormalMode)) 

      }

      "must go from OrganisationContactNamePage to OrganisationContactEmailPage" in {
        navigator
          .nextPage(OrganisationContactNamePage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.changeContactDetails.routes.OrganisationContactEmailController.onPageLoad(CheckMode))
      }

      "must go from OrganisationContactEmailPage to OrganisationContactHavePhonePage" in {
        navigator
          .nextPage(OrganisationContactEmailPage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.changeContactDetails.routes.OrganisationContactHavePhoneController.onPageLoad(CheckMode))
      }

      "must go from OrganisationContactHavePhonePage to ChangeOrganisationContactDetailsPage when user answers no" in {
        navigator
          .nextPage(OrganisationContactHavePhonePage, CheckMode, emptyUserAnswers.withPage(OrganisationContactHavePhonePage, false))
          .mustBe(controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad())
      }

      "must go from OrganisationContactHavePhonePage to OrganisationContactPhonePage when user answers yes" in {
        navigator
          .nextPage(OrganisationContactHavePhonePage, CheckMode, emptyUserAnswers.withPage(OrganisationContactHavePhonePage, true))
          .mustBe(controllers.changeContactDetails.routes.OrganisationContactPhoneController.onPageLoad(CheckMode))
      }

      "must go from OrganisationContactPhonePage to ChangeOrganisationContactDetailsPage" in {
        navigator
          .nextPage(OrganisationContactPhonePage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad())
      }

      "must go from OrganisationHaveSecondContactPage to ChangeOrganisationContactDetailsPage when user answers no" in {
        navigator
          .nextPage(OrganisationHaveSecondContactPage, CheckMode, emptyUserAnswers.withPage(OrganisationHaveSecondContactPage, false))
          .mustBe(controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad())
      }

      "must go from OrganisationHaveSecondContactPage to OrganisationSecondContactNamePage when user answers yes" in {
        navigator
          .nextPage(OrganisationHaveSecondContactPage, CheckMode, emptyUserAnswers.withPage(OrganisationHaveSecondContactPage, true))
          .mustBe(controllers.changeContactDetails.routes.OrganisationSecondContactNameController.onPageLoad(CheckMode))
      }

      "must go from OrganisationSecondContactNamePage to OrganisationSecondContactEmailPage" in {
        navigator
          .nextPage(OrganisationSecondContactNamePage, CheckMode, emptyUserAnswers.withPage(OrganisationHaveSecondContactPage, true))
          .mustBe(controllers.changeContactDetails.routes.OrganisationSecondContactEmailController.onPageLoad(CheckMode))
      }

      "must go from OrganisationSecondContactEmailPage to OrganisationSecondContactHavePhonePage" in {
        navigator
          .nextPage(OrganisationSecondContactEmailPage, CheckMode, emptyUserAnswers.withPage(OrganisationHaveSecondContactPage, true))
          .mustBe(controllers.changeContactDetails.routes.OrganisationSecondContactHavePhoneController.onPageLoad(CheckMode))
      }

      "must go from OrganisationSecondContactHavePhonePage to ChangeOrganisationContactDetailsPage when user answers no" in {
        navigator
          .nextPage(OrganisationSecondContactHavePhonePage, CheckMode, emptyUserAnswers.withPage(OrganisationSecondContactHavePhonePage, false))
          .mustBe(controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad())
      }

      "must go from OrganisationSecondContactHavePhonePage to OrganisationSecondContactPhonePage when user answers yes" in {
        navigator
          .nextPage(OrganisationSecondContactHavePhonePage, CheckMode, emptyUserAnswers.withPage(OrganisationSecondContactHavePhonePage, true))
          .mustBe(controllers.changeContactDetails.routes.OrganisationSecondContactPhoneController.onPageLoad(CheckMode))
      }

      "must go from OrganisationSecondContactPhonePage to ChangeOrganisationContactDetailsPage" in {
        navigator
          .nextPage(OrganisationSecondContactPhonePage, CheckMode, emptyUserAnswers)
          .mustBe(controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad())
      }

      "while changing Individual contact's details" - {
        "must go from IndividualEmailPage to IndividualHavePhonePage" in {
          navigator
            .nextPage(IndividualEmailPage, CheckMode, emptyUserAnswers)
            .mustBe(controllers.changeContactDetails.routes.IndividualHavePhoneController.onPageLoad(CheckMode))
        }

        "must go from IndividualHavePhonePage to ChangeIndividualContactDetailsPage when user answers no" in {
          navigator
            .nextPage(IndividualHavePhonePage, CheckMode, emptyUserAnswers.withPage(IndividualHavePhonePage, false))
            .mustBe(controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad())
        }

        "must go from IndividualHavePhonePage to IndividualPhonePage when user answers yes" in {
          navigator
            .nextPage(IndividualHavePhonePage, CheckMode, emptyUserAnswers.withPage(IndividualHavePhonePage, true))
            .mustBe(controllers.changeContactDetails.routes.IndividualPhoneController.onPageLoad(CheckMode))
        }

        "must go from IndividualPhonePage to ChangeIndividualContactDetailsPage" in {
          navigator
            .nextPage(IndividualPhonePage, CheckMode, emptyUserAnswers)
            .mustBe(controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad())
        }
      }

    }
  }

}
