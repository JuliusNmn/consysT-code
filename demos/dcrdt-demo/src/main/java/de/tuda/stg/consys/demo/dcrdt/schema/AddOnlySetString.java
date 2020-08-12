package de.tuda.stg.consys.demo.dcrdt.schema;

import de.tuda.stg.consys.core.akka.Delta;
import de.tuda.stg.consys.core.akka.DeltaCRDT;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author = Kris Frühwein, Julius Näumann
 * Add Only Set of Strings
 */
public class AddOnlySetString extends DeltaCRDT implements Serializable {
    // todo implement serializable!!!

    private Set<String> set = new HashSet<String>();

    /**
     * Constructor
     */
    public AddOnlySetString() {
        System.out.println("constructor");
    }


    /**
     * adds a String to the Set
     * @param str String that should be added
     * @return Delta Object with the information which String was added
     */
    public Delta addElement(String str) {
        System.out.println("Adding String " + str);
        set.add(str);
        Set<String> s = new HashSet<String>();

        s.add(str);
        System.out.println("TRANSMITTING DELTA");
        return new Delta(s);
    }

    /**
     * merges the current Set with incoming delta messages
     * @param other delta message
     */
    @Override
    public void merge(Object other) {
        if (other instanceof Set) {
            Set<String> s = (Set<String>) other;

            System.out.println("received delta. merging");

            set.addAll(s);
        }

        System.out.println("current state:" + toString());
    }

    @Override
    public String toString() {
        String s = "";
        for (String k : set){
            s = s + k + ",";
        }
        return s;
    }
}