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

import controllers.actions._
import models.requests.DataRequest
import pages.PageLists
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.IndCouldNotConfirmIdentityView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class IndCouldNotConfirmIdentityController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: IndCouldNotConfirmIdentityView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(key: String): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        val continueUrl = controllers.routes.IndexController.onPageLoad.url

        cleanPages match {
          case Success(cleaned) =>
            cleaned map (
              _ => Ok(view(continueUrl, key))
            )
          case _ =>
            logger.warn("WeCouldNotConfirmController: Could not clean pages")
            throw new Exception("WeCouldNotConfirmController: Cannot clean UserAnswers pages")
        }

    }

  private def cleanPages(implicit request: DataRequest[AnyContent]): Try[Future[Boolean]] = for {
    cleaned <- (PageLists.individualWithIDPages ++ PageLists.businessWithIdPages).foldLeft(Try(request.userAnswers))(PageLists.removePage)
  } yield sessionRepository.set(cleaned)

}
