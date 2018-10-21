package GroupG_Assignment_1;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.RangedRush;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

import java.util.List;

public class RangedRushWithHitAndRun extends RangedRush {

    public RangedRushWithHitAndRun(UnitTypeTable utt, PathFinding pf) {
        super(utt, pf);
    }

    public RangedRushWithHitAndRun(UnitTypeTable utt) {
        super(utt);
    }

    public void reset() {
        super.reset();
    }

    public void reset(UnitTypeTable utt) {
        super.reset(utt);
    }

    public AI clone() {
        return new RangedRushWithHitAndRun(utt);
    }

    public void meleeUnitBehavior(Unit u, int player, GameState gs) {

    }
}
