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

import base.ControllerMockFixtures
import controllers.actions.{CtUtrRetrievalAction, FakeCtUtrRetrievalAction}
import helpers.JsonFixtures._
import models.NormalMode
import org.mockito.ArgumentMatchers.{any, eq => mockitoEq}
import org.mockito.MockitoSugar.{reset, when}
import pages.AutoMatchedUTRPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ThereIsAProblemView

import java.time.Clock
import scala.concurrent.Future

class IndexControllerSpec extends ControllerMockFixtures {

  private val application = guiceApplicationBuilder()
    .overrides(
      bind[CtUtrRetrievalAction].toInstance(mockCtUtrRetrievalAction),
      bind[Clock].toInstance(fixedClock)
    )
    .build()

  override def beforeEach(): Unit = {
    reset(mockCtUtrRetrievalAction)
    super.beforeEach()
  }

  "Index Controller" - {

    "must redirect to ReporterTypePage for a GET when there is no CT UTR" in {
      when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction())

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.ReporterTypeController.onPageLoad(NormalMode).url

    }

    "must set AutoMatchedUTR field and redirect to IsThisYourBusinessPage for a GET when there is a CT UTR" in {
      val userAnswersWithAutoMatchedUtr = emptyUserAnswers.set(AutoMatchedUTRPage, utr).success.value

      when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction(Option(utr)))
      when(mockSessionRepository.set(mockitoEq(userAnswersWithAutoMatchedUtr))) thenReturn Future.successful(true)
      retrieveUserAnswersData(emptyUserAnswers)

      val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.organisation.routes.IsThisYourBusinessController.onPageLoad(NormalMode).url

    }

    "must return ThereIsAProblemPage for a GET when there is a CT UTR but call to session repository fails" in {
      when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction(Option(utr)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(false)
      retrieveUserAnswersData(emptyUserAnswers)

      val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString

    }
  }

}
