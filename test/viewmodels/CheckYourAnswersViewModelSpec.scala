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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import utils.CountryListFactory
import viewmodels.checkAnswers.CheckYourAnswersViewModel

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase with GuiceOneAppPerSuite {

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  def messagesApi: MessagesApi                         = app.injector.instanceOf[MessagesApi]
  implicit def messages: Messages                      = messagesApi.preferred(fakeRequest)
  val countryListFactory: CountryListFactory           = app.injector.instanceOf[CountryListFactory]
  val orgRegistrationInfo: OrgRegistrationInfo         = OrgRegistrationInfo(safeId, OrgName, AddressResponse("line1", None, None, None, None, "GB"))

  "CheckYourAnswersViewModel" - {

    "must return required rows for 'business-with-id' flow" in {
      val userAnswers = emptyUserAnswers
        .set(ReporterTypePage, ReporterType.LimitedCompany)
        .success
        .value
        .set(RegisteredAddressInUKPage, true)
        .success
        .value
        .set(WhatIsYourUTRPage, utr)
        .success
        .value
        .set(BusinessNamePage, OrgName)
        .success
        .value
        .set(RegistrationInfoPage, orgRegistrationInfo)
        .success
        .value
        .set(IsThisYourBusinessPage, true)
        .success
        .value
        .set(ContactNamePage, name.fullName)
        .success
        .value
        .set(ContactEmailPage, TestEmail)
        .success
        .value
        .set(ContactHavePhonePage, false)
        .success
        .value
        .set(HaveSecondContactPage, true)
        .success
        .value
        .set(SecondContactNamePage, TestPhoneNumber)
        .success
        .value
        .set(SecondContactEmailPage, TestEmail)
        .success
        .value
        .set(SecondContactHavePhonePage, true)
        .success
        .value
        .set(SecondContactPhonePage, TestMobilePhoneNumber)
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = true)

      result.size mustBe 3
      result.head.rows.size mustBe 1
      result.head.sectionName mustBe "Business details"

      result(1).sectionName mustBe "First contact"
      result(1).rows.size mustBe 3

      result(2).sectionName mustBe "Second contact"
      result(2).rows.size mustBe 4
    }

    "must return required rows for 'business-without-id' flow" in {
      val businessAddress = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = emptyUserAnswers
        .set(ReporterTypePage, ReporterType.LimitedCompany)
        .success
        .value
        .set(RegisteredAddressInUKPage, false)
        .success
        .value
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(BusinessNameWithoutIDPage, OrgName)
        .success
        .value
        .set(HaveTradingNamePage, true)
        .success
        .value
        .set(BusinessTradingNameWithoutIDPage, OrgName)
        .success
        .value
        .set(NonUKBusinessAddressWithoutIDPage, businessAddress)
        .success
        .value
        .set(ContactNamePage, name.fullName)
        .success
        .value
        .set(ContactEmailPage, TestEmail)
        .success
        .value
        .set(ContactHavePhonePage, false)
        .success
        .value
        .set(HaveSecondContactPage, false)
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = true)

      result.size mustBe 3
      result.head.rows.size mustBe 6
      result.head.sectionName mustBe "Business details"

      result(1).sectionName mustBe "First contact"
      result(1).rows.size mustBe 3

      result(2).sectionName mustBe "Second contact"
      result(2).rows.size mustBe 1
    }

    "must return required rows for 'individual-with-id' flow" in {
      val userAnswers = emptyUserAnswers
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(IndDoYouHaveNINumberPage, true)
        .success
        .value
        .set(IndWhatIsYourNINumberPage, Nino(TestNiNumber))
        .success
        .value
        .set(WhatIsYourNamePage, name)
        .success
        .value
        .set(IndDateOfBirthPage, LocalDate.now())
        .success
        .value
        .set(IndContactEmailPage, TestEmail)
        .success
        .value
        .set(IndContactHavePhonePage, false)
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = false)

      result.size mustBe 2

      result.head.sectionName mustBe "Your details"
      result.head.rows.size mustBe 5

      result(1).sectionName mustBe "Contact details"
      result(1).rows.size mustBe 2

    }

    "must return required rows for 'individual-without-id' flow" in {
      val address = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = emptyUserAnswers
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(IndDoYouHaveNINumberPage, false)
        .success
        .value
        .set(IndWhatIsYourNamePage, name)
        .success
        .value
        .set(IndDateOfBirthPage, LocalDate.now())
        .success
        .value
        .set(IndWhereDoYouLivePage, false)
        .success
        .value
        .set(IndNonUKAddressWithoutIdPage, address)
        .success
        .value
        .set(IndContactEmailPage, TestEmail)
        .success
        .value
        .set(IndContactHavePhonePage, false)
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = false)

      result.size mustBe 2

      result.head.sectionName mustBe "Your details"
      result.head.rows.size mustBe 5

      result(1).sectionName mustBe "Contact details"
      result(1).rows.size mustBe 2
    }
  }

}
