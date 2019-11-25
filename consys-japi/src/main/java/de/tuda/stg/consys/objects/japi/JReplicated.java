package de.tuda.stg.consys.objects.japi;

import de.tuda.stg.consys.objects.Replicated;
import de.tuda.stg.consys.objects.actors.AkkaReplicaSystem;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Created on 26.07.19.
 *
 * @author Mirko Köhler
 */
public interface JReplicated extends Replicated, Serializable {
	//Instances of this interface have to define the following field:
	//public transient AkkaReplicaSystem<String> replicaSystem = null;


	default Optional<JReplicaSystem> getSystem() {

		Field field = null;
		try {
			field = this.getClass().getField("replicaSystem");
			field.setAccessible(true);

			AkkaReplicaSystem<String> replicaSystem = (AkkaReplicaSystem<String>) field.get(this);

			if (replicaSystem != null)
				return Optional.of(new JReplicaSystemAkkaImpl(replicaSystem));


		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

}