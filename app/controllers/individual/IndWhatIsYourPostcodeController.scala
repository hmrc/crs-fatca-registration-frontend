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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.IndWhatIsYourPostcodeFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{AddressLookupPage, IndWhatIsYourPostcodePage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.IndWhatIsYourPostcodeView

import scala.concurrent.{ExecutionContext, Future}

class IndWhatIsYourPostcodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: IndWhatIsYourPostcodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  addressLookupConnector: AddressLookupConnector,
  view: IndWhatIsYourPostcodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      val preparedForm = request.userAnswers.get(IndWhatIsYourPostcodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val formReturned = form.bindFromRequest()

      formReturned
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          postCode =>
            addressLookupConnector.addressLookupByPostcode(postCode).flatMap {
              case Nil =>
                val formError = formReturned.withError(FormError("postCode", List("indWhatIsYourPostcode.error.notFound")))
                Future.successful(BadRequest(view(formError, mode)))

              case addresses =>
                for {
                  updatedAnswers            <- Future.fromTry(request.userAnswers.set(IndWhatIsYourPostcodePage, postCode))
                  updatedAnswersWithAddress <- Future.fromTry(updatedAnswers.set(AddressLookupPage, addresses))
                  _                         <- sessionRepository.set(updatedAnswersWithAddress)
                } yield Redirect(navigator.nextPage(IndWhatIsYourPostcodePage, mode, updatedAnswersWithAddress))
            }
        )
  }

}
