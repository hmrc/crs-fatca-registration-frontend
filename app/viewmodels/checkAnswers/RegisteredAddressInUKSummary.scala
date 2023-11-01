package viewmodels.checkAnswers

import controllers.organisation.routes
import models.{CheckMode, UserAnswers}
import pages.RegisteredAddressInUKPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RegisteredAddressInUKSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RegisteredAddressInUKPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "registeredAddressInUK.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.RegisteredAddressInUKController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("registeredAddressInUK.change.hidden"))
          )
        )
    }
}
