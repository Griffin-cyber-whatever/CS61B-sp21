package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

    @Test
    public void testIsEmpty() {
        StudentArrayDeque<Integer> s1 = new StudentArrayDeque();
        ArrayDequeSolution<Integer> s2 = new ArrayDequeSolution();
        int size = 0;
        assertTrue(s1.isEmpty());
        assertTrue(s2.isEmpty());
        s1.addFirst(10);
        s2.addFirst(20);
        assertFalse(s1.isEmpty());
        assertFalse(s2.isEmpty());
        s1.removeFirst();
        s2.removeFirst();
        assertTrue(s1.isEmpty());
        assertTrue(s2.isEmpty());
    }

    @Test
    public void testRandomly() {
        StudentArrayDeque<Integer> s1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> s2 = new ArrayDequeSolution<>();
        int size = 0;
        int rounds = 100;
        String error = "";
            // we use the random method to decide which method to choose for testing
            // 0 for addFirst
            // 1 for addLast
            // 2 for removeFirst
            // 3 for removeLast
            // it will cause NullPointerException when removing from an empty deque
            // so we will only invoke 3 or 4 when size is greater than 0
            // s2 the correct answer for expect argument
            // s1 the student's answer for actual argument
        for (int i = 0; i < rounds; i++) {
            if (size > 0) {
                int random = (int) (Math.random() * 4);
                if (random == 0) {
                    size ++;
                    s1.addFirst(i);
                    s2.addFirst(i);
                    error += String.format("addFirst(%d)\n",i);
                    assertEquals(error, s2.get(0), s1.get(0));
                } else if (random == 1) {
                    size ++;
                    s1.addLast(i);
                    s2.addLast(i);
                    int last = s1.size() - 1;
                    error += String.format("addLast(%d)\n",i);
                    assertEquals(error, s2.get(last), s1.get(last));
                } else if (random == 2) {
                    size --;
                    error += "removeFirst()\n";
                    assertEquals(error, s2.removeFirst(), s1.removeFirst());
                }else{
                    size --;
                    error += "removeLast()\n";
                    assertEquals(error, s2.removeLast(), s1.removeLast());
                }
            } else if  (s1.isEmpty() && s2.isEmpty()){
                int random = (int) (Math.random() * 2);
                if (random == 0){
                    size ++;
                    s1.addFirst(i);
                    s2.addFirst(i);
                    error += String.format("addFirst(%d)\n",i);
                    assertEquals(error, s2.get(0), s1.get(0));
                }else if (random == 1){
                    size ++;
                    s1.addLast(i);
                    s2.addLast(i);
                    int last = s1.size() - 1;
                    error += String.format("addLast(%d)\n",i);
                    assertEquals(error, s2.get(last), s1.get(last));
                }
            }
        }
    }

}

