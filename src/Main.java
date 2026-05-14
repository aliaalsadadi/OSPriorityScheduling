/*
   Student Names and IDs:
   Ali Alaa Alsadadi 202305500
   Hussain Ali Kadhem 202302274
   Husain Yaser Ali Albaqlawa 202304049
   Husain Jasim Ashoor 202307311
 */
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;




public class Main {

    public static class Process {
        final int id;
        final int arrival;
        final int burst;
        final int priority;
        final int inputIndex;

        int remaining;
        int startTime = -1;
        int completionTime = -1;

        Process(int id, int arrival, int burst, int priority, int inputIndex) {
            this.id = id;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.inputIndex = inputIndex;
            this.remaining = burst;
        }

        int turnaroundTime() {
            return completionTime - arrival;
        }

        int responseTime() {
            return startTime - arrival;
        }

        int waitingTime() {
            return turnaroundTime() - burst;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("CPU Priority Scheduling with Round Robin");
        System.out.print("Enter quantum time (ms): ");
        int quantum = readPositiveInt(scanner);

        System.out.println("Enter processes as: id burst(ms) arrival(ms) priority (0 0 0 0 to finish)");
        List<Process> processes = new ArrayList<>();
        Map<Integer, Boolean> seenIds = new HashMap<>();
        int inputIndex = 0;

        while (true) {
            System.out.print("Process " + (inputIndex + 1) + ": ");
            int id = scanner.nextInt();
            int burst = scanner.nextInt();
            int arrival = scanner.nextInt();
            int priority = scanner.nextInt();

            if (id == 0 && arrival == 0 && burst == 0 && priority == 0) {
                break;
            }

            if (id <= 0 || arrival < 0 || burst <= 0) {
                System.out.println("Invalid input. id>0, arrival>=0, burst>0 required.");
                continue;
            }

            if (seenIds.containsKey(id)) {
                System.out.println("Duplicate process ID. Please enter a unique ID.");
                continue;
            }

            seenIds.put(id, true);
            processes.add(new Process(id, arrival, burst, priority, inputIndex));
            inputIndex++;
        }

        if (processes.isEmpty()) {
            System.out.println("No processes entered. Exiting.");
            return;
        }

        String gantt = runPriorityRoundRobin(processes, quantum);

        System.out.println();
        System.out.println("Gantt Chart (ms):");
        System.out.println(gantt);

        printMetrics(processes);
    }

    private static int readPositiveInt(Scanner scanner) {
        int value = scanner.nextInt();
        while (value <= 0) {
            System.out.print("Please enter a positive integer: ");
            value = scanner.nextInt();
        }
        return value;
    }

    private static String runPriorityRoundRobin(List<Process> processes, int quantum) {
        List<Process> byArrival = new ArrayList<>(processes);
        byArrival.sort(Comparator
                .comparingInt((Process p) -> p.arrival)
                .thenComparingInt(p -> p.inputIndex));

        TreeMap<Integer, ArrayDeque<Process>> readyQueues = new TreeMap<>();
        int time = 0;
        int completed = 0;
        int arrivalIndex = 0;

        List<String> ganttTokens = new ArrayList<>();
        boolean started = false;

        while (completed < processes.size()) {
            while (arrivalIndex < byArrival.size() && byArrival.get(arrivalIndex).arrival <= time) {
                enqueue(readyQueues, byArrival.get(arrivalIndex));
                arrivalIndex++;
            }

            if (readyQueues.isEmpty()) {
                if (arrivalIndex < byArrival.size()) {
                    int idleStart = time;
                    time = Math.max(time, byArrival.get(arrivalIndex).arrival);
                    if (time > idleStart) {
                        if (!started) {
                            ganttTokens.add(String.valueOf(idleStart));
                            started = true;
                        }
                        ganttTokens.add("Idle");
                        ganttTokens.add(String.valueOf(time));
                    }
                    continue;
                }
                break;
            }

            Process current = dequeueHighestPriority(readyQueues);
            if (current.startTime == -1) {
                current.startTime = time;
            }

            int runStart = time;
            int runFor = Math.min(quantum, current.remaining);
            current.remaining -= runFor;
            time += runFor;

            while (arrivalIndex < byArrival.size() && byArrival.get(arrivalIndex).arrival <= time) {
                enqueue(readyQueues, byArrival.get(arrivalIndex));
                arrivalIndex++;
            }

            if (!started) {
                ganttTokens.add(String.valueOf(runStart));
                started = true;
            }
            ganttTokens.add("P" + current.id);
            ganttTokens.add(String.valueOf(time));

            if (current.remaining == 0) {
                current.completionTime = time;
                completed++;
            } else {
                enqueue(readyQueues, current);
            }
        }

        return String.join(" - ", ganttTokens);
    }

    private static void enqueue(
            TreeMap<Integer, ArrayDeque<Process>> readyQueues,
            Process process
    ) {
        ArrayDeque<Process> queue = readyQueues.computeIfAbsent(
                process.priority,
                k -> new ArrayDeque<>()
        );
        queue.addLast(process);
    }

    private static Process dequeueHighestPriority(
            TreeMap<Integer, ArrayDeque<Process>> readyQueues
    ) {
        int priority = readyQueues.firstKey();
        ArrayDeque<Process> queue = readyQueues.get(priority);
        Process process = queue.removeFirst();
        if (queue.isEmpty()) {
            readyQueues.remove(priority);
        }
        return process;
    }

    private static void printMetrics(List<Process> processes) {
        double totalTurnaround = 0;
        double totalResponse = 0;
        double totalWaiting = 0;

        System.out.println();
        System.out.println("Per-Process Metrics (input order):");
        System.out.printf("%-6s%-12s%-10s%-10s%-10s%-10s%-10s%n",
                "ID", "Arrival(ms)", "Burst(ms)", "Priority", "TAT(ms)", "RT(ms)", "WT(ms)");

        for (Process process : processes) {
            int tat = process.turnaroundTime();
            int rt = process.responseTime();
            int wt = process.waitingTime();

            totalTurnaround += tat;
            totalResponse += rt;
            totalWaiting += wt;

            System.out.printf("p%-5d%-12d%-10d%-10d%-10d%-10d%-10d%n",
                    process.id, process.arrival, process.burst, process.priority, tat, rt, wt);
        }

        int count = processes.size();
        System.out.println();
        System.out.printf("Average Turnaround Time (ms): %.2f%n", totalTurnaround / count);
        System.out.printf("Average Response Time (ms): %.2f%n", totalResponse / count);
        System.out.printf("Average Waiting Time (ms): %.2f%n", totalWaiting / count);
    }
}
