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

import helpers.JsonFixtures._
import models.ReporterType.Partnership
import models.register.request.details.AddressRequest
import models.register.response._
import models.register.response.details.{AddressResponse, IndividualResponse, OrganisationResponse}
import models.shared.{ContactDetails, Parameters, ResponseCommon}

object RegisterHelper {

  val addressRequest: AddressRequest = AddressRequest("100 Parliament Street", None, "", Some("London"), Some("SW1A 2BQ"), "GB")

  val addressResponse: AddressResponse = AddressResponse("100 Parliament Street", None, None, Some("London"), Some("SW1A 2BQ"), "GB")

  val contactDetails: ContactDetails = ContactDetails(Some("1111111"), Some("2222222"), Some("1111111"), Some(TestEmail))

  val registrationWithIDIndividualResponse: RegistrationWithIDResponse = RegistrationWithIDResponse(
    RegisterWithIDResponse(
      ResponseCommon("OK", Some("Sample status text"), "2016-08-16T15:55:30Z", Some(Vector(Parameters("SAP_NUMBER", "0123456789")))),
      Some(
        RegisterWithIDResponseDetail(
          safeId,
          Some("WARN8764123"),
          isEditable = true,
          isAnAgent = false,
          isAnASAgent = None,
          isAnIndividual = true,
          partnerDetails = IndividualResponse("Ron", Some("Madisson"), "Burgundy", Some("1980-12-12")),
          address = addressResponse,
          contactDetails = contactDetails
        )
      )
    )
  )

  val registrationWithIDOrganisationResponse: RegistrationWithIDResponse = RegistrationWithIDResponse(
    RegisterWithIDResponse(
      ResponseCommon("OK", Some("Sample status text"), "2016-08-16T15:55:30Z", Some(Vector(Parameters("SAP_NUMBER", "0123456789")))),
      Some(
        RegisterWithIDResponseDetail(
          safeId,
          Some("WARN8764123"),
          isEditable = true,
          isAnAgent = false,
          isAnASAgent = None,
          isAnIndividual = true,
          partnerDetails = OrganisationResponse(OrgName, isAGroup = false, organisationType = Some(Partnership.code), code = None),
          address = addressResponse,
          contactDetails = contactDetails
        )
      )
    )
  )

}
