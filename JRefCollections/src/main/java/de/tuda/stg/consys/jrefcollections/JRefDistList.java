package de.tuda.stg.consys.jrefcollections;


import de.tuda.stg.consys.checker.qual.Inconsistent;
import de.tuda.stg.consys.checker.qual.Strong;
import de.tuda.stg.consys.checker.qual.Weak;
import de.tuda.stg.consys.objects.ConsistencyLevel;
import de.tuda.stg.consys.objects.ReplicaSystem;
import de.tuda.stg.consys.objects.japi.JConsistencyLevel;
import de.tuda.stg.consys.objects.japi.JRef;
import de.tuda.stg.consys.objects.japi.JReplicaSystem;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.function.Predicate;

public class JRefDistList implements Serializable {

    public JRef head;

    public JRef current;

    public JRef tail;

    public ConsistencyLevel level;


    //Represents the Size the list thinks it is, used as an approximation to determine if
    // it should be traversed from front to back or back to front
    private int GuessedSize;

    //TODO: Add unsynced and synced functions, rerun benchmarks

    //TODO: Add option to search for exact Replicas or References

    public <T> JRefDistList(ConsistencyLevel level) {
        current = head; this.level = level;
        tail = head;
    }

    public int size(boolean sync){
        if(sync)
            reSyncHead();
        if(head == null){
            System.out.println("List is Empty");
            return 0;
        }else{
            current = head;
            return sizeRec(1, sync);
        }
    }

    private int sizeRec(int cnt, boolean sync){
        if(sync)
            current.sync();
        System.out.println("Element: " + current.toString());
        if(current.getField("next") == null){
            GuessedSize = cnt;
            return cnt;
        }else{
            current = (JRef) current.getField("next");
            return sizeRec(cnt + 1, sync);
        }
    }

    public <T> JRef<T> removeIndex(int index, boolean sync){
        current = head;
        if(findIndexFront(index, sync)){
            JRef<T> ret = (JRef) current.getField("content");
            JRef prev = ((JRef) current.getField("prev"));
            JRef next = ((JRef) current.getField("next"));

            if(prev == null && next == null){
                head = null; tail = null;
            }else if(prev == null){
                head = next;
            }else if(next == null){
                tail = prev;
            }else{
                prev.setField("next", next);
                next.setField("prev", prev);
            }
            GuessedSize--;
            return ret;
        }else{
            return null;
        }
    }

    public <T> JRef<T> removeItem(JRef<T> item, boolean sync) {
        current = head;
        if(findItemFront(item, sync)){
            JRef<T> ret = (JRef) current.getField("content");
            JRef prev = ((JRef) current.getField("prev"));
            JRef next = ((JRef) current.getField("next"));

            if(prev == null && next == null){
                head = null; tail = null;
            }else if(prev == null){
                head = next;
            }else if(next == null){
                tail = prev;
            }else{
                prev.setField("next", next);
                next.setField("prev", prev);
            }
            GuessedSize--;
            return ret;
        }else{
            return null;
        }
    }


    public <T> boolean append(JRef<T> item, JReplicaSystem sys) {
        JRef<@Inconsistent DistNode> node = sys.replicate(new DistNode(item), level);

        if (tail == null) {
            head = node;
            tail = head;
            current = head;
        } else {
            tail.invoke("setNext",node);
            node.invoke("setPrev", tail);
            tail = node;
        }
        GuessedSize++;
        return true;
    }

    public <T> void insert(int index, JRef<T> item, JReplicaSystem sys, boolean sync) throws IndexOutOfBoundsException{
        JRef<@Inconsistent DistNode> node = sys.replicate(new DistNode(item), level);

        if(findIndexFront(index, sync)){
            if(current.getField("prev") == null){
                head = node; node.invoke("setNext",current);
                current.invoke("setPrev",node);
            }else{
                node.invoke("setPrev",current.getField("prev"));
                node.invoke("setNext",current);

                JRef prevprev = ((JRef) current.getField("prev"));
                prevprev.invoke("setNext", node);
                current.invoke("setPrev", node);
            }
            current = head;
        }else throw new IndexOutOfBoundsException("List index out of bounds");
    }


