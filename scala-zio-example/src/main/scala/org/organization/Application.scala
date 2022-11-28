package org.organization

import org.organization.AppEnv.AppEnv
import org.organization.config.HttpServerConfig
import org.organization.http.swagger.SwaggerApiEndpoint
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zhttp.http._
import zhttp.service.Server
import zio._

import java.time.Duration

object Application extends App {
  // FIXME: for an unknown reason, the specified duration of 10 seconds simply doesn't work,
  // And the mysql connection timeout is used instead.
  private lazy val composedMiddlewares = Middleware.timeout(Duration.ofSeconds(10))

  private lazy val app: HttpApp[AppEnv, Throwable] =
    ZioHttpInterpreter().toHttp(SwaggerApiEndpoint.common) @@ composedMiddlewares

  def main: ZIO[Has[HttpServerConfig], Nothing, Server[AppEnv, Throwable]] = {
    for {
      conf <- ZIO.service[HttpServerConfig]
      server = Server(app).withBinding(conf.host, conf.port)
    } yield server
  }

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val io = main.flatMap(_.start).provideLayer(AppEnv.buildLiveEnv)
    io.exitCode
  }
}
