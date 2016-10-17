Akka extension for db connection pool
=====================================

How to add it to your sbt project.
-------------
add following to akka config file to load the extension.

`
akka.library-extensions += "org.guangwenz.akka.db.connpool.DbConnectionPoolExtension"
`
then add following to the application.conf

`
guangwenz.akka.db {
  jdbc {
    className = "${YOUR_JDBC_DRIVER_CLS}"
    url = "${JDBC_URL}"
    username = "${JDBC_USER}"
    password = "${JDBC_PASS}"
  }
}
akka.library-extensions += "org.guangwenz.akka.db.connpool.DbConnectionPoolExtension"
`
check reference.conf for more config options.

Example code to use it
----------------------
//TODO
