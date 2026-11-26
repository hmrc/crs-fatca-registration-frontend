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

import base.SpecBase
import cats.data.EitherT
import connectors.EmailConnector
import generators.ModelGenerators
import models.email.{ContactEmailInfo, EmailNotSent, EmailSent, EmailStatus}
import models.error.ApiError
import models.error.ApiError.InternalServerError
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.MockitoSugar.reset
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EmailServiceSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val mockEmailConnector: EmailConnector = mock[EmailConnector]

  private val application = applicationBuilder()
    .overrides(bind[EmailConnector].toInstance(mockEmailConnector))

  override def beforeEach(): Unit = {
    reset(mockEmailConnector)
    super.beforeEach()
  }

  val service: EmailService = application.injector().instanceOf[EmailService]

  private inline val expectedTemplateId = "crs_fatca_registration_successful"
  private val primaryContact            = ContactEmailInfo(name = "Ben White", email = "ben@example.com")
  private val secondaryContact          = ContactEmailInfo(name = "John Smith", email = "john@example.com")

  "EmailService" - {
    "sendEmails" - {
      "must return Right(()) without calling the connector when contacts list is empty" in {

        val result = service.sendEmails(contacts = Nil).value.futureValue

        result mustBe Right(())

        verify(mockEmailConnector, never()).sendEmail(any(), any(), any())(any[HeaderCarrier](), any[ExecutionContext]())
      }

      "must send emails to all contacts and return Right(()) when all succeed" in {
        when(
          mockEmailConnector.sendEmail(
            eqTo(primaryContact.email),
            eqTo(expectedTemplateId),
            eqTo(Map("name" -> primaryContact.name))
          )(any[HeaderCarrier](), any[ExecutionContext]())
        ).thenReturn(EitherT.rightT[Future, ApiError](EmailSent))

        when(
          mockEmailConnector.sendEmail(
            eqTo(secondaryContact.email),
            eqTo(expectedTemplateId),
            eqTo(Map("name" -> secondaryContact.name))
          )(any[HeaderCarrier](), any[ExecutionContext]())
        ).thenReturn(EitherT.rightT[Future, ApiError](EmailSent))

        val result = service.sendEmails(contacts = List(primaryContact, secondaryContact)).value.futureValue

        result mustBe Right(())

        verify(mockEmailConnector, times(1)).sendEmail(
          eqTo(primaryContact.email),
          eqTo(expectedTemplateId),
          eqTo(Map("name" -> primaryContact.name))
        )(any[HeaderCarrier](), any[ExecutionContext]())

        verify(mockEmailConnector, times(1)).sendEmail(
          eqTo(secondaryContact.email),
          eqTo(expectedTemplateId),
          eqTo(Map("name" -> secondaryContact.name))
        )(any[HeaderCarrier](), any[ExecutionContext]())
      }

      "must return Right(()) even when one or more emails return EmailNotSent" in {
        when(
          mockEmailConnector.sendEmail(
            eqTo(primaryContact.email),
            eqTo(expectedTemplateId),
            eqTo(Map("name" -> primaryContact.name))
          )(any[HeaderCarrier](), any[ExecutionContext]())
        ).thenReturn(EitherT.rightT[Future, ApiError](EmailSent))

        when(
          mockEmailConnector.sendEmail(
            eqTo(secondaryContact.email),
            eqTo(expectedTemplateId),
            eqTo(Map("name" -> secondaryContact.name))
          )(any[HeaderCarrier](), any[ExecutionContext]())
        ).thenReturn(EitherT.rightT[Future, ApiError](EmailNotSent))

        val result = service.sendEmails(
          contacts = List(primaryContact, secondaryContact)
        ).value.futureValue

        result mustBe Right(())

        verify(mockEmailConnector, times(2)).sendEmail(any(), any(), any())(any[HeaderCarrier](), any[ExecutionContext]())
      }

      "must return Left(ApiError) when the connector fails with an error" in {
        when(
          mockEmailConnector.sendEmail(
            eqTo(primaryContact.email),
            eqTo(expectedTemplateId),
            eqTo(Map("name" -> primaryContact.name))
          )(any[HeaderCarrier](), any[ExecutionContext]())
        ).thenReturn(EitherT.leftT[Future, EmailStatus](InternalServerError))

        val result = service.sendEmails(contacts = List(primaryContact, secondaryContact)).value.futureValue

        result mustBe Left(InternalServerError)
      }
    }
  }

}
