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

import controllers.actions._
import forms.AddressWithoutIdFormProvider
import models.{Country, Mode}
import navigation.Navigator
import pages.BusinessAddressWithoutIDPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CountryListFactory
import views.html.organisation.BusinessAddressWithoutIDView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessAddressWithoutIDController @Inject() (
  override val messagesApi: MessagesApi,
  countryListFactory: CountryListFactory,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: AddressWithoutIdFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessAddressWithoutIDView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val countriesList: Option[Seq[Country]] = countryListFactory.countryListWithoutGB

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      countriesList match {
        case Some(countries) =>
          val form = formProvider(countries)
          val preparedForm = request.userAnswers.get(BusinessAddressWithoutIDPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(
            view(
              preparedForm,
              countryListFactory.countrySelectList(form.data, countries),
              routes.BusinessAddressWithoutIDController.onSubmit(mode),
              "business",
              mode
            )
          )
        case None =>
          logger.error("Could not retrieve countries list from JSON file.")
          // TODO: Change this to ThereIsAProblemController when implemented
          Redirect(routes.BusinessAddressWithoutIDController.onPageLoad(mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      countriesList match {
        case Some(countries) =>
          val form = formProvider(countries)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      countryListFactory.countrySelectList(form.data, countries),
                      routes.BusinessAddressWithoutIDController.onSubmit(mode),
                      "business",
                      mode
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessAddressWithoutIDPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(BusinessAddressWithoutIDPage, mode, updatedAnswers))
            )
        case None =>
          logger.error("Could not retrieve countries list from JSON file.")
          // TODO: Change this to ThereIsAProblemController when implemented
          Future.successful(Redirect(routes.BusinessAddressWithoutIDController.onPageLoad(mode)))
      }
  }

}
