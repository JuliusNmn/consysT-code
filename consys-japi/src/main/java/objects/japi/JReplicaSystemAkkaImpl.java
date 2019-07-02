package objects.japi;

import akka.actor.ActorSystem;
import de.tuda.stg.consys.checker.qual.Local;
import de.tuda.stg.consys.objects.ConsistencyLevel;
import de.tuda.stg.consys.objects.Ref;
import de.tuda.stg.consys.objects.ReplicaSystems;
import de.tuda.stg.consys.objects.actors.AkkaReplicaSystem;

/**
 * Created on 01.03.19.
 *
 * @author Mirko Köhler
 */
public class JReplicaSystemAkkaImpl implements JReplicaSystem {

	public final AkkaReplicaSystem<String> replicaSystem;


	public JReplicaSystemAkkaImpl(ActorSystem actorSystem) {
		replicaSystem = ReplicaSystems.fromActorSystem(actorSystem);
	}

	public JReplicaSystemAkkaImpl(String hostname, int port) {
		replicaSystem = ReplicaSystems.fromActorSystem(hostname, port);
	}

	public JReplicaSystemAkkaImpl(int port) {
		replicaSystem = ReplicaSystems.fromActorSystem(port);
	}

	@Override
	public <T> JRef<T> replicate(String addr, @Local T obj, ConsistencyLevel consistencyLevel) {
		Class<T> objCls = (Class<T>) obj.getClass();
		Ref<String, T> ref = replicaSystem.replicate(addr, obj, objCls, consistencyLevel);

		return new JRefImpl<>(ref);
	}

	@Override
	public <T> JRef<T> replicate(@Local T obj, ConsistencyLevel consistencyLevel) {
		Class<T> objCls = (Class<T>) obj.getClass();
		Ref<String, T> ref = replicaSystem.replicate(obj, objCls, consistencyLevel);

		return new JRefImpl<>(ref);
	}

	@Override
	public <T> JRef<T> ref(String addr, Class<T> objCls, ConsistencyLevel consistencyLevel) {
		Ref<String, T> ref = replicaSystem.ref(addr, objCls, consistencyLevel);
		return new JRefImpl<>(ref);
	}

	@Override
	public void addReplicaSystem(String hostname, int port) {
		replicaSystem.addOtherReplica(hostname, port);
	}


	@Override
	public void close() throws Exception {
		replicaSystem.close();
	}
}
