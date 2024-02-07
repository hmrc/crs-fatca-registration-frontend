package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.RegistrationConfirmationView

class RegistrationConfirmationControllerSpec extends SpecBase {

  "RegistrationConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }

}
