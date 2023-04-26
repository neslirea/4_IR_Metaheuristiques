package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;
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

        int multistart = 5; //Choix arbitraire
        Optional<Schedule> multistart_best_sol = null;
        int multistart_best_makespan = -1;

        for (int i = 0; i < multistart; i++) {

            long starting_time = System.currentTimeMillis();

            Optional<Schedule> best_sol = baseSolver.solve(instance, deadline);

            ResourceOrder solution_ro = new ResourceOrder(best_sol.get());
            List<ResourceOrder> neighbors = neighborhood.generateNeighbors(solution_ro);

            boolean ameliorant = true;
            while (ameliorant && ((System.currentTimeMillis() - starting_time) < deadline)) {
                ameliorant = false;
                int makespan = best_sol.get().makespan();
                for (ResourceOrder ro : neighbors) {
                    Optional<Schedule> candidate = ro.toSchedule();
                    if (candidate.isPresent() && candidate.get().isValid() && candidate.get().makespan() < makespan) {
                        makespan = candidate.get().makespan();
                        best_sol = candidate;
                        ameliorant = true;
                    }
                }
                solution_ro = new ResourceOrder(best_sol.get());
                neighbors = neighborhood.generateNeighbors(solution_ro);
            }

            if (best_sol.get().makespan() < multistart_best_makespan || multistart_best_makespan == -1) {
                multistart_best_makespan = best_sol.get().makespan();
                multistart_best_sol = best_sol;
            }
        }
        return multistart_best_sol;
    }

}
