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

package models.subscription.request

import pages._
import pages.changeContactDetails._

sealed trait ContactTypePage {
  def contactNamePage: QuestionPage[String]
  def contactEmailPage: QuestionPage[String]
  def contactPhoneNumberPage: QuestionPage[String]
  def havePhoneNumberPage: QuestionPage[Boolean]
}

case class ChangeOrganisationPrimaryContactDetailsPages(contactNamePage: QuestionPage[String],
                                                        contactEmailPage: QuestionPage[String],
                                                        contactPhoneNumberPage: QuestionPage[String],
                                                        havePhoneNumberPage: QuestionPage[Boolean]
) extends ContactTypePage

case class ChangeIndividualContactDetailsPages(contactNamePage: QuestionPage[String],
                                               contactEmailPage: QuestionPage[String],
                                               contactPhoneNumberPage: QuestionPage[String],
                                               havePhoneNumberPage: QuestionPage[Boolean]
) extends ContactTypePage

case class ChangeOrganisationSecondaryContactDetailsPages(
  contactNamePage: QuestionPage[String],
  contactEmailPage: QuestionPage[String],
  contactPhoneNumberPage: QuestionPage[String],
  havePhoneNumberPage: QuestionPage[Boolean]
) extends ContactTypePage

object ContactTypePage {

  implicit val individualContactDetailsPages: ChangeIndividualContactDetailsPages =
    ChangeIndividualContactDetailsPages(
      ContactNamePage,
      IndividualEmailPage,
      IndividualPhonePage,
      IndividualHavePhonePage
    )

  implicit val primaryContactDetailsPages: ChangeOrganisationPrimaryContactDetailsPages =
    ChangeOrganisationPrimaryContactDetailsPages(
      OrganisationContactNamePage,
      OrganisationContactEmailPage,
      OrganisationContactPhonePage,
      OrganisationContactHavePhonePage
    )

  implicit val secondaryContactDetailsPages: ChangeOrganisationSecondaryContactDetailsPages =
    ChangeOrganisationSecondaryContactDetailsPages(
      OrganisationSecondContactNamePage,
      OrganisationSecondContactEmailPage,
      OrganisationSecondContactPhonePage,
      OrganisationSecondContactHavePhonePage
    )

}
