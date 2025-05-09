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

package controllers.organisation

import base.SpecBase
import controllers.actions._
import forms.ContactPhoneFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages.{ContactNamePage, ContactPhonePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.organisation.ContactPhoneView

import scala.concurrent.Future

class ContactPhoneControllerSpec extends SpecBase with MockitoSugar with TableDrivenPropertyChecks {

  val formProvider = new ContactPhoneFormProvider()
  val form         = formProvider()

  lazy val contactPhoneRoute = routes.ContactPhoneController.onPageLoad(NormalMode).url

  val contactName              = "Contact name"
  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(ContactNamePage, contactName).success.value

  "ContactPhone Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, contactPhoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, contactName, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ContactPhonePage, "answer")
        .success
        .value
        .set(ContactNamePage, "Contact name")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, contactPhoneRoute)

        val view = application.injector.instanceOf[ContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), contactName, NormalMode)(request, messages(application)).toString
      }
    }

    forAll(Table("affinityGroup", Seq(AffinityGroup.Individual, AffinityGroup.Organisation, AffinityGroup.Agent): _*)) {
      affinityGroup =>
        s"must redirect to the next page when valid data is submitted and affinityGroup is $affinityGroup" in {

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          retrieveUserAnswersData(emptyUserAnswers)

          val application = new GuiceApplicationBuilder()
            .overrides(
              bind[DataRequiredAction].to[DataRequiredActionImpl],
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[IdentifierAction].toInstance(new FakeIdentifierAction(injectedParsers, affinityGroup)),
              bind[DataRetrievalAction].toInstance(mockDataRetrievalAction),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, routes.ContactPhoneController.onSubmit(NormalMode).url)
                .withFormUrlEncodedBody(("value", "07 777 777"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }
    }

    "must redirect to PageUnavailable when UserAnswers is empty" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, contactPhoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, contactPhoneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, contactName, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, contactPhoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, contactPhoneRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
