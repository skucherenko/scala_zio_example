package org.organization.db.repository

import org.organization.db.DbContext.ctx._
import org.organization.db.DbContext._
import org.organization.db.model.Person
import zio.{Task, ZIO}

trait PersonRepository {
  def getAllPersons: Task[List[Person]] = ZIO.attempt {
    val q = ctx.quote {
      persons
    }
    ctx.run(q)
  }
}
