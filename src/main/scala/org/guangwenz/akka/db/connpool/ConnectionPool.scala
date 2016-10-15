package org.guangwenz.akka.db.connpool

import java.sql.Connection

import akka.actor.{Actor, ActorLogging}
import com.jolbox.bonecp.{BoneCP, BoneCPConfig}

/**
  * Created by shrek.zhou on 9/8/16.
  */

object ConnectionPool {

  sealed trait DbCmd

  case object ShutdownConnectionPool extends DbCmd

  case class GetDbConnection(reqId: String) extends DbCmd

  case class PrintDbStats(reqId: String) extends DbCmd

  case class DbConnectionRetrieved(reqId: String, conn: Connection)

  case class GetDbConnectionException(reqId: String, msg: String) extends Exception

}

class ConnectionPool(className: Option[String] = None, url: Option[String] = None, username: Option[String] = None, password: Option[String] = None) extends Actor with ActorLogging {

  import ConnectionPool._

  var connectionPool: Option[BoneCP] = None

  override def postStop(): Unit = {
    super.postStop()
    connectionPool match {
      case Some(cp) =>
        log.info("Shutting down BoneCP pool")
        cp.shutdown()
        connectionPool = None
      case None =>
    }
  }

  override def preStart(): Unit = {
    super.preStart()
    try {
      Class.forName(className.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.className")))

      val config = new BoneCPConfig()
      config.setJdbcUrl(url.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.url")))
      config.setUsername(username.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.username")))
      config.setPassword(password.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.password")))
      config.setMinConnectionsPerPartition(context.system.settings.config.getInt("guangwenz.akka.db.jdbc.min-connections-per-partition"))
      config.setMaxConnectionsPerPartition(context.system.settings.config.getInt("guangwenz.akka.db.jdbc.max-connections-per-partition"))
      config.setPartitionCount(context.system.settings.config.getInt("guangwenz.akka.db.jdbc.partition-count"))
      connectionPool = Some(new BoneCP(config))
    } catch {
      case ex: Exception =>
        log.error(ex.getLocalizedMessage, ex)
    }
  }

  def receive = {
    case GetDbConnection(reqId) =>
      connectionPool match {
        case Some(cp) =>
          log.info("Connection pool is ready, sending back connection.")
          sender ! DbConnectionRetrieved(reqId, cp.getConnection)
        case None =>
          log.warning("No connection pool created")
          sender ! GetDbConnectionException(reqId, "Connection pool is not ready")
      }
    case PrintDbStats(reqId) =>
      connectionPool match {
        case Some(cp) =>
          log.info(s"CacheHitRatio:${cp.getStatistics.getCacheHitRatio}")
          log.info(s"CacheHits:${cp.getStatistics.getCacheHits}")
          log.info(s"CacheMiss:${cp.getStatistics.getCacheMiss}")
          log.info(s"ConnectionsRequested:${cp.getStatistics.getConnectionsRequested}")
          log.info(s"ConnectionWaitTimeAvg:${cp.getStatistics.getConnectionWaitTimeAvg}")
          log.info(s"CumulativeConnectionWaitTime:${cp.getStatistics.getCumulativeConnectionWaitTime}")
          log.info(s"CumulativeStatementExecutionTime:${cp.getStatistics.getCumulativeStatementExecutionTime}")
          log.info(s"StatementExecuteTimeAvg:${cp.getStatistics.getStatementExecuteTimeAvg}")
          log.info(s"StatementPrepareTimeAvg:${cp.getStatistics.getStatementPrepareTimeAvg}")
          log.info(s"StatementsCached:${cp.getStatistics.getStatementsCached}")
          log.info(s"StatementsExecuted:${cp.getStatistics.getStatementsExecuted}")
          log.info(s"StatementsPrepared:${cp.getStatistics.getStatementsPrepared}")
          log.info(s"TotalCreatedConnections:${cp.getStatistics.getTotalCreatedConnections}")
          log.info(s"TotalFree:${cp.getStatistics.getTotalFree}")
          log.info(s"TotalLeased:${cp.getStatistics.getTotalLeased}")
          sender ! "Done"
        case None =>
          log.warning("No connection pool created")
          sender ! "Fail"
      }
    case ShutdownConnectionPool =>
      if (connectionPool.isDefined)
        connectionPool.get.shutdown()
  }
}
