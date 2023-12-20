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

import connectors.AddressLookupConnector
import controllers.actions._
import helpers.JsonFixtures.UserAnswersId
import models.{UUIDGen, UUIDGenImpl, UserAnswers}
import org.mockito.MockitoSugar.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.QuestionPage
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Call, PlayBodyParsers}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.{Clock, Instant, ZoneId}

trait SpecBase
    extends AnyFreeSpec
    with GuiceOneAppPerSuite
    with MockitoSugar
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach
    with IntegrationPatience {

  val userAnswersId: String = "id"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val uuidGenerator: UUIDGen = new UUIDGenImpl

  private val UtcZoneId          = "UTC"
  implicit val fixedClock: Clock = Clock.fixed(Instant.parse("2021-11-14T14:23:34.312535Z"), ZoneId.of(UtcZoneId))

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def onwardRoute: Call                                    = Call("GET", "/foo")
  final val mockDataRetrievalAction: DataRetrievalAction   = mock[DataRetrievalAction]
  final val mockSessionRepository: SessionRepository       = mock[SessionRepository]
  final val mockAddressLookupConnector                     = mock[AddressLookupConnector]
  final val mockCtUtrRetrievalAction: CtUtrRetrievalAction = mock[CtUtrRetrievalAction]

  def emptyUserAnswers: UserAnswers = UserAnswers(UserAnswersId, Json.obj(), Instant.now(fixedClock))

  protected def retrieveNoData(): Unit =
    when(mockDataRetrievalAction.apply()).thenReturn(new FakeDataRetrievalAction(None))

  protected def retrieveUserAnswersData(userAnswers: UserAnswers): Unit =
    when(mockDataRetrievalAction.apply()).thenReturn(new FakeDataRetrievalAction(Some(userAnswers)))

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  def injectedParsers: PlayBodyParsers = app.injector.instanceOf[PlayBodyParsers]

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None,
    affinityGroup: AffinityGroup = AffinityGroup.Individual
  ): GuiceApplicationBuilder = {
    when(mockDataRetrievalAction.apply()).thenReturn(new FakeDataRetrievalAction(userAnswers))

    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(injectedParsers, affinityGroup)),
        bind[DataRetrievalAction].toInstance(mockDataRetrievalAction),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
  }

  implicit class UserAnswersExtension(userAnswers: UserAnswers) {

    def withPage[T](page: QuestionPage[T], value: T)(implicit writes: Writes[T]): UserAnswers =
      userAnswers.set(page, value).success.value

  }

}
