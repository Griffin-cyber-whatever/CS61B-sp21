package deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node<T> {
        public T item;
        public Node<T> next;
        public Node<T> prev;

        public Node(T item, Node<T> next){
            this.item = item;
            this.next = next;
        }

        public Node(T item){
            this.item = item;
        }
        public Node(){
            this.item = null;
        }
    }


    private int size;
    private Node<T> sentinel;
    /* The first item (if it exists) is at sentinel.next. */
    /* The last item (if it exists) is at sentinel.prev. */

    /*  Creates an empty linked list deque. */
    public LinkedListDeque(){
        this.sentinel = new Node<T>();
        this.sentinel.next = sentinel;
        this.sentinel.prev = sentinel;
        this.size = 0;
    }

    /* Adds an item of type T to the front of the deque. You can assume that item is never null. */
    @Override
    public void addFirst(T item){
        Node<T> tmp = new Node<>(item);
        tmp.next = sentinel.next;
        tmp.prev = sentinel;
        sentinel.next.prev = tmp;
        sentinel.next = tmp;
        this.size++;
    }

    /* append a new node at the end of current list which took constant time*/
    @Override
    public void addLast(T item){
        Node<T> tmp = new Node<>(item);
        tmp.prev = sentinel.prev;
        tmp.next = sentinel;
        sentinel.prev.next = tmp;
        sentinel.prev = tmp;
        this.size++;
    }

    /* Returns the number of items in the deque. */
    @Override
    public int size(){
        return this.size;
    }

    /*  Prints the items in the deque from first to last, separated by a space. Once all the items have been printed, print out a new line. */
    @Override
    public void printDeque(){
        if(size == 0){
            return ;
        }
        for(int i = 0; i < size-1; i++){
            System.out.print(get(i) + " ");
        }
        System.out.println(get(size-1));
    }

    /*  Removes and returns the item at the front of the deque. If no such item exists, returns null. */
    @Override
    public T removeFirst(){
        if (this.size <= 0){
            return null;
        } else {
            Node<T> tmp = this.sentinel.next;
            this.sentinel.next = sentinel.next.next;
            this.sentinel.next.prev = sentinel;
            size--;
            return tmp.item;
        }
    }

    /* Removes and returns the item at the back of the deque. If no such item exists, returns null. */
    @Override
    public T removeLast(){
        if (this.size <= 0){
            return null;
        }else {
            Node<T> tmp = sentinel.prev;
            sentinel.prev = sentinel.prev.prev;
            this.sentinel.prev.next = sentinel;
            this.size--;
            return tmp.item;
        }
    }

    /* return the ith element of the total list*/
    @Override
    public T get(int index){
        if (index < 0 || index >= this.size){
            return null;
        }
        /*  tmp keep tracking on the current node*/
        Node<T> tmp = sentinel.next;
        int count = 0;
        while(tmp != sentinel){
            if (count == index){
                break;
            }
            count++;
            tmp = tmp.next;
        }
        return tmp.item;
    }

    /* implement the same effect as previous get method instead of using the recursive version of it*/
    public T getRecursive(int index){
        return getRecursive(index, sentinel.next).item;
    }

    private Node<T> getRecursive(int index, Node<T> tmp){
        if(tmp == sentinel){
            return null;
        }
        if(index == 0){
            return tmp;
        }
    return getRecursive(index-1, sentinel.next);
    }

    /* Returns whether the parameter o is equal to the Deque. */
    public boolean equals(Object o){
        if (!(o instanceof LinkedListDeque)){
            return false;
        }
        LinkedListDeque<T> tmp = ((LinkedListDeque<T>)o);
        if(tmp.size() != this.size()){
            return false;
        }
        for(int i = 0; i < size; i++){
            if(!tmp.get(i).equals(this.get(i))){
                return false;
            }
        }
        return true;
    }

    private class DequeIterator implements Iterator<T> {
        private Node<T> current = sentinel;
        private int remainingElements;

        public DequeIterator(){
            this.current = current.next;
            this.remainingElements = size;
        }

        @Override
        public boolean hasNext(){
            return this.current != sentinel;
        }

        @Override
        public T next(){
            if (this.current == sentinel){
                throw new NoSuchElementException();
            }
            T ret = this.current.item;
            current = current.next;
            remainingElements--;
            return ret;
        }

        @Override
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterator<T> iterator(){
        return new DequeIterator();
    }
}