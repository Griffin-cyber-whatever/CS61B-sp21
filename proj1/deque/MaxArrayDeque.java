package deque;

import java.util.Comparator;

public class MaxArrayDeque <T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    public T max(Comparator<T> comparator) {
        if(size == 0){
            return null;
        }
        T max = array[indexvalidator(0)];
        for(int i = 1; i < size; i++){
            if(comparator.compare(array[indexvalidator(i)], max) > 0){
                max = array[indexvalidator(i)];
            }
        }
        return max;
    }

    /*  returns the maximum element in the deque as governed by the previously given Comparator */
    /* returns null if array is empty or comparator hasn't been initialized*/
    public T max(){
        if(size == 0){
            return null;
        }
        if(this.comparator == null){
            return null;
        }
        T max = array[indexvalidator(0)];
        for(int i = 1; i < size; i++){
            if(comparator.compare(array[indexvalidator(i)], max) > 0){
                max = array[indexvalidator(i)];
            }
        }
        return max;
    }

    private boolean equals(Object o, Comparator<T> comparator){
        if (!(o instanceof MaxArrayDeque)){
            return false;
        }
        MaxArrayDeque<T> other = (MaxArrayDeque<T>) o;
        for(int i = 0; i < size; i++){
            if(!this.comparator.equals(other.array[indexvalidator(i)])){
                return false;
            }
        }
        return true;
    }
}
