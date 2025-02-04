/*
 * Copyright 2025 HM Revenue & Customs
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

package pages

import models.{Name, UserAnswers}
import models.matching.RegistrationInfo
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class IndDateOfBirthPageSpec extends PageBehaviours {

  private val testParamGenerator = for {
    dob              <- arbitrary[LocalDate]
    registrationInfo <- arbitrary[RegistrationInfo]

  } yield (dob, registrationInfo)

  def createUserAnswersForIndividualCleanup: Gen[UserAnswers] =
    for {
      (dob, registrationInfo) <- testParamGenerator
    } yield emptyUserAnswers
      .withPage(IndDateOfBirthPage, dob)
      .withPage(RegistrationInfoPage, registrationInfo)

  "cleanUp" - {
    "must clear answers" - {
      "when details are changed in change journey'" in {
        val ua     = createUserAnswersForIndividualCleanup.sample.get
        val result = IndDateOfBirthPage.cleanup(Some(LocalDate.now()), ua).success.value

        result.get(RegistrationInfoPage) mustBe empty

      }
    }
    "must not clear RegistrationInfo" - {
      "when there is no change in answers" in {
        val ua = createUserAnswersForIndividualCleanup.sample.get
        val result = IndDateOfBirthPage.cleanup(
          ua.get(IndDateOfBirthPage),
          ua.withPage(IdMatchInfoPage, IdMatchInfo(dob = ua.get(IndDateOfBirthPage))) // use same generated dob
        ).success.value

        result.get(RegistrationInfoPage) must not be empty
      }
    }
  }

}
