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

import models.matching.RegistrationInfo
import models.{Name, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class IndContactNamePageSpec extends PageBehaviours {

  private val testParamGenerator = for {
    name             <- arbitrary[Name]
    registrationInfo <- arbitrary[RegistrationInfo]

  } yield (name, registrationInfo)

  def createUserAnswersForIndividualCleanup: Gen[UserAnswers] =
    for {
      (name, registrationInfo) <- testParamGenerator.suchThat(_ != null)
    } yield emptyUserAnswers
      .withPage(IndContactNamePage, name)
      .withPage(RegistrationInfoPage, registrationInfo)

  "cleanUp" - {
    "must clear answers" - {
      "when details are changed in change journey'" in {
        val ua     = createUserAnswersForIndividualCleanup.sample.get
        val result = IndContactNamePage.cleanup(Some(Name("John", "Smith")), ua).success.value

        result.get(RegistrationInfoPage) mustBe empty

      }
    }
    "must not clear RegistrationInfo" - {
      "when there is no change in answers" in {
        val ua = createUserAnswersForIndividualCleanup.sample.get

        val result = IndContactNamePage.cleanup(
          ua.get(IndContactNamePage),
          ua.withPage(IdMatchInfoPage, IdMatchInfo(name = ua.get(IndContactNamePage))) // use same generated name
        ).success.value

        result.get(RegistrationInfoPage) must not be empty
      }
    }
  }

}
