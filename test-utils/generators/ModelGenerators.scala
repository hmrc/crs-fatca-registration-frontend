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
        firstName <- nonEmptyString
        lastName  <- nonEmptyString
      } yield IndContactName(firstName, lastName)
    }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] = Arbitrary {
    for {
      organisationName <- Gen.listOfN(MaxNameLength, Gen.asciiPrintableChar).map(_.mkString)
    } yield OrganisationDetails(organisationName)
  }

  implicit val arbitraryIndividualDetails: Arbitrary[IndividualDetails] = Arbitrary {
    for {
      firstName <- nonEmptyString
      lastName  <- nonEmptyString
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
      paramName  <- nonEmptyString
      paramValue <- nonEmptyString
    } yield RequestParameter(paramName, paramValue)
  }

  implicit val arbitraryCreateRequestDetail: Arbitrary[CreateSubscriptionRequest] = Arbitrary {
    for {
      idType           <- nonEmptyString
      idNumber         <- nonEmptyString
      tradingName      <- Gen.option(nonEmptyString)
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
    } yield CreateSubscriptionRequest(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryRequestDetail: Arbitrary[ReadSubscriptionRequest] = Arbitrary {
    for {
      idNumber <- nonEmptyString
    } yield ReadSubscriptionRequest(idNumber)
  }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        code <- Gen.pick(countryNumber, 'A' to 'Z')
        name <- nonEmptyString
      } yield Country(code.mkString, name)
    }

  implicit lazy val arbitraryAddressWithoutId: Arbitrary[models.Address] =
    Arbitrary {
      for {
        addressLine1 <- nonEmptyString
        addressLine2 <- Gen.option(nonEmptyString)
        addressLine3 <- nonEmptyString
        addressLine4 <- Gen.option(nonEmptyString)
        postCode     <- Gen.option(nonEmptyString)
        country      <- arbitrary[Country]
      } yield Address(addressLine1, addressLine2, addressLine3, addressLine4, postCode, country)
    }

  implicit lazy val arbitraryAddressLookup: Arbitrary[models.AddressLookup] =
    Arbitrary {
      for {
        addressLine1 <- Gen.option(nonEmptyString)
        addressLine2 <- Gen.option(nonEmptyString)
        addressLine3 <- Gen.option(nonEmptyString)
        addressLine4 <- Gen.option(nonEmptyString)
        postCode     <- nonEmptyString
        town         <- nonEmptyString
        county       <- Gen.option(nonEmptyString)
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
      addressline  <- nonEmptyString
      addressline2 <- Gen.option(nonEmptyString)
      addressline3 <- Gen.option(nonEmptyString)
      addressline4 <- Gen.option(nonEmptyString)
      postcode     <- postCode
      countrycode  <- nonEmptyString
    } yield RegistrationAddressResponse(addressline, addressline2, addressline3, addressline4, postcode, countrycode)
  }

  implicit val arbitraryOrgRegistrationInfo: Arbitrary[OrgRegistrationInfo] = Arbitrary {
    for {
      idNumber        <- nonEmptyString
      name            <- arbitrary[Name]
      addressResponse <- arbitrary[RegistrationAddressResponse]
    } yield OrgRegistrationInfo(SafeId(idNumber), name.fullName, addressResponse)
  }

  implicit val arbitraryIndRegistrationInfo: Arbitrary[IndRegistrationInfo] = Arbitrary {
    nonEmptyString
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
      id <- nonEmptyString
    } yield UniqueTaxpayerReference(id)
  }

  // Line holder for template scripts
}
