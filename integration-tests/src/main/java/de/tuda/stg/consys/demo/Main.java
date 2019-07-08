package de.tuda.stg.consys.demo;


import de.tuda.stg.consys.demo.schema.ObjA;
import de.tuda.stg.consys.demo.schema.ObjB;
import de.tuda.stg.consys.checker.qual.Strong;
import de.tuda.stg.consys.checker.qual.Weak;
import de.tuda.stg.consys.objects.japi.JConsistencyLevel;
import de.tuda.stg.consys.objects.japi.JRef;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created on 29.05.18.
 *
 * @author Mirko Köhler
 */
public class Main {

	public static void main(String... args) throws Exception {
		example1();
	}

	public static void example1() throws Exception {
		JRef<@Strong ObjA> ref1Strong = Replicas.replicaSystem1.replicate("os", new ObjA(), JConsistencyLevel.STRONG);
		JRef<@Weak ObjA> ref1Weak = Replicas.replicaSystem1.replicate("ow", new ObjA(), JConsistencyLevel.WEAK);

		JRef<@Strong ObjA> ref2Strong = Replicas.replicaSystem2.ref("os", (Class<@Strong ObjA>) ObjA.class, JConsistencyLevel.STRONG);
		JRef<@Weak ObjA> ref2Weak = Replicas.replicaSystem2.ref("ow", (Class<@Weak ObjA>) ObjA.class, JConsistencyLevel.WEAK);


		ref1Strong.remote().f = 34;
		ref1Weak.remote().f = 42;
		ref1Strong.remote().f = 42;

		ref1Strong.remote().inc();
		ref1Strong.remote().incBy(4 + 21);
		ref1Strong.remote().incBy(4 + (21 * 13) );


		System.out.println("ref1Strong.f = "  + ref1Strong.remote().f);
		System.out.println("ref2Strong.f = "  + ref2Strong.remote().f);

		System.out.println("ref1Weak.f = "  + ref1Weak.remote().f);
		System.out.println("ref2Weak.f = "  + ref2Weak.remote().f);

		ref2Weak.syncAll();

		System.out.println("ref1Weak.f = "  + ref1Weak.remote().f);
		System.out.println("ref2Weak.f = "  + ref2Weak.remote().f);

		ref1Strong.setField("f", ref1Weak.remote().f);

		Replicas.replicaSystem1.close();
		Replicas.replicaSystem2.close();
	}

// Desugared version of example1
//	public static void example1() throws Exception {
//
//		JRef<@Strong ObjA> ref1Strong = replicaSystem1.replicate("os", new ObjA(), JConsistencyLevel.STRONG);
//		JRef<@Strong ObjA> ref2Strong = replicaSystem2.ref("os", (Class<@Strong ObjA>) ObjA.class, JConsistencyLevel.STRONG);
//
//		JRef<@Weak ObjA> ref1Weak = replicaSystem1.replicate("ow", new ObjA(), JConsistencyLevel.WEAK);
//		JRef<@Weak ObjA> ref2Weak = replicaSystem2.ref("ow", (Class<@Weak ObjA>) ObjA.class, JConsistencyLevel.WEAK);
//
//
//		ref1Strong.setField("f", 34);
//		ref1Weak.setField("f", 42);
//		ref1Strong.setField("f", 42);
//
//		ref1Strong.invoke("inc");
//		ref1Strong.invoke("incBy", 4 + 21);
//		ref1Strong.invoke("incBy", new Object[] {} );
//
//
//		System.out.println("ref1Strong.f = "  + ref1Strong.getField("f"));
//		System.out.println("ref2Strong.f = "  + ref2Strong.getField("f"));
//
//		System.out.println("ref1Weak.f = "  + ref1Weak.getField("f"));
//		System.out.println("ref2Weak.f = "  + ref2Weak.getField("f"));
//
//		ref2Weak.syncAll();
//
//		System.out.println("ref1Weak.f = "  + ref1Weak.getField("f"));
//		System.out.println("ref2Weak.f = "  + ref2Weak.getField("f"));
//
//		ref1Strong.setField("f", ref1Weak.getField("f"));
//
//		replicaSystem1.close();
//		replicaSystem2.close();
//	}


	public static void example2() throws Exception {


		JRef<@Strong ObjA> a1 = Replicas.replicaSystem1.replicate("a", new ObjA(), JConsistencyLevel.STRONG);
		JRef<@Weak ObjB> b1 = Replicas.replicaSystem1.replicate("b", new ObjB(a1), JConsistencyLevel.WEAK);

		JRef<@Strong ObjA> a2 = Replicas.replicaSystem2.ref("a", (Class<@Strong ObjA>) ObjA.class, JConsistencyLevel.STRONG);
		JRef<@Weak ObjB> b2 = Replicas.replicaSystem2.ref("b", (Class<@Weak ObjB>) ObjB.class, JConsistencyLevel.WEAK);

		b1.remote().incAll();
		b2.remote().incAll();

		System.out.println("#1");
		System.out.println(
			"a1.f = " + a1.remote().f + ", " +
			"a2.f = " + a2.remote().f + ", " +
			"b1.g = " + b1.remote().g + ", " +
			"b2.g = " + b2.remote().g
		);


		b2.syncAll();

		System.out.println("#2");
		System.out.println(
			"a1.f = " + a1.remote().f + ", " +
			"a2.f = " + a2.remote().f + ", " +
			"b1.g = " + b1.remote().g + ", " +
			"b2.g = " + b2.remote().g
		);

		Replicas.replicaSystem1.close();
		Replicas.replicaSystem2.close();
	}

	public static void example2Parallel() throws Exception {


		JRef<@Strong ObjA> a1 = Replicas.replicaSystem1.replicate("a", new ObjA(), JConsistencyLevel.STRONG);
		JRef<@Weak ObjB> b1 = Replicas.replicaSystem1.replicate("b", new ObjB(a1), JConsistencyLevel.WEAK);

		JRef<@Strong ObjA> a2 = Replicas.replicaSystem2.ref("a", (Class<@Strong ObjA>) ObjA.class, JConsistencyLevel.STRONG);
		JRef<@Weak ObjB> b2 = Replicas.replicaSystem2.ref("b", (Class<@Weak ObjB>) ObjB.class, JConsistencyLevel.WEAK);


		ExecutorService exec = Executors.newFixedThreadPool(4);
		Future<?> fut1 = exec.submit(
			() -> b1.remote().incAll()
		);
		Future<?> fut2 = exec.submit(
			() -> b2.remote().incAll()
		);

		exec.shutdown();
		exec.awaitTermination(10, TimeUnit.SECONDS);


		System.out.println("#1");
		System.out.println(
			"a1.f = " + a1.remote().f + ", " +
				"a2.f = " + a2.remote().f + ", " +
				"b1.g = " + b1.remote().g + ", " +
				"b2.g = " + b2.remote().g
		);



		b2.syncAll();

		System.out.println("#2");
		System.out.println(
			"a1.f = " + a1.remote().f + ", " +
				"a2.f = " + a2.remote().f + ", " +
				"b1.g = " + b1.remote().g + ", " +
				"b2.g = " + b2.remote().g
		);


		Replicas.replicaSystem1.close();
		Replicas.replicaSystem2.close();
	}


}