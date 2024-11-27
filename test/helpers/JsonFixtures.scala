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

package helpers

import base.TestValues
import models.IdentifierType._
import models.ReporterType.Partnership
import play.api.libs.json.{JsNull, JsObject, JsString, Json}

object JsonFixtures extends TestValues {

  val registerWithIDPayloadJson: String =
    s"""
      |{
      |"registerWithIDRequest": {
      |"requestCommon": {
      |"regime": "CRFA",
      |"receiptDate": "2016-08-16T15:55:30Z",
      |"acknowledgementReference": "ec031b045855445e96f98a569ds56cd2",
      |"requestParameters": [
      |{
      |"paramName": "REGIME",
      |"paramValue": "CRFA"
      |}
      |]
      |},
      |"requestDetail": {
      |"IDType": "$NINO",
      |"IDNumber": "$TestNiNumber",
      |"requiresNameMatch": true,
      |"isAnAgent": false,
      |"$IndividualKey": {
      |"firstName": "${individualContactName.firstName}",
      |"middleName": "$MiddleName",
      |"lastName": "${individualContactName.lastName}",
      |"dateOfBirth": "$TestDate"
      |}
      |}
      |}
      |}""".stripMargin

  val registerWithIDJson: JsObject = Json.obj(
    "registerWithIDRequest" -> Json.obj(
      "requestCommon" -> Json.obj(
        "regime"                   -> "CRFA",
        "receiptDate"              -> "2016-08-16T15:55:30Z",
        "acknowledgementReference" -> "ec031b045855445e96f98a569ds56cd2",
        "requestParameters" -> Json.arr(
          Json.obj(
            "paramName"  -> "REGIME",
            "paramValue" -> "CRFA"
          )
        )
      ),
      "requestDetail" -> Json.obj(
        "IDType"            -> NINO,
        "IDNumber"          -> TestNiNumber,
        "requiresNameMatch" -> true,
        "isAnAgent"         -> false,
        IndividualKey -> Json.obj(
          "firstName"   -> individualContactName.firstName,
          "middleName"  -> MiddleName,
          "lastName"    -> individualContactName.lastName,
          "dateOfBirth" -> TestDate
        )
      )
    )
  )

  val withIDIndividualResponse: String =
    s"""
      |{
      |"registerWithIDResponse": {
      |"responseCommon": {
      |"status": "OK",
      |"statusText": "Sample status text",
      |"processingDate": "2016-08-16T15:55:30Z",
      |"returnParameters": [
      |{
      |"paramName":
      |"SAP_NUMBER",
      |"paramValue":
      |"0123456789"
      |}
      |]
      |},
      |"responseDetail": {
      |"SAFEID": "${safeId.value}",
      |"ARN": "WARN8764123",
      |"isEditable": true,
      |"isAnAgent": false,
      |"isAnIndividual": true,
      |"$IndividualKey": {
      |"firstName": "Ron",
      |"middleName":
      |"Madisson",
      |"lastName": "Burgundy",
      |"dateOfBirth":
      |"1980-12-12"
      |},
      |"address": {
      |"addressLine1": "100 Parliament Street",
      |"addressLine4": "London",
      |"postalCode": "SW1A 2BQ",
      |"countryCode": "GB"
      |},
      |"contactDetails": {
      |"phoneNumber":
      |"1111111",
      |"mobileNumber":
      |"2222222",
      |"faxNumber":
      |"1111111",
      |"emailAddress":
      |"$TestEmail"
      |}
      |}
      |}
      |}""".stripMargin

