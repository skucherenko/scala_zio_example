package org.organization.db

import org.organization.db.model.Person
import io.getquill.{MysqlJdbcContext, SnakeCase}

case object DbContext {
  lazy val ctx = new MysqlJdbcContext(SnakeCase, "mysql")

  import ctx._
  val persons = quote {
    querySchema[Person]("person")
  }
}
