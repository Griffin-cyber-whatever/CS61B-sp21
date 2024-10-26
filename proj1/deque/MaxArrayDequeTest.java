package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MaxArrayDequeTest {
    @Test
    public void IntegerTestComparator() {
        MaxArrayDeque<Integer> deque = new MaxArrayDeque<>();
        deque.addLast(3);
        deque.addLast(5);
        deque.addLast(2);
        deque.addLast(7);

        IntegerComparator comparator = new IntegerComparator();
        assertEquals(7, (int) deque.max(comparator));
    }

    @Test
    public void StringTestComparator() {
        StringComparator comparator = new StringComparator();
        MaxArrayDeque<String> deque = new MaxArrayDeque<>(comparator);
        deque.addLast("z");
        deque.addLast("Hello");
        deque.addLast("World");

        assertEquals("Hello", deque.max());
    }
}


class IntegerComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        return Integer.compare(o1, o2); // Compare integers directly
    }
}

class StringComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        int s1 = o1.length();
        int s2 = o2.length();
        return Integer.compare(s1, s2);
    }
}



