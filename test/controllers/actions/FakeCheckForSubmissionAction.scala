package controllers.actions

import models.requests.DataRequest
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

class FakeCheckForSubmissionAction extends CheckForSubmissionAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = Future.successful(Right(request))

  override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
