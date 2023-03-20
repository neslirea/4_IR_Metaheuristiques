package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Optional;

/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

    /** All possible priorities for the greedy solver. */
    public enum Priority {
        SPT, LPT, SRPT, LRPT, EST_SPT, EST_LPT, EST_SRPT, EST_LRPT
    }

    /** Priority that the solver should use. */
    final Priority priority;

    /** Creates a new greedy solver that will use the given priority. */
    public GreedySolver(Priority p) {
        this.priority = p;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        // Init
        long starting_time = System.currentTimeMillis();
        ResourceOrder sol = new ResourceOrder(instance);

        // Possible tasks array
        ArrayList<Task> possible_tasks = new ArrayList<>();
        for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
            possible_tasks.add(new Task(jobNumber, 0));
        }

        // Remaining processing time array
        int[] remaining_times = new int[instance.numJobs];
        for (int i = 0; i<instance.numJobs; i++) {
            remaining_times[i] = 0;
            for(int j=0; j<(instance.numTasks); j++){
                remaining_times[i] += instance.duration(i, j);
            }
        }

        // Possible task starting time array
        int[] task_start = new int[instance.numJobs];
        for (int i = 0; i<instance.numJobs; i++) {
            task_start[i] = 0;
        }

        // Possible machine starting time array
        int[] machine_start = new int[instance.numMachines];
        for (int i = 0; i<instance.numMachines; i++) {
            machine_start[i] = 0;
        }

        // Treatment
        while(!possible_tasks.isEmpty()&&((System.currentTimeMillis()-starting_time)<deadline)){
            // choose the next task depending on the chosen priority
            Task next_task = null;
            int best;
            if(Math.random()<0.95){
                switch (this.priority) {
                    case EST_SPT:
                        ArrayList<Task> best_tasks = new ArrayList<>();
                        best = Integer.MAX_VALUE;
                        int max = 0;
                        for (Task current:possible_tasks) {
                            max = Math.max(task_start[current.job], machine_start[instance.machine(current)]);
                            if (max < best) {
                                best = max;
                                best_tasks.clear();
                                best_tasks.add(current);
                            }
                            else if (max == best) {
                                best_tasks.add(current);
                            }
                        }

                        best = instance.duration(best_tasks.get(0));
                        next_task = best_tasks.get(0);
                        for (Task current:best_tasks){
                            if (instance.duration(current)<best){
                                best = instance.duration(current);
                                next_task = current;
                            }
                        }
                        break;

                    case SPT:
                        // Shortest processing time
                        best = instance.duration(possible_tasks.get(0));
                        next_task = possible_tasks.get(0);
                        for (Task current:possible_tasks){
                            if (instance.duration(current)<best){
                                best = instance.duration(current);
                                next_task = current;
                            }
                        }
                        break;
                    case LRPT:
                        // Longest remaining processing time
                        best = -1;
                        for(Task current:possible_tasks){
                            if (best==-1 || remaining_times[current.job] > best){
                                best = remaining_times[current.job] ;
                                next_task = current;
                            }
                        }
                        break;

                    case EST_LRPT:
                        ArrayList<Task> best_tasks_l = new ArrayList<>();
                        best = Integer.MAX_VALUE;
                        int max_l = 0;
                        for (Task current:possible_tasks) {
                            max_l = Math.max(task_start[current.job], machine_start[instance.machine(current)]);
                            if (max_l < best) {
                                best = max_l;
                                best_tasks_l.clear();
                                best_tasks_l.add(current);
                            }
                            else if (max_l == best) {
                                best_tasks_l.add(current);
                            }
                        }

                        best = -1;
                        for(Task current:best_tasks_l){
                            if (best==-1 || remaining_times[current.job] > best){
                                best = remaining_times[current.job] ;
                                next_task = current;
                            }
                        }
                        break;
                }
            }
            else {
                next_task = possible_tasks.get((int) Math.floor(Math.random()*possible_tasks.size()));
            }
            // add it to the solution
            sol.addTaskToMachine(instance.machine(next_task.job, next_task.task), next_task);

            // update the possible tasks
            if (next_task.task < instance.numTasks-1) {
                possible_tasks.add(new Task(next_task.job, next_task.task+1));
            }
            possible_tasks.remove(next_task);
            remaining_times[next_task.job] -= instance.duration(next_task);

            // EST
            int finishing_time = Math.max(task_start[next_task.job], machine_start[instance.machine(next_task)])+ instance.duration(next_task);
            task_start[next_task.job] = finishing_time;
            machine_start[instance.machine(next_task)] = finishing_time;

        }
        //System.out.println("GANTT: " + sol.toSchedule().get().asciiGantt());
        //System.out.println("GANTT: " + sol.toSchedule().get().toString());

        // End
        return sol.toSchedule();
    }
}
