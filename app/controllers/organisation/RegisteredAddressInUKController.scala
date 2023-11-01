package controllers.organisation

import controllers.actions._
import forms.RegisteredAddressInUKFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.RegisteredAddressInUKPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.organisation.RegisteredAddressInUKView

import scala.concurrent.{ExecutionContext, Future}

class RegisteredAddressInUKController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         standardActionSets: StandardActionSets,
                                         formProvider: RegisteredAddressInUKFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RegisteredAddressInUKView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(RegisteredAddressInUKPage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RegisteredAddressInUKPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RegisteredAddressInUKPage, mode, updatedAnswers))
      )
  }
}
