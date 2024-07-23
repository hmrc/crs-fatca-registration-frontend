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

package generators

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryIndividualContactPhoneUserAnswersEntry: Arbitrary[(pages.IndContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndContactPhonePage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIndividualHaveContactTelephoneUserAnswersEntry: Arbitrary[(pages.IndContactHavePhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndContactHavePhonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIndividualContactEmailUserAnswersEntry: Arbitrary[(pages.IndContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndContactEmailPage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsTradingNameUserAnswersEntry: Arbitrary[(pages.BusinessTradingNameWithoutIDPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.BusinessTradingNameWithoutIDPage.type]
        value <- Gen.alphaStr.map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessWithoutIDNameUserAnswersEntry: Arbitrary[(pages.BusinessNameWithoutIDPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.BusinessNameWithoutIDPage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourPostcodeUserAnswersEntry: Arbitrary[(pages.IndWhatIsYourPostcodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndWhatIsYourPostcodePage.type]
        value <- Gen.alphaStr.map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNameUserAnswersEntry: Arbitrary[(pages.WhatIsYourNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatIsYourNamePage.type]
        value <- arbitrary[models.Name].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIndNameUserAnswersEntry: Arbitrary[(pages.IndWhatIsYourNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndWhatIsYourNamePage.type]
        value <- arbitrary[models.Name].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIndWhereDoYouLivePageEntry: Arbitrary[(pages.IndWhereDoYouLivePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndWhereDoYouLivePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIndNonUKAddressWithoutIdPageEntry: Arbitrary[(pages.IndNonUKAddressWithoutIdPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndNonUKAddressWithoutIdPage.type]
        value <- arbitrary[models.Address].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIndUKAddressWithoutIdPageEntry: Arbitrary[(pages.IndUKAddressWithoutIdPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndUKAddressWithoutIdPage.type]
        value <- arbitrary[models.Address].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySoleNameUserAnswersEntry: Arbitrary[(pages.IndContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndContactNamePage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddressWithoutIdUserAnswersEntry: Arbitrary[(pages.NonUKBusinessAddressWithoutIDPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.NonUKBusinessAddressWithoutIDPage.type]
        value <- arbitrary[models.Address].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourDateOfBirthUserAnswersEntry: Arbitrary[(pages.IndDateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndDateOfBirthPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsThisYourBusinessUserAnswersEntry: Arbitrary[(pages.IsThisYourBusinessPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IsThisYourBusinessPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessNameUserAnswersEntry: Arbitrary[(pages.BusinessNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.BusinessNamePage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUTRUserAnswersEntry: Arbitrary[(pages.WhatIsYourUTRPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatIsYourUTRPage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessTypeUserAnswersEntry: Arbitrary[(pages.ReporterTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ReporterTypePage.type]
        value <- arbitrary[models.ReporterType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHaveUniqueTaxPayerReferenceUserAnswersEntry: Arbitrary[(pages.DoYouHaveUniqueTaxPayerReferencePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.DoYouHaveUniqueTaxPayerReferencePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHaveNINUserAnswersEntry: Arbitrary[(pages.IndDoYouHaveNINumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndDoYouHaveNINumberPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndConHavePhoneUserAnswersEntry: Arbitrary[(pages.SecondContactHavePhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SecondContactHavePhonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndContactPhoneUserAnswersEntry: Arbitrary[(pages.SecondContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SecondContactPhonePage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndContactEmailUserAnswersEntry: Arbitrary[(pages.SecondContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SecondContactEmailPage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndContactNameUserAnswersEntry: Arbitrary[(pages.SecondContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SecondContactNamePage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondContactUserAnswersEntry: Arbitrary[(pages.HaveSecondContactPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.HaveSecondContactPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactHavePhoneUserAnswersEntry: Arbitrary[(pages.ContactHavePhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactHavePhonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactPhoneUserAnswersEntry: Arbitrary[(pages.ContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactPhonePage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactNameUserAnswersEntry: Arbitrary[(pages.ContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactNamePage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactEmailUserAnswersEntry: Arbitrary[(pages.ContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactEmailPage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNINumberUserAnswersEntry: Arbitrary[(pages.IndWhatIsYourNINumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IndWhatIsYourNINumberPage.type]
        value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDateOfBirthUserAnswersEntry: Arbitrary[(pages.DateOfBirthWithoutIdPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.DateOfBirthWithoutIdPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

}
