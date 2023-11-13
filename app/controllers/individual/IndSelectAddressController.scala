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
import forms.IndSelectAddressFormProvider
import models.{AddressLookup, Mode}
import navigation.Navigator
import pages.{AddressLookupPage, IndSelectAddressPage, IndSelectedAddressLookupPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndSelectAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndSelectAddressController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  formProvider: IndSelectAddressFormProvider,
  navigator: Navigator,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: IndSelectAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData() {
      implicit request =>
        request.userAnswers.get(AddressLookupPage) match {
          case Some(addresses) =>
            val preparedForm: Form[String] = request.userAnswers.get(IndSelectAddressPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            val radios: Seq[RadioItem] = addresses.map(
              address => RadioItem(content = Text(s"${formatAddress(address)}"), value = Some(s"${formatAddress(address)}"))
            )

            Ok(view(preparedForm, radios, mode))

          case None => Redirect(controllers.individual.routes.IndAddressWithoutIdController.onPageLoad(mode))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        request.userAnswers.get(AddressLookupPage) match {
          case Some(addresses) =>
            val radios: Seq[RadioItem] = addresses.map(
              address => RadioItem(content = Text(s"${formatAddress(address)}"), value = Some(s"${formatAddress(address)}"))
            )

            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, radios, mode))),
                value => {
                  val addressToStore: AddressLookup = addresses.find(formatAddress(_) == value).getOrElse(throw new Exception("Cannot get address"))

                  for {
                    updatedAnswers                    <- Future.fromTry(request.userAnswers.set(IndSelectAddressPage, value))
                    updatedAnswersWithSelectedAddress <- Future.fromTry(updatedAnswers.set(IndSelectedAddressLookupPage, addressToStore))
                    _                                 <- sessionRepository.set(updatedAnswersWithSelectedAddress)
                  } yield Redirect(navigator.nextPage(IndSelectAddressPage, mode, updatedAnswersWithSelectedAddress))
                }
              )

          case None => Future.successful(Redirect(controllers.individual.routes.IndAddressWithoutIdController.onPageLoad(mode)))
        }
    }

  private def formatAddress(address: AddressLookup): String = {
    val lines = Seq(address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4).flatten.mkString(", ")
    val county = address.county.fold("")(
      county => s"$county, "
    )

    s"$lines, ${address.town}, $county${address.postcode}"
  }

}
