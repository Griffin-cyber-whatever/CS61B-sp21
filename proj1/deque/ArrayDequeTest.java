package deque;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ArrayDequeTest{

    @Test
    public void addFirstTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        assertTrue("A newly initialized AD should be empty", ad1.isEmpty());

        ad1.addFirst(1);
        assertTrue(!ad1.isEmpty());
        assertTrue(ad1.get(0) == 1);
        ad1.addFirst(2);
        assertTrue(ad1.get(1) == 1);
        assertTrue(ad1.get(0) == 2);

        ArrayDeque<Integer> ad2 = new ArrayDeque<Integer>();
        ad2.addFirst(3);
        ad2.addFirst(4);
        ad2.addFirst(5);
        ad2.addFirst(6);
        ad2.addFirst(7);

        assertTrue(ad2.get(4) == 3);
        assertTrue(ad2.get(3) == 4);
        assertTrue(ad2.get(2) == 5);
        assertTrue(ad2.get(1) == 6);
        assertTrue(ad2.get(0) == 7);
        assertTrue(ad2.indexvalidator(0) == 3);
        assertTrue(ad2.indexvalidator(1) == 4);
    }

    public void getTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ad1.addFirst(1);
        ad1.addFirst(2);
        ad1.addLast(3);
        assertTrue(ad1.get(0) == 2);
        assertTrue(ad1.get(1) == 1);
        assertTrue(ad1.get(2) == 3);
    }

    public void addLastTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ad1.addLast(1);
        ad1.addLast(2);
        ad1.addLast(3);
        assertTrue(ad1.get(0) == 1);
        assertTrue(ad1.get(1) == 2);
        assertTrue(ad1.get(2) == 3);
        assertTrue(ad1.indexvalidator(0) == 0);
    }

    public void removeFirstTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ad1.addLast(1);
        ad1.addLast(2);
        ad1.addLast(3);
        assertTrue(ad1.removeFirst() == 1);
        assertTrue(ad1.removeFirst() == 2);
        assertTrue(ad1.removeFirst() == 3);

        ad1.addFirst(1);
        ad1.addFirst(2);
        ad1.addFirst(3);
        ad1.addLast(3);
        assertTrue(ad1.removeFirst() == 3);
        assertTrue(ad1.removeFirst() == 2);
        assertTrue(ad1.removeFirst() == 1);
        assertTrue(ad1.removeFirst() == 3);
    }

    public void removeLastTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ad1.addLast(1);
        ad1.addLast(2);
        ad1.addLast(3);
        assertTrue(ad1.removeFirst() == 3);
        assertTrue(ad1.removeFirst() == 2);
        assertTrue(ad1.removeFirst() == 1);
    }

    public void mutateFirstTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ad1.addLast(1);
        ad1.addLast(2);
        ad1.addLast(3);
        ad1.addLast(4);
        ad1.addLast(5);
        ad1.addLast(6);
        ad1.addLast(7);
        ad1.addLast(8);
        assertTrue(ad1.get(7) == 8);

        ad1.addLast(9);
        assertTrue(ad1.removeFirst() == 1);
        assertTrue(ad1.removeFirst() == 2);
        assertTrue(ad1.removeFirst() == 3);
        assertTrue(ad1.removeFirst() == 4);
        assertTrue(ad1.removeFirst() == 5);
        assertTrue(ad1.removeFirst() == 6);
        assertTrue(ad1.removeFirst() == 7);
        assertTrue(ad1.removeFirst() == 8);
        assertTrue(ad1.removeFirst() == 9);
        assertTrue(ad1.removeFirst() == null);
    }

}
