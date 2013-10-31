package com.dbschools.mgb.snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import com.dbschools.mgb.schema.AppSchema

trait MusicianFromReq {
  val opMusician = for {
    musicianId  <- S.param("id").flatMap(asInt).toOption
    musician    <- AppSchema.musicians.lookup(musicianId)
  } yield musician
}
