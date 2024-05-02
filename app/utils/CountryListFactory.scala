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

package utils

import config.FrontendAppConfig
import models.Country
import play.api.Environment
import play.api.libs.json.Json
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import javax.inject.{Inject, Singleton}

@Singleton
class CountryListFactory @Inject() (environment: Environment, appConfig: FrontendAppConfig) {

  private val countryCodesForUkCountries: Set[String] = Set("GB", "UK", "GG", "JE", "IM")

  def countryList: Option[Seq[Country]] = getCountryList

  private def getCountryList: Option[Seq[Country]] =
    environment.resourceAsStream(appConfig.countryCodeJson) map Json.parse map {
      _.as[Seq[Country]]
        .map(
          country => if (country.alternativeName.isEmpty) country.copy(alternativeName = Option(country.description)) else country
        )
        .sortWith(
          (country, country2) => country.description.toLowerCase < country2.description.toLowerCase
        )
    }

  def getDescriptionFromCode(code: String): Option[String] = countryList map {
    _.filter(
      (p: Country) => p.code == code
    ).head.description
  }

  lazy val countryListWithoutGB: Option[Seq[Country]] = countryList.map {
    _.filter(
      x => x.code != "GB"
    )
  }

  lazy val countryListWithoutUKCountries: Option[Seq[Country]] = countryList.map {
    countries =>
      countries.filter(
        country => !countryCodesForUkCountries.contains(country.code)
      )
  }

  lazy val countryListWithUKCountries: Option[Seq[Country]] = countryList.map {
    countries =>
      countries.filter(
        country => countryCodesForUkCountries.contains(country.code)
      )
  }

  def countrySelectList(value: Map[String, String], countries: Seq[Country]): Seq[SelectItem] = {
    val countryJsonList =
      for {
        country         <- countries
        alternativeName <- country.alternativeName
      } yield SelectItem(
        country.alternativeName,
        alternativeName,
        value.get("country") == country.alternativeName
      )
    SelectItem(None, "") +: countryJsonList
  }

}
