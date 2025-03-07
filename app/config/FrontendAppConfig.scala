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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  val timeoutSeconds: Int   = configuration.get[Int]("session.timeoutSeconds")
  val countdownSeconds: Int = configuration.get[Int]("session.countdownSeconds")

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "crs-fatca-registration-frontend"

  lazy val addressLookUpUrl: String = configuration.get[Service]("microservice.services.address-lookup").baseUrl

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String                        = configuration.get[String]("urls.login")
  val loginContinueUrl: String                = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String                      = configuration.get[String]("urls.signOut")
  val crsFatcaFIManagementFrontendUrl: String = configuration.get[String]("urls.crsFatcaFIManagementFrontend")
  val addFinancialInstitutionUrl: String      = configuration.get[String]("urls.addFinancialInstitution")
  lazy val lostUTRUrl: String                 = configuration.get[String]("urls.lostUTR")
  lazy val findUTRUrl: String                 = configuration.get[String]("urls.findUTR")
  lazy val findCorpTaxUTRUrl: String          = configuration.get[String]("urls.findCorporationTaxUTR")
  lazy val findCompanyName: String            = configuration.get[String]("urls.findCompanyName")

  lazy val businessMatchingUrl: String =
    s"${configuration.get[Service]("microservice.services.business-matching").baseUrl}${configuration.get[String]("microservice.services.business-matching.startUrl")}"

  lazy val enrolmentStoreProxyUrl: String =
    s"${configuration.get[Service]("microservice.services.enrolment-store-proxy").baseUrl}${configuration.get[String]("microservice.services.enrolment-store-proxy.startUrl")}"

  val taxEnrolmentsUrl1: String = s"${configuration.get[Service]("microservice.services.tax-enrolments").baseUrl}${configuration
      .get[String]("microservice.services.tax-enrolments.url1")}"

  val taxEnrolmentsUrl2: String = s"${configuration.get[String]("microservice.services.tax-enrolments.url2")}"

  private val exitSurveyBaseUrl: String        = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String                    = s"$exitSurveyBaseUrl/feedback/crs-fatca-registration-frontend"
  lazy val businessTaxAccountLink: String      = configuration.get[String]("urls.btaLogin")
  lazy val corporationTaxEnquiriesLink: String = configuration.get[String]("urls.corporationTaxEnquiries")
  lazy val selfAssessmentEnquiriesLink: String = configuration.get[String]("urls.selfAssessmentEnquiries")
  lazy val emailEnquiries: String              = configuration.get[String]("urls.emailEnquiries")
  lazy val findNINumberUrl: String             = configuration.get[String]("urls.findNINumberUrl")

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en")
  )

  val timeout: Int                 = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int               = configuration.get[Int]("timeout-dialog.countdown")
  val enrolmentKey: String         = configuration.get[String]("keys.enrolmentKey.crsFatca")
  val ctEnrolmentKey: String       = configuration.get[String]("keys.enrolmentKey.ct")
  lazy val countryCodeJson: String = configuration.get[String]("json.countries")

  val cacheTtl: Int        = configuration.get[Int]("mongodb.timeToLiveInSeconds")
  val subscriptionTtl: Int = configuration.get[Int]("mongodb.subscriptionTimeToLiveInSeconds")

  val mongoEncryptionEnabled: Boolean = configuration.get[Boolean]("mongodb.encryptionEnabled")
}
