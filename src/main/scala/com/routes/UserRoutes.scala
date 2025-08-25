//package com.routes
//
//import akka.http.scaladsl.server.Directives._
//import akka.http.scaladsl.server.Route
//import akka.http.scaladsl.model.StatusCodes
//import com.models.{JsonSupport, CreateUserRequest}
//import com.services.UserServices
//import scala.concurrent.ExecutionContext
//import scala.util.{Success, Failure}
//
//class UserRoutes(userService: UserServices)(implicit ec: ExecutionContext) extends JsonSupport {
//
//  val routes: Route = pathPrefix("api" / "users") {
//    concat(
//      pathEndOrSingleSlash {
//        concat(
//          get {
//            onComplete(userService.getAllUsers()) {
//              case Success(users) => complete(users) // This should now work
//              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
//            }
//          },
//          post {
//            entity(as[CreateUserRequest]) { request =>
//              onComplete(userService.createUser(request)) {
//                case Success(user) => complete(StatusCodes.Created, user)
//                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
//              }
//            }
//          }
//        )
//      },
//      path(Segment) { userId =>
//        concat(
//          get {
//            onComplete(userService.getUserById(userId)) {
//              case Success(Some(user)) => complete(user)
//              case Success(None) => complete(StatusCodes.NotFound, "User not found")
//              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
//            }
//          },
//          put {
//            entity(as[CreateUserRequest]) { request =>
//              onComplete(userService.updateUser(userId, request)) {
//                case Success(Some(user)) => complete(user)
//                case Success(None) => complete(StatusCodes.NotFound, "User not found")
//                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
//              }
//            }
//          },
//          delete {
//            onComplete(userService.deleteUser(userId)) {
//              case Success(true) => complete(StatusCodes.NoContent)
//              case Success(false) => complete(StatusCodes.NotFound, "User not found")
//              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
//            }
//          }
//        )
//      }
//    )
//  }
//
//  val healthRoutes: Route = path("health") {
//    get {
//      complete("OK")
//    }
//  }
//}
package com.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import com.models.{JsonSupport, CreateUserRequest}
import com.services.UserServices
import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}

class UserRoutes(userService: UserServices)(implicit ec: ExecutionContext) extends JsonSupport {

  val routes: Route = pathPrefix("api" / "users") {
    concat(
      // GET /api/users - List all users
      // POST /api/users - Create new user
      pathEndOrSingleSlash {
        concat(
          get {
            onComplete(userService.getAllUsers()) {
              case Success(users) => complete(users)
              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
            }
          },
          post {
            entity(as[CreateUserRequest]) { request =>
              onComplete(userService.createUser(request)) {
                case Success(user) => complete(StatusCodes.Created, user)
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
              }
            }
          }
        )
      },

      // GET /api/users/{id} - Get user by ID
      // PUT /api/users/{id} - Update user
      // DELETE /api/users/{id} - Delete user
      path(Segment) { userId =>
        concat(
          get {
            onComplete(userService.getUserById(userId)) {
              case Success(Some(user)) => complete(user)
              case Success(None) => complete(StatusCodes.NotFound, "User not found")
              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
            }
          },
          put {
            entity(as[CreateUserRequest]) { request =>
              onComplete(userService.updateUser(userId, request)) {
                case Success(Some(user)) => complete(user)
                case Success(None) => complete(StatusCodes.NotFound, "User not found")
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
              }
            }
          },
          delete {
            onComplete(userService.deleteUser(userId)) {
              case Success(true) => complete(StatusCodes.NoContent)
              case Success(false) => complete(StatusCodes.NotFound, "User not found")
              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
            }
          }
        )
      }
    )
  }

  // Bonus: Health check route
  val healthRoutes: Route = path("health") {
    get {
      complete("OK")
    }
  }
}
