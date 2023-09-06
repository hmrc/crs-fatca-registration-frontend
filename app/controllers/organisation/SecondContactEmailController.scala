package controllers.organisation

import controllers.actions._
import forms.SecondContactEmailFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.SecondContactEmailPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.organisation.SecondContactEmailView

import scala.concurrent.{ExecutionContext, Future}

class SecondContactEmailController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        standardActionSets: StandardActionSets,
                                        formProvider: SecondContactEmailFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: SecondContactEmailView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(SecondContactEmailPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getSecondContactName(request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, getSecondContactName(request.userAnswers)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondContactEmailPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SecondContactEmailPage, mode, updatedAnswers))
      )
  }
}
