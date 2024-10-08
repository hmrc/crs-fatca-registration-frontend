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
import controllers.actions.{FakeSubscriptionIdRetrievalAction, SubscriptionIdRetrievalAction}
import helpers.JsonFixtures.subscriptionId
import org.jsoup.Jsoup
import org.mockito.MockitoSugar.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.DetailsUpdatedView

class DetailsUpdatedControllerSpec extends SpecBase {

  private val mockSubscriptionIdRetrievalAction = mock[SubscriptionIdRetrievalAction]

  "detailsUpdated Controller" - {

    when(mockSubscriptionIdRetrievalAction.apply())
      .thenReturn(new FakeSubscriptionIdRetrievalAction(subscriptionId, injectedParsers))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionIdRetrievalAction].toInstance(mockSubscriptionIdRetrievalAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DetailsUpdatedController.onPageLoad().url)
        val view    = application.injector.instanceOf[DetailsUpdatedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString

        val page = Jsoup.parse(contentAsString(result))
        page.getElementsContainingText("Back to manage your CRS and FATCA reports").toString
          .must(include(routes.IndexController.onPageLoad.url))
      }
    }
  }

}
