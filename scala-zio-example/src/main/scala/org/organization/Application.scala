package org.organization

import org.organization.AppEnv.AppEnv
import org.organization.config.HttpServerConfig
import org.organization.http.swagger.SwaggerApiEndpoint
import org.organization.utils.db.Migration
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zhttp.http._
import zhttp.service.{EventLoopGroup, Server, ServerChannelFactory}
import zio._

import java.time.Duration

object Application extends App {
  // FIXME: for an unknown reason, the specified duration of 10 seconds simply doesn't work,
  // And the mysql connection timeout is used instead.
  private lazy val composedMiddlewares = Middleware.timeout(Duration.ofSeconds(10))

  private lazy val app: HttpApp[AppEnv, Throwable] =
    ZioHttpInterpreter().toHttp(SwaggerApiEndpoint.common) @@ composedMiddlewares

  def main: ZIO[AppEnv, Throwable, ZIO[AppEnv with EventLoopGroup with ServerChannelFactory, Throwable, Nothing]] =
    for {
      _    <- Migration.migrate
      conf <- ZIO.service[HttpServerConfig]
    } yield Server(app).withBinding(conf.host, conf.port).start

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = main
    .provideLayer(AppEnv.buildLiveEnv)
    .exitCode
}
