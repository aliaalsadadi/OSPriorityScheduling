class Process {
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

