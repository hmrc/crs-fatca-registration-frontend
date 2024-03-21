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
import config.FrontendAppConfig
import forms.UKAddressWithoutIdFormProvider
import models.{Address, Country, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.IndUKAddressWithoutIdPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CountryListFactory
import views.html.individual.IndUKAddressWithoutIdView

import scala.concurrent.Future

class IndUKAddressWithoutIDControllerSpec extends SpecBase with MockitoSugar {

  private val testCountry: Country  = Country("valid", "GG", "Guernsey", Option("Guernsey"))
  val testCountryList: Seq[Country] = Seq(testCountry)
  val formProvider                  = new UKAddressWithoutIdFormProvider()
  val form: Form[Address]           = formProvider(testCountryList)
  val address: Address              = Address("value 1", Some("value 2"), "value 3", Some("value 4"), Some("XX9 9XX"), testCountry)

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val countryListFactory: CountryListFactory = new CountryListFactory(app.environment, mockAppConfig) {
    override lazy val countryList: Option[Seq[Country]]          = Some(testCountryList)
    override lazy val countryListWithoutGB: Option[Seq[Country]] = Some(testCountryList)
  }

  lazy val SubmitUKIndAddressWithoutIDRoute = routes.IndUKAddressWithoutIdController.onSubmit(NormalMode).url
  lazy val LoadUKIndAddressWithoutIDRoute   = routes.IndUKAddressWithoutIdController.onSubmit(NormalMode).url

  "BusinessAddressWithoutID Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[CountryListFactory].to(countryListFactory)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, LoadUKIndAddressWithoutIDRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndUKAddressWithoutIdView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          countryListFactory.countrySelectList(form.data, testCountryList),
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IndUKAddressWithoutIdPage, address).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[CountryListFactory].to(countryListFactory)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, LoadUKIndAddressWithoutIDRoute)

        val view = application.injector.instanceOf[IndUKAddressWithoutIdView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(address),
          countryListFactory.countrySelectList(form.data, testCountryList),
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[CountryListFactory].to(countryListFactory),
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, LoadUKIndAddressWithoutIDRoute)
            .withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("addressLine3", "value 2"),
              ("addressLine4", "value 2"),
              ("postCode", "XX9 9XX"),
              ("country", "Guernsey")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[CountryListFactory].to(countryListFactory)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, LoadUKIndAddressWithoutIDRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IndUKAddressWithoutIdView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          countryListFactory.countrySelectList(form.data, testCountryList),
          NormalMode
        )(request, messages(application)).toString
      }
    }
  }

}
