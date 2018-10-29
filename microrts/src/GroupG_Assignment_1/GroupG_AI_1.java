package GroupG_Assignment_1;

import ai.abstraction.AbstractAction;
import ai.abstraction.Harvest;
import ai.abstraction.WorkerRushPlusPlus;
import ai.abstraction.pathfinding.PathFinding;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GroupG_AI_1 extends WorkerRushPlusPlus {
    Random r = new Random();
    static final int baseAlarmDistance = 3;
    static final int workersInEachBase = 3;
    static final int resourceRange = 5;
    static final int[] roundX = {-2, -2, -2, -2, -2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 0, -1};
    static final int[] roundY = {-2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2};
    static final int[] conjX = {-1, 0, 1, 0};
    static final int[] conjY = {0, 1, 0, -1};

    protected UnitType baseType, workerType, barrackType, lightType, heavyType, rangedType;

    protected int resourceUsed;

    protected void setTypes() {
        baseType = utt.getUnitType("Base");
        workerType = utt.getUnitType("Worker");
        barrackType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");
        heavyType = utt.getUnitType("Heavy");
        rangedType = utt.getUnitType("Ranged");
    }

    public GroupG_AI_1(UnitTypeTable utt) {
        super(utt);
        setTypes();
    }

    public GroupG_AI_1(UnitTypeTable utt, PathFinding pf) {
        super(utt, pf);
        setTypes();
    }

    public void reset() {
        super.reset();
        setTypes();
    }

    public void reset(UnitTypeTable utt) {
        super.reset(utt);
        setTypes();
    }

    protected int ManhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    protected int ManhattanDistance(Unit u, int x, int y) {
        return ManhattanDistance(u.getX(), u.getY(), x, y);
    }

    protected int ManhattanDistance(Unit a, Unit b) {
        return ManhattanDistance(a, b.getX(), b.getY());
    }

    protected void AttackClosestEnemy(Unit u, List<Unit> enemyList) {
        Unit closestEnemy = null;
        int closestDistance = -1, distance;

        for (Unit enemy : enemyList) {
            distance = ManhattanDistance(u, enemy);
            if (closestDistance == -1 || distance < closestDistance) {
                closestEnemy = enemy;
                closestDistance = distance;
            }
        }

        if (closestEnemy != null)
            attack(u, closestEnemy);
    }

    protected void AttackClosestEnemy(List<Unit> uList, List<Unit> enemyList) {
        for (Unit u : uList)
            AttackClosestEnemy(u, enemyList);
    }

    protected void BuildBase(Unit worker, List<Unit> freeResource, PhysicalGameState pgs) {
        int X = -1, Y = -1;
        for (Unit target : freeResource) {
            for (int i = 0; i < 16; ++i) {
                X = target.getX() + roundX[i];
                Y = target.getY() + roundY[i];
                if (X >= 0 && Y >= 0 && X < pgs.getWidth() && Y < pgs.getHeight() && pgs.getUnitAt(X, Y) == null) {
                    break;
                } else {
                    X = -1;
                    Y = -1;
                }
            }
            if (X >= 0)
                break;
        }
        if (X >= 0) {
            build(worker, baseType, X, Y);
            resourceUsed += baseType.cost;
            LinkedList<Unit> removingResource = new LinkedList<>();
            for (Unit resource : freeResource)
                if (ManhattanDistance(resource, X, Y) <= resourceRange)
                    removingResource.add(resource);
            for (Unit resource : removingResource)
                freeResource.remove(resource);
        }
    }

    protected void BuildBarrack(Unit worker, List<Unit> baseList, PhysicalGameState pgs) {
        int X = -1, Y = -1;
        for (Unit target : baseList) {
            for (int i = 0; i < 16; ++i) {
                X = target.getX() + roundX[i];
                Y = target.getY() + roundY[i];
                if (X >= 0 && Y >= 0 && X < pgs.getWidth() && Y < pgs.getHeight() && pgs.getUnitAt(X, Y) == null) {
                    for (Unit u : pgs.getUnitsAround(X, Y, 2))
                        if (u.getType().isResource) {
                            X = -1;
                            Y = -1;
                            break;
                        }
                    if (X > 0)
                        break;
                } else {
                    X = -1;
                    Y = -1;
                }
            }
            if (X >= 0)
                break;
        }
        if (X >= 0) {
            build(worker, barrackType, X, Y);
            resourceUsed += barrackType.cost;
        }
    }

    protected void WorkerHarvest(Unit worker, List<Unit> baseList, PhysicalGameState pgs) {
        Unit closestBase = null;
        Unit closestResource = null;

        int closestDistance = -1;
        for (Unit u : pgs.getUnits()) {
            if (u.getType().isResource) {
                int distance = ManhattanDistance(worker, u);
                if (closestDistance == -1 || distance < closestDistance) {
                    closestResource = u;
                    closestDistance = distance;
                }
            }
        }

        closestDistance = -1;
        for (Unit base : baseList) {
            int distance = ManhattanDistance(worker, base);
            if (closestDistance == -1 || distance < closestDistance) {
                closestBase = base;
                closestDistance = distance;
            }
        }

        if (closestResource != null && closestBase != null) {
            AbstractAction aa = getAbstractAction(worker);
            if (aa instanceof Harvest) {
                Harvest h_aa = (Harvest) aa;
                if (h_aa.getTarget() != closestResource || h_aa.getBase() != closestBase) {
                    harvest(worker, closestResource, closestBase);
                }
            } else {
                harvest(worker, closestResource, closestBase);
            }
        }
    }

    public PlayerAction getAction(int player, GameState gs) {
        if (gs.getTime() == 210) {
            int a = 0;
        }
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        PlayerAction pa = new PlayerAction();

        int enemyPlayer = 1 - player;
        LinkedList<Unit>
            baseList = new LinkedList<>(),
            barrackList = new LinkedList<>(),
            workerList = new LinkedList<>(),
            meleeList = new LinkedList<>(),
            enemyMovableUnits = new LinkedList<>(),
            enemyBuildings = new LinkedList<>(),
            enemyAroundBase = new LinkedList<>();
        for (Unit u : pgs.getUnits())
            if (u.getPlayer() == player)
                if (u.getType() == baseType)
                    baseList.add(u);
                else if (u.getType() == barrackType)
                    barrackList.add(u);
                else if (u.getType() == workerType)
                    workerList.add(u);
                else
                    meleeList.add(u);
            else if (u.getPlayer() == enemyPlayer)
                if (u.getType().canMove)
                    enemyMovableUnits.add(u);
                else
                    enemyBuildings.add(u);
        for (Unit enemy : enemyMovableUnits)
            for (Unit base : baseList)
                if (ManhattanDistance(enemy, base) <= baseAlarmDistance) {
                    enemyAroundBase.add(enemy);
                    break;
                }

        if (enemyAroundBase.isEmpty()) {    // no enemy around our bases
            boolean pathToEnemyExists = false;
            ArrayList<Unit> ourUnits = new ArrayList<>(workerList);
            ourUnits.addAll(meleeList);
            ArrayList<Unit> enemyUnits = new ArrayList<>(enemyMovableUnits);
            enemyUnits.addAll(enemyBuildings);
            for (Unit u : ourUnits) {
                for (Unit target : enemyUnits)
                    if (pf.pathToPositionInRangeExists(u, target.getPosition(pgs), 1, gs, null)) {
                        pathToEnemyExists = true;
                        break;
                    }
                if (pathToEnemyExists)
                    break;
            }

            if (pathToEnemyExists)  // Exist a path to enemy
                return super.getAction(player, gs);
            else {  // No path to enemy
                resourceUsed = 0;
                // Base behavior
                int workerLimit = baseList.size() * workersInEachBase;
                for (Unit base : baseList)
                    if (gs.getActionAssignment(base) == null &&
                            workerList.size() < workerLimit && p.getResources() - resourceUsed >= workerType.cost) {
                        train(base, workerType);
                        --workerLimit;
                        resourceUsed += workerType.cost;
                    }

                // Worker behavior
                LinkedList<Unit> freeResource = new LinkedList<>();
                for (Unit u : pgs.getUnits())
                    if (u.getType().isResource) {
                        boolean baseAroundResource = false;
                        for (Unit v : pgs.getUnitsAround(u.getX(), u.getY(), resourceRange))
                            if (v.getType() == baseType) {
                                baseAroundResource = true;
                                break;
                            }
                        if (!baseAroundResource)
                            freeResource.add(u);
                    }
                for (Unit worker : workerList)
                    if (gs.getActionAssignment(worker) == null) {
                        // build new base
                        if (!freeResource.isEmpty() && p.getResources() - resourceUsed >= baseType.cost)
                            BuildBase(worker, freeResource, pgs);
                        // build new barrack
                        else if (barrackList.isEmpty() && p.getResources() - resourceUsed >= barrackType.cost)
                            BuildBarrack(worker, baseList, pgs);
                        else    // Harvest
                            WorkerHarvest(worker, baseList, pgs);
                    }

                // Barrack behavior
                int lightNum = 0, heavyNum = 0, rangedNum = 0;
                for (Unit melee : meleeList)
                    if (melee.getType() == lightType)
                        ++lightNum;
                    else if (melee.getType() == heavyType)
                        ++heavyNum;
                    else
                        ++rangedNum;
                for (Unit barrack : barrackList)
                    if (lightNum == heavyNum && lightNum == rangedNum || lightNum < heavyNum || lightNum < rangedNum) {
                        if (p.getResources() - resourceUsed >= lightType.cost) {
                            train(barrack, lightType);
                            resourceUsed += lightType.cost;
                            ++lightNum;
                        }
                    }
                    else if (heavyNum < lightNum || heavyNum < rangedNum) {
                        if (p.getResources() - resourceUsed >= heavyType.cost) {
                            train(barrack, heavyType);
                            resourceUsed += heavyType.cost;
                            ++heavyNum;
                        }
                    } else {
                        if (p.getResources() - resourceUsed >= rangedType.cost) {
                            train(barrack, rangedType);
                            resourceUsed += rangedType.cost;
                            ++rangedNum;
                        }
                    }

                // Melee Behavior
                for (Unit melee : meleeList) {
                    int emptyDirection[] = {-1, -1, -1, -1};
                    int emptyDirectionNum = 0;
                    boolean conjWithBarrack = false;
                    for (int i = 0; i < 4; ++i) {
                        int X = melee.getX() + conjX[i];
                        int Y = melee.getY() + conjY[i];
                        if (X >= 0 && X < pgs.getWidth() && Y >= 0 && Y <= pgs.getHeight()) {
                            Unit u = pgs.getUnitAt(melee.getX() + conjX[i], melee.getY() + conjY[i]);
                            if (u == null)
                                emptyDirection[emptyDirectionNum++] = i;
                            else if (u.getType() == barrackType)
                                conjWithBarrack = true;
                        }
                    }
                    if (emptyDirectionNum > 0 && (emptyDirectionNum < 3 || conjWithBarrack)) {
                        int direction = r.nextInt(emptyDirectionNum);
                        move(melee, melee.getX() + conjX[direction], melee.getY() + conjY[direction]);
                    }
                }
            }
        } else {    // have enemy around our bases
            AttackClosestEnemy(meleeList, enemyAroundBase);
            AttackClosestEnemy(workerList, enemyAroundBase);
        }

        return translateActions(player, gs);
    }
}
