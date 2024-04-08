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

package models.subscription.request

import base.SpecBase
import generators.ModelGenerators
import models.error.ApiError.MandatoryInformationMissingError
import models.{ReporterType, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.auth.core.AffinityGroup

class ContactTypeSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "ContactInformation" - {

    "convertToPrimary" - {

      "Individual with ID" - {

        "return None if any required fields are missing" in {
          val contactName   = arbitraryName.arbitrary.sample.value
          val contactNumber = validPhoneNumber(11).sample.value

          // no contact email
          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.Individual).success.value
            .set(IndContactNamePage, contactName).success.value
            .set(IndContactPhonePage, contactNumber).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe None
        }

        "return ContactInformation if all fields have been filled in" in {
          val contactName   = arbitraryName.arbitrary.sample.value
          val contactNumber = validPhoneNumber(11).sample.value
          val contactEmail  = validEmailAddressToLong(3).sample.value

          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.Individual).success.value
            .set(IndContactNamePage, contactName).success.value
            .set(IndContactEmailPage, contactEmail).success.value
            .set(IndContactPhonePage, contactNumber).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe Some(ContactInformation(
            contactInformation = IndividualDetails(
              firstName = contactName.firstName,
              lastName = contactName.lastName
            ),
            email = contactEmail,
            phone = Some(contactNumber)
          ))
        }

      }

      "Individual without ID" - {

        "return None if any required fields are missing" in {
          val contactNumber = validPhoneNumber(11).sample.value
          val contactEmail  = validEmailAddressToLong(3).sample.value

          // missing contact name
          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.Individual).success.value
            .set(IndContactEmailPage, contactEmail).success.value
            .set(IndContactPhonePage, contactNumber).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe None
        }

        "return ContactInformation if all fields have been filled in" in {
          val contactName  = arbitraryName.arbitrary.sample.value
          val contactEmail = validEmailAddressToLong(3).sample.value

          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.Individual).success.value
            .set(IndWhatIsYourNamePage, contactName).success.value
            .set(IndContactEmailPage, contactEmail).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe Some(ContactInformation(
            contactInformation = IndividualDetails(
              firstName = contactName.firstName,
              lastName = contactName.lastName
            ),
            email = contactEmail,
            phone = None
          ))
        }

      }

      "Business with ID" - {

        "return None if any required fields are missing" in {
          val contactNumber = validPhoneNumber(11).sample.value
          val contactEmail  = validEmailAddressToLong(3).sample.value

          // no contact name
          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
            .set(ContactEmailPage, contactEmail).success.value
            .set(ContactPhonePage, contactNumber).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe None
        }

        "return ContactInformation if all fields have been filled in" in {
          val contactName   = arbitraryName.arbitrary.sample.value
          val contactNumber = validPhoneNumber(11).sample.value
          val contactEmail  = validEmailAddressToLong(3).sample.value

          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
            .set(ContactNamePage, contactName.fullName).success.value
            .set(ContactEmailPage, contactEmail).success.value
            .set(ContactPhonePage, contactNumber).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe Some(ContactInformation(
            contactInformation = OrganisationDetails(
              name = contactName.fullName
            ),
            email = contactEmail,
            phone = Some(contactNumber)
          ))
        }

      }

      "Business without ID" - {

        "return None if any required fields are missing" in {
          val contactEmail = validEmailAddressToLong(3).sample.value

          // no contact name
          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
            .set(ContactEmailPage, contactEmail).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe None
        }

        "return ContactInformation if all fields have been filled in" in {
          val contactName  = arbitraryName.arbitrary.sample.value
          val contactEmail = validEmailAddressToLong(3).sample.value

          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
            .set(ContactNamePage, contactName.fullName).success.value
            .set(ContactEmailPage, contactEmail).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe Some(ContactInformation(
            contactInformation = OrganisationDetails(
              name = contactName.fullName
            ),
            email = contactEmail,
            phone = None
          ))
        }

      }

      "Sole Trader with ID" - {

        "return None if any required fields are missing" in {
          val contactNumber = validPhoneNumber(11).sample.value

          // missing name and email
          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.Sole).success.value
            .set(IndContactPhonePage, contactNumber).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe None
        }

        "return ContactInformation if all fields have been filled in" in {
          val contactName   = arbitraryName.arbitrary.sample.value
          val contactNumber = validPhoneNumber(11).sample.value
          val contactEmail  = validEmailAddressToLong(3).sample.value

          val userAnswers = UserAnswers("")
            .set(ReporterTypePage, ReporterType.Sole).success.value
            .set(WhatIsYourNamePage, contactName).success.value
            .set(IndContactEmailPage, contactEmail).success.value
            .set(IndContactPhonePage, contactNumber).success.value

          val result = ContactInformation.convertToPrimary(userAnswers = userAnswers)

          result mustBe Some(ContactInformation(
            contactInformation = IndividualDetails(
              firstName = contactName.firstName,
              lastName = contactName.lastName
            ),
            email = contactEmail,
            phone = Some(contactNumber)
          ))
        }

      }

    }

    "convertToSecondary" - {

      "Individual Affinity Group" - {

        "return None" in {
          val userAnswers = UserAnswers("")
          val result      = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Individual)
          result mustBe Right(None)
        }

      }

      "Organisation Affinity Group" - {

        "registering as a Individual" - {

          "return None" in {
            val userAnswers = UserAnswers("").set(ReporterTypePage, ReporterType.Individual).success.value
            val result      = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Organisation)
            result mustBe Right(None)
          }

        }

        "registering as a Sole Trader" - {

          "return None" in {
            val userAnswers = UserAnswers("").set(ReporterTypePage, ReporterType.Sole).success.value
            val result      = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Organisation)
            result mustBe Right(None)
          }

        }

        "registering as a Business" - {

          "success scenarios" - {

            "return None if user has answered that they have no second contact" in {
              val userAnswers = UserAnswers("")
                .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
                .set(HaveSecondContactPage, false).success.value

              val result = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Organisation)

              result mustBe Right(None)
            }

            "return ContactInformation if second contact information was provided (including phone)" in {
              val contactName   = arbitraryName.arbitrary.sample.value.fullName
              val contactNumber = validPhoneNumber(11).sample.value
              val contactEmail  = validEmailAddressToLong(3).sample.value

              val userAnswers = UserAnswers("")
                .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
                .set(HaveSecondContactPage, true).success.value
                .set(SecondContactNamePage, contactName).success.value
                .set(SecondContactEmailPage, contactEmail).success.value
                .set(SecondContactHavePhonePage, true).success.value
                .set(SecondContactPhonePage, contactNumber).success.value

              val result = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Organisation)

              result mustBe Right(Some(ContactInformation(
                contactInformation = OrganisationDetails(contactName),
                email = contactEmail,
                phone = Some(contactNumber)
              )))
            }

            "return ContactInformation if second contact information was provided (no phone)" in {
              val contactName  = arbitraryName.arbitrary.sample.value.fullName
              val contactEmail = validEmailAddressToLong(3).sample.value

              val userAnswers = UserAnswers("")
                .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
                .set(HaveSecondContactPage, true).success.value
                .set(SecondContactNamePage, contactName).success.value
                .set(SecondContactEmailPage, contactEmail).success.value
                .set(SecondContactHavePhonePage, false).success.value

              val result = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Organisation)

              result mustBe Right(Some(ContactInformation(
                contactInformation = OrganisationDetails(contactName),
                email = contactEmail,
                phone = None
              )))
            }

          }

          "error scenarios" - {

            "return MandatoryInformationMissingError if a required field is missing" in {
              val contactName  = arbitraryName.arbitrary.sample.value.fullName
              val contactEmail = validEmailAddressToLong(3).sample.value

              val userAnswers = UserAnswers("")
                .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
                .set(HaveSecondContactPage, true).success.value
                .set(SecondContactNamePage, contactName).success.value

              val result = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Organisation)

              result mustBe Left(MandatoryInformationMissingError())
            }

            "return MandatoryInformationMissingError if multiple required fields are missing" in {
              val userAnswers = UserAnswers("")
                .set(ReporterTypePage, ReporterType.LimitedCompany).success.value
                .set(HaveSecondContactPage, true).success.value

              val result = ContactInformation.convertToSecondary(userAnswers = userAnswers, affinityGroup = AffinityGroup.Organisation)

              result mustBe Left(MandatoryInformationMissingError())
            }

          }

        }

      }

    }

  }

}
