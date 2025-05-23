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

package controllers.individual

import base.SpecBase
import connectors.AddressLookupConnector
import forms.IndWhatIsYourPostcodeFormProvider
import generators.UserAnswersGenerator
import models.{AddressLookup, Country, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.IndWhatIsYourPostcodePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.individual.IndWhatIsYourPostcodeView

import scala.concurrent.Future

class IndWhatIsYourPostcodeControllerSpec extends SpecBase with UserAnswersGenerator {

  val formProvider = new IndWhatIsYourPostcodeFormProvider()
  val form         = formProvider()

  lazy val indWhatIsYourPostcodeRoute = routes.IndWhatIsYourPostcodeController.onPageLoad(NormalMode).url

  "IndWhatIsYourPostcode Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(indWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, indWhatIsYourPostcodeRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndWhatIsYourPostcodeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
          }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IndWhatIsYourPostcodePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, indWhatIsYourPostcodeRoute)

        val view = application.injector.instanceOf[IndWhatIsYourPostcodeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to PageUnavailable when UserAnswers is empty" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, indWhatIsYourPostcodeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val addresses: Seq[AddressLookup] = Seq(
        AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ", Some(Country.GB)),
        AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ", Some(Country.GB))
      )

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockAddressLookupConnector.addressLookupByPostcode(any[String])(any(), any()))
        .thenReturn(Future.successful(addresses))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, indWhatIsYourPostcodeRoute)
            .withFormUrlEncodedBody(("postCode", "AA1 1AA"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockAddressLookupConnector, times(1)).addressLookupByPostcode(any())(any(), any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, indWhatIsYourPostcodeRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IndWhatIsYourPostcodeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, indWhatIsYourPostcodeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, indWhatIsYourPostcodeRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
