package deque;

public class ArrayDeque<T> implements Deque<T>{
    /* size will keep tracking on how many elements are we actually using
    *  capacity will keep tracking on the actual capacity of the array*/
    int size;
    int capacity;
    T[] array;
    int start;
    int next;

    /* start keep tracking on the next available initial index of this array -> start+1 keep tracking on the index of this array which count left to right*/
    /* next keep tracking on the next available next index of this array -> next-1 keep tracking on the index of this array which count from 0 to right*/
    public ArrayDeque() {
        array = (T[]) new Object[8];
        size = 0;
        capacity = 8;
        start = capacity - 1;
        next = 0;
    }

    /* change the size of array*/
    public void resize(int newCapacity) {
        T[] newArray = (T[]) new Object[newCapacity];

        // Calculate where the elements are in the current array and copy them over
        int current = (start + 1) % capacity; // Start from the first actual element
        for (int i = 0; i < size; i++) {
            newArray[i] = array[current];
            current = (current + 1) % capacity; // Move circularly within bounds
        }

        // Update references and variables to match the new array
        array = newArray;
        capacity = newCapacity;
        start = newCapacity - 1; // Start points to one position before the first element
        next = size; // Next points to the first empty slot after the last element
    }



    /*Adds an item of type T to the front of the deque. You can assume that item is never null.*/
    @Override
    public void addFirst(T item){
        if(size == capacity){
            resize(capacity*2);
        }
        array[start] = item;
        start = (start - 1 + capacity) % capacity;
        size++;
    }

    /*Adds an item of type T to the back of the deque. You can assume that item is never null.*/
    @Override
    public void addLast(T item){
        if(size == capacity){
            resize(capacity*2);
        }
        array[next] = item;
        next = (next + 1) % capacity; // Wrap around if necessary
        size++;
    }

    /*  return the index in the instance vaiable array*/
    public int indexvalidator(int index){
        int i = index + start + 1;
        if(i < capacity){
            return i;
        }else{return i % capacity;}
    }

    @Override
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
    @Override
    public T removeFirst(){
        if(size == 0){
            return null;
        }
        int a = (start + 1 + capacity) % capacity;
        T temp = array[a];
        array[a] = null;
        start = a;
        size--;
        memoryEfficiency();
        return temp;
    }

    @Override
    public T removeLast(){
        if(size == 0){
            return null;
        }
        int a = (next - 1 + capacity) % capacity;
        T temp = array[a];
        array[a] = null;
        next = a;
        size--;
        memoryEfficiency();
        return temp;
    }

    @Override
    public T get(int index){
        return array[indexvalidator(index)];
    }

    @Override
    public int size(){
        return size;
    }
    
    public boolean equals(Object o){
        if (!(o instanceof ArrayDeque)){
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