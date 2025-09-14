package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
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

  val apiDocs = """
    |<html>
    |<head>
    |    <title>User Management API Documentation</title>
    |    <style>
    |        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
    |        h1 { color: #333; border-bottom: 2px solid #333; padding-bottom: 10px; }
    |        h2 { color: #444; margin-top: 30px; }
    |        .endpoint { background: #f5f5f5; padding: 15px; margin: 10px 0; border-radius: 5px; }
    |        .method { color: #fff; padding: 4px 8px; border-radius: 3px; font-weight: bold; }
    |        .get { background-color: #61affe; }
    |        .post { background-color: #49cc90; }
    |        .put { background-color: #fca130; }
    |        .delete { background-color: #f93e3e; }
    |        .description { margin: 10px 0; }
    |        .example { background: #e8f5e9; padding: 10px; border-radius: 3px; font-family: monospace; }
    |    </style>
    |</head>
    |<body>
    |    <h1>🚀 User Management API Documentation</h1>
    |
    |    <div class="endpoint">
    |        <span class="method get">GET</span> <code>/health</code>
    |        <div class="description">Check if the API is up and running.</div>
    |        <div class="example">curl -X GET "https://scala-backendv1.onrender.com/health"</div>
    |    </div>
    |
    |    <div class="endpoint">
    |        <span class="method get">GET</span> <code>/api/users</code>
    |        <div class="description">Get all users in the system.</div>
    |        <div class="example">curl -X GET "https://scala-backendv1.onrender.com/api/users"</div>
    |    </div>
    |
    |    <div class="endpoint">
    |        <span class="method post">POST</span> <code>/api/users</code>
    |        <div class="description">Create a new user.</div>
    |        <div class="example">curl -X POST "https://scala-backendv1.onrender.com/api/users" \<br>
    |            -H "Content-Type: application/json" \<br>
    |            -d '{"name": "John Doe", "email": "john@example.com", "age": 30}'</div>
    |    </div>
    |
    |    <div class="endpoint">
    |        <span class="method get">GET</span> <code>/api/users/{id}</code>
    |        <div class="description">Get a specific user by ID.</div>
    |        <div class="example">curl -X GET "https://scala-backendv1.onrender.com/api/users/123"</div>
    |    </div>
    |
    |    <div class="endpoint">
    |        <span class="method put">PUT</span> <code>/api/users/{id}</code>
    |        <div class="description">Update an existing user.</div>
    |        <div class="example">curl -X PUT "https://scala-backendv1.onrender.com/api/users/123" \<br>
    |            -H "Content-Type: application/json" \<br>
    |            -d '{"name": "John Updated", "email": "john.updated@example.com", "age": 31}'</div>
    |    </div>
    |
    |    <div class="endpoint">
    |        <span class="method delete">DELETE</span> <code>/api/users/{id}</code>
    |        <div class="description">Delete a user by ID.</div>
    |        <div class="example">curl -X DELETE "https://scala-backendv1.onrender.com/api/users/123"</div>
    |    </div>
    |
    |    <h2>Response Format</h2>
    |    <p>All responses are in JSON format. Success responses have a 2xx status code. Errors have appropriate 4xx or 5xx status codes with error messages.</p>
    |
    |    <h2>Example Response</h2>
    |    <div class="example">
    |    {
    |        "id": "123",
    |        "name": "John Doe",
    |        "email": "john@example.com",
    |        "age": 30
    |    }
    |    </div>
    |</body>
    |</html>
  """.stripMargin

  val routes = concat(
    userRoutes.routes,
    userRoutes.healthRoutes,
    pathSingleSlash {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, apiDocs))
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
        try {
          val shutdownF = binding.terminate(30.seconds).flatMap { _ =>
            system.log.info("✅ Server shutdown completed")
            system.whenTerminated
          }
          Await.result(shutdownF, 40.seconds)
        } catch {
          case ex: Throwable =>
            system.log.error("❌ Error during shutdown: {}", ex.getMessage)
            system.terminate()
        }
      }))

    case Failure(ex) =>
      system.log.error("❌ Failed to bind HTTP endpoint, terminating system: {}", ex.getMessage)
      system.terminate()
      System.exit(1)  // Ensure container restarts on binding failure
  }

  // Keep the application running indefinitely
  try {
    Await.result(system.whenTerminated, Duration.Inf)
  } catch {
    case ex: Throwable =>
      system.log.error("❌ Fatal error, system terminating: {}", ex.getMessage)
      System.exit(1)  // Ensure container restarts on fatal errors
  }
}