  val withIDOrganisationResponse: String =
    s"""
      |{
      |"registerWithIDResponse": {
      |"responseCommon": {
      |"status": "OK",
      |"statusText": "Sample status text",
      |"processingDate": "2016-08-16T15:55:30Z",
      |"returnParameters": [
      |{
      |"paramName":
      |"SAP_NUMBER",
      |"paramValue":
      |"0123456789"
      |}
      |]
      |},
      |"responseDetail": {
      |"SAFEID": "${safeId.value}",
      |"ARN": "WARN8764123",
      |"isEditable": true,
      |"isAnAgent": false,
      |"isAnIndividual": true,
      |"$OrganisationKey": {
      |"organisationName": "$OrgName",
      |"isAGroup": false,
      |"organisationType": "${Partnership.code}"
      |},
      |"address": {
      |"addressLine1": "100 Parliament Street",
      |"addressLine4": "London",
      |"postalCode": "SW1A 2BQ",
      |"countryCode": "GB"
      |},
      |"contactDetails": {
      |"phoneNumber":
      |"1111111",
      |"mobileNumber":
      |"2222222",
      |"faxNumber":
      |"1111111",
      |"emailAddress":
      |"$TestEmail"
      |}
      |}
      |}
      |}""".stripMargin

  val withoutIDResponse: String =
    s"""{
      |"registerWithoutIDResponse": {
      |"responseCommon":{
      |"status": "OK",
      |"statusText": "Sample status text",
      |"processingDate": "2016-08-16T15:55:30Z",
      |"returnParameters": [
      |{
      |"paramName":
      |"SAP_NUMBER",
      |"paramValue":
      |"0123456789"
      |}
      |]
      |},
      |"responseDetail":{
      |"SAFEID": "${safeId.value}",
      |"ARN": "WARN8764123"
      |}
      |}
      |}""".stripMargin

  val withIDResponseJson: JsObject = Json.obj(
    "registerWithIDResponse" -> Json.obj(
      "responseCommon" -> Json.obj(
        "status"         -> "OK",
        "statusText"     -> "Sample status text",
        "processingDate" -> "2016-08-16T15:55:30Z",
        "returnParameters" -> Json.arr(
          Json.obj(
            "paramName"  -> "SAP_NUMBER",
            "paramValue" -> "0123456789"
          )
        )
      ),
      "responseDetail" -> Json.obj(
        "SAFEID"         -> safeId.value,
        "ARN"            -> "WARN8764123",
        "isEditable"     -> true,
        "isAnAgent"      -> false,
        "isAnIndividual" -> true,
        "isAnASAgent"    -> JsNull,
        IndividualKey -> Json.obj(
          "firstName"   -> "Ron",
          "middleName"  -> "Madisson",
          "lastName"    -> "Burgundy",
          "dateOfBirth" -> "1980-12-12"
        ),
        "address" -> Json.obj(
          "addressLine1" -> "100 Parliament Street",
          "addressLine4" -> "London",
          "postalCode"   -> "SW1A 2BQ",
          "countryCode"  -> "GB"
        ),
        "contactDetails" -> Json.obj(
          "phoneNumber"  -> "1111111",
          "mobileNumber" -> "2222222",
          "faxNumber"    -> "1111111",
          "emailAddress" -> TestEmail
        )
      )
    )
  )

  val requestCouldNotBeProcessedResponse: String =
    """
      |{
      |  "errorDetail": {
      |    "source": "Back End",
      |    "timestamp": "2020-11-11T13:19:52.307Z",
      |    "errorMessage": "Request could not be processed",
      |    "errorCode": "503",
      |    "correlationId": "36147652-e594-94a4-a229-23f28e20e841",
      |    "sourceFaultDetail": {
      |      "detail": [
      |        "003 - Request could not be processed"
      |      ]
      |    }
      |  }
      |}""".stripMargin

  val badRequestResponse: String =
    """
      |{
      |  "errorDetail": {
      |    "timestamp" : "2017-02-14T12:58:44Z",
      |    "correlationId": "c181e730-2386-4359-8ee0-f911d6e5f3bc",
      |    "errorCode": "016",
      |    "errorMessage": "Invalid ID",
      |    "source": "Back End",
      |    "sourceFaultDetail":{
      |      "detail":[
      |        "001 - Regime missing or invalid"
      |      ]
      |    }
      | }
      |}""".stripMargin

