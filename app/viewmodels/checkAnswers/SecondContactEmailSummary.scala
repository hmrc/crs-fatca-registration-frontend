package viewmodels.checkAnswers

import controllers.organisation.routes
import models.{CheckMode, UserAnswers}
import pages.SecondContactEmailPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SecondContactEmailSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SecondContactEmailPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "secondContactEmail.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.SecondContactEmailController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("secondContactEmail.change.hidden"))
          )
        )
    }
}
