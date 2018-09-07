package de.tudarmstadt.consistency.store.shim

import de.tudarmstadt.consistency.store.shim.EventRef.{TxRef, UpdateRef}


/**
	* Created on 04.09.18.
	*
	* @author Mirko Köhler
	*/
sealed trait Event[Id, Key, Data] {
	/*
		Theoretically, dependencies only need to contain the id.
		However, we also include the key in order to have more
		efficient Cassandra reads (key is the partitioning key in the
		data table).
	 */
	def id :Id
	def readDependencies : Set[UpdateRef[Id, Key]]
	def txDependency : Option[TxRef[Id]]

	def dependencies : Set[EventRef[Id, Key]] =
		readDependencies ++ txDependency

	def toRef : EventRef[Id, Key]
}



object Event {
	//Note: val dependencies does not contain the txid.
	case class Update[Id, Key, Data](id : Id, key : Key, data : Data, txDependency : Option[TxRef[Id]], readDependencies : Set[UpdateRef[Id, Key]]) extends Event[Id, Key, Data] {
		def toRef : UpdateRef[Id, Key] = UpdateRef(id, key)
	}
	object Update {
		def apply[Id, Key, Data](id : Id, key : Key, data : Data, txDependency : Option[Id], readDependencies : (Id, Key)*) : Update[Id, Key, Data] =
			Update(id, key, data, txDependency.map(TxRef(_)), readDependencies.toSet[(Id, Key)].map(t => UpdateRef[Id, Key](t._1,t._2)))
	}

	case class Tx[Id, Key, Data](id : Id, readDependencies : Set[UpdateRef[Id, Key]]) extends Event[Id, Key, Data] {
		override def txDependency : Option[TxRef[Id]] = None
		override def toRef : TxRef[Id] = TxRef(id)
	}
	object Tx {
		def apply[Id, Key, Data](id : Id, readDependencies : (Id, Key)*) : Tx[Id, Key, Data] =
			Tx(id, readDependencies.toSet[(Id, Key)].map(t => UpdateRef(t._1,t._2)))
	}
}

