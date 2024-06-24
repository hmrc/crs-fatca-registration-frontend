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
import connectors.AddressLookupConnector
import generators.ModelGenerators
import helpers.JsonFixtures._
import models.enrolment.GroupIds
import models.error.ApiError._
import models.matching.IndRegistrationInfo
import models.{Address, Country, ReporterType, SubscriptionID, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BusinessMatchingWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.ThereIsAProblemView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach with TableDrivenPropertyChecks with ModelGenerators {

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

    "onPageLoad" - {
      "when affinity group is Individual" - {
        "must return OK and the correct view for a GET when contact has a phone number" in {
          val userAnswers: UserAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, ReporterType.Individual)
            .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
            .withPage(ContactNamePage, firstContactName)
            .withPage(ContactEmailPage, firstContactEmail)
            .withPage(ContactHavePhonePage, true)
            .withPage(ContactPhonePage, firstContactPhone)

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
            contentAsString(result).contains(firstContactName) mustBe true
            contentAsString(result).contains(firstContactEmail) mustBe false
            contentAsString(result).contains(firstContactPhone) mustBe false
          }
        }

        "must redirect to Information sent when UserAnswers is empty" in {
          val application = applicationBuilder(userAnswers = Option(emptyUserAnswers), AffinityGroup.Individual)
            .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe routes.InformationSentController.onPageLoad().url
          }
        }

        "must return OK and the correct view for a GET when contact does not have a phone number" in {
          val userAnswers: UserAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, ReporterType.Individual)
            .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
            .withPage(ContactNamePage, firstContactName)
            .withPage(ContactEmailPage, firstContactEmail)
            .withPage(ContactHavePhonePage, false)

          val application = applicationBuilder(userAnswers = Option(userAnswers), AffinityGroup.Individual)
            .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result).contains(firstContactName) mustBe true
            contentAsString(result).contains(firstContactEmail) mustBe false
            contentAsString(result).contains(firstContactPhone) mustBe false
          }
        }
      }

      "when affinity group is not individual" - {
        forAll(Table("nonIndividualAffinityGroup", Seq(AffinityGroup.Organisation, AffinityGroup.Agent): _*)) {
          affinityGroup =>
            s"must return OK and the correct view for a GET when first Contact has a phone number and affinity group $affinityGroup" in {
              val userAnswers: UserAnswers = emptyUserAnswers
                .withPage(ReporterTypePage, ReporterType.LimitedCompany)
                .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
                .withPage(ContactNamePage, firstContactName)
                .withPage(ContactEmailPage, firstContactEmail)
                .withPage(ContactPhonePage, firstContactPhone)

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
                contentAsString(result).contains(firstContactName) mustBe true
                contentAsString(result).contains(firstContactEmail) mustBe true
                contentAsString(result).contains(firstContactPhone) mustBe true
              }
            }

            s"must redirect to Information sent when UserAnswers is empty and affinity group $affinityGroup" in {
              val application = applicationBuilder(userAnswers = Option(emptyUserAnswers), affinityGroup)
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustBe routes.InformationSentController.onPageLoad().url
              }
            }

            s"must return OK and the correct view for a GET when first contact does not have a phone number and affinity group $affinityGroup" in {
              val userAnswers: UserAnswers = emptyUserAnswers
                .withPage(ReporterTypePage, ReporterType.LimitedCompany)
                .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
                .withPage(ContactNamePage, firstContactName)
                .withPage(ContactEmailPage, firstContactEmail)

              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
                contentAsString(result).contains(firstContactName) mustBe true
                contentAsString(result).contains(firstContactEmail) mustBe true
                contentAsString(result).contains(firstContactPhone) mustBe false
              }
            }

            s"must return OK and the correct view for a GET when there is no second contact and affinity group $affinityGroup" in {
              val userAnswers: UserAnswers = emptyUserAnswers
                .withPage(ReporterTypePage, ReporterType.LimitedCompany)
                .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
                .withPage(ContactNamePage, firstContactName)
                .withPage(ContactEmailPage, firstContactEmail)
                .withPage(ContactPhonePage, firstContactPhone)
                .withPage(HaveSecondContactPage, false)

              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
                contentAsString(result).contains(firstContactName) mustBe true
                contentAsString(result).contains(firstContactEmail) mustBe true
                contentAsString(result).contains(secondContactName) mustBe false
              }
            }

            s"must return OK and the correct view for a GET when the second contact has a phone number and affinity group $affinityGroup" in {
              val userAnswers: UserAnswers = emptyUserAnswers
                .withPage(ReporterTypePage, ReporterType.LimitedCompany)
                .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
                .withPage(ContactEmailPage, TestEmail)
                .withPage(ContactNamePage, name.fullName)
                .withPage(ContactHavePhonePage, false)
                .withPage(HaveSecondContactPage, true)
                .withPage(SecondContactNamePage, secondContactName)
                .withPage(SecondContactEmailPage, secondContactEmail)
                .withPage(SecondContactHavePhonePage, true)
                .withPage(SecondContactPhonePage, secondContactPhone)

              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
                contentAsString(result).contains(name.fullName) mustBe true
                contentAsString(result).contains(TestEmail) mustBe true
                contentAsString(result).contains(secondContactName) mustBe true
                contentAsString(result).contains(secondContactEmail) mustBe true
                contentAsString(result).contains(secondContactPhone) mustBe true
              }
            }

            s"must return OK and the correct view for a GET when the second contact has no phone number and affinity group $affinityGroup" in {
              val userAnswers: UserAnswers = emptyUserAnswers
                .withPage(ReporterTypePage, ReporterType.LimitedCompany)
                .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
                .withPage(ContactNamePage, firstContactName)
                .withPage(ContactEmailPage, firstContactEmail)
                .withPage(ContactPhonePage, firstContactPhone)
                .withPage(HaveSecondContactPage, true)
                .withPage(SecondContactNamePage, secondContactName)
                .withPage(SecondContactEmailPage, secondContactEmail)
                .withPage(SecondContactHavePhonePage, false)

              val application = applicationBuilder(userAnswers = Option(userAnswers), affinityGroup)
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

              running(application) {
                val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

                val result = route(application, request).value

                status(result) mustEqual OK
                contentAsString(result).contains(firstContactName)
                contentAsString(result).contains(firstContactEmail)
                contentAsString(result).mustNot(contain(secondContactName))

                contentAsString(result).contains(firstContactName) mustBe true
                contentAsString(result).contains(firstContactEmail) mustBe true
                contentAsString(result).contains(firstContactPhone) mustBe true
                contentAsString(result).contains(secondContactName) mustBe true
                contentAsString(result).contains(secondContactEmail) mustBe true
                contentAsString(result).contains(secondContactPhone) mustBe false
              }
            }
        }
      }

    }

    "onSubmit" - {

      "must redirect to RegistrationConfirmationPage for Individual with Id" in {
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
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
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
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
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
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
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
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

        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
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
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
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
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
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
          redirectLocation(result).value mustBe controllers.routes.PreRegisteredController.onPageLoad(withId = false).url
        }
      }

      "must redirect to BusinessAlreadyRegisteredPage when EnrolmentExistsError occurs in registration with Id" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID(UserAnswersId))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(EnrolmentExistsError(GroupIds(Seq(UserAnswersId), Seq.empty)))))

        val userAnswers = emptyUserAnswers
          .withPage(DoYouHaveUniqueTaxPayerReferencePage, true)
          .withPage(RegistrationInfoPage, IndRegistrationInfo(safeId))

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
          redirectLocation(result).value mustBe controllers.routes.PreRegisteredController.onPageLoad(withId = true).url
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

    "getPagesMissingAnswers" - {

      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers), AffinityGroup.Individual)
        .build()

      running(application) {
        val controller = application.injector.instanceOf[CheckYourAnswersController]

        "if reporter type is missing, return that it is missing" in {
          controller.getMissingAnswers(emptyUserAnswers) mustBe List(ReporterTypePage)
        }

        "if reporter type is individual, return all missing individual answers" in {
          val userAnswers: UserAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, ReporterType.Individual)

          controller.getMissingAnswers(userAnswers) mustBe List(IndDoYouHaveNINumberPage, IndContactEmailPage, IndContactHavePhonePage)
        }

        "if reporter type is individual and they have a phone, require it is entered" in {
          val userAnswers: UserAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, ReporterType.Individual)
            .withPage(IndContactHavePhonePage, true)

          controller.getMissingAnswers(userAnswers).contains(IndContactPhonePage) mustBe true
        }

        "if reporter type is individual and they do not have a phone, do not require it is entered" in {
          val userAnswers: UserAnswers = emptyUserAnswers
            .withPage(ReporterTypePage, ReporterType.Individual)
            .withPage(IndContactHavePhonePage, false)

          controller.getMissingAnswers(userAnswers).contains(IndContactHavePhonePage) mustBe false
          controller.getMissingAnswers(userAnswers).contains(IndContactPhonePage) mustBe false
        }

        "individual without id journey" - {

          "return an empty list if no answers are missing" in {
            val userAnswers: UserAnswers = emptyUserAnswers
              .withPage(ReporterTypePage, ReporterType.Individual)
              .withPage(IndDoYouHaveNINumberPage, false)
              .withPage(IndWhatIsYourNamePage, arbitraryName.arbitrary.sample.value)
              .withPage(DateOfBirthWithoutIdPage, validDateOfBirth().sample.value)
              .withPage(IndWhereDoYouLivePage, false)
              .withPage(IndNonUKAddressWithoutIdPage, arbitraryAddressWithoutId.arbitrary.sample.value)
              .withPage(IndContactEmailPage, validEmailAddressToLong(10).sample.value)
              .withPage(IndContactHavePhonePage, false)

            controller.getMissingAnswers(userAnswers) mustBe Nil
          }

          "return any missing answers for this journey" in {
            val userAnswers: UserAnswers = emptyUserAnswers
              .withPage(ReporterTypePage, ReporterType.Individual)
              .withPage(IndDoYouHaveNINumberPage, false)

            controller.getMissingAnswers(userAnswers) mustBe List(
              IndWhatIsYourNamePage,
              DateOfBirthWithoutIdPage,
              IndWhereDoYouLivePage,
              IndContactEmailPage,
              IndContactHavePhonePage
            )
          }

          "return any missing answers for the non-uk address journey" in {
            val userAnswers: UserAnswers = emptyUserAnswers
              .withPage(ReporterTypePage, ReporterType.Individual)
              .withPage(IndDoYouHaveNINumberPage, false)
              .withPage(IndWhereDoYouLivePage, false)

            controller.getMissingAnswers(userAnswers).contains(IndNonUKAddressWithoutIdPage) mustBe true
          }

          "return any missing answers for the uk address journey" in {
            val userAnswers: UserAnswers = emptyUserAnswers
              .withPage(ReporterTypePage, ReporterType.Individual)
              .withPage(IndDoYouHaveNINumberPage, false)
              .withPage(IndWhereDoYouLivePage, true)

            controller.getMissingAnswers(userAnswers).contains(IndWhatIsYourPostcodePage) mustBe true

          }

        }

        "individual with id journey" - {

        }
      }

    }

  }

}
