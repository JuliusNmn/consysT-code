package de.tudarmstadt.consistency.demo.schema;

import de.tudarmstadt.consistency.checker.qual.Strong;

/**
 * Created on 05.03.19.
 *
 * @author Mirko Köhler
 */
public class ObjA {

	public int f;

	public ObjA() {
		f = 0;
	}

	public String inc() {
		f = f + 1;
		return "done";
	}

	public int incBy(int x) {
		f = f + x;
		return f;
	}
}