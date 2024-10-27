package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        /*Create an SLList.
          Add N items to the SLList.
          Start the timer.
          Perform M getLast operations on the SLList.
          Check the timer. This gives the total time to complete all M operations.*/
        // M = 10000 for step 4 of the procedure described above.
        int m = 10000;
        int testRounds = 64000;
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        for (int i = 1000; i <= testRounds; i *= 2) {
            tableDateN(i, m, Ns, times, opCounts);
        }
        printTimingTable(Ns, times, opCounts);
    }

    public static void tableDateN(int n, int m, AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        Stopwatch timer = new Stopwatch();
        SLList<Integer> list = new SLList<>();
        for (int i = 0; i < n; i++) {
            list.addLast(1);
        }
        for (int i = 0; i < m; i++) {
            list.getLast();
        }
        double time = timer.elapsedTime();
        Ns.addLast(n);
        times.addLast(time);
        opCounts.addLast(m);
    }

}
