package controllers

import controllers.actions._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.InformationMissingView

class InformationMissingController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: InformationMissingView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      Ok(view())
  }
}
