package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {

    @Test
    public void testBuggyAList() {
        AListNoResizing<Integer> Correct = new AListNoResizing<>();
        BuggyAList<Integer> Incorrect = new BuggyAList<>();

        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                Correct.addLast(randVal);
                Incorrect.addLast(randVal);
                assertEquals((Integer) randVal, Correct.getLast());
            } else if (operationNumber == 1) {
                // size
                int size1 = Correct.size();
                int size2 = Incorrect.size();
                assertEquals(size1, size2);
            }
                //getLast
                else if (operationNumber == 2) {
                    if (Correct.size() >0 && Incorrect.size()>0) {
                        int lastVal1 = Incorrect.getLast();
                        int lastVal2 = Correct.getLast();
                        assertEquals(lastVal1, lastVal2);
                    }
                }
                //remove
                else if (operationNumber == 3) {
                    if (Incorrect.size() >0&&Correct.size()>0) {
                        int remove1 = Incorrect.removeLast();
                        int remove2 = Correct.removeLast();
                        assertEquals(remove1, remove2);
                    }

            }

        }
    }
}
