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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.DataRequestWithSubscriptionId
import models.subscription.response.{IndividualRegistrationType, OrganisationRegistrationType, RegistrationType}
import models.{IdentifierType, SubscriptionID}
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import services.SubscriptionService
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait SubscriptionIdRetrievalAction {

  def apply(requiredRegistrationType: Option[RegistrationType] = None): ActionBuilder[DataRequestWithSubscriptionId, AnyContent]
    with ActionFunction[Request, DataRequestWithSubscriptionId]

}

class SubscriptionIdRetrievalActionImpl @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  subscriptionService: SubscriptionService,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends SubscriptionIdRetrievalAction
    with AuthorisedFunctions {

  override def apply(requiredRegistrationType: Option[RegistrationType]): ActionBuilder[DataRequestWithSubscriptionId, AnyContent]
    with ActionFunction[Request, DataRequestWithSubscriptionId] =
    new SubscriptionIdRetrievalActionExtractor(authConnector, subscriptionService, requiredRegistrationType, config, parser)

}

class SubscriptionIdRetrievalActionExtractor @Inject() (
  override val authConnector: AuthConnector,
  subscriptionService: SubscriptionService,
  requiredRegistrationType: Option[RegistrationType],
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[DataRequestWithSubscriptionId, AnyContent]
    with ActionFunction[Request, DataRequestWithSubscriptionId]
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](request: Request[A], block: DataRequestWithSubscriptionId[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments) {
        case Some(internalId) ~ enrolments =>
          getSubscriptionId(enrolments) match {
            case Some(subscriptionId) =>
              subscriptionService.getSubscription(SubscriptionID(subscriptionId)).flatMap {
                case Some(subscription) => (requiredRegistrationType, subscription.registrationType) match {
                    case (Some(OrganisationRegistrationType), IndividualRegistrationType) =>
                      Future.successful(Redirect(controllers.changeContactDetails.routes.ChangeIndividualContactDetailsController.onPageLoad()))
                    case (Some(IndividualRegistrationType), OrganisationRegistrationType) =>
                      Future.successful(Redirect(controllers.changeContactDetails.routes.ChangeOrganisationContactDetailsController.onPageLoad()))
                    case _ => block(DataRequestWithSubscriptionId(request, internalId, SubscriptionID(subscriptionId)))
                  }
                case None =>
                  val errorMessage = s"User is enrolled but we can't retrieve subscription: $subscriptionId"
                  logger.warn(errorMessage)
                  throw new Exception(errorMessage)
              }
            case None => Future.successful(Redirect(routes.IndexController.onPageLoad))
          }
        case _ =>
          val msg = "Unable to retrieve internal id"
          logger.warn(msg)
          throw AuthorisationException.fromString(msg)
      } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  private def getSubscriptionId(
    enrolments: Enrolments
  ): Option[String] =
    for {
      enrolment      <- enrolments.getEnrolment(config.enrolmentKey)
      id             <- enrolment.getIdentifier(IdentifierType.FATCAID)
      subscriptionId <- if (id.value.nonEmpty) Some(id.value) else None
    } yield subscriptionId

}