  /** *******************For creating an EIS subscription below ********************
    */

  def jsonPayloadForInd(firstName: JsString, lastName: JsString, primaryEmail: JsString): String =
    s"""
       |{
       |  "createSubscriptionRequest": {
       |    "requestCommon": {
       |      "regime": "CRFA",
       |      "receiptDate": "2020-09-23T16:12:11Z",
       |      "acknowledgementReference": "AB123c",
       |      "originatingSystem": "MDTP",
       |      "requestParameters": [{
       |        "paramName":"Name",
       |        "paramValue":"Value"
       |      }]
       |    },
       |    "requestDetail": {
       |      "IDType": "idType",
       |      "IDNumber": "idNumber",
       |      "isGBUser": true,
       |      "primaryContact": {
       |        "$IndividualKey": {
       |          "firstName": $firstName,
       |          "lastName": $lastName
       |        },
       |        "email": $primaryEmail
       |      }
       |    }
       |  }
       |}
       |""".stripMargin

  def jsonPayloadForOrg(organisationName: JsString, primaryEmail: JsString, phone: JsString): String =
    s"""
      |{
      |  "createSubscriptionRequest": {
      |    "requestCommon": {
      |      "regime": "CRFA",
      |      "receiptDate": "2020-09-23T16:12:11Z",
      |      "acknowledgementReference": "AB123c",
      |      "originatingSystem": "MDTP",
      |      "requestParameters": [{
      |        "paramName":"Name",
      |        "paramValue":"Value"
      |      }]
      |    },
      |    "requestDetail": {
      |      "IDType": "idType",
      |      "IDNumber": "idNumber",
      |      "isGBUser": true,
      |      "primaryContact": {
      |        "$OrganisationKey": {
      |          "organisationName": $organisationName
      |        },
      |        "email": $primaryEmail,
      |        "phone": $phone,
      |        "mobile": $phone
      |      }
      |    }
      |  }
      |}
      |""".stripMargin

  def jsonPayloadForIndWithSecondaryContact(firstName: JsString,
                                            lastName: JsString,
                                            organisationName: JsString,
                                            primaryEmail: JsString,
                                            secondaryEmail: JsString,
                                            phone: JsString
  ): String =
    s"""
       |{
       |  "createSubscriptionRequest": {
       |    "requestCommon": {
       |      "regime": "CRFA",
       |      "receiptDate": "2020-09-23T16:12:11Z",
       |      "acknowledgementReference": "AB123c",
       |      "originatingSystem": "MDTP"
       |    },
       |    "requestDetail": {
       |      "IDType": "idType",
       |      "IDNumber": "idNumber",
       |      "isGBUser": true,
       |      "primaryContact": {
       |        "$IndividualKey": {
       |          "firstName": $firstName,
       |          "lastName": $lastName
       |        },
       |        "email": $primaryEmail
       |      },
       |      "secondaryContact": {
       |        "$OrganisationKey": {
       |          "organisationName": $organisationName
       |        },
       |        "email": $secondaryEmail,
       |        "phone": $phone,
       |        "mobile": $phone
       |      }
       |    }
       |  }
       |}
       |""".stripMargin

