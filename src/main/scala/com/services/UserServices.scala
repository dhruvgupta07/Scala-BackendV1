package com.services

import com.models.{CreateUserRequest, User}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Future

case class UserServices( ec : scala.concurrent.ExecutionContext) {

  // Placeholder for user service methods
  // This is where you would define methods to handle user-related operations
  // such as creating, updating, deleting users, etc.

  // Example method signature:
  // def createUser(user: User): Future[User] = {
  //   // Implementation goes here
  // }

  // def getUser(id: String): Future[Option[User]] = {
  //   // Implementation goes here
  // }
  private val logger = LoggerFactory.getLogger("UserServices")
  private val  user  = mutable.Map[String, User]()

  def createUser(paramUser : CreateUserRequest) : Future[User] = {
    paramUser match {
      case CreateUserRequest(name, email,age) =>
        val id = java.util.UUID.randomUUID().toString
        val newUser = User(id, name, email,age)
        user.put(id, newUser)
        logger.info(s"User created: $newUser")
        Future.successful(newUser)
      case _ =>
        logger.error("Invalid user creation request")
        Future.failed(new IllegalArgumentException("Invalid user creation request"))
    }
  }
  def getAllUsers(): Future[List[User]] = {
    logger.info("Fetching all users")
    Future.successful(user.values.toList)
  }

  def getUserById(id: String): Future[Option[User]] = {
    logger.info(s"Fetching user by ID: $id")
    Future.successful(user.get(id))
  }

  def updateUser(id: String, paramUser: CreateUserRequest): Future[Option[User]] = {
    user.get(id) match {
      case Some(existingUser) =>
        val updatedUser = existingUser.copy(name = paramUser.name, email = paramUser.email)
        user.put(id, updatedUser)
        logger.info(s"User updated: $updatedUser")
        Future.successful(Some(updatedUser))
      case None =>
        logger.warn(s"User not found for ID: $id")
        Future.successful(None)
    }
  }

  def deleteUser(id: String): Future[Boolean] = {
    user.remove(id) match {
      case Some(_) =>
        logger.info(s"User deleted with ID: $id")
        Future.successful(true)
      case None =>
        logger.warn(s"User not found for ID: $id")
        Future.successful(false)
    }
  }
}
