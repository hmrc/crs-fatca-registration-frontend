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
import forms.WhatIsYourUTRFormProvider
import generators.UserAnswersGenerator
import models.ReporterType.{LimitedCompany, LimitedPartnership, Partnership, Sole, UnincorporatedAssociation}
import models.{NormalMode, ReporterType, UniqueTaxpayerReference, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{ReporterTypePage, WhatIsYourUTRPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.organisation.WhatIsYourUTRView

import scala.concurrent.Future

class WhatIsYourUTRControllerSpec extends SpecBase with MockitoSugar with UserAnswersGenerator {

  lazy val loadRoute   = routes.WhatIsYourUTRController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.WhatIsYourUTRController.onSubmit(NormalMode).url
  private def form     = new WhatIsYourUTRFormProvider().apply("Self Assessment")

  val userAnswers                  = emptyUserAnswers.set(ReporterTypePage, Sole).success.value
  val taxType                      = "Self Assessment"
  val UtrValue                     = "1234567890"
  val utr: UniqueTaxpayerReference = UniqueTaxpayerReference(UtrValue)

  private def getTaxTypeMessage(userAnswers: UserAnswers) =
    userAnswers.get(ReporterTypePage) match {
      case Some(LimitedCompany) | Some(UnincorporatedAssociation) => "whatIsYourUTR.corporation"
      case Some(Partnership) | Some(LimitedPartnership)           => "whatIsYourUTR.partnership"
      case Some(Sole)                                             => "whatIsYourUTR.soleTrader"
      case _                                                      => ""
    }

  "WhatIsYourUTR Controller" - {

    "must return OK and the correct view for a GET when FI is self assessment" in {
      val userAnswers = emptyUserAnswers.set(ReporterTypePage, Sole).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      when(mockSessionRepository.set(userAnswers.copy(data = userAnswers.data))).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        val form              = new WhatIsYourUTRFormProvider().apply(taxType)
        val view              = application.injector.instanceOf[WhatIsYourUTRView]
        val updatedForm       = userAnswers.get(WhatIsYourUTRPage).map(form.fill).getOrElse(form)
        val taxTypeMessageKey = getTaxTypeMessage(userAnswers)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(updatedForm, NormalMode, taxTypeMessageKey)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when FI is a corporation" in {
      val userAnswers = emptyUserAnswers.set(ReporterTypePage, ReporterType.LimitedCompany).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsYourUTRView]

        val taxTypeMessageKey = getTaxTypeMessage(userAnswers)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxTypeMessageKey)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when is FI is a partnership" in {
      val userAnswers = emptyUserAnswers.set(ReporterTypePage, Partnership).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      when(mockSessionRepository.set(userAnswers.copy(data = userAnswers.data))).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        val form              = new WhatIsYourUTRFormProvider().apply(taxType)
        val view              = application.injector.instanceOf[WhatIsYourUTRView]
        val updatedForm       = userAnswers.get(WhatIsYourUTRPage).map(form.fill).getOrElse(form)
        val taxTypeMessageKey = getTaxTypeMessage(userAnswers)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(updatedForm, NormalMode, taxTypeMessageKey)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        emptyUserAnswers
          .set(ReporterTypePage, ReporterType.Sole)
          .success
          .value
          .set(WhatIsYourUTRPage, utr)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, loadRoute)

        val view      = application.injector.instanceOf[WhatIsYourUTRView]
        val boundForm = form.bind(Map("value" -> utr.uniqueTaxPayerReference))

        val result            = route(application, request).value
        val taxTypeMessageKey = getTaxTypeMessage(userAnswers)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxTypeMessageKey)(request, messages(application)).toString
      }
    }

    "must redirect to PageUnavailable when UserAnswers is empty" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", utr.uniqueTaxPayerReference))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WhatIsYourUTRView]

        val result            = route(application, request).value
        val taxTypeMessageKey = getTaxTypeMessage(userAnswers)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxTypeMessageKey)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, loadRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }

}
