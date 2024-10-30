package deque;

import java.util.Comparator;

public class MaxArrayDeque <T> extends ArrayDeque<T> {
    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    public T max(Comparator<T> comparator) {
        if(super.isEmpty()){
            return null;
        }
        T max = get(0);
        for(int i = 1; i < super.size(); i++){
            if(comparator.compare(get(i), max) > 0){
                max = get(i);
            }
        }
        return max;
    }

    /*  returns the maximum element in the deque as governed by the previously given Comparator */
    /* returns null if array is empty or comparator hasn't been initialized*/
//    public T max(){
//        if(super.isEmpty()){
//            return null;
//        }
//        if(this.comparator == null){
//            return null;
//        }
//        T max = get(0);
//        for(int i = 1; i < super.size(); i++){
//            if(comparator.compare(get(i), max) > 0){
//                max = get(i);
//            }
//        }
//        return max;
//    }
    // inefficient approach when u have already design the whale

    // better apporach
    public T max(){
        return max(comparator);
    }

    private boolean equals(Object o, Comparator<T> comparator){
        if (!(o instanceof MaxArrayDeque)){
            return false;
        }
        MaxArrayDeque<T> other = (MaxArrayDeque<T>) o;
        for(int i = 0; i < super.size() ; i++){
            if(!this.comparator.equals(other.get(i))){
                return false;
            }
        }
        return true;
    }

}
