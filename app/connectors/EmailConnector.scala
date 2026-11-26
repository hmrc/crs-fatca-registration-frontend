/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import cats.data.EitherT
import com.google.inject.Inject
import config.FrontendAppConfig
import models.email.{EmailNotSent, EmailSent, EmailStatus, SendEmailRequest}
import models.error.ApiError
import models.error.ApiError.InternalServerError
import play.api.Logging
import play.api.http.Status.ACCEPTED
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailConnector @Inject() (
  appConfig: FrontendAppConfig,
  httpV2Client: HttpClientV2
) extends Logging {

  def sendEmail(
    emailAddress: String,
    templateName: String,
    templateParams: Map[String, String]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, EmailStatus] =
    EitherT {
      val url = url"${appConfig.emailUrl}"
      httpV2Client
        .post(url)
        .withBody(
          Json.toJson(
            SendEmailRequest(
              to = List(emailAddress),
              templateId = templateName,
              parameters = templateParams,
              force = appConfig.emailSendForce
            )
          )
        )
        .execute[HttpResponse]
        .map {
          response =>
            response.status match {
              case ACCEPTED =>
                logger.info("EmailConnector: Email sent successfully")
                Right(EmailSent)
              case status =>
                logger.error(s"EmailConnector: Sending email failed with response status $status")
                Right(EmailNotSent)
            }
        }
        .recover {
          case t: Throwable =>
            logger.error("EmailConnector: Unable to connect to Email Service")
            Left(InternalServerError)
        }
    }

}