  def jsonPayloadForOrgWithSecondaryContact(firstName: JsString,
                                            lastName: JsString,
                                            organisationName: JsString,
                                            primaryEmail: JsString,
                                            secondaryEmail: JsString,
                                            phone: JsString
  ): String =
    s"""
       |{
       |  "createSubscriptionRequest": {
       |    "requestCommon": {
       |      "regime": "CRFA",
       |      "receiptDate": "2020-09-23T16:12:11Z",
       |      "acknowledgementReference": "AB123c",
       |      "originatingSystem": "MDTP"
       |    },
       |    "requestDetail": {
       |      "IDType": "idType",
       |      "IDNumber": "idNumber",
       |      "isGBUser": true,
       |      "primaryContact": {
       |        "$OrganisationKey": {
       |          "organisationName": $organisationName
       |        },
       |        "email": $primaryEmail,
       |        "phone": $phone,
       |        "mobile": $phone
       |      },
       |      "secondaryContact": {
       |        "$IndividualKey": {
       |          "firstName": $firstName,
       |          "lastName": $lastName
       |        },
       |        "email": $secondaryEmail
       |      }
       |    }
       |  }
       |}
       |""".stripMargin

  def indRequestJson(firstName: String, lastName: String, primaryEmail: String): JsObject =
    Json.obj(
      "createSubscriptionRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
          "regime"                   -> "CRFA",
          "receiptDate"              -> "2020-09-23T16:12:11Z",
          "acknowledgementReference" -> "AB123c",
          "originatingSystem"        -> "MDTP",
          "requestParameters" -> Json.arr(
            Json.obj(
              "paramName"  -> "Name",
              "paramValue" -> "Value"
            )
          )
        ),
        "requestDetail" -> Json.obj(
          "IDType"   -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            IndividualKey -> Json.obj(
              "firstName" -> firstName,
              "lastName"  -> lastName
            ),
            "email" -> primaryEmail
          )
        )
      )
    )

  def orgRequestJson(organisationName: String, primaryEmail: String, phone: String): JsObject =
    Json.obj(
      "createSubscriptionRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
          "regime"                   -> "CRFA",
          "receiptDate"              -> "2020-09-23T16:12:11Z",
          "acknowledgementReference" -> "AB123c",
          "originatingSystem"        -> "MDTP",
          "requestParameters" -> Json.arr(
            Json.obj(
              "paramName"  -> "Name",
              "paramValue" -> "Value"
            )
          )
        ),
        "requestDetail" -> Json.obj(
          "IDType"   -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            OrganisationKey -> Json.obj(
              "organisationName" -> organisationName
            ),
            "email"  -> primaryEmail,
            "phone"  -> phone,
            "mobile" -> phone
          )
        )
      )
    )

  def indWithSecondaryContactJson(firstName: String,
                                  lastName: String,
                                  organisationName: String,
                                  primaryEmail: String,
                                  secondaryEmail: String,
                                  phone: String
  ): JsObject =
    Json.obj(
      "createSubscriptionRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
          "regime"                   -> "CRFA",
          "receiptDate"              -> "2020-09-23T16:12:11Z",
          "acknowledgementReference" -> "AB123c",
          "originatingSystem"        -> "MDTP"
        ),
        "requestDetail" -> Json.obj(
          "IDType"   -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            IndividualKey -> Json.obj(
              "firstName" -> firstName,
              "lastName"  -> lastName
            ),
            "email" -> primaryEmail
          ),
          "secondaryContact" -> Json.obj(
            OrganisationKey -> Json.obj(
              "organisationName" -> organisationName
            ),
            "email"  -> secondaryEmail,
            "phone"  -> phone,
            "mobile" -> phone
          )
        )
      )
    )

  def orgWithSecondaryContactJson(firstName: String,
                                  lastName: String,
                                  organisationName: String,
                                  primaryEmail: String,
                                  secondaryEmail: String,
                                  phone: String
  ): JsObject =
    Json.obj(
      "createSubscriptionRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
          "regime"                   -> "CRFA",
          "receiptDate"              -> "2020-09-23T16:12:11Z",
          "acknowledgementReference" -> "AB123c",
          "originatingSystem"        -> "MDTP"
        ),
        "requestDetail" -> Json.obj(
          "IDType"   -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            OrganisationKey -> Json.obj(
              "organisationName" -> organisationName
            ),
            "email"  -> primaryEmail,
            "phone"  -> phone,
            "mobile" -> phone
          ),
          "secondaryContact" -> Json.obj(
            IndividualKey -> Json.obj(
              "firstName" -> firstName,
              "lastName"  -> lastName
            ),
            "email" -> secondaryEmail
          )
        )
      )
    )

  //// Without ID

  val registerWithoutIDPayloadJson: String =
    s"""
      |{
      |"registerWithoutIDRequest": {
      |"requestCommon": {
      |"regime": "CRFA",
      |"receiptDate": "2016-08-16T15:55:30Z",
      |"acknowledgementReference": "ec031b045855445e96f98a569ds56cd2",
      |"requestParameters": [
      |{
      |"paramName": "REGIME",
      |"paramValue": "CRFA"
      |}
      |]
      |},
      |"requestDetail": {
      |"$IndividualKey": {
      |"firstName": "${individualContactName.firstName}",
      |"lastName": "${individualContactName.lastName}",
      |"dateOfBirth": "$TestDate"
      |},
      |"address": {
      |"addressLine1": "line 1",
      |"addressLine2": "line 2",
      |"addressLine3": "line 3",
      |"addressLine4": "line 4",
      |"postalCode": "SW1A 2BQ",
      |"countryCode": "GB"
      |},
      |"contactDetails": {
      |"phoneNumber": "111111",
      |"mobileNumber": "222222",
      |"faxNumber": "333333",
      |"emailAddress": "$TestEmail"
      |},
      |"identification": {
      |"idNumber": "",
      |"issuingInstitution": "",
      |"issuingCountryCode": ""
      |}
      |}
      |}
      |}""".stripMargin

  val registerWithoutIDJson: JsObject = Json.obj(
    "registerWithoutIDRequest" -> Json.obj(
      "requestCommon" -> Json.obj(
        "regime"                   -> "CRFA",
        "receiptDate"              -> "2016-08-16T15:55:30Z",
        "acknowledgementReference" -> "ec031b045855445e96f98a569ds56cd2",
        "requestParameters" -> Json.arr(
          Json.obj(
            "paramName"  -> "REGIME",
            "paramValue" -> "CRFA"
          )
        )
      ),
      "requestDetail" -> Json.obj(
        IndividualKey -> Json.obj(
          "firstName"   -> individualContactName.firstName,
          "lastName"    -> individualContactName.lastName,
          "dateOfBirth" -> TestDate
        ),
        "address" -> Json.obj(
          "addressLine1" -> "line 1",
          "addressLine2" -> "line 2",
          "addressLine3" -> "line 3",
          "addressLine4" -> "line 4",
          "postalCode"   -> "SW1A 2BQ",
          "countryCode"  -> "GB"
        ),
        "contactDetails" -> Json.obj(
          "phoneNumber"  -> "111111",
          "mobileNumber" -> "222222",
          "faxNumber"    -> "333333",
          "emailAddress" -> TestEmail
        ),
        "identification" -> Json.obj(
          "idNumber"           -> "",
          "issuingInstitution" -> "",
          "issuingCountryCode" -> ""
        )
      )
    )
  )

  val registerWithoutIDResponse: String =
    s"""
      |{
      |  "registerWithoutIDResponse": {
      |    "responseCommon": {
      |      "status": "OK",
      |      "statusText": "Success",
      |      "processingDate": "2020-09-01T01:00:00Z"
      |    },
      |    "responseDetail": {
      |      "SAFEID": "${safeId.value}"
      |    }
      |  }
      |}
      |""".stripMargin

  val registerWithoutIDResponseJson: JsObject = Json.obj(
    "registerWithoutIDResponse" -> Json.obj(
      "responseCommon" -> Json.obj(
        "status"         -> "OK",
        "statusText"     -> "Success",
        "processingDate" -> "2020-09-01T01:00:00Z"
      ),
      "responseDetail" -> Json.obj(
        "SAFEID" -> safeId.value
      )
    )
  )

}
