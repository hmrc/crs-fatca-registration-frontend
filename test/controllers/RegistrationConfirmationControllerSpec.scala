/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import base.SpecBase
import generators.UserAnswersGenerator
import models.audit.AuditResult
import models.audit.AuditResult.AuditSent
import models.{SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar.{atLeastOnce, never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.SubscriptionIDPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.{PageUnavailableView, RegistrationConfirmationView}

import scala.concurrent.{Future, Promise}

class RegistrationConfirmationControllerSpec
    extends SpecBase
    with UserAnswersGenerator
    with BeforeAndAfterEach {

  private val mockAuditService: AuditService =
    mock[AuditService]

  override def beforeEach(): Unit = {
    reset(mockAuditService)

    when(
      mockAuditService.sendCreateRegistration(
        any(),
        any(),
        any()
      )(any())
    ).thenReturn(
      Future.successful(AuditSent)
    )

    super.beforeEach()
  }

  "RegistrationConfirmation Controller" - {

    "must return OK and the correct view for a GET with valid orgWithId userAnswers" in {

      forAll(
        orgWithId.arbitrary,
        arbitrarySubscriptionID.arbitrary
      ) {
        (
          userAnswers: UserAnswers,
          subscriptionId: SubscriptionID
        ) =>
          val answersWithSubscriptionId =
            userAnswers.withPage(
              SubscriptionIDPage,
              subscriptionId
            )

          val application =
            applicationBuilder(
              userAnswers = Some(
                answersWithSubscriptionId
              ),
              AffinityGroup.Organisation
            )
              .overrides(
                bind[AuditService]
                  .toInstance(mockAuditService)
              )
              .build()

          when(
            mockSessionRepository.set(
              eqTo(
                answersWithSubscriptionId.copy(
                  data = Json.obj()
                )
              )
            )
          ).thenReturn(
            Future.successful(true)
          )

          running(application) {
            val request =
              FakeRequest(
                GET,
                routes.RegistrationConfirmationController
                  .onPageLoad()
                  .url
              )

            val result =
              route(application, request).value

            val view =
              application.injector
                .instanceOf[
                  RegistrationConfirmationView
                ]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                subscriptionId.value
              )(
                request,
                messages(application)
              ).toString
          }
      }

      verify(
        mockAuditService,
        atLeastOnce
      ).sendCreateRegistration(
        userAnswers = any[UserAnswers],
        subscriptionId = any[SubscriptionID],
        affinityGroup =
          eqTo(AffinityGroup.Organisation)
      )(any())
    }

    "must return OK and the correct view for a GET with valid indWithId userAnswers" in {

      forAll(
        indWithId.arbitrary,
        arbitrarySubscriptionID.arbitrary
      ) {
        (
          userAnswers: UserAnswers,
          subscriptionId: SubscriptionID
        ) =>
          val answersWithSubscriptionId =
            userAnswers.withPage(
              SubscriptionIDPage,
              subscriptionId
            )

          val application =
            applicationBuilder(
              userAnswers = Some(
                answersWithSubscriptionId
              ),
              AffinityGroup.Individual
            )
              .overrides(
                bind[AuditService]
                  .toInstance(mockAuditService)
              )
              .build()

          when(
            mockSessionRepository.set(
              eqTo(
                answersWithSubscriptionId.copy(
                  data = Json.obj()
                )
              )
            )
          ).thenReturn(
            Future.successful(true)
          )

          running(application) {
            val request =
              FakeRequest(
                GET,
                routes.RegistrationConfirmationController
                  .onPageLoad()
                  .url
              )

            val result =
              route(application, request).value

            val view =
              application.injector
                .instanceOf[
                  RegistrationConfirmationView
                ]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                subscriptionId.value
              )(
                request,
                messages(application)
              ).toString
          }
      }

      verify(
        mockAuditService,
        atLeastOnce
      ).sendCreateRegistration(
        userAnswers = any[UserAnswers],
        subscriptionId = any[SubscriptionID],
        affinityGroup =
          eqTo(AffinityGroup.Individual)
      )(any())
    }

    "must return OK and PageUnavailable view for a GET when unable to empty user answers data" in {

      forAll(
        orgWithId.arbitrary,
        arbitrarySubscriptionID.arbitrary
      ) {
        (
          userAnswers: UserAnswers,
          subscriptionId: SubscriptionID
        ) =>
          val answersWithSubscriptionId =
            userAnswers.withPage(
              SubscriptionIDPage,
              subscriptionId
            )

          val application =
            applicationBuilder(
              userAnswers =
                Some(answersWithSubscriptionId),
              AffinityGroup.Organisation
            )
              .overrides(
                bind[AuditService]
                  .toInstance(mockAuditService)
              )
              .build()

          when(
            mockSessionRepository.set(
              any[UserAnswers]
            )
          ).thenReturn(
            Future.successful(false)
          )

          running(application) {
            val request =
              FakeRequest(
                GET,
                routes.RegistrationConfirmationController
                  .onPageLoad()
                  .url
              )

            val result =
              route(application, request).value

            val view =
              application.injector
                .instanceOf[PageUnavailableView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                routes.IndexController
                  .onPageLoad
                  .url
              )(
                request,
                messages(application)
              ).toString
          }
      }

      verify(
        mockAuditService,
        never
      ).sendCreateRegistration(
        any(),
        any(),
        any()
      )(any())
    }

    "must return OK and the PageUnavailable view for a GET when unable to find subscriptionId in user answers data" in {

      forAll(orgWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application =
            applicationBuilder(
              userAnswers = Some(userAnswers),
              AffinityGroup.Organisation
            )
              .overrides(
                bind[AuditService]
                  .toInstance(mockAuditService)
              )
              .build()

          when(
            mockSessionRepository.set(
              any[UserAnswers]
            )
          ).thenReturn(
            Future.successful(true)
          )

          running(application) {
            val request =
              FakeRequest(
                GET,
                routes.RegistrationConfirmationController
                  .onPageLoad()
                  .url
              )

            val result =
              route(application, request).value

            val view =
              application.injector
                .instanceOf[PageUnavailableView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                routes.IndexController
                  .onPageLoad
                  .url
              )(
                request,
                messages(application)
              ).toString
          }
      }

      verify(
        mockAuditService,
        never
      ).sendCreateRegistration(
        any(),
        any(),
        any()
      )(any())
    }

    "must not wait for the audit event to complete before returning the confirmation page" in {

      val subscriptionId =
        SubscriptionID("FATCA123456")

      val userAnswers =
        orgWithId.arbitrary.sample.value
          .withPage(
            SubscriptionIDPage,
            subscriptionId
          )

      val auditPromise =
        Promise[AuditResult]()

      when(
        mockAuditService.sendCreateRegistration(
          any(),
          any(),
          any()
        )(any())
      ).thenReturn(
        auditPromise.future
      )

      when(
        mockSessionRepository.set(
          eqTo(
            userAnswers.copy(
              data = Json.obj()
            )
          )
        )
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          AffinityGroup.Organisation
        )
          .overrides(
            bind[AuditService]
              .toInstance(mockAuditService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.RegistrationConfirmationController
              .onPageLoad()
              .url
          )

        val result =
          route(application, request).value

        val view =
          application.injector
            .instanceOf[RegistrationConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            subscriptionId.value
          )(
            request,
            messages(application)
          ).toString
      }
    }
  }

}
