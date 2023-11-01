package viewmodels.checkAnswers

import controllers.organisation.routes
import models.{CheckMode, UserAnswers}
import pages.WhatIsYourNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhatIsYourNameSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WhatIsYourNamePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "whatIsYourName.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.WhatIsYourNameController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("whatIsYourName.change.hidden"))
          )
        )
    }
}
