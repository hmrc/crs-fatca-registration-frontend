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
import forms.IndIsThisYourAddressFormProvider
import models.Mode
import navigation.Navigator
import pages.{AddressLookupPage, IndSelectAddressPage, IndSelectedAddressLookupPage, IsThisYourAddressPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThereIsAProblemView
import views.html.individual.IndIsThisYourAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndIsThisYourAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: IndIsThisYourAddressFormProvider,
  checkForSubmission: CheckForSubmissionAction,
  val controllerComponents: MessagesControllerComponents,
  view: IndIsThisYourAddressView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      val preparedForm = request.userAnswers.get(IsThisYourAddressPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(AddressLookupPage) match {
        case Some(value) => Ok(view(preparedForm, value.head, mode))
        case None        => InternalServerError(errorView())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val maybeAddresses = request.userAnswers.get(AddressLookupPage)
      val error          = InternalServerError(errorView())

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              maybeAddresses match {
                case Some(address :: _) => BadRequest(view(formWithErrors, address, mode))
                case _ =>
                  logger.error("No selected address was found")
                  error
              }
            ),
          value =>
            maybeAddresses match {
              case Some(address :: _) =>
                for {
                  updatedAnswers                 <- Future.fromTry(request.userAnswers.set(IsThisYourAddressPage, value))
                  userAnswersWithAddress         <- Future.fromTry(updatedAnswers.set(IndSelectAddressPage, address.format))
                  userAnswersWithSelectedAddress <- Future.fromTry(userAnswersWithAddress.set(IndSelectedAddressLookupPage, address))
                  _                              <- sessionRepository.set(userAnswersWithSelectedAddress)
                } yield Redirect(navigator.nextPage(IsThisYourAddressPage, mode, userAnswersWithAddress))
              case _ =>
                logger.error("No selected address was found")
                Future.successful(error)
            }
        )
  }

}
