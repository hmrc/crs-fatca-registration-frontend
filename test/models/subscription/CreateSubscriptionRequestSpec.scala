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

package models.subscription

import base.SpecBase
import generators.ModelGenerators
import helpers.JsonFixtures._
import models.subscription.request.{ContactInformation, CreateSubscriptionRequest, IndividualDetails, OrganisationDetails}
import models.{Address, AddressLookup, Country, IdentifierType, ReporterType, UniqueTaxpayerReference, UserAnswers}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.auth.core.AffinityGroup

class CreateSubscriptionRequestSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val nameGen: Gen[String] = stringsLongerThan(10)

  "buildSubscriptionRequest" - {

    "returns a CreateSubscriptionRequest" - {

      "for a business with id (org affinity group)" in {
        val businessName = nameGen.sample.value
        val contactName  = nameGen.sample.value

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
          .set(RegisteredAddressInUKPage, true).success.value
          .set(WhatIsYourUTRPage, UniqueTaxpayerReference(validUtr.sample.value)).success.value
          .set(BusinessNamePage, businessName).success.value
          .set(ContactNamePage, contactName).success.value
          .set(ContactEmailPage, TestEmail).success.value
          .set(ContactHavePhonePage, true).success.value
          .set(ContactPhonePage, TestPhoneNumber).success.value
          .set(HaveSecondContactPage, false).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Organisation)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = None,
            gbUser = true,
            primaryContact = ContactInformation(
              contactInformation = OrganisationDetails(
                name = contactName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = None
          )
        )

      }

      "for a business without id (org affinity group)" in {
        val businessName         = nameGen.sample.value
        val tradingName          = nameGen.sample.value
        val address              = Address("", None, "", None, None, Country("state", "GG", "Guernsey"))
        val primaryContactName   = nameGen.sample.value
        val secondaryContactName = nameGen.sample.value

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
          .set(RegisteredAddressInUKPage, false).success.value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false).success.value
          .set(BusinessNameWithoutIDPage, businessName).success.value
          .set(HaveTradingNamePage, true).success.value
          .set(BusinessTradingNameWithoutIDPage, tradingName).success.value
          .set(NonUKBusinessAddressWithoutIDPage, address).success.value
          .set(ContactNamePage, primaryContactName).success.value
          .set(ContactEmailPage, TestEmail).success.value
          .set(ContactHavePhonePage, true).success.value
          .set(ContactPhonePage, TestPhoneNumber).success.value
          .set(HaveSecondContactPage, true).success.value
          .set(SecondContactNamePage, secondaryContactName).success.value
          .set(SecondContactEmailPage, TestEmail).success.value
          .set(SecondContactHavePhonePage, true).success.value
          .set(SecondContactPhonePage, TestPhoneNumber).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Organisation)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = Some(tradingName),
            gbUser = false,
            primaryContact = ContactInformation(
              contactInformation = OrganisationDetails(
                name = primaryContactName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = Some(ContactInformation(
              contactInformation = OrganisationDetails(
                name = secondaryContactName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ))
          )
        )

      }

      "for a business with id (individual affinity group)" in {
        val businessName = nameGen.sample.value
        val contactName  = nameGen.sample.value

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
          .set(RegisteredAddressInUKPage, true).success.value
          .set(WhatIsYourUTRPage, UniqueTaxpayerReference(validUtr.sample.value)).success.value
          .set(BusinessNamePage, businessName).success.value
          .set(ContactNamePage, contactName).success.value
          .set(ContactEmailPage, TestEmail).success.value
          .set(ContactHavePhonePage, true).success.value
          .set(ContactPhonePage, TestPhoneNumber).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Individual)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = None,
            gbUser = true,
            primaryContact = ContactInformation(
              contactInformation = OrganisationDetails(
                name = contactName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = None
          )
        )

      }

      "for a business without id (individual affinity group)" in {
        val businessName       = nameGen.sample.value
        val tradingName        = nameGen.sample.value
        val address            = Address("", None, "", None, None, Country("state", "GG", "Guernsey"))
        val primaryContactName = nameGen.sample.value

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
          .set(RegisteredAddressInUKPage, false).success.value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false).success.value
          .set(BusinessNameWithoutIDPage, businessName).success.value
          .set(HaveTradingNamePage, true).success.value
          .set(BusinessTradingNameWithoutIDPage, tradingName).success.value
          .set(NonUKBusinessAddressWithoutIDPage, address).success.value
          .set(ContactNamePage, primaryContactName).success.value
          .set(ContactEmailPage, TestEmail).success.value
          .set(ContactHavePhonePage, true).success.value
          .set(ContactPhonePage, TestPhoneNumber).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Individual)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = Some(tradingName),
            gbUser = false,
            primaryContact = ContactInformation(
              contactInformation = OrganisationDetails(
                name = primaryContactName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = None
          )
        )

      }

      "for a individual with id" in {
        val contactDob  = validDateOfBirth().sample.value
        val contactName = arbitraryName.arbitrary.sample.value

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.Individual).success.value
          .set(IndDoYouHaveNINumberPage, true).success.value
          .set(IndWhatIsYourNINumberPage, arbitraryNino.arbitrary.sample.value).success.value
          .set(IndContactNamePage, contactName).success.value
          .set(IndDateOfBirthPage, contactDob).success.value
          .set(IndContactEmailPage, TestEmail).success.value
          .set(IndContactHavePhonePage, true).success.value
          .set(IndContactPhonePage, TestPhoneNumber).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Individual)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = None,
            gbUser = true,
            primaryContact = ContactInformation(
              contactInformation = IndividualDetails(
                firstName = contactName.firstName,
                lastName = contactName.lastName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = None
          )
        )
      }

      "for a individual without id" in {
        val contactDob     = validDateOfBirth().sample.value
        val contactName    = arbitraryName.arbitrary.sample.value
        val contactAddress = Address("", None, "", None, None, Country("state", "GB", "United Kingdom"))

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.Individual).success.value
          .set(IndDoYouHaveNINumberPage, false).success.value
          .set(IndWhatIsYourNamePage, contactName).success.value
          .set(DateOfBirthWithoutIdPage, contactDob).success.value
          .set(IndWhereDoYouLivePage, true).success.value
          .set(IndUKAddressWithoutIdPage, contactAddress).success.value
          .set(IndContactEmailPage, TestEmail).success.value
          .set(IndContactHavePhonePage, true).success.value
          .set(IndContactPhonePage, TestPhoneNumber).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Individual)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = None,
            gbUser = true,
            primaryContact = ContactInformation(
              contactInformation = IndividualDetails(
                firstName = contactName.firstName,
                lastName = contactName.lastName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = None
          )
        )
      }

      "for a sole trader with id" in {
        val contactName = arbitraryName.arbitrary.sample.value

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.Sole).success.value
          .set(RegisteredAddressInUKPage, true).success.value
          .set(WhatIsYourUTRPage, UniqueTaxpayerReference(validUtr.sample.value)).success.value
          .set(WhatIsYourNamePage, contactName).success.value
          .set(IndContactEmailPage, TestEmail).success.value
          .set(IndContactHavePhonePage, true).success.value
          .set(IndContactPhonePage, TestPhoneNumber).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Individual)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = None,
            gbUser = true,
            primaryContact = ContactInformation(
              contactInformation = IndividualDetails(
                firstName = contactName.firstName,
                lastName = contactName.lastName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = None
          )
        )
      }

      "for a sole trader without id" in {
        val contactDob     = validDateOfBirth().sample.value
        val contactAddress = Address("", None, "", None, None, Country("state", "US", "United States of America"))

        val userAnswers = UserAnswers("")
          .set(ReporterTypePage, ReporterType.Sole).success.value
          .set(RegisteredAddressInUKPage, false).success.value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false).success.value
          .set(IndDoYouHaveNINumberPage, false).success.value
          .set(IndWhatIsYourNamePage, name).success.value
          .set(DateOfBirthWithoutIdPage, contactDob).success.value
          .set(IndWhereDoYouLivePage, false).success.value
          .set(IndNonUKAddressWithoutIdPage, contactAddress).success.value
          .set(IndContactEmailPage, TestEmail).success.value
          .set(IndContactHavePhonePage, true).success.value
          .set(IndContactPhonePage, TestPhoneNumber).success.value

        val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Individual)

        result mustBe Some(
          CreateSubscriptionRequest(
            idType = IdentifierType.SAFE,
            idNumber = safeId.value,
            tradingName = None,
            gbUser = false,
            primaryContact = ContactInformation(
              contactInformation = IndividualDetails(
                firstName = name.firstName,
                lastName = name.lastName
              ),
              email = TestEmail,
              phone = Some(TestPhoneNumber)
            ),
            secondaryContact = None
          )
        )
      }

    }

    "returns None if any required fields are missing" - {
      val userAnswers = UserAnswers("")

      val result = CreateSubscriptionRequest.buildSubscriptionRequest(safeId, userAnswers, AffinityGroup.Individual)

      result mustBe None
    }

  }

  "isGBUser" - {

    "returns true" - {

      "when a business has a utr" in {
        val userAnswers = UserAnswers("").set(WhatIsYourUTRPage, UniqueTaxpayerReference("1234567890")).success.value

        val result = CreateSubscriptionRequest.isGBUser(userAnswers)

        result mustBe true
      }

      "when a individual has a national insurance number" in {
        val userAnswers = UserAnswers("").set(IndDoYouHaveNINumberPage, true).success.value

        val result = CreateSubscriptionRequest.isGBUser(userAnswers)

        result mustBe true
      }

      "when a individual selected a gb address" in {
        val address     = AddressLookup(Some(""), None, None, None, "town", None, "")
        val userAnswers = UserAnswers("").set(IndSelectedAddressLookupPage, address).success.value

        val result = CreateSubscriptionRequest.isGBUser(userAnswers)

        result mustBe true
      }

      "when a individual manually entered a gb address" in {
        val address     = Address("", None, "", None, None, Country("state", "GB", "United Kingdom"))
        val userAnswers = UserAnswers("").set(IndUKAddressWithoutIdPage, address).success.value

        val result = CreateSubscriptionRequest.isGBUser(userAnswers)

        result mustBe true
      }

    }

    "returns false" - {

      "when a business does not have a utr" in {
        val userAnswers = UserAnswers("").set(WhatIsYourUTRPage, UniqueTaxpayerReference(" ")).success.value

        val result = CreateSubscriptionRequest.isGBUser(userAnswers)

        result mustBe false
      }

      "when a individual manually entered a crown dependency address" in {
        val address     = Address("", None, "", None, None, Country("state", "GG", "Guernsey"))
        val userAnswers = UserAnswers("").set(IndUKAddressWithoutIdPage, address).success.value

        val result = CreateSubscriptionRequest.isGBUser(userAnswers)

        result mustBe false
      }

      "when a individual manually entered a non-uk address" in {
        val address     = Address("", None, "", None, None, Country("state", "US", "United States of America"))
        val userAnswers = UserAnswers("").set(IndNonUKAddressWithoutIdPage, address).success.value

        val result = CreateSubscriptionRequest.isGBUser(userAnswers)

        result mustBe false
      }

    }

  }

}
