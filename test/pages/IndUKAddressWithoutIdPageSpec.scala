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

package pages

import models.Address
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class IndUKAddressWithoutIdPageSpec extends PageBehaviours {

  "IndUKAddressWithoutIdPage" - {

    beRetrievable[Address](IndUKAddressWithoutIdPage)

    beSettable[Address](IndUKAddressWithoutIdPage)

    beRemovable[Address](IndUKAddressWithoutIdPage)

    "cleanup" - {
      "must remove IndSelectAddressPage" in {
        val generator = for {
          addressLookup <- arbitrary[models.AddressLookup]
          address       <- arbitrary[models.Address]
        } yield (addressLookup, address)

        forAll(generator) {
          case (addressLookup, address) =>
            val userAnswers = emptyUserAnswers.withPage(IndSelectAddressPage, addressLookup.format)

            val result = IndUKAddressWithoutIdPage.cleanup(Option(address), userAnswers).success.value

            result.get(IndSelectAddressPage) mustBe empty
        }
      }
    }
  }

}
