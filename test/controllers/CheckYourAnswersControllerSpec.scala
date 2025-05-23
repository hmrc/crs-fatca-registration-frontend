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

package controllers

import base.{ControllerMockFixtures, SpecBase}
import cats.data.EitherT
import cats.implicits._
import connectors.AddressLookupConnector
import generators.{ModelGenerators, UserAnswersGenerator}
import helpers.JsonFixtures._
import models.ReporterType.{Individual, LimitedCompany, Sole}
import models.enrolment.GroupIds
import models.error.ApiError
import models.error.ApiError._
import models.{Address, Country, ReporterType, SubscriptionID, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BusinessMatchingWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.ThereIsAProblemView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach with TableDrivenPropertyChecks
    with ScalaCheckPropertyChecks with ModelGenerators with UserAnswersGenerator {

  final val mockRegistrationService: BusinessMatchingWithoutIdService = mock[BusinessMatchingWithoutIdService]

  lazy val loadRoute: String   = routes.CheckYourAnswersController.onPageLoad().url
  lazy val submitRoute: String = routes.CheckYourAnswersController.onSubmit().url

  // first contact
  val firstContactName  = "first-contact-name"
  val firstContactEmail = "first-contact-email"
  val firstContactPhone = "+44 0000 000 0000"

  val secondContactName                             = "second-contact-name"
  val secondContactEmail                            = "second-contact-email"
  val secondContactPhone                            = "+44 0808 157 0193"
  val mockSubscriptionService: SubscriptionService  = mock[SubscriptionService]
  val mockTaxEnrolmentsService: TaxEnrolmentService = mock[TaxEnrolmentService]

  override def beforeEach(): Unit = {
    reset(mockSubscriptionService, mockRegistrationService, mockTaxEnrolmentsService)
    super.beforeEach()
  }

  val address: Address = Address("line 1", Some("line 2"), "line 3", Some("line 4"), Some(""), Country.GB)

  "CheckYourAnswers Controller" - {

    "handleErrorResult" - {
      lazy val application: Application          = new GuiceApplicationBuilder().build()
      val controller: CheckYourAnswersController = application.injector.instanceOf[CheckYourAnswersController]

      def callHandleErrorResult(errorResult: EitherT[Future, ApiError, Result], reporterType: ReporterType) = {
        val messages: Messages           = stubMessages()
        val requestHeader: RequestHeader = FakeRequest()
        val handleErrorResult            = PrivateMethod[Future[Result]](Symbol("handleErrorResult"))

        controller.invokePrivate(handleErrorResult(
          errorResult,
          emptyUserAnswers.withPage(ReporterTypePage, reporterType),
          messages,
          requestHeader
        ))
      }

      "redirect to InformationMissing when MandatoryInformationMissingError occurs" in {
        val errorResult: EitherT[Future, ApiError, Result] = EitherT.leftT[Future, Result](MandatoryInformationMissingError(""))
        val result                                         = callHandleErrorResult(errorResult, LimitedCompany)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.InformationMissingController.onPageLoad().url)
      }
      "redirect to IndividualAlreadyRegistered when AlreadyRegisteredError occurs for a Sole Trader" in {
        val errorResult: EitherT[Future, ApiError, Result] = EitherT.leftT[Future, Result](AlreadyRegisteredError)
        val result =
          callHandleErrorResult(errorResult, Sole)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad().url)
      }
      "redirect to IndividualAlreadyRegistered when AlreadyRegisteredError occurs for a Individual" in {
        val errorResult: EitherT[Future, ApiError, Result] = EitherT.leftT[Future, Result](AlreadyRegisteredError)
        val result =
          callHandleErrorResult(errorResult, Individual)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad().url)
      }
      "redirect to PreRegistered when AlreadyRegisteredError occurs for an Organisation" in {
        val errorResult: EitherT[Future, ApiError, Result] = EitherT.leftT[Future, Result](AlreadyRegisteredError)
        val result =
          callHandleErrorResult(errorResult, LimitedCompany)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.PreRegisteredController.onPageLoad().url)
      }
      "return ServiceUnavailable when ServiceUnavailableError occurs" in {
        val errorResult: EitherT[Future, ApiError, Result] = EitherT.leftT[Future, Result](ServiceUnavailableError)
        val result =
          callHandleErrorResult(errorResult, LimitedCompany)

        status(result) mustBe SERVICE_UNAVAILABLE
      }
      "return InternalServerError for any other ApiError" in {
        val errorResult: EitherT[Future, ApiError, Result] = EitherT.leftT[Future, Result](InternalServerError)
        val result =
          callHandleErrorResult(errorResult, LimitedCompany)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

    }

    "onPageLoad" - {
      "when affinity group is Individual" - {
        "must return OK and the correct view for a GET valid answers for individual with id" in {
          forAll(indWithId.arbitrary) {
            (userAnswers: UserAnswers) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), AffinityGroup.Individual)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
              }
          }
        }

        "redirect to Missing Information when missing some UserAnswers for individual with id" in {
          forAll(indWithIdMissingAnswers.arbitrary) {
            (userAnswers: UserAnswers) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), AffinityGroup.Individual)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustBe routes.InformationMissingController.onPageLoad().url
              }
          }
        }

        "must return OK and the correct view for a GET valid answers for individual without id" in {
          forAll(indWithoutId.arbitrary) {
            (userAnswers: UserAnswers) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), AffinityGroup.Individual)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
              }
          }
        }

        "redirect to Missing Information when missing some UserAnswers for individual without id" in {
          forAll(indWithoutIdMissingAnswers.arbitrary) {
            (userAnswers: UserAnswers) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), AffinityGroup.Individual)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustBe routes.InformationMissingController.onPageLoad().url
              }
          }
        }

        "must redirect to PageUnavailable when UserAnswers is empty" in {
          val application = applicationBuilder(userAnswers = Option(emptyUserAnswers), AffinityGroup.Individual)
            .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe routes.PageUnavailableController.onPageLoad().url
          }
        }
      }

      "when affinity group is not individual" - {

        "must return OK and the correct view for a GET valid answers for organisation or sole trader with id" in {
          forAll(orgWithId.arbitrary, arbitraryOrgAffinityGroup.arbitrary) {
            (userAnswers, affinityGroup) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
              }
          }
        }

        "must redirect to Missing Information when missing some UserAnswers for organisation with id" in {
          forAll(orgWithIdMissingAnswers.arbitrary, arbitraryOrgAffinityGroup.arbitrary) {
            (userAnswers, affinityGroup) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustBe routes.InformationMissingController.onPageLoad().url
              }
          }
        }

        "must return OK and the correct view for a GET valid answers for organisation or sole trader without id" in {
          forAll(orgWithoutId.arbitrary, arbitraryOrgAffinityGroup.arbitrary) {
            (userAnswers, affinityGroup) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
              }
          }
        }

        "must redirect to Missing Information when missing some UserAnswers for organisation without id" in {
          forAll(orgWithoutIdMissingAnswers.arbitrary, arbitraryOrgAffinityGroup.arbitrary) {
            (userAnswers, affinityGroup) =>
              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SubscriptionService].toInstance(mockSubscriptionService),
                  bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
                  bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
                )
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustBe routes.InformationMissingController.onPageLoad().url
              }
          }
        }

        "must redirect to PageUnavailable when UserAnswers is empty and affinity group is non-individual" in {
          forAll(arbitraryOrgAffinityGroup.arbitrary) {
            affinityGroup =>
              val application = applicationBuilder(userAnswers = Option(emptyUserAnswers), affinityGroup)
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustBe routes.PageUnavailableController.onPageLoad().url
              }
          }
        }

      }

    }

    "onSubmit" - {

      "must redirect to RegistrationConfirmationPage for Individual with Id" in {
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID(UserAnswersId))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
          .withPage(IndDoYouHaveNINumberPage, true)

        val application = applicationBuilder(Option(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
        }
      }

      "must redirect to RegistrationConfirmationPage for Business with Id" in {
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID(UserAnswersId))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers.withPage(DoYouHaveUniqueTaxPayerReferencePage, true)

        val application = applicationBuilder(userAnswers = Option(userAnswers), AffinityGroup.Organisation)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
        }
      }

      "must return ThereIsAProblemPage for Business with Id when tax enrolment fails" in {
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnableToCreateEnrolmentError)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any())).thenReturn(
          Future.successful(Right(SubscriptionID(UserAnswersId)))
        )
        val userAnswers = emptyUserAnswers.withPage(DoYouHaveUniqueTaxPayerReferencePage, true)

        val application = applicationBuilder(userAnswers = Option(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value
          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "must redirect to RegistrationConfirmationPage for Individual without Id" in {
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID(UserAnswersId))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
          .withPage(IndDoYouHaveNINumberPage, false)

        val application = applicationBuilder(Option(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
        }
      }

      "must redirect to RegistrationConfirmationPage for Business without Id" in {

        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID(UserAnswersId))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
          .withPage(IndDoYouHaveNINumberPage, false)

        val application = applicationBuilder(Option(userAnswers), AffinityGroup.Organisation)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
        }
      }

      "must redirect to MissingInformationPage when information is missing" in {

        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Left(MandatoryInformationMissingError())))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(Option(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.InformationMissingController.onPageLoad().url
        }
      }

      "must redirect to IndividualAlreadyRegisteredPage when there is EnrolmentExistsError and Affinity Group is Individual" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID(UserAnswersId))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(EnrolmentExistsError(GroupIds(Seq(UserAnswersId), Seq.empty)))))

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
          .withPage(IndDoYouHaveNINumberPage, true)

        val application = applicationBuilder(Option(userAnswers), AffinityGroup.Individual)
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService),
            bind[Navigator].toInstance(fakeNavigator),
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad().url
        }
      }

      "must redirect to BusinessAlreadyRegisteredPage when there is EnrolmentExistsError" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID(UserAnswersId))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(EnrolmentExistsError(GroupIds(Seq(UserAnswersId), Seq.empty)))))

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
          .withPage(IndDoYouHaveNINumberPage, true)

        val application = applicationBuilder(Option(userAnswers), AffinityGroup.Organisation)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.PreRegisteredController.onPageLoad().url
        }
      }

      "must return ThereIsAProblemPage when subscription creation fails with UnableToCreateEMTPSubscriptionError" in {
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnableToCreateEMTPSubscriptionError)))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
          .withPage(IndDoYouHaveNINumberPage, true)

        val application = applicationBuilder(Option(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {

          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ThereIsAProblemView]

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) mustEqual view()(request, messages).toString
        }
      }

      "must return ThereIsAProblemPage when subscription creation fails with ServiceUnavailableError" in {
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(ServiceUnavailableError)))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, false)
          .withPage(IndDoYouHaveNINumberPage, true)

        val application = applicationBuilder(Option(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, submitRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ThereIsAProblemView]

          status(result) mustEqual SERVICE_UNAVAILABLE
          contentAsString(result) mustEqual view()(request, messages).toString
        }
      }

      "must redirect to PreRegistered page when user is already registered" in {
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(AlreadyRegisteredError)))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(safeId)))

        val application = applicationBuilder(userAnswers = Option(emptyUserAnswers), AffinityGroup.Organisation)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService)
          )
          .build()
        running(application) {
          val request = FakeRequest(POST, submitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.PreRegisteredController.onPageLoad().url
        }
      }

      "must redirect to InformationMissingPage if both individual and organisation are not present" in {
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnableToCreateEMTPSubscriptionError)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockRegistrationService.registerWithoutId()(any(), any())).thenReturn(Future.successful(Left(MandatoryInformationMissingError())))

        val application = applicationBuilder(Option(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
            bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.InformationMissingController.onPageLoad().url
        }
      }
    }

  }

}
