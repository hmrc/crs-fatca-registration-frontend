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

import base.SpecBase
import config.FrontendAppConfig
import models.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.Inspectors.forAll
import play.api.Environment
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import java.io.ByteArrayInputStream

class CountryListFactorySpec extends SpecBase {

  private val ukCountryCodes = Set("GB", "UK", "GG", "JE", "IM")

  "Factory  must " - {
    "return option of country sequence when given a valid json file" in {

      val conf: FrontendAppConfig = mock[FrontendAppConfig]
      val env                     = mock[Environment]

      val countries = Json.arr(
        Json.obj("state" -> "valid", "code" -> "AB", "description" -> "Country_1"),
        Json.obj("state" -> "valid", "code" -> "AB", "description" -> "Country_1", "alternativeName" -> "Country_1_2"),
        Json.obj("state" -> "valid", "code" -> "BC", "description" -> "Country_2")
      )

      when(conf.countryCodeJson).thenReturn("countries.json")

      val is = new ByteArrayInputStream(countries.toString.getBytes)
      when(env.resourceAsStream(any())).thenReturn(Some(is))

      val factory = sut(env, conf)

      factory.countryList mustBe Some(
        Seq(
          Country("valid", "AB", "Country_1", Some("Country_1")),
          Country("valid", "AB", "Country_1", Some("Country_1_2")),
          Country("valid", "BC", "Country_2", Some("Country_2"))
        )
      )
    }

    "return option of country sequence without GB when given a valid json file" in {

      val conf: FrontendAppConfig = mock[FrontendAppConfig]
      val env                     = mock[Environment]

      val countries = Json.arr(Json.obj("state" -> "valid", "code" -> "ZW", "description" -> "Zimbabwe"),
                               Json.obj("state" -> "valid", "code" -> "GB", "description" -> "Great Britain")
      )

      when(conf.countryCodeJson).thenReturn("countries.json")

      val is = new ByteArrayInputStream(countries.toString.getBytes)
      when(env.resourceAsStream(any())).thenReturn(Some(is))

      val factory = sut(env, conf)

      factory.countryListWithoutGB mustBe Some(Seq(Country("valid", "ZW", "Zimbabwe", Some("Zimbabwe"))))
    }

    "return None when country list cannot be loaded from environment" in {
      val conf: FrontendAppConfig = mock[FrontendAppConfig]
      val env                     = mock[Environment]

      when(conf.countryCodeJson).thenReturn("doesntmatter.json")
      when(env.resourceAsStream(any())).thenReturn(None)

      val factory = sut(env, conf)

      factory.countryList mustBe None
    }

    "return the description when given the country code" in {
      val conf: FrontendAppConfig = mock[FrontendAppConfig]
      val env                     = mock[Environment]

      val countries = Json.arr(Json.obj("state" -> "valid", "code" -> "XX", "description" -> "Somewhere"))

      when(conf.countryCodeJson).thenReturn("countries.json")

      val is = new ByteArrayInputStream(countries.toString.getBytes)
      when(env.resourceAsStream(any())).thenReturn(Some(is))

      val factory = sut(env, conf)

      factory.getDescriptionFromCode("XX") mustBe Some("Somewhere")
    }
  }

  "countryListWithUKCountries" - {
    "return countries in the UK" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))

      forAll(factory.countryListWithUKCountries.value) {
        country =>
          ukCountryCodes must contain(country.code)
      }
    }
  }

  "countryListWithoutUKCountries" - {
    "return countries not in the UK" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))

      forAll(factory.countryListWithoutUKCountries.value) {
        country =>
          ukCountryCodes must not contain country.code
      }
    }
  }

  "countryListWithoutGB" - {
    "return countries with country code excluding GB" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))

      forAll(factory.countryListWithoutGB.value) {
        country => country.code must not be "GB"
      }
    }
  }

  "countrySelectList" - {

    "must return non-breaking-space item when no country is selected" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))
      val countries = Seq(
        Country("valid", "AB", "Country_1", Some("Country_1")),
        Country("valid", "AB", "Country_1", Some("Country_1_2")),
        Country("valid", "BC", "Country_2", Some("Country_2"))
      )

      factory.countrySelectList(Map.empty, countries) must contain theSameElementsAs Seq(
        SelectItem(None, "&nbsp"),
        SelectItem(value = Some("Country_1"), text = "Country_1", selected = false),
        SelectItem(value = Some("Country_1_2"), text = "Country_1_2", selected = false),
        SelectItem(value = Some("Country_2"), text = "Country_2", selected = false)
      )
    }

    "must return selected country when there is one" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))
      val countries = Seq(
        Country("valid", "AB", "Country_1", Some("Country_1")),
        Country("valid", "AB", "Country_1", Some("Country_1_2")),
        Country("valid", "BC", "Country_2", Some("Country_2"))
      )
      val selectedCountry = Map("country" -> "Country_2")

      factory.countrySelectList(selectedCountry, countries) must contain theSameElementsAs Seq(
        SelectItem(value = None, text = "&nbsp"),
        SelectItem(value = Some("Country_1"), text = "Country_1", selected = false),
        SelectItem(value = Some("Country_1_2"), text = "Country_1_2", selected = false),
        SelectItem(value = Some("Country_2"), text = "Country_2", selected = true)
      )
    }

    "must return the correct selected country when there are alternative names" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))
      val countries = Seq(
        Country("valid", "AB", "Country_1", Some("Country_1")),
        Country("valid", "AB", "Country_1", Some("Country_1_2")),
        Country("valid", "BC", "Country_2", Some("Country_2"))
      )
      val selectedCountry = Map("country" -> "Country_1_2")

      factory.countrySelectList(selectedCountry, countries) must contain theSameElementsAs Seq(
        SelectItem(value = None, text = "&nbsp"),
        SelectItem(value = Some("Country_1"), text = "Country_1", selected = false),
        SelectItem(value = Some("Country_1_2"), text = "Country_1_2", selected = true),
        SelectItem(value = Some("Country_2"), text = "Country_2", selected = false)
      )
    }
  }

  def sut(env: Environment = mock[Environment], config: FrontendAppConfig = mock[FrontendAppConfig]) =
    new CountryListFactory(env, config)

}
