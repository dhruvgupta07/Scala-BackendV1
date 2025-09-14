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
    |        .example { background: #f8f9fa; padding: 15px; border-radius: 5px; font-family: monospace; margin: 10px 0; border: 1px solid #e1e4e8; }
    |        .try-it { margin-top: 10px; }
    |        .try-it button { background-color: #4CAF50; color: white; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
    |        .try-it button:hover { background-color: #45a049; }
    |        .note { background-color: #fff3cd; padding: 10px; border-radius: 4px; margin: 10px 0; }
    |        code { background: #f0f0f0; padding: 2px 5px; border-radius: 3px; }
    |        .copy-button { background-color: #6c757d; color: white; border: none; border-radius: 3px; padding: 4px 8px; margin-left: 10px; cursor: pointer; }
    |        .copy-button:hover { background-color: #5a6268; }
    |        .test-interface { margin-top: 20px; padding: 20px; background: #f8f9fa; border-radius: 8px; }
    |        input, textarea { width: 100%; padding: 8px; margin: 5px 0; border: 1px solid #ddd; border-radius: 4px; }
    |    </style>
    |    <script>
    |        function copyToClipboard(text) {
    |            navigator.clipboard.writeText(text).then(() => {
    |                alert('Command copied to clipboard!');
    |            });
    |        }
    |
    |        function sendRequest(method, endpoint, body = null) {
    |            const baseUrl = 'https://scala-backendv1.onrender.com';
    |            const url = baseUrl + endpoint;
    |            const options = {
    |                method: method,
    |                headers: {
    |                    'Content-Type': 'application/json'
    |                }
    |            };
    |            if (body) {
    |                options.body = body;
    |            }
    |            fetch(url, options)
    |                .then(response => response.json())
    |                .then(data => {
    |                    document.getElementById('response').value = JSON.stringify(data, null, 2);
    |                })
    |                .catch(error => {
    |                    document.getElementById('response').value = 'Error: ' + error.message;
    |                });
    |        }
    |    </script>
    |</head>
    |<body>
    |    <div class="container">
    |        <h1>🚀 User Management API Documentation</h1>
    |
    |        <div class="note">
    |            <strong>Important:</strong> These endpoints are REST APIs and should be called using appropriate HTTP tools (like curl, Postman) or programming languages.
    |            They cannot be accessed directly through a browser URL.
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method get">GET</span> <code>/health</code>
    |            <div class="description">Check if the API is up and running.</div>
    |            <div class="example">
    |                curl -X GET "https://scala-backendv1.onrender.com/health"
    |                <button class="copy-button" onclick="copyToClipboard('curl -X GET \"https://scala-backendv1.onrender.com/health\"')">Copy</button>
    |            </div>
    |            <div class="try-it">
    |                <button onclick="sendRequest('GET', '/health')">Try it!</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method get">GET</span> <code>/api/users</code>
    |            <div class="description">Get all users in the system.</div>
    |            <div class="example">
    |                curl -X GET "https://scala-backendv1.onrender.com/api/users"
    |                <button class="copy-button" onclick="copyToClipboard('curl -X GET \"https://scala-backendv1.onrender.com/api/users\"')">Copy</button>
    |            </div>
    |            <div class="try-it">
    |                <button onclick="sendRequest('GET', '/api/users')">Try it!</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method post">POST</span> <code>/api/users</code>
    |            <div class="description">Create a new user.</div>
    |            <div class="example">
    |                curl -X POST "https://scala-backendv1.onrender.com/api/users" \<br>
    |                -H "Content-Type: application/json" \<br>
    |                -d '{"name": "John Doe", "email": "john@example.com", "age": 30}'
    |                <button class="copy-button" onclick="copyToClipboard('curl -X POST \"https://scala-backendv1.onrender.com/api/users\" -H \"Content-Type: application/json\" -d \'{\"name\": \"John Doe\", \"email\": \"john@example.com\", \"age\": 30}\'')">Copy</button>
    |            </div>
    |            <div class="try-it">
    |                <button onclick="sendRequest('POST', '/api/users', JSON.stringify({
    |                    name: document.getElementById('userName').value,
    |                    email: document.getElementById('userEmail').value,
    |                    age: parseInt(document.getElementById('userAge').value)
    |                }))">Try it!</button>
    |                <div class="test-interface">
    |                    <input type="text" id="userName" placeholder="Name (e.g., John Doe)" />
    |                    <input type="email" id="userEmail" placeholder="Email (e.g., john@example.com)" />
    |                    <input type="number" id="userAge" placeholder="Age (e.g., 30)" />
    |                </div>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method get">GET</span> <code>/api/users/{id}</code>
    |            <div class="description">Get a specific user by ID.</div>
    |            <div class="example">
    |                curl -X GET "https://scala-backendv1.onrender.com/api/users/123"
    |                <button class="copy-button" onclick="copyToClipboard('curl -X GET \"https://scala-backendv1.onrender.com/api/users/123\"')">Copy</button>
    |            </div>
    |            <div class="try-it">
    |                <input type="text" id="getUserId" placeholder="Enter user ID" />
    |                <button onclick="sendRequest('GET', '/api/users/' + document.getElementById('getUserId').value)">Try it!</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method put">PUT</span> <code>/api/users/{id}</code>
    |            <div class="description">Update an existing user.</div>
    |            <div class="example">
    |                curl -X PUT "https://scala-backendv1.onrender.com/api/users/123" \<br>
    |                -H "Content-Type: application/json" \<br>
    |                -d '{"name": "John Updated", "email": "john.updated@example.com", "age": 31}'
    |                <button class="copy-button" onclick="copyToClipboard('curl -X PUT \"https://scala-backendv1.onrender.com/api/users/123\" -H \"Content-Type: application/json\" -d \'{\"name\": \"John Updated\", \"email\": \"john.updated@example.com\", \"age\": 31}\'')">Copy</button>
    |            </div>
    |            <div class="try-it">
    |                <input type="text" id="updateUserId" placeholder="Enter user ID to update" />
    |                <div class="test-interface">
    |                    <input type="text" id="updateName" placeholder="Updated name" />
    |                    <input type="email" id="updateEmail" placeholder="Updated email" />
    |                    <input type="number" id="updateAge" placeholder="Updated age" />
    |                </div>
    |                <button onclick="sendRequest('PUT', '/api/users/' + document.getElementById('updateUserId').value, JSON.stringify({
    |                    name: document.getElementById('updateName').value,
    |                    email: document.getElementById('updateEmail').value,
    |                    age: parseInt(document.getElementById('updateAge').value)
    |                }))">Try it!</button>
    |            </div>
    |        </div>
    |
    |        <div class="endpoint">
    |            <span class="method delete">DELETE</span> <code>/api/users/{id}</code>
    |            <div class="description">Delete a user by ID.</div>
    |            <div class="example">
    |                curl -X DELETE "https://scala-backendv1.onrender.com/api/users/123"
    |                <button class="copy-button" onclick="copyToClipboard('curl -X DELETE \"https://scala-backendv1.onrender.com/api/users/123\"')">Copy</button>
    |            </div>
    |            <div class="try-it">
    |                <input type="text" id="deleteUserId" placeholder="Enter user ID to delete" />
    |                <button onclick="sendRequest('DELETE', '/api/users/' + document.getElementById('deleteUserId').value)">Try it!</button>
    |            </div>
    |        </div>
    |
    |        <h2>Response</h2>
    |        <textarea id="response" rows="10" readonly style="width: 100%; margin-top: 10px;" placeholder="Response will appear here..."></textarea>
    |
    |        <h2>Response Format</h2>
    |        <p>All responses are in JSON format. Success responses have a 2xx status code. Errors have appropriate 4xx or 5xx status codes with error messages.</p>
    |
    |        <h2>Example Response</h2>
    |        <div class="example">
    |        {
    |            "id": "123",
    |            "name": "John Doe",
    |            "email": "john@example.com",
    |            "age": 30
    |        }
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
