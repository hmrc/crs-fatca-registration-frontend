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

package connectors

import config.FrontendAppConfig
import models.Regime.CRFA
import models.{AddressLookup, LookupAddressByPostcode}
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Reads
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AddressLookupConnector @Inject() (http: HttpClient, config: FrontendAppConfig) extends Logging {

  def addressLookupByPostcode(postCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[AddressLookup]] = {

    val addressLookupUrl: String = s"${config.addressLookUpUrl}/lookup"

    implicit val reads: Reads[Seq[AddressLookup]] = AddressLookup.addressesLookupReads

    val lookupAddressByPostcode = LookupAddressByPostcode(postCode, None)

    http.POST[LookupAddressByPostcode, HttpResponse](addressLookupUrl, lookupAddressByPostcode, headers = Seq("X-Hmrc-Origin" -> CRFA.toString)) flatMap {
      case response if response.status equals OK =>
        Future.successful(
          sortAddresses(
            response.json
              .as[Seq[AddressLookup]]
              .filterNot(
                address => address.addressLine1.isEmpty && address.addressLine2.isEmpty
              )
          )
        )
      case response =>
        val message = s"Address Lookup failed with status ${response.status} Response body: ${response.body}"
        Future.failed(new HttpException(message, response.status))
    } recover {
      case e: Exception =>
        logger.error("Exception in Address Lookup", e)
        throw e
    }
  }

  def mkString(p: AddressLookup) = List[Option[String]](p.addressLine1, p.addressLine2, p.addressLine3, p.addressLine4).flatten.mkString(" ")

  def numbersOnly(adr: AddressLookup): Seq[Option[Int]] =
    "([0-9]+)".r
      .findAllIn(mkString(adr))
      .map(
        n => Try(n.toInt).toOption
      )
      .toSeq
      .reverse :+ None

  def sortAddresses(items: Seq[AddressLookup]): Seq[AddressLookup] =
    items.sortWith {
      (a, b) =>
        def sort(zipped: Seq[(Option[Int], Option[Int])]): Boolean = zipped match {
          case (Some(nA), Some(nB)) :: tail =>
            if (nA == nB) sort(tail) else nA < nB
          case (Some(_), None) :: _ => true
          case (None, Some(_)) :: _ => false
          case _                    => mkString(a) < mkString(b)
        }

        sort(numbersOnly(a).zipAll(numbersOnly(b), None, None).toList)
    }

}
