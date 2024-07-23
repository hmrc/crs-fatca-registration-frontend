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

import models.{RichJsObject, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.{JsObject, JsPath, JsValue, Json}

trait UserAnswersGenerator extends UserAnswersEntryGenerators with TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(IndContactPhonePage.type, JsValue)] ::
      arbitrary[(IndContactHavePhonePage.type, JsValue)] ::
      arbitrary[(IndContactEmailPage.type, JsValue)] ::
      arbitrary[(BusinessTradingNameWithoutIDPage.type, JsValue)] ::
      arbitrary[(BusinessNameWithoutIDPage.type, JsValue)] ::
      arbitrary[(IndWhatIsYourPostcodePage.type, JsValue)] ::
      arbitrary[(ContactNamePage.type, JsValue)] ::
      arbitrary[(WhatIsYourNamePage.type, JsValue)] ::
      arbitrary[(NonUKBusinessAddressWithoutIDPage.type, JsValue)] ::
      arbitrary[(DateOfBirthWithoutIdPage.type, JsValue)] ::
      arbitrary[(IndWhatIsYourNamePage.type, JsValue)] ::
      arbitrary[(IndWhatIsYourNINumberPage.type, JsValue)] ::
      arbitrary[(BusinessNamePage.type, JsValue)] ::
      arbitrary[(IsThisYourBusinessPage.type, JsValue)] ::
      arbitrary[(BusinessNamePage.type, JsValue)] ::
      arbitrary[(DoYouHaveUniqueTaxPayerReferencePage.type, JsValue)] ::
      arbitrary[(ReporterTypePage.type, JsValue)] ::
      arbitrary[(SecondContactHavePhonePage.type, JsValue)] ::
      arbitrary[(IndDoYouHaveNINumberPage.type, JsValue)] ::
      arbitrary[(SecondContactHavePhonePage.type, JsValue)] ::
      arbitrary[(SecondContactEmailPage.type, JsValue)] ::
      arbitrary[(SecondContactPhonePage.type, JsValue)] ::
      arbitrary[(SecondContactEmailPage.type, JsValue)] ::
      arbitrary[(SecondContactNamePage.type, JsValue)] ::
      arbitrary[(HaveSecondContactPage.type, JsValue)] ::
      arbitrary[(ContactHavePhonePage.type, JsValue)] ::
      arbitrary[(ContactNamePage.type, JsValue)] ::
      arbitrary[(ContactPhonePage.type, JsValue)] ::
      arbitrary[(ContactEmailPage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id <- nonEmptyString
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        id = id,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }

  private def generateJsObject(gens: List[Gen[(QuestionPage[_], JsValue)]]): Gen[JsObject] =
    Gen.sequence[Seq[(QuestionPage[_], JsValue)], (QuestionPage[_], JsValue)](gens).map {
      seq =>
        seq.foldLeft(Json.obj()) {
          case (obj, (page, value)) =>
            obj.setObject(page.path, value).get
        }
    }

  private def setFields(obj: JsObject, fields: (JsPath, JsValue)*): JsObject =
    fields.foldLeft(obj) {
      case (acc, (path, value)) => acc.setObject(path, value).get
    }

  private lazy val indAddress =
    Arbitrary {
      for {
        whereDoYouLive <- arbitrary[Boolean]
        gens = if (whereDoYouLive) {
          arbitrary[(IndWhatIsYourPostcodePage.type, JsValue)] :: arbitrary[(IndUKAddressWithoutIdPage.type, JsValue)] :: Nil
        } else {
          arbitrary[(IndNonUKAddressWithoutIdPage.type, JsValue)] :: Nil
        }
        data <- generateJsObject(gens)
        obj = setFields(
          Json.obj(),
          IndWhereDoYouLivePage.path -> Json.toJson(whereDoYouLive)
        ) ++ data
      } yield obj
    }

  private lazy val indPhone =
    Arbitrary {
      for {
        havePhone <- arbitrary[Boolean]
        gens = if (havePhone) {
          arbitrary[(IndContactPhonePage.type, JsValue)] ::
            Nil
        } else {
          Nil
        }
        data <- generateJsObject(gens)
        obj = setFields(
          Json.obj(),
          IndContactHavePhonePage.path -> Json.toJson(havePhone)
        ) ++ data
      } yield obj
    }

  lazy val indWithoutId: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id      <- nonEmptyString
        address <- indAddress.arbitrary
        phone   <- indPhone.arbitrary
        gens = arbitrary[(IndWhatIsYourNamePage.type, JsValue)] ::
          arbitrary[(DateOfBirthWithoutIdPage.type, JsValue)] ::
          arbitrary[(IndContactEmailPage.type, JsValue)] ::
          Nil
        additionalData <- generateJsObject(gens)
        obj =
          setFields(
            Json.obj(),
            ReporterTypePage.path         -> Json.toJson(ReporterType.Individual.asInstanceOf[ReporterType]),
            IndDoYouHaveNINumberPage.path -> Json.toJson(false)
          ) ++ address ++ phone ++ additionalData
      } yield UserAnswers(
        id = id,
        data = obj
      )
    }
  }

  lazy val indWithoutIdMissingAnswers: Arbitrary[UserAnswers] =
    Arbitrary {
      for {
        answers <- indWithoutId.arbitrary
        basicQuestions = Seq(IndWhatIsYourNamePage, DateOfBirthWithoutIdPage, IndContactEmailPage, IndWhereDoYouLivePage, IndContactHavePhonePage)
        addressQuestions = answers.get(IndWhereDoYouLivePage) match {
          case Some(true) => Seq(IndWhatIsYourPostcodePage, IndUKAddressWithoutIdPage)
          case _          => Seq(IndNonUKAddressWithoutIdPage)
        }
        phoneQuestions = answers.get(IndContactHavePhonePage) match {
          case Some(true) => Seq(IndContactPhonePage)
          case _          => Nil
        }
        allQuestions = basicQuestions ++ addressQuestions ++ phoneQuestions
        n                <- Gen.choose(1, allQuestions.length)
        missingQuestions <- Gen.pick(n, allQuestions)
        updatedAnswers = missingQuestions.foldLeft(answers) {
          case (acc, page) => acc.remove(page.asInstanceOf[QuestionPage[_]]).success.value
        }
      } yield updatedAnswers
    }

}
