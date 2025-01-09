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
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, RegistrationInfo, SafeId}
import models.register.response.details.{AddressResponse => RegistrationAddressResponse}
import models.subscription.request._
import models.subscription.response.{CrfaSubscriptionDetails, DisplayResponseDetail, DisplaySubscriptionResponse}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.const
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Nino
import utils.RegexConstants
import wolfendale.scalacheck.regexp.RegexpGen

trait ModelGenerators extends RegexConstants with Generators {

  val maximumNumber     = 999999
  val minimumNumber     = 1
  val countryNumber     = 2
  val EmailLength       = 132
  val PhoneNumberLength = 24
  val MaxNameLength     = 35

  implicit lazy val arbitraryReporterType: Arbitrary[ReporterType] =
    Arbitrary {
      Gen.oneOf(ReporterType.values)
    }

  implicit lazy val arbitraryIndContactName: Arbitrary[IndContactName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName  <- arbitrary[String]
      } yield IndContactName(firstName, lastName)
    }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] = Arbitrary {
    for {
      organisationName <- Gen.listOfN(MaxNameLength, Gen.asciiPrintableChar).map(_.mkString)
    } yield OrganisationDetails(organisationName)
  }

  implicit val arbitraryIndividualDetails: Arbitrary[IndividualDetails] = Arbitrary {
    for {
      firstName <- arbitrary[String]
      lastName  <- arbitrary[String]
    } yield IndividualDetails(firstName, lastName)
  }

  implicit lazy val arbitraryContactInformation: Arbitrary[ContactType] = Arbitrary {
    Gen.oneOf[ContactType](arbitrary[OrganisationDetails], arbitrary[IndividualDetails])
  }

  implicit lazy val arbitrarySubscriptionID: Arbitrary[SubscriptionID] = Arbitrary {
    for {
      id <- Gen.alphaNumChar.map(_.toString)
    } yield SubscriptionID(id)
  }

  implicit val arbitraryPrimaryContact: Arbitrary[ContactInformation] = Arbitrary {
    for {
      contactInformation <- arbitrary[ContactType]
      email              <- emailMatchingRegexAndLength(emailRegex, EmailLength)
      phone              <- Gen.option(validPhoneNumber(PhoneNumberLength))
    } yield ContactInformation(contactInformation, email, phone)
  }

  implicit val arbitraryRequestParameter: Arbitrary[RequestParameter] = Arbitrary {
    for {
      paramName  <- arbitrary[String]
      paramValue <- arbitrary[String]
    } yield RequestParameter(paramName, paramValue)
  }

  implicit val arbitraryCreateRequestDetail: Arbitrary[CreateSubscriptionRequest] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
    } yield CreateSubscriptionRequest(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryRequestDetail: Arbitrary[ReadSubscriptionRequest] = Arbitrary {
    for {
      idNumber <- arbitrary[String]
    } yield ReadSubscriptionRequest(idNumber)
  }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        code <- Gen.pick(countryNumber, 'A' to 'Z')
        name <- arbitrary[String]
      } yield Country(code.mkString, name)
    }

  implicit lazy val arbitraryAddressWithoutId: Arbitrary[models.Address] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String].suchThat(_.nonEmpty)
        addressLine2 <- arbitrary[Option[String]]
        addressLine3 <- arbitrary[String].suchThat(_.nonEmpty)
        addressLine4 <- arbitrary[Option[String]]
        postCode     <- arbitrary[Option[String]]
        country      <- arbitrary[Country]
      } yield Address(addressLine1, addressLine2, addressLine3, addressLine4, postCode, country)
    }

  implicit lazy val arbitraryAddressLookup: Arbitrary[models.AddressLookup] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[Option[String]]
        addressLine2 <- arbitrary[Option[String]]
        addressLine3 <- arbitrary[Option[String]]
        addressLine4 <- arbitrary[Option[String]]
        postCode     <- arbitrary[String]
        town         <- arbitrary[String]
        county       <- arbitrary[Option[String]]
        country      <- arbitrary[Option[Country]]
      } yield AddressLookup(addressLine1, addressLine2, addressLine3, addressLine4, town, county, postCode, country)
    }

  implicit lazy val arbitraryName: Arbitrary[models.Name] =
    Arbitrary {
      for {
        firstName <- RegexpGen.from(Name.RegexString)
        lastName  <- RegexpGen.from(Name.RegexString)
      } yield models.Name(firstName, lastName)
    }

  implicit val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    for {
      prefix <- Gen.oneOf(Nino.validPrefixes)
      number <- Gen.choose(minimumNumber, maximumNumber)
      suffix <- Gen.oneOf(Nino.validSuffixes)
    } yield Nino(f"$prefix$number%06d$suffix")
  }

  implicit val arbitraryAddressResponse: Arbitrary[RegistrationAddressResponse] = Arbitrary {
    val postCode = for {
      size     <- Gen.chooseNum(5, 7)
      postCode <- Gen.option(Gen.listOfN(size, Gen.alphaNumChar).map(_.mkString))
    } yield postCode
    for {
      addressline  <- arbitrary[String]
      addressline2 <- arbitrary[Option[String]]
      addressline3 <- arbitrary[Option[String]]
      addressline4 <- arbitrary[Option[String]]
      postcode     <- postCode
      countrycode  <- arbitrary[String]
    } yield RegistrationAddressResponse(addressline, addressline2, addressline3, addressline4, postcode, countrycode)
  }

  implicit val arbitraryOrgRegistrationInfo: Arbitrary[OrgRegistrationInfo] = Arbitrary {
    for {
      idNumber        <- arbitrary[String].suchThat(_.nonEmpty)
      name            <- arbitrary[Name]
      addressResponse <- arbitrary[RegistrationAddressResponse]
    } yield OrgRegistrationInfo(SafeId(idNumber), name.fullName, addressResponse)
  }

  implicit val arbitraryIndRegistrationInfo: Arbitrary[IndRegistrationInfo] = Arbitrary {
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .map(
        idNumber => IndRegistrationInfo(SafeId(idNumber))
      )
  }

  implicit val arbitraryRegistrationInfo: Arbitrary[RegistrationInfo] = Arbitrary {
    Gen.oneOf(arbitrary[OrgRegistrationInfo], arbitrary[IndRegistrationInfo])
  }

  implicit val arbitraryDisplaySubscriptionResponse: Arbitrary[DisplaySubscriptionResponse] = Arbitrary {
    arbitrary[CreateSubscriptionRequest].flatMap {
      subscription =>
        DisplaySubscriptionResponse(
          DisplayResponseDetail(
            CrfaSubscriptionDetails(
              subscription.idNumber,
              subscription.tradingName,
              subscription.gbUser,
              subscription.primaryContact,
              subscription.secondaryContact
            )
          )
        )
    }
  }

//Line holder for template scripts
  implicit val arbitraryUniqueTaxpayerReference: Arbitrary[UniqueTaxpayerReference] = Arbitrary {
    for {
      id <- arbitrary[String]
    } yield UniqueTaxpayerReference(id)
  }

  // Line holder for template scripts
}
