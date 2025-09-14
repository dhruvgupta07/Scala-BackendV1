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
import scala.io.StdIn

object Main extends App {

  // Phase 1: INITIALIZATION
  // =====================

  // Create the ActorSystem - this is the foundation of your app
  implicit val system: ActorSystem[Nothing] = ActorSystem(
    Behaviors.empty,
    "user-management-system"  // Choose a meaningful name
  )
  implicit val executionContext: ExecutionContext = system.executionContext

  // Initialize your business logic layer
  val userService = new UserServices(executionContext)
  val userRoutes = new UserRoutes(userService)

  // Combine all routes into the application's route tree
  val routes = concat(
    userRoutes.routes,
    userRoutes.healthRoutes,
    pathSingleSlash {
      get {
        complete("Welcome to User Management API! Try /api/users or /health")
      }
    }
  )

  // Phase 2: STARTUP
  // ===============

  // Start the HTTP server (this returns immediately with a Future)
  val bindingFuture: Future[Http.ServerBinding] = Http()
    .newServerAt("0.0.0.0", 8080)  // Changed from localhost to 0.0.0.0 to accept external connections
    .bind(routes)                    // bind our route tree

  // Handle server startup - this is crucial for debugging
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
      system.log.info("Press RETURN to stop...")

    case Failure(ex) =>
      system.log.error("❌ Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()  // Clean shutdown on startup failure
  }

  // Phase 3: RUNTIME
  // ===============
  // (Server is now running and handling requests)

  // Phase 4: SHUTDOWN SETUP
  // ======================
  setupGracefulShutdown(bindingFuture)

  /**
   * Sets up graceful shutdown handling
   * This is production-ready shutdown logic!
   */
  private def setupGracefulShutdown(bindingFuture: Future[Http.ServerBinding]): Unit = {
    // Simple shutdown trigger for development (press ENTER)
    // In production, this would be replaced with signal handling
    StdIn.readLine()

    system.log.info("🔄 Received shutdown signal. Starting graceful shutdown...")

    // Step 1: Unbind the server port (stop accepting new connections)
    val shutdown = for {
      binding <- bindingFuture
      _ = system.log.info("📡 Unbinding server port...")
      _ <- binding.unbind()  // Stop accepting new connections immediately
      _ = system.log.info("⏳ Waiting for in-flight requests to complete (30s timeout)...")
      _ <- binding.terminate(hardDeadline = 30.seconds)  // Wait for existing requests
    } yield ()

    // Step 2: Handle shutdown completion
    shutdown.onComplete { result =>
      result match {
        case Success(_) =>
          system.log.info("✅ All requests completed. Terminating actor system...")
        case Failure(ex) =>
          system.log.error("⚠️  Shutdown completed with errors: {}", ex.getMessage)
      }

      // Step 3: Terminate the ActorSystem (this shuts down all actors, thread pools, etc.)
      system.terminate()
    }
  }
}
