package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.routes.UserRoutes
import com.services.UserServices
import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import akka.Done

object Main extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(
    Behaviors.empty,
    "user-management-system"
  )
  implicit val executionContext: ExecutionContext = system.executionContext

  val userService = new UserServices(executionContext)
  val userRoutes = new UserRoutes(userService)

  val routes = concat(
    userRoutes.routes,
    userRoutes.healthRoutes,
    pathSingleSlash {
      get {
        complete("Welcome to User Management API! Try /api/users or /health")
      }
    }
  )

  val bindingFuture = Http()
    .newServerAt("0.0.0.0", 8080)
    .bind(routes)

  bindingFuture.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("🚀 Server online at http://{}:{}/", address.getHostString, address.getPort)
      system.log.info("📋 Available endpoints:")
      system.log.info("  GET    /health")
      system.log.info("  GET    /api/users")
      system.log.info("  POST   /api/users")
      system.log.info("  GET    /api/users/{{id}}")
      system.log.info("  PUT    /api/users/{{id}}")
      system.log.info("  DELETE /api/users/{{id}}")

      Runtime.getRuntime.addShutdownHook(new Thread(() => {
        system.log.info("🔄 Received shutdown signal. Starting graceful shutdown...")
        val shutdownF = binding.terminate(30.seconds).flatMap { _ =>
          system.log.info("✅ Server shutdown completed")
          system.whenTerminated
        }
        Await.result(shutdownF, 40.seconds)
      }))

    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.whenTerminated
  }

  // Keep the application running indefinitely
  Await.result(system.whenTerminated, Duration.Inf)
}
