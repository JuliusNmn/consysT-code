package de.tudarmstadt.consistency.replobj.actors

import de.tudarmstadt.consistency.replobj.ConsistencyLevel.Strong
import de.tudarmstadt.consistency.replobj.actors.Data.A
import de.tudarmstadt.consistency.replobj.{ConsistencyLevel, Ref, actors}
import org.scalatest.{Outcome, fixture}

import scala.reflect.runtime.universe._

/**
	* Created on 09.04.19.
	*
	* @author Mirko Köhler
	*/
trait AkkaReplicaSystemSuite { this: fixture.FunSuite =>

	override type FixtureParam = F

	case class F(replicas : Array[AkkaReplicaSystem[String]]) {
		def apply(index : Int) : AkkaReplicaSystem[String] = replicas(index)

		def refs[T <: AnyRef : TypeTag](name : String, consistencyLevel : ConsistencyLevel) : Array[Ref[String, T]] =
			replicas.map(replica => replica.ref[T]("a", consistencyLevel))

	}

	def numOfReplicas : Int

	def populate(replica : AkkaReplicaSystem[String], index : Int) : Unit = { }

	override def withFixture(testCode : OneArgTest) : Outcome = {
		val replicaSystems : Array[AkkaReplicaSystem[String]] = new Array(numOfReplicas)

		try {
			for (i <- replicaSystems.indices) {
				replicaSystems(i) = actors.createReplicaSystem(2552 + i)
			}

			for (i <- replicaSystems.indices; j <- replicaSystems.indices) {
				if (i != j) replicaSystems(i).addOtherReplica("127.0.0.1", 2552 + j)
			}

			for (i <- replicaSystems.indices) {
				populate(replicaSystems(i), i)
			}

			val result = testCode(F(replicaSystems))
			result
		} finally {
			replicaSystems.foreach { replica => if (replica != null) replica.close() }
		}
	}

}