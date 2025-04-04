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

package base

import controllers.matchers.JsonMatchers
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.MockitoSugar.reset
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup

trait ControllerMockFixtures extends SpecBase with JsonMatchers {

  protected val fakeNavigator: Navigator = new FakeNavigator(onwardRoute)

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  def messagesApi: MessagesApi                         = app.injector.instanceOf[MessagesApi]
  implicit def messages: Messages                      = messagesApi.preferred(fakeRequest)

  override def beforeEach(): Unit = {
    Seq(mockSessionRepository.asInstanceOf[AnyRef], mockDataRetrievalAction.asInstanceOf[AnyRef])
      .foreach(
        mock => reset(mock)
      )
    super.beforeEach()
  }

  def guiceApplicationBuilder(
    userAnswers: Option[UserAnswers] = None,
    affinityGroup: AffinityGroup = AffinityGroup.Organisation
  ): GuiceApplicationBuilder =
    applicationBuilder(userAnswers, affinityGroup)
      .overrides(
        bind[Navigator].toInstance(fakeNavigator)
      )

}
