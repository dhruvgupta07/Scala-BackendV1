name := "Projectx"
// Sets the name of the project. Used by sbt for project identification.

version := "0.1.0"
// Defines the current version of the project. Useful for release and dependency management.

resolvers += "Akka library repository".at("https://repo.akka.io/maven")
// Adds an external repository for resolving Akka dependencies not found in default repositories.

val akkaVersion = "2.6.21"
// Declares the version of Akka libraries to ensure consistency across dependencies.

val akkaHttpVersion = "10.2.10"
// Declares the version of Akka HTTP libraries for consistent dependency management.

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  // Adds Akka Typed Actor library for building concurrent, distributed, and resilient message-driven applications.

  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // Adds Akka Streams library for processing and handling streams of data.

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  // Adds Akka HTTP library for building HTTP-based services.

  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion, // Essential
  // Adds integration between Akka HTTP and Spray JSON for JSON serialization/deserialization.

  "ch.qos.logback" % "logback-classic" % "1.2.11",
  // Adds Logback Classic for logging support.

  "io.spray" %% "spray-json" % "1.3.6"
  // Adds Spray JSON library for JSON parsing and formatting.
)