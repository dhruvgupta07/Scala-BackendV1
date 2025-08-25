package com.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol._
import spray.json._

// Define your case classes
case class User(id: String, name: String, email: String, age: Int)
case class CreateUserRequest(name: String, email: String, age: Int)

// Create JsonSupport trait
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User.apply)
  implicit val createUserRequestFormat: RootJsonFormat[CreateUserRequest] = jsonFormat3(CreateUserRequest.apply)

  // This is automatically provided by DefaultJsonProtocol for List[T] when T has a format
  // But we can be explicit if needed:
  // implicit val userListFormat: RootJsonFormat[List[User]] = listFormat[User]
}
