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

package utils

import generators.{ModelGenerators, UserAnswersGenerator}
import models.{ReporterType, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class CheckYourAnswersValidatorSpec extends AnyFreeSpec with Matchers with ModelGenerators with UserAnswersGenerator with ScalaCheckPropertyChecks {

  "CheckYourAnswersValidator" - {

    "individual without id journey" - {

      "return an empty list if no answers are missing" in {
        forAll(indWithoutId.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustBe Some(ReporterType.Individual)
            userAnswers.get(IndDoYouHaveNINumberPage) mustBe Some(false)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustBe Nil
        }
      }

      "return missing answers" in {
        forAll(indWithoutIdMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustBe Some(ReporterType.Individual)
            userAnswers.get(IndDoYouHaveNINumberPage) mustBe Some(false)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustNot be(empty)
            Set(
              IndWhatIsYourNamePage,
              DateOfBirthWithoutIdPage,
              IndContactEmailPage,
              IndWhereDoYouLivePage,
              IndContactHavePhonePage,
              IndWhatIsYourPostcodePage,
              IndUKAddressWithoutIdPage,
              IndNonUKAddressWithoutIdPage,
              IndSelectAddressPage,
              IndContactPhonePage
            ) must contain allElementsOf result
        }
      }
    }

    "individual with id journey" - {

      "return an empty list if no answers are missing" in {
        forAll(indWithId.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustBe Some(ReporterType.Individual)
            userAnswers.get(IndDoYouHaveNINumberPage) mustBe Some(true)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustBe Nil
        }
      }

      "return missing answers" in {
        forAll(indWithIdMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustBe Some(ReporterType.Individual)
            userAnswers.get(IndDoYouHaveNINumberPage) mustBe Some(true)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustNot be(empty)
            Set(
              IndWhatIsYourNINumberPage,
              IndContactNamePage,
              IndDateOfBirthPage,
              RegistrationInfoPage,
              IndContactEmailPage,
              IndContactHavePhonePage,
              IndContactPhonePage
            ) must contain allElementsOf result
        }
      }
    }

    "org or sole trader with utr" - {
      "return an empty list if no answers are missing" in {
        forAll(orgWithId.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustNot be(Some(ReporterType.Individual))
            userAnswers.get(RegisteredAddressInUKPage) mustBe Some(true)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustBe Nil
        }
      }

      "return missing answers" in {
        forAll(orgWithIdMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustNot be(Some(ReporterType.Individual))
            userAnswers.get(RegisteredAddressInUKPage) mustBe Some(true)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustNot be(empty)
            Set(
              WhatIsYourUTRPage,
              WhatIsYourNamePage,
              BusinessNamePage,
              IsThisYourBusinessPage,
              RegistrationInfoPage,
              IndContactEmailPage,
              IndContactHavePhonePage,
              IndContactPhonePage,
              ContactNamePage,
              ContactEmailPage,
              ContactHavePhonePage,
              ContactPhonePage,
              HaveSecondContactPage,
              SecondContactNamePage,
              SecondContactEmailPage,
              SecondContactHavePhonePage,
              SecondContactPhonePage
            ) must contain allElementsOf result
        }
      }
    }

    "org without utr" - {
      "return an empty list if no answers are missing" in {
        forAll(orgWithoutId.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustNot be(Some(ReporterType.Individual))
            userAnswers.get(RegisteredAddressInUKPage) mustBe Some(false)
            userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage) mustBe Some(false)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustBe Nil
        }
      }

      "return missing answers" in {
        forAll(orgWithoutIdMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            userAnswers.get(ReporterTypePage) mustNot be(Some(ReporterType.Individual))
            userAnswers.get(RegisteredAddressInUKPage) mustBe Some(false)
            userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage) mustBe Some(false)

            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustNot be(empty)
            Set(
              BusinessNameWithoutIDPage,
              HaveTradingNamePage,
              BusinessTradingNameWithoutIDPage,
              NonUKBusinessAddressWithoutIDPage,
              IndContactEmailPage,
              IndContactHavePhonePage,
              IndContactPhonePage,
              ContactNamePage,
              ContactEmailPage,
              ContactHavePhonePage,
              ContactPhonePage,
              HaveSecondContactPage,
              SecondContactNamePage,
              SecondContactEmailPage,
              SecondContactHavePhonePage,
              SecondContactPhonePage
            ) must contain allElementsOf result
        }
      }
    }
  }

}
