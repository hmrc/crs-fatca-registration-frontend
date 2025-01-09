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

import controllers.ControllerHelper
import controllers.actions._
import forms.IsThisYourBusinessFormProvider
import models.IdentifierType.UTR
import models.ReporterType.Sole
import models.error.ApiError.NotFoundError
import models.matching.{AutoMatchedRegistrationRequest, OrgRegistrationInfo, RegistrationRequest}
import models.register.request.RegisterWithID
import models.requests.DataRequest
import models.{Mode, UUIDGen, UniqueTaxpayerReference}
import navigation.Navigator
import pages._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.{BusinessMatchingWithIdService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.organisation.IsThisYourBusinessView
import views.html.ThereIsAProblemView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class IsThisYourBusinessController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: IsThisYourBusinessFormProvider,
  checkForSubmission: CheckForSubmissionAction,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingWithIdService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  uuidGen: UUIDGen,
  clock: Clock,
  view: IsThisYourBusinessView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  implicit private val uuidGenerator: UUIDGen = uuidGen
  implicit private val implicitClock: Clock   = clock

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.identifiedUserWithData() andThen checkForSubmission) async {
    implicit request =>
      val autoMatchedUtr = request.userAnswers.get(AutoMatchedUTRPage)
      buildRegisterWithId(autoMatchedUtr) match {
        case Some(registerWithID) =>
          matchingService.sendBusinessRegistrationInformation(registerWithID).flatMap {
            case Right(response) =>
              handleRegistrationFound(mode, autoMatchedUtr, response)
            case Left(NotFoundError) =>
              handleRegistrationNotFound(mode, autoMatchedUtr, request.userAnswers.get(ReporterTypePage).contains(Sole))
            case _ =>
              Future.successful(InternalServerError(errorView()))
          }
        case _ =>
          Future.successful(InternalServerError(errorView()))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val thereIsAProblem = Future.successful(InternalServerError(errorView()))
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers
              .get(RegistrationInfoPage)
              .fold(thereIsAProblem) {
                case registrationInfo: OrgRegistrationInfo =>
                  Future.successful(BadRequest(view(formWithErrors, registrationInfo, mode)))
                case _ => thereIsAProblem
              },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IsThisYourBusinessPage, mode, updatedAnswers))
        )
  }

  private def result(mode: Mode, form: Form[Boolean], registrationInfo: OrgRegistrationInfo)(implicit
    ec: ExecutionContext,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    subscriptionService.getSubscription(registrationInfo.safeId) flatMap {
      case Some(subscriptionId) =>
        controllerHelper.updateSubscriptionIdAndCreateEnrolment(registrationInfo.safeId, subscriptionId)
      case _ =>
        val preparedForm = request.userAnswers.get(IsThisYourBusinessPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, registrationInfo, mode)))
    }

  private def handleRegistrationFound(
    mode: Mode,
    autoMatchedUtr: Option[UniqueTaxpayerReference],
    registrationInfo: OrgRegistrationInfo
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val updatedAnswersWithUtrPage = autoMatchedUtr.map(request.userAnswers.set(WhatIsYourUTRPage, _)).getOrElse(Success(request.userAnswers))
    for {
      updatedAnswers <- Future.fromTry(updatedAnswersWithUtrPage.flatMap(_.set(RegistrationInfoPage, registrationInfo)))
      updatedRequest = DataRequest(request.request, request.userId, request.affinityGroup, updatedAnswers)
      result <- sessionRepository.set(updatedAnswers).flatMap {
        case true => result(mode, form, registrationInfo)(ec, updatedRequest)
        case false =>
          logger.error(s"Failed to update user answers after registration was found for userId: [${request.userId}]")
          Future.successful(InternalServerError(errorView()))
      }
    } yield result
  }

  private def handleRegistrationNotFound(
    mode: Mode,
    autoMatchedUtr: Option[UniqueTaxpayerReference],
    isSoleTrader: Boolean
  )(implicit request: DataRequest[AnyContent]): Future[Result] =
    if (autoMatchedUtr.nonEmpty) {
      resultWithAutoMatchedFieldCleared(mode)
    } else {
      if (isSoleTrader) {
        Future.successful(Redirect(controllers.routes.SoleTraderNotIdentifiedController.onPageLoad))
      } else {
        Future.successful(Redirect(controllers.organisation.routes.BusinessNotIdentifiedController.onPageLoad()))
      }
    }

  private def resultWithAutoMatchedFieldCleared(mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] =
    for {
      autoMatchedUtrRemoved <- Future.fromTry(request.userAnswers.remove(AutoMatchedUTRPage))
      result <- sessionRepository.set(autoMatchedUtrRemoved) flatMap {
        case true => Future.successful(Redirect(controllers.routes.ReporterTypeController.onPageLoad(mode)))
        case false =>
          logger.error(s"Failed to clear autoMatchedUTR field from user answers for userId: [${request.userId}]")
          Future.successful(InternalServerError(errorView()))
      }
    } yield result

  private def buildRegisterWithId(autoMatchedUtr: Option[UniqueTaxpayerReference])(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    request.userAnswers.get(ReporterTypePage) match {
      case Some(Sole) => buildIndividualRegistrationRequest()
      case _ =>
        autoMatchedUtr match {
          case Some(utr) => buildAutoMatchedBusinessRegistrationRequest(utr)
          case None      => buildBusinessRegistrationRequest()
        }
    }

  private def buildBusinessRegistrationRequest()(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      utr          <- request.userAnswers.get(WhatIsYourUTRPage)
      businessName <- request.userAnswers.get(BusinessNamePage)
      businessType = request.userAnswers.get(ReporterTypePage)
    } yield RegisterWithID(RegistrationRequest(UTR, utr.uniqueTaxPayerReference, businessName, businessType, None))

  def buildAutoMatchedBusinessRegistrationRequest(utr: UniqueTaxpayerReference): Option[RegisterWithID] =
    Option(RegisterWithID(AutoMatchedRegistrationRequest(UTR, utr.uniqueTaxPayerReference)))

  def buildIndividualRegistrationRequest()(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      utr             <- request.userAnswers.get(WhatIsYourUTRPage)
      soleTradersName <- request.userAnswers.get(WhatIsYourNamePage)
    } yield RegisterWithID(soleTradersName, None, UTR, utr.uniqueTaxPayerReference)

}
