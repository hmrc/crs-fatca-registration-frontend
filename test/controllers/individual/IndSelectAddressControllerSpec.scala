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
import forms.IndSelectAddressFormProvider
import generators.UserAnswersGenerator
import models.{AddressLookup, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.{AddressLookupPage, IndSelectAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.individual.IndSelectAddressView

import scala.concurrent.Future

class IndSelectAddressControllerSpec extends SpecBase with MockitoSugar with UserAnswersGenerator {

  lazy val selectAddressRoute: String = controllers.individual.routes.IndSelectAddressController.onPageLoad(NormalMode).url

  val formProvider       = new IndSelectAddressFormProvider()
  val form: Form[String] = formProvider()

  val addresses: Seq[AddressLookup] = Seq(
    AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ"),
    AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ")
  )

  val addressRadios: Seq[RadioItem] = Seq(
    RadioItem(content = Text("1 Address line 1, Town, ZZ1 1ZZ"), value = Some("1 Address line 1, Town, ZZ1 1ZZ")),
    RadioItem(content = Text("2 Address line 1, Town, ZZ1 1ZZ"), value = Some("2 Address line 1, Town, ZZ1 1ZZ"))
  )

  val userAnswers = emptyUserAnswers
    .set(AddressLookupPage, addresses)
    .success
    .value

  "SelectAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(AddressLookupPage, addresses)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()

      when(mockSessionRepository.set(userAnswers.copy(data = userAnswers.data))).thenReturn(Future.successful(true))

      running(application) {
        implicit val request = FakeRequest(GET, selectAddressRoute)

        val result = route(application, request).value

        val view        = application.injector.instanceOf[IndSelectAddressView]
        val updatedForm = userAnswers.get(IndSelectAddressPage).map(form.fill).getOrElse(form)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(updatedForm, addressRadios, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to manual UK address page if there are no address matches" in {

      forAll(indWithId.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Some(userAnswers.remove(AddressLookupPage).success.value))
            .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
            .build()
          running(application) {
            val request = FakeRequest(GET, selectAddressRoute)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.individual.routes.IndUKAddressWithoutIdController.onPageLoad(NormalMode).url
          }
      }

    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(AddressLookupPage, addresses)
        .success
        .value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute).withFormUrlEncodedBody(("value", "1 Address line 1, Town, ZZ1 1ZZ"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      running(application) {
        implicit val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IndSelectAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, addressRadios, NormalMode)(request, messages(application)).toString
      }
    }

  }

}
