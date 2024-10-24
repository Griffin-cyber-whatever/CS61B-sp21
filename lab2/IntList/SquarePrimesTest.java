package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst,false);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testIsPrime(){
        assertTrue(Primes.isPrime(7));
        assertTrue(Primes.isPrime(13));
        assertTrue(Primes.isPrime(2));
        assertTrue(Primes.isPrime(3));
        assertTrue(Primes.isPrime(89));

    }

    @Test
    public void testSquarePrimes() {
        // only one element
        IntList lst1 = IntList.of(17);
        boolean changed1 = IntListExercises.squarePrimes(lst1,false);
        assertEquals("289", lst1.toString());
        assertTrue(changed1);

        // need to change multiple primes in a roll
        IntList lst2 = IntList.of(2,2,3);
        boolean changed2 = IntListExercises.squarePrimes(lst2,false);
        assertEquals("4 -> 4 -> 9", lst2.toString());
        assertTrue(changed2);
    }
}
