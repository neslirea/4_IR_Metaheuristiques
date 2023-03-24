package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.List;
import java.util.Optional;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public DescentSolver(Neighborhood neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {

        long starting_time = System.currentTimeMillis();

        Optional<Schedule> solution_schedule = baseSolver.solve(instance,deadline);
        ResourceOrder solution_ro = new ResourceOrder(solution_schedule.get());
        List<ResourceOrder> neighbors = neighborhood.generateNeighbors(solution_ro);
        Optional<Schedule> best_sol = solution_schedule;

        int makespan = solution_schedule.get().makespan();

        for (ResourceOrder ro : neighbors) {
            Optional<Schedule> candidate = ro.toSchedule();
            if (candidate.isPresent() && candidate.get().isValid() && candidate.get().makespan() < makespan) {
                makespan = candidate.get().makespan();
                best_sol = candidate;
            }
        }

        return best_sol;
    }

}
