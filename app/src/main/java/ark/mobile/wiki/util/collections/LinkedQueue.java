package ark.mobile.wiki.util.collections;

public class LinkedQueue<E> {

    private class Node{
        final E value;
        Node next;

        Node(E value) {
            this.next = null;
            this.value = value;
        }

        boolean hasNext(){return next != null;}

    }

    private Node first;
    private Node last;

    public LinkedQueue(){}

    public boolean add(E item){
        if(item == null) return false;
        if(first == null){
            first = new Node(item);
            last = first;
        }else{
            Node node = new Node(item);
            last.next = node;
            last = node;
        }
        return true;
    }

    public boolean remove(E item){
        if(first == null) return false;
        if(first.value.equals(item)){
            first = first.next;
            return true;
        }
        Node node = first;
        Node prev = first;
        while(!node.value.equals(item)){
            prev = node;
            node = node.next;
            if(node.next == null) return false;
        }
        prev.next = node.next;
        return true;
    }

    public E getFirst(){
        return first.value;
    }

    public E getAndRemoveFirst(){
        if(first == null) return null;
        E value = first.value;
        first = first.next;
        return value;
    }

    public E getLast(){
        return last.value;
    }

}
