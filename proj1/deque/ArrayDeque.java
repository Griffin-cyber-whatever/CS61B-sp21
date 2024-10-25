package deque;

public class ArrayDeque<T> {
    /* size will keep tracking on how many elements are we actually using
    *  capacity will keep tracking on the actual capacity of the array*/
    int size;
    int capacity;
    T[] array;
    int start;
    int next;

    /* start keep tracking on the start index of this array -> start-1 keep tracking on the index of this array which count from 0 to left*/
    /* next keep tracking on the next index of this array -> next+1 keep tracking on the index of this array which count from left to right*/
    public ArrayDeque() {
        array = (T[]) new Object[8];
        size = 0;
        capacity = 8;
        start = capacity - 1;
        next = 0;
    }

    public ArrayDeque(T[] array) {
        size = array.length;
        T[] newarray = (T[]) new Object[size+1];
        System.arraycopy(array, 0, newarray, 0, size);
        this.array = array;
        capacity = array.length;
        start = capacity-1;
        next = capacity-1;
    }

    /* change the size of array*/
    public void resize(int newCapacity) {
        if (newCapacity < capacity) {
            return;
        }
        T[] newArray = (T[]) new Object[newCapacity];
        System.arraycopy(array, 0, newArray, 0, next);
        int t = newCapacity-(size - next);
        for(int i = 0; i < size-next; i++) {
            newArray[t+i] = array[next+i];
        }
        array = newArray;
        start = newCapacity-(size - next);
    }

    /*Adds an item of type T to the front of the deque. You can assume that item is never null.*/
    public void addFirst(T item){
        if(size == capacity){
            resize(capacity+1);
        }
        array[start] = item;
        start--;
        size++;
    }

    /*Adds an item of type T to the back of the deque. You can assume that item is never null.*/
    public void addLast(T item){
        if(size == capacity){
            resize(capacity+1);
        }
        array[next] = item;
        next++;
        size++;
    }

    /*Returns true if deque is empty, false otherwise.*/
    public boolean isEmpty(){
        return size == 0;
    }

    /*  return the index in the instance vaiable array*/
    public int indexvalidator(int index){
        int i = index + start + 1;
        if(i < capacity){
            return i;
        }else{return i % capacity;}
    }

    public void printDeque(){
        if(size == 0){
            return ;
        }
        for(int i = 0; i < size-1; i++){
            System.out.print(array[indexvalidator(i)]);
        }
        System.out.println(array[indexvalidator(size-1)]);
    }

    public void memoryEfficiency(){
        if (size <= 16){
            return;
        }
        if ((double)size/capacity < 0.25){
            resize(size * 4 -1);
        }
    }

    // For arrays of length 16 or more, the number of elements in the array under 25% the length of the array, you should resize the size of the array down.
    public T removeFirst(){
        if(size == 0){
            return null;
        }
        T temp = array[start+1];
        array[start+1] = null;
        start++;
        start = start % capacity;
        size++;
        memoryEfficiency();
        return temp;
    }

    public T removeLast(){
        if(size == 0){
            return null;
        }
        T temp = array[next-1];
        array[next-1] = null;
        next--;
        size++;
        memoryEfficiency();
        return temp;
    }

    public T get(int index){
        return array[indexvalidator(index)];
    }

    public boolean equals(Object o){
        if (!(o instanceof LinkedListDeque)){
            return false;
        }
        ArrayDeque<T> other = (ArrayDeque<T>) o;
        for(int i = 0; i < size; i++){
            if(!(other.get(i)).equals(this.get(i))){
                return false;
            }
        }
        return true;
    }
}