package controllers.organisation

import controllers.actions._
import forms.WhatIsYourNameFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.WhatIsYourNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.organisation.WhatIsYourNameView

import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourNameController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        standardActionSets: StandardActionSets,
                                        formProvider: WhatIsYourNameFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: WhatIsYourNameView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhatIsYourNamePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsYourNamePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatIsYourNamePage, mode, updatedAnswers))
      )
  }
}
