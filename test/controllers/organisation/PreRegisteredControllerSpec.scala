package controllers.organisation

import base.SpecBase
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers._

class PreRegisteredControllerSpec extends SpecBase {

  "PreRegistered Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.PreRegisteredController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PreRegisteredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }

}
