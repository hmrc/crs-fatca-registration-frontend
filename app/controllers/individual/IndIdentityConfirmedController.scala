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

import config.FrontendAppConfig
import controllers.ControllerHelper
import controllers.actions._
import models.{IdentifierType, Mode, UUIDGen}
import models.error.ApiError.NotFoundError
import models.register.request.RegisterWithID
import models.requests.DataRequest
import navigation.Navigator
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{BusinessMatchingWithIdService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.IndIdentityConfirmedView
import views.html.ThereIsAProblemView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndIdentityConfirmedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  standardActionSets: StandardActionSets,
  val appConfig: FrontendAppConfig,
  navigator: Navigator,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingWithIdService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  uuidGen: UUIDGen,
  clock: Clock,
  view: IndIdentityConfirmedView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  implicit private val uuidGenerator: UUIDGen = uuidGen
  implicit private val implicitClock: Clock   = clock

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        buildRegisterWithID() match {
          case Some(registrationRequest) =>
            matchingService
              .sendIndividualRegistrationInformation(registrationRequest)
              .flatMap {
                case Right(info) =>
                  request.userAnswers.set(RegistrationInfoPage, info).map(sessionRepository.set)
                  subscriptionService.getDisplaySubscriptionId(info.safeId) flatMap {
                    case Some(subscriptionId) =>
                      controllerHelper.updateSubscriptionIdAndCreateEnrolment(info.safeId, subscriptionId)
                    case _ =>
                      val action = navigator.nextPage(RegistrationInfoPage, mode, request.userAnswers).url
                      Future.successful(Ok(view(mode, action)))
                  }
                case Left(NotFoundError) =>
                  Future.successful(Redirect(routes.IndCouldNotConfirmIdentityController.onPageLoad("identity")))
                case _ =>
                  Future.successful(InternalServerError(errorView()))
              }
          case _ =>
            Future.successful(InternalServerError(errorView()))
        }
    }

  private def buildRegisterWithID()(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      nino <-
        request.userAnswers.get(IndWhatIsYourNINumberPage)

      name <-
        request.userAnswers.get(IndContactNamePage)

      dob <-
        request.userAnswers.get(IndDateOfBirthPage)

    } yield RegisterWithID(name, Some(dob), IdentifierType.NINO, nino.nino)

}
