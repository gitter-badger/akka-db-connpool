Akka extension for db connection pool
=====================================

How to add it to your sbt project.
-------------
add dependency to build.sbt

```scala
"org.guangwenz" %% "akka-db-connpool" % "1.0.0-SNAPSHOT",
```

then add following to the application.conf

```scala
guangwenz.akka.db {
  jdbc {
    className = "${YOUR_JDBC_DRIVER_CLS}"
    url = "${JDBC_URL}"
    username = "${JDBC_USER}"
    password = "${JDBC_PASS}"
  }
}
akka.library-extensions += "org.guangwenz.akka.db.connpool.DbConnectionPoolExtension"
```

check `reference.conf` for more config options.

Example code to use it
----------------------
//TODO
