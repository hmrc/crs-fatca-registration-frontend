package viewmodels.checkAnswers

import controllers.organisation.routes
import models.{CheckMode, UserAnswers}
import pages.BusinessNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessNameSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BusinessNamePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "businessName.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BusinessNameController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("businessName.change.hidden"))
          )
        )
    }
}
