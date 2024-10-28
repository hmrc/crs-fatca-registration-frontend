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
import forms.IndContactNameFormProvider
import generators.UserAnswersGenerator
import models.{Name, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.IndContactNamePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.individual.IndContactNameView

import scala.concurrent.Future

class IndContactNameControllerSpec extends SpecBase with MockitoSugar with UserAnswersGenerator {

  val formProvider = new IndContactNameFormProvider()
  val form         = formProvider()

  val FirstName = "Fred"
  val LastName  = "Flintstone"

  val name: Name = Name(FirstName, LastName)

  val validData = Map(
    "firstName" -> name.firstName,
    "lastName"  -> name.lastName
  )

  lazy val indContactNameRoute = controllers.individual.routes.IndContactNameController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      IndContactNamePage.toString -> Json.obj(
        "firstName" -> "Fred",
        "lastName"  -> "Flintstone"
      )
    )
  )

  "IndContactName Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(indWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
          when(mockSessionRepository.set(userAnswers.copy(data = userAnswers.data))).thenReturn(Future.successful(true))

          running(application) {
            val request = FakeRequest(GET, indContactNameRoute)

            val view = application.injector.instanceOf[IndContactNameView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
          }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, indContactNameRoute)

        val view = application.injector.instanceOf[IndContactNameView]

        val result     = route(application, request).value
        val filledForm = form.bind(validData)
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(filledForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, indContactNameRoute)
            .withFormUrlEncodedBody(("firstName", "nameOne"), ("lastName", "nameTwo"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, indContactNameRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IndContactNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, indContactNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, indContactNameRoute)
            .withFormUrlEncodedBody(("firstName", "nameOne"), ("lastName", "nameTwo"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
