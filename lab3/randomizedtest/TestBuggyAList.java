package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    // Your code should also compare the return values of every method which has a return value.
    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int sizeL = L.size();
                int sizeB = B.size();
                System.out.println("size: " + sizeL);
                assertEquals(sizeL, sizeB);
            } else if (operationNumber == 2) {
                // getLast
                if (L.size() == 0){
                    continue;
                }
                int lastL = L.getLast();
                int lastB = B.getLast();
                System.out.println("getLast: " + lastL);
                assertEquals(lastL, lastB);
            } else if (operationNumber == 3) {
                // removeLast
                if (L.size() == 0){
                    continue;
                }
                int removedL = L.removeLast();
                int removedB = B.removeLast();
                System.out.println("removeLast(" + removedL + ")");
                assertEquals(removedL, removedB);
            }
        }
    }
}
