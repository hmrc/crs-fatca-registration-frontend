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
import controllers.organisation.routes._
import controllers.individual.routes._
import controllers.routes
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go from YourContactDetailsPage to ContactNamePage" in {

        navigator.nextPage(YourContactDetailsPage, NormalMode, UserAnswers("id")) mustBe ContactNameController.onPageLoad(NormalMode)
      }

      "must go from ContactEmailPage to ContactHavePhonePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(ContactEmailPage, "test@email").success.value
        navigator.nextPage(ContactEmailPage, NormalMode, userAnswers) mustBe ContactHavePhoneController.onPageLoad(NormalMode)
      }

      "must go from ContactHavePhonePage to ContactPhonePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(ContactHavePhonePage, true).success.value
        navigator.nextPage(ContactHavePhonePage, NormalMode, userAnswers) mustBe ContactPhoneController.onPageLoad(NormalMode)
      }

      "must go from SecondContactNamePage to SecondContactEmailPage" in {

        val userAnswers = emptyUserAnswers.set(SecondContactNamePage, "value").success.value
        navigator.nextPage(SecondContactNamePage, NormalMode, userAnswers) mustBe SecondContactEmailController.onPageLoad(NormalMode)
      }

      "must go from SecondContactHavePhonePage to SecondContactPhonePage when user answers yes" in {

        val userAnswers = emptyUserAnswers.set(SecondContactHavePhonePage, true).success.value
        navigator.nextPage(SecondContactHavePhonePage, NormalMode, userAnswers) mustBe SecondContactPhoneController.onPageLoad(NormalMode)
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
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }

}
