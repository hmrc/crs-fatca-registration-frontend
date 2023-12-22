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

package viewmodels

import base.SpecBase
import helpers.JsonFixtures._
import models.matching.OrgRegistrationInfo
import models.register.response.details.AddressResponse
import models.{Address, Country, ReporterType}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.domain.Nino
import utils.CountryListFactory
import viewmodels.checkAnswers.CheckYourAnswersViewModel

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase with GuiceOneAppPerSuite with TableDrivenPropertyChecks {

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  def messagesApi: MessagesApi                         = app.injector.instanceOf[MessagesApi]
  implicit def messages: Messages                      = messagesApi.preferred(fakeRequest)
  val countryListFactory: CountryListFactory           = app.injector.instanceOf[CountryListFactory]
  val orgRegistrationInfo: OrgRegistrationInfo         = OrgRegistrationInfo(safeId, OrgName, AddressResponse("line1", None, None, None, None, "GB"))

  "CheckYourAnswersViewModel" - {

    forAll(Table("nonIndividualAffinityGroup", Seq(AffinityGroup.Organisation, AffinityGroup.Agent): _*)) {
      affinityGroup =>
        s"must return required rows for 'business-with-id' flow with $affinityGroup affinity group" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, ReporterType.LimitedCompany)
            .withPage(RegisteredAddressInUKPage, true)
            .withPage(WhatIsYourUTRPage, utr)
            .withPage(BusinessNamePage, OrgName)
            .withPage(RegistrationInfoPage, orgRegistrationInfo)
            .withPage(IsThisYourBusinessPage, true)
            .withPage(ContactNamePage, name.fullName)
            .withPage(ContactEmailPage, TestEmail)
            .withPage(ContactHavePhonePage, false)
            .withPage(HaveSecondContactPage, true)
            .withPage(SecondContactNamePage, TestPhoneNumber)
            .withPage(SecondContactEmailPage, TestEmail)
            .withPage(SecondContactHavePhonePage, true)
            .withPage(SecondContactPhonePage, TestMobilePhoneNumber)

          val result: Seq[Section] = CheckYourAnswersViewModel
            .buildPages(userAnswers, countryListFactory, affinityGroup)

          result.size mustBe 3
          result.head.rows.size mustBe 1
          result.head.sectionName mustBe "Business details"

          result(1).sectionName mustBe "First contact"
          result(1).rows.size mustBe 3

          result(2).sectionName mustBe "Second contact"
          result(2).rows.size mustBe 4
        }

        s"must return required rows for 'business-without-id' flow with $affinityGroup affinity group" in {
          val businessAddress = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
          val userAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, ReporterType.LimitedCompany)
            .withPage(RegisteredAddressInUKPage, false)
            .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
            .withPage(BusinessNameWithoutIDPage, OrgName)
            .withPage(HaveTradingNamePage, true)
            .withPage(BusinessTradingNameWithoutIDPage, OrgName)
            .withPage(NonUKBusinessAddressWithoutIDPage, businessAddress)
            .withPage(ContactNamePage, name.fullName)
            .withPage(ContactEmailPage, TestEmail)
            .withPage(ContactHavePhonePage, false)
            .withPage(HaveSecondContactPage, false)

          val result: Seq[Section] = CheckYourAnswersViewModel
            .buildPages(userAnswers, countryListFactory, affinityGroup)

          result.size mustBe 3
          result.head.rows.size mustBe 6
          result.head.sectionName mustBe "Business details"

          result(1).sectionName mustBe "First contact"
          result(1).rows.size mustBe 3

          result(2).sectionName mustBe "Second contact"
          result(2).rows.size mustBe 1
        }
    }

    "must return required rows without second contact for 'business-with-id' flow with individual affinity group" in {
      val userAnswers = emptyUserAnswers
        .withPage(ReporterTypePage, ReporterType.LimitedCompany)
        .withPage(RegisteredAddressInUKPage, true)
        .withPage(WhatIsYourUTRPage, utr)
        .withPage(BusinessNamePage, OrgName)
        .withPage(RegistrationInfoPage, orgRegistrationInfo)
        .withPage(IsThisYourBusinessPage, true)
        .withPage(ContactNamePage, name.fullName)
        .withPage(ContactEmailPage, TestEmail)
        .withPage(ContactHavePhonePage, false)
        .withPage(HaveSecondContactPage, true)
        .withPage(SecondContactNamePage, TestPhoneNumber)
        .withPage(SecondContactEmailPage, TestEmail)
        .withPage(SecondContactHavePhonePage, true)
        .withPage(SecondContactPhonePage, TestMobilePhoneNumber)

      val result: Seq[Section] = CheckYourAnswersViewModel
        .buildPages(userAnswers, countryListFactory, AffinityGroup.Individual)

      result.size mustBe 2
      result.head.rows.size mustBe 1
      result.head.sectionName mustBe "Business details"

      result(1).sectionName mustBe "First contact"
      result(1).rows.size mustBe 3
    }

    "must return required rows without second contact for 'business-without-id' flow with Individual affinity group" in {
      val businessAddress = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = emptyUserAnswers
        .withPage(ReporterTypePage, ReporterType.LimitedCompany)
        .withPage(RegisteredAddressInUKPage, false)
        .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
        .withPage(BusinessNameWithoutIDPage, OrgName)
        .withPage(HaveTradingNamePage, true)
        .withPage(BusinessTradingNameWithoutIDPage, OrgName)
        .withPage(NonUKBusinessAddressWithoutIDPage, businessAddress)
        .withPage(ContactNamePage, name.fullName)
        .withPage(ContactEmailPage, TestEmail)
        .withPage(ContactHavePhonePage, false)
        .withPage(HaveSecondContactPage, false)

      val result: Seq[Section] = CheckYourAnswersViewModel
        .buildPages(userAnswers, countryListFactory, AffinityGroup.Individual)

      result.size mustBe 2
      result.head.rows.size mustBe 6
      result.head.sectionName mustBe "Business details"

      result(1).sectionName mustBe "First contact"
      result(1).rows.size mustBe 3
    }

    "must return required rows for 'individual-with-id' flow" in {
      val userAnswers = emptyUserAnswers
        .withPage(ReporterTypePage, ReporterType.Individual)
        .withPage(IndDoYouHaveNINumberPage, true)
        .withPage(IndWhatIsYourNINumberPage, Nino(TestNiNumber))
        .withPage(WhatIsYourNamePage, name)
        .withPage(IndDateOfBirthPage, LocalDate.now())
        .withPage(IndContactEmailPage, TestEmail)
        .withPage(IndContactHavePhonePage, false)

      val result: Seq[Section] = CheckYourAnswersViewModel
        .buildPages(userAnswers, countryListFactory, AffinityGroup.Individual)

      result.size mustBe 2

      result.head.sectionName mustBe "Your details"
      result.head.rows.size mustBe 5

      result(1).sectionName mustBe "Contact details"
      result(1).rows.size mustBe 2
    }

    "must return required rows for 'individual-without-id' flow" in {
      val address = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = emptyUserAnswers
        .withPage(ReporterTypePage, ReporterType.Individual)
        .withPage(IndDoYouHaveNINumberPage, false)
        .withPage(IndWhatIsYourNamePage, name)
        .withPage(IndDateOfBirthPage, LocalDate.now())
        .withPage(IndWhereDoYouLivePage, false)
        .withPage(IndNonUKAddressWithoutIdPage, address)
        .withPage(IndContactEmailPage, TestEmail)
        .withPage(IndContactHavePhonePage, false)

      val result: Seq[Section] = CheckYourAnswersViewModel
        .buildPages(userAnswers, countryListFactory, AffinityGroup.Individual)

      result.size mustBe 2

      result.head.sectionName mustBe "Your details"
      result.head.rows.size mustBe 5

      result(1).sectionName mustBe "Contact details"
      result(1).rows.size mustBe 2
    }
  }

}
