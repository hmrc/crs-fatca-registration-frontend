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
import models.register.response.details.{AddressResponse => RegistrationAddressResponse}
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, RegistrationInfo, SafeId}
import models.subscription.request._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Nino
import wolfendale.scalacheck.regexp.RegexpGen

trait ModelGenerators {

  val maximumNumber = 999999
  val minimumNumber = 1
  val countryNumber = 2

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
      organisationName <- arbitrary[String]
    } yield OrganisationDetails(organisationName)
  }

  implicit val arbitraryIndividualDetails: Arbitrary[IndividualDetails] = Arbitrary {
    for {
      firstName  <- arbitrary[String]
      middleName <- Gen.option(arbitrary[String])
      lastName   <- arbitrary[String]
    } yield IndividualDetails(firstName, middleName, lastName)
  }

  implicit lazy val arbitraryContactInformation: Arbitrary[ContactType] = Arbitrary {
    Gen.oneOf[ContactType](arbitrary[OrganisationDetails], arbitrary[IndividualDetails])
  }

  implicit val arbitraryPrimaryContact: Arbitrary[ContactInformation] = Arbitrary {
    for {
      contactInformation <- arbitrary[ContactType]
      email              <- arbitrary[String]
      phone              <- Gen.option(arbitrary[String])
      mobile             <- Gen.option(arbitrary[String])
    } yield ContactInformation(contactInformation, email, phone, mobile)
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
      idType   <- arbitrary[String]
      idNumber <- arbitrary[String]
    } yield ReadSubscriptionRequest(idType, idNumber)
  }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        state <- Gen.oneOf(Seq("Valid", "Invalid"))
        code  <- Gen.pick(countryNumber, 'A' to 'Z')
        name  <- arbitrary[String]
      } yield Country(state, code.mkString, name)
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
      } yield AddressLookup(addressLine1, addressLine2, addressLine3, addressLine4, town, county, postCode)
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
    arbitrary[Address].map {
      address =>
        RegistrationAddressResponse(
          address.addressLine1,
          address.addressLine2,
          Option(address.addressLine3),
          address.addressLine4,
          address.postCode,
          address.country.code
        )
    }
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

//Line holder for template scripts
}
