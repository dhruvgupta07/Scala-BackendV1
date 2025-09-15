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
    |        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; background-color: #f8f9fa; }
    |        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    |        h1 { color: #333; border-bottom: 2px solid #333; padding-bottom: 10px; }
    |        h2 { color: #444; margin-top: 30px; }
    |        .endpoint { background: #ffffff; padding: 20px; margin: 15px 0; border-radius: 8px; border: 1px solid #e1e4e8; }
    |        .method { color: #fff; padding: 4px 8px; border-radius: 3px; font-weight: bold; display: inline-block; min-width: 60px; text-align: center; }
    |        .get { background-color: #61affe; }
    |        .post { background-color: #49cc90; }
    |        .put { background-color: #fca130; }
    |        .delete { background-color: #f93e3e; }
    |        .description { margin: 10px 0; color: #555; }
    |        .example { background: #f8f9fa; padding: 15px; border-radius: 5px; font-family: monospace; margin: 10px 0; border: 1px solid #e1e4e8; position: relative; }
    |        .copy-button {
    |            position: absolute;
    |            top: 5px;
    |            right: 5px;
    |            background-color: #6c757d;
    |            color: white;
    |            border: none;
    |            border-radius: 3px;
    |            padding: 4px 8px;
    |            cursor: pointer;
    |            font-size: 12px;
    |        }
    |        .copy-button:hover { background-color: #5a6268; }
    |        .copy-button.copied { background-color: #28a745; }
    |        .command { margin-right: 50px; }
    |    </style>
    |    <script>
    |        function copyCommand(element, command) {
    |            navigator.clipboard.writeText(command).then(() => {
    |                element.textContent = 'Copied!';
    |                element.classList.add('copied');
    |                setTimeout(() => {
    |                    element.textContent = 'Copy';
    |                    element.classList.remove('copied');
    |                }, 2000);
    |            }).catch(err => {
    |                console.error('Failed to copy:', err);
    |                element.textContent = 'Failed!';
    |                setTimeout(() => {
    |                    element.textContent = 'Copy';
    |                }, 2000);
    |            });
    |        }
    |    </script>
    |</head>
    |<body>
    |    <div class="container">
    |        <h1>🚀 User Management API Documentation</h1>
    |
    |        <div class="endpoint">
    |            <span class="method get">GET</span> <code>/health</code>
    |            <div class="description">Check if the API is up and running.</div>
    |            <div class="example">
    |                <div class="command">curl -X GET "https://scala-backendv1.onrender.com/health"</div>
    |                <button class="copy-button" onclick="copyCommand(this, 'curl -X GET \"https://scala-backendv1.onrender.com/health\"')">Copy</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method get">GET</span> <code>/api/users</code>
    |            <div class="description">Get all users in the system.</div>
    |            <div class="example">
    |                <div class="command">curl -X GET "https://scala-backendv1.onrender.com/api/users"</div>
    |                <button class="copy-button" onclick="copyCommand(this, 'curl -X GET \"https://scala-backendv1.onrender.com/api/users\"')">Copy</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method post">POST</span> <code>/api/users</code>
    |            <div class="description">Create a new user.</div>
    |            <div class="example">
    |                <div class="command">curl -X POST "https://scala-backendv1.onrender.com/api/users" \\
    |                    -H "Content-Type: application/json" \\
    |                    -d '{"name": "John Doe", "email": "john@example.com", "age": 30}'</div>
    |                <button class="copy-button" onclick="copyCommand(this, 'curl -X POST \"https://scala-backendv1.onrender.com/api/users\" -H \"Content-Type: application/json\" -d \'{\"name\": \"John Doe\", \"email\": \"john@example.com\", \"age\": 30}\'')">Copy</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method get">GET</span> <code>/api/users/{id}</code>
    |            <div class="description">Get a specific user by ID.</div>
    |            <div class="example">
    |                <div class="command">curl -X GET "https://scala-backendv1.onrender.com/api/users/123"</div>
    |                <button class="copy-button" onclick="copyCommand(this, 'curl -X GET \"https://scala-backendv1.onrender.com/api/users/123\"')">Copy</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method put">PUT</span> <code>/api/users/{id}</code>
    |            <div class="description">Update an existing user.</div>
    |            <div class="example">
    |                <div class="command">curl -X PUT "https://scala-backendv1.onrender.com/api/users/123" \\
    |                    -H "Content-Type: application/json" \\
    |                    -d '{"name": "John Updated", "email": "john.updated@example.com", "age": 31}'</div>
    |                <button class="copy-button" onclick="copyCommand(this, 'curl -X PUT \"https://scala-backendv1.onrender.com/api/users/123\" -H \"Content-Type: application/json\" -d \'{\"name\": \"John Updated\", \"email\": \"john.updated@example.com\", \"age\": 31}\'')">Copy</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method delete">DELETE</span> <code>/api/users/{id}</code>
    |            <div class="description">Delete a user by ID.</div>
    |            <div class="example">
    |                <div class="command">curl -X DELETE "https://scala-backendv1.onrender.com/api/users/123"</div>
    |                <button class="copy-button" onclick="copyCommand(this, 'curl -X DELETE \"https://scala-backendv1.onrender.com/api/users/123\"')">Copy</button>
    |            </div>
    |        </div>
    |
    |        <h2>Response Format</h2>
    |        <p>All responses are in JSON format. Success responses have a 2xx status code. Errors have appropriate 4xx or 5xx status codes with error messages.</p>
    |
    |        <h2>Example Response</h2>
    |        <div class="example">
    |            <div class="command">{
    |    "id": "123",
    |    "name": "John Doe",
    |    "email": "john@example.com",
    |    "age": 30
    |}</div>
    |        </div>
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
