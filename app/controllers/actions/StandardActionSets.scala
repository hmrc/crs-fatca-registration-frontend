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

import models.requests.DataRequest
import play.api.mvc.{ActionBuilder, AnyContent}

import javax.inject.Inject

class StandardActionSets @Inject() (identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    requireData: DataRequiredAction,
                                    initializeData: DataInitializeAction
) {

  def identifiedWithoutEnrolmentCheck(): ActionBuilder[DataRequest, AnyContent] =
    identify andThen getData andThen requireData

  def identifiedWithoutEnrolmentCheckInitialisedData(): ActionBuilder[DataRequest, AnyContent] =
    identify andThen getData andThen initializeData

  // ToDo Update with enrolment check when implemented
  def identifiedUserWithData(): ActionBuilder[DataRequest, AnyContent] =
    identify andThen getData andThen requireData

}