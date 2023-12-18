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

package viewmodels

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{__, OWrites}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

final case class Section(sectionName: String, rows: Seq[SummaryListRow])

object Section {

  implicit val sectionWrites: OWrites[Section] =
    (
      (__ \ "sectionName").write[String] and
        (__ \ "rows").write[Seq[SummaryListRow]]
    )(unlift(Section.unapply))

}
