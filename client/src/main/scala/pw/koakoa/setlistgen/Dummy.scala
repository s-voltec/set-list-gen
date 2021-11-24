/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package pw.koakoa.setlistgen

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom

sealed trait Pages
case object Home extends Pages

object ScalaJSExample {
  @JSExport
  def main(args: Array[String]): Unit = {
    val routerConfig = RouterConfigDsl[Pages].buildConfig { dsl =>
      import dsl._

      (emptyRule
         | staticRedirect(root) ~> redirectToPage(Home)(SetRouteVia.HistoryReplace)
         | staticRoute("#" / "", Home) ~> render( <.h1("Home") )
      ).notFound(redirectToPage(Home)(SetRouteVia.HistoryReplace))
    }

    val baseUrl = BaseUrl.fromWindowOrigin
    val router = Router(baseUrl, routerConfig)
    router().renderIntoDOM(dom.document.getElementById("main"))
  }
}
