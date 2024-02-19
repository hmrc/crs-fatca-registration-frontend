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
import models.subscription.request._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Nino

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

  implicit val arbitraryCreateRequestDetail: Arbitrary[CreateSubscriptionDetailsRequest] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
    } yield CreateSubscriptionDetailsRequest(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitrarySubscriptionRequest: Arbitrary[SubscriptionRequest] = Arbitrary {
    for {
      requestDetails <- arbitrary[CreateSubscriptionDetailsRequest]
    } yield SubscriptionRequest(requestDetails)
  }

  implicit val arbitraryCreateSubscriptionRequest: Arbitrary[CreateSubscriptionRequest] = Arbitrary {
    for {
      subscriptionRequest <- arbitrary[SubscriptionRequest]
    } yield CreateSubscriptionRequest(subscriptionRequest)
  }

  implicit val arbitraryRequestDetail: Arbitrary[RequestDetail] = Arbitrary {
    for {
      idType   <- arbitrary[String]
      idNumber <- arbitrary[String]
    } yield RequestDetail(idType, idNumber)
  }

  implicit val arbitraryReadSubscriptionRequest: Arbitrary[ReadSubscriptionRequest] = Arbitrary {
    for {
      requestDetails <- arbitrary[RequestDetail]
    } yield ReadSubscriptionRequest(requestDetails)
  }

  implicit val arbitraryDisplaySubscriptionRequest: Arbitrary[DisplaySubscriptionRequest] = Arbitrary {
    for {
      subscriptionRequest <- arbitrary[ReadSubscriptionRequest]
    } yield DisplaySubscriptionRequest(subscriptionRequest)
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
        addressLine1 <- arbitrary[String]
        addressLine2 <- arbitrary[Option[String]]
        addressLine3 <- arbitrary[String]
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
        firstName <- arbitrary[String]
        lastName  <- arbitrary[String]
      } yield models.Name(firstName, lastName)
    }

  implicit val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    for {
      prefix <- Gen.oneOf(Nino.validPrefixes)
      number <- Gen.choose(minimumNumber, maximumNumber)
      suffix <- Gen.oneOf(Nino.validSuffixes)
    } yield Nino(f"$prefix$number%06d$suffix")
  }

//Line holder for template scripts
}
