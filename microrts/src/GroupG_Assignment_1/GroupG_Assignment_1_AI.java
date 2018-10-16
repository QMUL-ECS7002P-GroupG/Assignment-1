package GroupG_Assignment_1;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

import java.util.List;

public class GroupG_Assignment_1_AI extends AbstractionLayerAI {
    public GroupG_Assignment_1_AI(UnitTypeTable utt, PathFinding pf, int timeBudget, int cycleBudget) {
        super(pf, timeBudget, cycleBudget);
        // TODO: construct method
    }

    public GroupG_Assignment_1_AI clone() {
        // TODO: clone method
        return null;
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        // TODO: getAction method
        return null;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        // TODO: getParameters method
        return null;
    }
}
