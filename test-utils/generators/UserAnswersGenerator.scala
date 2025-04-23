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

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.changeContactDetails._
import play.api.libs.json.{JsObject, JsPath, JsValue, Json}
import uk.gov.hmrc.auth.core.AffinityGroup

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

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = Arbitrary {
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

  implicit lazy val arbitraryOrgAffinityGroup: Arbitrary[AffinityGroup] = Arbitrary {
    for {
      affinityGroup <- Gen.oneOf(AffinityGroup.Organisation, AffinityGroup.Agent)
    } yield affinityGroup
  }

  private def genJsObj(gens: Gen[(QuestionPage[_], JsValue)]*): Gen[JsObject] =
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

  private lazy val indUkAddress =
    Arbitrary {
      for {
        postCode          <- genJsObj(arbitrary[(IndWhatIsYourPostcodePage.type, JsValue)])
        isThisYourAddress <- arbitrary[Boolean]
        data <- if (isThisYourAddress) {
          genJsObj(arbitrary[(IndSelectAddressPage.type, JsValue)])
        } else {
          genJsObj(arbitrary[(IndUKAddressWithoutIdPage.type, JsValue)])
        }
      } yield postCode ++ data
    }

  private lazy val indAddress =
    Arbitrary {
      for {
        whereDoYouLive <- arbitrary[Boolean]
        data <- if (whereDoYouLive) {
          indUkAddress.arbitrary
        } else {
          genJsObj(arbitrary[(IndNonUKAddressWithoutIdPage.type, JsValue)])
        }
        obj = setFields(
          Json.obj(),
          IndWhereDoYouLivePage.path -> Json.toJson(whereDoYouLive)
        ) ++ data
      } yield obj
    }

  private def phoneNumberArbitrary[T <: QuestionPage[Boolean], U <: QuestionPage[String]](
    havePhonePage: T,
    phonePage: U
  )(implicit arb: Arbitrary[(phonePage.type, JsValue)]) = Arbitrary {
    for {
      havePhone <- arbitrary[Boolean]
      data <- if (havePhone) {
        genJsObj(arbitrary[(phonePage.type, JsValue)])
      } else {
        Gen.const(Json.obj())
      }
      obj = setFields(
        Json.obj(),
        havePhonePage.path -> Json.toJson(havePhone)
      ) ++ data
    } yield obj
  }

  private def pageArbitrary[T <: QuestionPage[_]](page: T)(implicit arb: Arbitrary[(page.type, JsValue)]) = Arbitrary {
    for {
      email <- genJsObj(arbitrary[(page.type, JsValue)])
    } yield email
  }

  private def contactArbitrary[T <: QuestionPage[String], U <: QuestionPage[String], V <: QuestionPage[Boolean], W <: QuestionPage[String]](
    namePage: T,
    emailPage: U,
    havePhonePage: V,
    phonePage: W
  )(
    implicit
    arbName: Arbitrary[(namePage.type, JsValue)],
    arbEmail: Arbitrary[(emailPage.type, JsValue)],
    arbPhone: Arbitrary[(phonePage.type, JsValue)]
  ) = Arbitrary {
    for {
      name  <- pageArbitrary(namePage).arbitrary
      email <- pageArbitrary(emailPage).arbitrary
      phone <- phoneNumberArbitrary(havePhonePage, phonePage).arbitrary
    } yield name ++ email ++ phone
  }

  private lazy val indContactDetails = Arbitrary {
    for {
      email <- genJsObj(arbitrary[(IndContactEmailPage.type, JsValue)])
      phone <- phoneNumberArbitrary(IndContactHavePhonePage, IndContactPhonePage).arbitrary
    } yield email ++ phone
  }

  private lazy val indChangeContactDetails = Arbitrary {
    for {
      email <- genJsObj(arbitrary[(IndividualEmailPage.type, JsValue)])
      phone <- phoneNumberArbitrary(IndividualHavePhonePage, IndividualPhonePage).arbitrary
    } yield email ++ phone
  }

  private lazy val orgContactDetails = Arbitrary {
    for {
      haveSecondContact <- arbitrary[Boolean]
      firstContact      <- contactArbitrary(ContactNamePage, ContactEmailPage, ContactHavePhonePage, ContactPhonePage).arbitrary
      secondContact <- if (haveSecondContact) {
        contactArbitrary(SecondContactNamePage, SecondContactEmailPage, SecondContactHavePhonePage, SecondContactPhonePage).arbitrary
      } else {
        Gen.const(Json.obj())
      }
      obj = setFields(
        Json.obj(),
        HaveSecondContactPage.path -> Json.toJson(haveSecondContact)
      ) ++ firstContact ++ secondContact
    } yield obj
  }

  private lazy val orgChangeContactDetails = Arbitrary {
    for {
      haveSecondContact <- arbitrary[Boolean]
      firstContact <-
        contactArbitrary(OrganisationContactNamePage, OrganisationContactEmailPage, OrganisationContactHavePhonePage, OrganisationContactPhonePage).arbitrary
      secondContact <- if (haveSecondContact) {
        contactArbitrary(OrganisationSecondContactNamePage,
                         OrganisationSecondContactEmailPage,
                         OrganisationSecondContactHavePhonePage,
                         OrganisationSecondContactPhonePage
        ).arbitrary
      } else {
        Gen.const(Json.obj())
      }
      obj = setFields(
        Json.obj(),
        OrganisationHaveSecondContactPage.path -> Json.toJson(haveSecondContact)
      ) ++ firstContact ++ secondContact
    } yield obj
  }

  lazy val indWithoutId: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id             <- nonEmptyString
      address        <- indAddress.arbitrary
      contactDetails <- indContactDetails.arbitrary
      additionalData <- genJsObj(arbitrary[(IndWhatIsYourNamePage.type, JsValue)], arbitrary[(DateOfBirthWithoutIdPage.type, JsValue)])
      obj =
        setFields(
          Json.obj(),
          ReporterTypePage.path         -> Json.toJson(ReporterType.Individual.asInstanceOf[ReporterType]),
          IndDoYouHaveNINumberPage.path -> Json.toJson(false)
        ) ++ address ++ contactDetails ++ additionalData
    } yield UserAnswers(
      id = id,
      data = obj
    )
  }

  lazy val indWithId: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id             <- nonEmptyString
      contactDetails <- indContactDetails.arbitrary
      additionalData <- genJsObj(
        arbitrary[(IndWhatIsYourNINumberPage.type, JsValue)],
        arbitrary[(IndContactNamePage.type, JsValue)],
        arbitrary[(IndDateOfBirthPage.type, JsValue)],
        arbitrary[(RegistrationInfoPage.type, JsValue)],
        arbitrary[(IndContactEmailPage.type, JsValue)]
      )
      obj =
        setFields(
          Json.obj(),
          ReporterTypePage.path         -> Json.toJson(ReporterType.Individual.asInstanceOf[ReporterType]),
          IndDoYouHaveNINumberPage.path -> Json.toJson(true)
        ) ++ additionalData ++ contactDetails
    } yield UserAnswers(
      id = id,
      data = obj
    )
  }

  lazy val indChangeContact: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id             <- nonEmptyString
      contactDetails <- indChangeContactDetails.arbitrary
    } yield UserAnswers(
      id = id,
      data = contactDetails
    )
  }

  lazy val missingIndChangeContact: Arbitrary[UserAnswers] = missingAnswersArb(
    indChangeContact,
    Seq(
      IndividualEmailPage,
      IndividualHavePhonePage,
      IndividualPhonePage
    )
  )

  lazy val orgWithId: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id             <- nonEmptyString
      autoMatchedUtr <- arbitrary[Boolean]
      reporterType <- if (autoMatchedUtr) {
        Gen.const(None)
      } else {
        Gen.oneOf(ReporterType.values.filterNot(_ == ReporterType.Individual)).map(Some(_))
      }
      additionalData <- genJsObj(
        arbitrary[(WhatIsYourUTRPage.type, JsValue)],
        arbitrary[(RegistrationInfoPage.type, JsValue)]
      )
      businessName <- if (reporterType.contains(ReporterType.Sole)) {
        genJsObj(arbitrary[(WhatIsYourNamePage.type, JsValue)])
      } else if (reporterType.isDefined) {
        genJsObj(arbitrary[(BusinessNamePage.type, JsValue)])
      } else {
        Gen.const(Json.obj())
      }
      contactDetails <- if (reporterType.contains(ReporterType.Sole)) {
        indContactDetails.arbitrary
      } else {
        orgContactDetails.arbitrary
      }
      autoMatchedUtrObj = if (autoMatchedUtr) {
        Json.obj().setObject(AutoMatchedUTRPage.path, (additionalData \ WhatIsYourUTRPage.toString).as[JsObject]).get
      } else {
        Json.obj()
      }
      reportTypeObj = reporterType.fold(Json.obj())(
        r => Json.obj().setObject(ReporterTypePage.path, Json.toJson(r)).get
      )
      obj = setFields(
        Json.obj(),
        RegisteredAddressInUKPage.path -> Json.toJson(true),
        IsThisYourBusinessPage.path    -> Json.toJson(true)
      ) ++ reportTypeObj ++ autoMatchedUtrObj ++ businessName ++ additionalData ++ contactDetails
    } yield UserAnswers(
      id = id,
      data = obj
    )
  }

  lazy val orgChangeContact: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id             <- nonEmptyString
      contactDetails <- orgChangeContactDetails.arbitrary
    } yield UserAnswers(
      id = id,
      data = contactDetails
    )
  }

  lazy val missingOrgChangeContact: Arbitrary[UserAnswers] = missingAnswersArb(
    orgChangeContact,
    Seq(
      OrganisationContactNamePage,
      OrganisationContactEmailPage,
      OrganisationContactHavePhonePage,
      OrganisationContactPhonePage,
      OrganisationHaveSecondContactPage,
      OrganisationSecondContactNamePage,
      OrganisationSecondContactEmailPage,
      OrganisationSecondContactHavePhonePage,
      OrganisationSecondContactPhonePage
    )
  )

  lazy val orgWithoutId: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id           <- nonEmptyString
      reporterType <- Gen.oneOf(ReporterType.values.filterNot(Seq(ReporterType.Individual, ReporterType.Sole).contains))
      additionalData <- genJsObj(
        arbitrary[(BusinessNameWithoutIDPage.type, JsValue)],
        arbitrary[(NonUKBusinessAddressWithoutIDPage.type, JsValue)]
      )
      haveTradingName <- arbitrary[Boolean]
      tradingName <- if (haveTradingName) {
        genJsObj(arbitrary[(BusinessTradingNameWithoutIDPage.type, JsValue)])
      } else {
        Gen.const(Json.obj())
      }
      contactDetails <- orgContactDetails.arbitrary
      obj = setFields(
        Json.obj(),
        ReporterTypePage.path                     -> Json.toJson(reporterType),
        RegisteredAddressInUKPage.path            -> Json.toJson(false),
        DoYouHaveUniqueTaxPayerReferencePage.path -> Json.toJson(false),
        HaveTradingNamePage.path                  -> Json.toJson(haveTradingName)
      ) ++ tradingName ++ additionalData ++ contactDetails
    } yield UserAnswers(
      id = id,
      data = obj
    )
  }

  private def missingAnswersArb(arb: Arbitrary[UserAnswers], possibleAnswers: Seq[QuestionPage[_]]): Arbitrary[UserAnswers] = Arbitrary {
    for {
      answers <- arb.arbitrary
      validPossibleAnswers = possibleAnswers.filter(
        a => answers.data.keys.toSeq.contains(a.toString)
      )
      n                <- Gen.oneOf(1, validPossibleAnswers.length)
      missingQuestions <- Gen.pick(n, validPossibleAnswers)
      updatedAnswers = missingQuestions.foldLeft(answers) {
        case (acc, page) => acc.remove(page).success.value
      }
    } yield updatedAnswers
  }

  lazy val indWithoutIdMissingAnswers: Arbitrary[UserAnswers] =
    missingAnswersArb(
      indWithoutId,
      Seq(
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
      )
    )

  lazy val indWithIdMissingAnswers: Arbitrary[UserAnswers] =
    missingAnswersArb(
      indWithId,
      Seq(
        IndWhatIsYourNINumberPage,
        IndContactNamePage,
        IndDateOfBirthPage,
        RegistrationInfoPage,
        IndContactEmailPage,
        IndContactHavePhonePage,
        IndContactPhonePage
      )
    )

  lazy val orgWithIdMissingAnswers: Arbitrary[UserAnswers] =
    missingAnswersArb(
      orgWithId,
      Seq(
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
      )
    )

  lazy val orgWithoutIdMissingAnswers: Arbitrary[UserAnswers] =
    missingAnswersArb(
      orgWithoutId,
      Seq(
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
      )
    )

}
