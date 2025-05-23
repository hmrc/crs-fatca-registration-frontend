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
import models.{Name, UniqueTaxpayerReference}
import pages.behaviours.PageBehaviours

class WhatIsYourNamePageTest extends PageBehaviours {

  "cleanup" - {
    "if answer have value" in {
      forAll {
        (name: Name, registrationInfo: RegistrationInfo, booleanField: Boolean) =>
          val ua = emptyUserAnswers
            .withPage(WhatIsYourUTRPage, UniqueTaxpayerReference("utr12345"))
            .withPage(WhatIsYourNamePage, name)
            .withPage(BusinessNamePage, "businessName")
            .withPage(IsThisYourBusinessPage, booleanField)
            .withPage(RegistrationInfoPage, registrationInfo)

          val result = WhatIsYourNamePage.cleanup(Some(name), ua).success.value

          result.get(IsThisYourBusinessPage) mustBe empty
          result.get(RegistrationInfoPage) mustBe empty
          result.get(BusinessNamePage) mustBe empty
          result.get(WhatIsYourUTRPage) must not be empty
          result.get(WhatIsYourNamePage) must not be empty

      }
    }
  }

}
