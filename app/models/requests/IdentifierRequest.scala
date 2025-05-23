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

package models.requests

import models.UniqueTaxpayerReference
import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment}

case class IdentifierRequest[A](
  request: Request[A],
  userId: String,
  affinityGroup: AffinityGroup,
  enrolments: Set[Enrolment] = Set.empty,
  utr: Option[UniqueTaxpayerReference] = None,
  group: Option[String] = None
) extends WrappedRequest[A](request)
