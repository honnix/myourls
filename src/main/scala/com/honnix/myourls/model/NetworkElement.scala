package com.honnix.myourls.model

import org.bson.types.ObjectId
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoId, MongoRecord}
import net.liftweb.mongodb.{JsonObjectMeta, JsonObject}
import net.liftweb.mongodb.record.field.{MongoJsonObjectListField, MongoListField}

class FunctionMeta extends MongoRecord[FunctionMeta] with MongoId[FunctionMeta] {
  def meta = FunctionMeta

  object typee extends StringField(this, 100)

  object vendor extends StringField(this, 100)

  object version extends StringField(this, 100)

  override def toString = typee.value + ":" + vendor.value + ":" + version.value
}

object FunctionMeta extends FunctionMeta with MongoMetaRecord[FunctionMeta] {
  override def collectionName = "function"

  def createRecord = new FunctionMeta
}

case class Function(var typee: String, var vendor: String, var version: String) extends JsonObject[Function] {
  val id = ObjectId.get.toString

  def meta = Function

  override def toString = typee + ":" + vendor + ":" + version
}

object Function extends JsonObjectMeta[Function]

case class ConnectionParameter(val name: String, val displayName: String, val typee: String, var value: String) extends JsonObject[ConnectionParameter] {
  def meta = ConnectionParameter
}

object ConnectionParameter extends JsonObjectMeta[ConnectionParameter]

class ConnectionMeta extends MongoRecord[ConnectionMeta] with MongoId[ConnectionMeta] {
  def meta = ConnectionMeta

  object typee extends StringField(this, 100)

  object parameters extends MongoJsonObjectListField[ConnectionMeta, ConnectionParameter](this, ConnectionParameter)
}

object ConnectionMeta extends ConnectionMeta with MongoMetaRecord[ConnectionMeta] {
  override def collectionName = "connection"

  def createRecord = new ConnectionMeta
}

case class Connection(var typee: String, var parameters: List[ConnectionParameter]) extends JsonObject[Connection] {
  val id = ObjectId.get.toString

  def meta = Connection
}

object Connection extends JsonObjectMeta[Connection]

class NetworkElement extends MongoRecord[NetworkElement] with MongoId[NetworkElement] {
  def meta = NetworkElement

  def exists(function: Function) = functions.value.exists(_.toString == function.toString)

  def exists(connection: Connection) = connections.value.exists(_.typee == connection.typee)

  def add(function: Function) {
    functions(function :: functions.value)
  }

  def add(connection: Connection) {
    connections(connection :: connections.value)
  }

  def remove(function: Function) {
    functions(functions.value.remove(_.toString == function.toString))
  }

  def remove(connection: Connection) {
    connections(connections.value.remove(_.typee == connection.typee))
  }

  object name extends StringField(this, 100)

  object functions extends MongoJsonObjectListField[NetworkElement, Function](this, Function)

  object connections extends MongoJsonObjectListField[NetworkElement, Connection](this, Connection)
}

object NetworkElement extends NetworkElement with MongoMetaRecord[NetworkElement] {
  override def collectionName = "ne"

  def createRecord = new NetworkElement
}