    public <T> JRef<T> getItem(JRef<T> item, boolean sync) throws Exception{
        if(!findItemFront(item, sync)){
            return null;
        }else{
            JRef<T> ret = (JRef) current.getField("content");
            current = head;
            return ret;
        }
    }

    private <T> boolean findItemFront(JRef<T> item, boolean sync){
        current = head;
        if(sync)
            reSyncHead();
        return recFindItemFront(item, sync);
    }

    private <T> boolean recFindItemFront(JRef<T> item, boolean sync){
        if(current == null){
            return false;
        }else{
            if(sync)
                current.sync();
            if(refEquals((JRef) current.getField("content"),item)){
                return true;
            }else{
                current =(JRef) current.getField("next");
                return recFindItemFront(item, sync);
            }
        }
    }


    public <T> JRef<T> getIndex(int index, boolean sync) throws Exception {

        if(!findIndexFront(index, sync)){
            return null;
        }else{
            JRef<T> ret = (JRef) current.getField("content");
            current = head;
            return ret;
        }
    }

    private boolean findIndexFront(int index, boolean sync){
        current = head;
        if(sync)
            reSyncHead();
        return recFindIndexFront(index, sync);
    }

    private boolean recFindIndexFront(int index, boolean sync){
        if(current == null){
            return false;
        }else{
            if(sync)
                current.sync();
            if(index == 0){
                return true;
            }else{
                current =(JRef) current.getField("next");
                return recFindIndexFront(index - 1, sync);
            }
        }
    }

    /*
     * Syncing Function that checks if the head has been changed in a remote system.
     * Syncs the head until the new head is found.
     * Only works if the list knows there exists an element in the list, if not the JRefDistList object needs
     * to be synced.
     */
    private boolean reSyncHead(){
        if(head != null){
            head.sync();
            if(head.getField("prev") != null){
                while(head.getField("prev") != null){
                    head = (JRef) head.getField("prev");
                    head.sync();
                }
                System.out.println("Yes");
                return true;
            }
        }
        return false;
    }


    /*
     * Syncing Function that checks if the tail has been changed in a remote system.
     * Syncs the tail until the new tail is found.
     * Only works if the list knows there exists an element in the list, if not the JRefDistList object needs
     * to be synced.
     */
    private boolean reSyncTail(){
        if(tail != null){
            tail.sync();
            if(tail.getField("next") != null){
                while(tail.getField("next") != null){
                    tail = (JRef) tail.getField("next");
                    tail.sync();
                }
                return true;
            }
        }
        return false;
    }

    /*
     * A janky method to check if two refs refer to the same item.
     */
    private boolean refEquals(JRef ref1, JRef ref2){
        return (ref1.toString().equals(ref2.toString()));
    }

    /*
     * Type safety is not guaranteed if the list contains elements of different types.
     * Use with caution.
     */
    public <T> LinkedList getNonReplicatedSublist(Predicate<T> function, boolean sync){
        LinkedList<T> retList = new LinkedList<T>();
        if(sync)
            reSyncHead();
        current = head;
        while(current != null){
            T currContent = (T) current.getField("content");
            if(function.test(currContent)){
                retList.add(currContent);
            }
        }
        return retList;
    }

    public <T> LinkedList getAsNonReplicatedLinkedList(boolean sync){
        LinkedList<T> retList = new LinkedList<T>();
        if(sync)
            reSyncHead();
        current = head;
        while(current != null){
            retList.add((T) current.getField("content"));
        }
        return retList;
    }

    /*
     * Searchers the list using a predicate
     */
    public <T> T search(Predicate<T> function, boolean sync){
        if(sync)
            reSyncHead();
        current = head;
        while(current != null){
            T currContent = (T) current.getField("content");
            if(function.test(currContent)){
                return currContent;
            }
        }
        return null;
    }

    public boolean clear(){
        head = null; tail = null;
        current = head;
        return true;
    }
}

class DistNode<T> implements Serializable {
    public JRef<T> prev;

    public JRef<T> next;

    public JRef<T> content;

    public DistNode(JRef<T> content) {
        this.content = content;
    }

    public void setPrev(JRef prev) {
        this.prev = prev;
    }

    public void setNext(JRef next) {
        this.next = next;
    }
}

