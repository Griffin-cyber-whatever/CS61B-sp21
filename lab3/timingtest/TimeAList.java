package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
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
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // Your times should be in seconds. You should use the Stopwatch class.
        int testRounds = 10000000;
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        for (int i = 1000; i <= testRounds; i *= 2) {
            tableDateN(i, Ns, times, opCounts);
        }
        printTimingTable(Ns, times, opCounts);
    }


    // collect data based on the first argument N, modify the given object with the collected data
    public static void tableDateN(int n, AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        AList<Integer> N = new AList<>();
        Stopwatch timer = new Stopwatch();
        for (int i = 0; i < n; i++) {
            N.addLast(1);
        }
        double timeInSeconds = timer.elapsedTime();
        Ns.addLast(n);
        times.addLast(timeInSeconds);
        opCounts.addLast(n);
    }
}
