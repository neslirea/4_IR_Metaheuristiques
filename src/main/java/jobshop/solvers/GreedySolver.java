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
        ArrayList<Task> possible_tasks = new ArrayList<>();
        for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
            possible_tasks.add(new Task(jobNumber, 0));
        }

        // Treatment
        while(!possible_tasks.isEmpty()&&((System.currentTimeMillis()-starting_time)<deadline)){
            // choose the next task depending on the chosen priority
            Task next_task = null;
            switch (this.priority){
                case SPT:
                    int best = instance.duration(possible_tasks.get(0));
                    next_task = possible_tasks.get(0);
                    for (Task current:possible_tasks){
                        if (instance.duration(current)<best){
                            best = instance.duration(current);
                            next_task = current;
                        }
                    }
                    break;
                case LRPT:
                    System.out.println("HELLO");
                    break;
            }
            // add it to the solution
            sol.addTaskToMachine(instance.machine(next_task.job, next_task.task), next_task);

            // update the possible tasks
            possible_tasks.add(new Task(next_task.job, next_task.task+1));
        }

        // End
        return sol.toSchedule();
    }
}
