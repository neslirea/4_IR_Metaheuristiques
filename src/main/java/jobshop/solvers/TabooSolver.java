package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class TabooSolver implements Solver{

    Neighborhood neighborhood;
    Solver baseSolver;
    int tabooTimer;
    /** Creates a new taboo solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public TabooSolver(Neighborhood neighborhood, Solver baseSolver, int timer) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
        this.tabooTimer = timer;
    }

    private boolean contains(Nowicki.Swap[] array, Nowicki.Swap item){
        boolean res = false;
        if (item != null){
            for (int i =0; i<array.length; i++){
                if(item.equals(array[i])) {
                    res = true;
                }
            }
        }
        return res;
    }


    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {

        Optional<Schedule> current_sol = baseSolver.solve(instance,deadline);
        int current_makespan = current_sol.get().makespan();
        Optional<Schedule> best_sol = current_sol;
        int best_makespan = best_sol.get().makespan();

        Nowicki.Swap best_swap = null;

        ResourceOrder solution_ro;

        Nowicki.Swap[] tabooSwaps = new Nowicki.Swap[tabooTimer];
        for (int i =0; i <tabooTimer; i++){
            tabooSwaps[i] = null;
        }
        int i = 0;

        while(current_sol!=null&&((System.currentTimeMillis())<deadline)){
            solution_ro = new ResourceOrder(current_sol.get());

            List<Nowicki.Swap> swaps = ((Nowicki)neighborhood).allSwaps(solution_ro);

            Optional<Schedule> best_neighboor = null;
            int best_neighboor_makespan =-1;
            best_swap = null;
            for (Nowicki.Swap swap : swaps) {
                Optional<Schedule> candidate = swap.generateFrom(solution_ro).toSchedule();
                // si pas taboo
                if (!contains(tabooSwaps, swap)) {
                    if (candidate.isPresent() && candidate.get().isValid() && (best_neighboor==null || candidate.get().makespan() < best_neighboor_makespan)) {
                        best_neighboor_makespan = candidate.get().makespan();
                        best_neighboor = candidate;
                        best_swap = swap;
                    }
                // Taboo mais améliore la meilleure solution
                } else if (candidate.isPresent() && candidate.get().isValid() && (candidate.get().makespan() < best_makespan)) {
                    best_neighboor_makespan = candidate.get().makespan();
                    best_neighboor = candidate;
                    best_swap = swap;
                }
            }
            current_sol = best_neighboor;
            current_makespan = best_neighboor_makespan;
            if (current_makespan<best_makespan && current_sol != null){
                best_sol = current_sol;
                best_makespan = current_makespan;
            }

            if (best_swap != null) {
                tabooSwaps[i]=best_swap;
                i= (i+1)%tabooTimer;
            }

        }

        return best_sol;
    }
}
