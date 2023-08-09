package Reducer;

import soot.Body;
import soot.Trap;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class MyUnitGraph extends BriefUnitGraph {


    public MyUnitGraph(Body body) {
        super(body);
        UnitPatchingChain units = body.getUnits();
        for(Trap trap : body.getTraps()){
            Unit caughtUnit = trap.getHandlerUnit();
            Unit caughtPre = units.getPredOf(caughtUnit);
            if(this.unitToSuccs.get(caughtPre) == null || this.unitToSuccs.get(caughtPre).size() == 0){
                this.addEdge(this.unitToSuccs, this.unitToPreds, caughtPre, caughtUnit);
                this.tails.remove(caughtPre);
            }
        }
    }

}
