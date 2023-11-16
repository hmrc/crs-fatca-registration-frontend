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

package models.register.request.details

import models.Name
import play.api.libs.json._

import java.time.LocalDate

case class Individual(name: Name, dateOfBirth: LocalDate)

object Individual {

  implicit lazy val writes: OWrites[Individual] = OWrites[Individual] {
    individual =>
      Json.obj(
        "firstName"   -> individual.name.firstName,
        "lastName"    -> individual.name.lastName,
        "dateOfBirth" -> individual.dateOfBirth.toString
      )
  }

  implicit lazy val reads: Reads[Individual] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "firstName").read[String] and
        (__ \ "lastName").read[String] and
        (__ \ "dateOfBirth").read[LocalDate]
    )(
      (firstName, secondName, dob) => Individual(Name(firstName, secondName), dob)
    )
  }

}
