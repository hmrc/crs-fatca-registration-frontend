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

package services

import cats.data.EitherT
import cats.implicits.*
import cats.syntax.all.*
import connectors.EmailConnector
import models.email.*
import models.error.ApiError
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService @Inject() (
  emailConnector: EmailConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  final private val regSuccessfulTemplateId = "crs_fatca_registration_successful"

  def sendEmails(contacts: List[ContactEmailInfo])(
    implicit hc: HeaderCarrier
  ): EitherT[Future, ApiError, Unit] =
    if (contacts.isEmpty) {
      logger.warn("EmailService: No contacts to send registration confirmation emails to")
      EitherT.pure[Future, ApiError](())
    } else {

      val emailTraversable = contacts.traverse {
        contact =>
          val parameters = Map("name" -> contact.name)
          emailConnector.sendEmail(contact.email, regSuccessfulTemplateId, parameters)
      }

      for {
        emailResults <- emailTraversable
      } yield {
        val successCount = emailResults.count(_ == EmailSent)
        val failureCount = emailResults.length - successCount

        if (failureCount > 0) {
          logger.warn(
            s"EmailService: Failed to send $failureCount out of ${emailResults.length} registration confirmation email(s)"
          )
        }

        logger.info(
          s"EmailService: Successfully sent $successCount out of ${emailResults.length} registration confirmation email(s)"
        )
      }
    }

}
