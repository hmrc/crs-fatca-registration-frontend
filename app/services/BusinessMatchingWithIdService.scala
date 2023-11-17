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

package services

import cats.implicits.catsStdInstancesForFuture
import connectors.RegistrationConnector
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo}
import models.register.request.RegisterWithID
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingWithIdService @Inject() (registrationConnector: RegistrationConnector) {

  def sendBusinessRegistrationInformation(registerWithID: RegisterWithID)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, OrgRegistrationInfo]] =
    registrationConnector
      .withOrganisationUtr(registerWithID)
      .subflatMap {
        response =>
          (for {
            safeId  <- response.safeId
            name    <- response.name
            address <- response.address
          } yield OrgRegistrationInfo(safeId, name, address)).toRight(MandatoryInformationMissingError())
      }
      .value

  def sendIndividualRegistrationInformation(registerWithID: RegisterWithID)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, IndRegistrationInfo]] =
    registrationConnector
      .withIndividualNino(registerWithID)
      .subflatMap {
        response =>
          response.safeId
            .map {
              safeId => IndRegistrationInfo(safeId)
            }
            .toRight(MandatoryInformationMissingError())
      }
      .value

}
