package org.organization.http

import org.organization.db.model._
import org.organization.db.repository.PersonRepository
import sttp.tapir._
import io.circe.generic.auto._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task}

object PersonEndpoint extends PersonRepository{

  val personListing: PublicEndpoint[Unit, Unit, List[Person], Any] = endpoint
    .name("Default-endpoint")
    .description("Get all persons from database")
    .get
    .in("person" / "list" / "all")
    .out(jsonBody[List[Person]])

  val personListingServerEndpoint: ZServerEndpoint[Any, Any] =
    personListing.serverLogicSuccess(_ => getAllPersons)

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(personListingServerEndpoint)

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "scala zio example", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  val all: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)
}