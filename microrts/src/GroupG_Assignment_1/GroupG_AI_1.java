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

import static java.lang.Math.max;
import static java.lang.Math.min;

public class GroupG_AI_1 extends WorkerRushPlusPlus {
    Random r = new Random();
    static final int baseAlarmDistance = 3;
    static final int workersInEachBase = 3;
    static final double workersForEachResource = 1.5;
    static final int resourceRange = 3;
    static final int attackThreshold = 1;
    static final int newBarracksThreshold = 12;
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

    /*
        BFS path finding from (startX, startY) to (targetX, targetY) in range.
        Methods in the PathFind.class require a unit, this method does not.
     */
    protected boolean PathExistsInRange(int startX, int startY, int targetX, int targetY, int range,
                                        PhysicalGameState pgs) {
        boolean visited[][] = new boolean[pgs.getWidth()][pgs.getHeight()];
        for (int i = 0; i < pgs.getWidth(); ++i)
            for (int j = 0; j < pgs.getHeight(); ++j)
                visited[i][j] = false;
        for (Unit u : pgs.getUnits())
            if (u.getType().isResource)
                visited[u.getX()][u.getY()] = true;
        int queX[] = new int[pgs.getWidth() * pgs.getHeight() + 1],
            queY[] = new int[pgs.getWidth() * pgs.getHeight() + 1];
        int head = 0, tail = 1;
        queX[0] = startX;
        queY[0] = startY;
        visited[startX][startY] = true;
        while (head < tail) {
            for (int i = 0; i < 4; ++i) {
                int nextX = queX[head] + conjX[i],
                    nextY = queY[head] + conjY[i];
                if (nextX >= 0 && nextX < pgs.getWidth() && nextY >= 0 && nextY < pgs.getHeight() &&
                        !visited[nextX][nextY]) {
                    if (ManhattanDistance(nextX, nextY, targetX, targetY) <= range)
                        return true;
                    queX[tail] = nextX;
                    queY[tail] = nextY;
                    visited[nextX][nextY] = true;
                    ++tail;
                }
            }
            ++head;
        }
        return false;
    }

    /*
        1. attack enemies near the melee unit;
        2. try not to block the way;
        3. do nothing.
     */
    protected void MeleeStandby(Unit melee, List<Unit> enemies, PhysicalGameState pgs) {
        boolean noEnemyAround = true;
        for (Unit enemy : enemies)
            // attack nearby enemies
            if (ManhattanDistance(melee, enemy) <= 1 + max(melee.getType().attackRange, enemy.getType().attackRange)) {
                attack(melee, enemy);
                noEnemyAround = false;
                break;
            }
        if (noEnemyAround) {
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
            // try not to block the way (especially the barrack)
            if (emptyDirectionNum > 0 && (emptyDirectionNum < 3 || conjWithBarrack)) {
                int direction = r.nextInt(emptyDirectionNum);
                move(melee, melee.getX() + conjX[direction], melee.getY() + conjY[direction]);
            }
        }
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

    // Build a new base near a resource that no bases around it
    protected void BuildBase(Unit worker, List<Unit> freeResource, Player p, PhysicalGameState pgs) {
        int X = -1, Y = -1;
        for (Unit target : freeResource) {
            // find an empty position
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

        List<Integer> reservedPositions = new LinkedList<>();
        if (X >= 0) {
            // build new base
            buildIfNotAlreadyBuilding(worker, baseType, X, Y, reservedPositions, p, pgs);
            resourceUsed += baseType.cost;

            // remove resources nearby the new base from the freeResource list
            LinkedList<Unit> removingResource = new LinkedList<>();
            for (Unit resource : freeResource)
                if (ManhattanDistance(resource, X, Y) <= resourceRange)
                    removingResource.add(resource);
            for (Unit resource : removingResource)
                freeResource.remove(resource);
        }
    }

    // Build a new barrack near a base and away from resources (try not to block the harvesting workers)
    protected void BuildBarrack(Unit worker, List<Unit> baseList, Player p, PhysicalGameState pgs) {
        int X = -1, Y = -1;
        for (Unit target : baseList) {
            // find an empty position
            for (int i = 0; i < 16; ++i) {
                X = target.getX() + roundX[i];
                Y = target.getY() + roundY[i];
                // the position must be away from resources
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
            // build new barrack
            buildIfNotAlreadyBuilding(worker, barrackType, X, Y, new LinkedList<>(), p, pgs);
            resourceUsed += barrackType.cost;
        }
    }

    // harvest the closest resource
    protected void WorkerHarvest(Unit worker, List<Unit> baseList, List<Unit> ourResource) {
        Unit closestBase = null;
        Unit closestResource = null;

        int closestDistance = -1;
        for (Unit resource : ourResource) {
            int distance = ManhattanDistance(worker, resource);
            if (closestDistance == -1 || distance < closestDistance) {
                closestResource = resource;
                closestDistance = distance;
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

    // train workers if the current number of workers is lower than workerLimit, otherwise do noting
    protected void BasesBehavior(List<Unit> baseList, List<Unit> workerList, int resourceNum, Player p, GameState gs) {
        int workerLimit = min((int)(resourceNum * workersForEachResource), baseList.size() * workersInEachBase);
        for (Unit base : baseList)
            if (gs.getActionAssignment(base) == null &&
                    workerList.size() < workerLimit && p.getResources() - resourceUsed >= workerType.cost) {
                train(base, workerType);
                --workerLimit;
                resourceUsed += workerType.cost;
            }
    }

    /*
        1. Try building new bases if there are free resources;
        2. Try building new barracks if the current number of resources is equal to or higher than newBarracksThreshold;
        3. Harvest resources with no more than workerLimit workers;
        4. After 1, 2, 3, workers with no missions act as melee units.
     */
    protected void WorkersBehavior(List<Unit> baseList, List<Unit> workerList, List<Unit> ourResource,
                                   List<Unit> freeResource, List<Unit> meleeList, int barrackNum,
                                   Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int workerLimit = min((int)(ourResource.size() * workersForEachResource),
                baseList.size() * workersInEachBase);
        for (Unit worker : workerList)
            if (gs.getActionAssignment(worker) == null) {
                // build new base
                if (!freeResource.isEmpty() && p.getResources() - resourceUsed >= baseType.cost)
                    BuildBase(worker, freeResource, p, pgs);
                // build new barrack
                else if ((barrackNum == 0 || p.getResources() - resourceUsed >= newBarracksThreshold) &&
                        p.getResources() - resourceUsed >= barrackType.cost)
                    BuildBarrack(worker, baseList, p, pgs);
                else if (workerLimit > 0) { // Harvest
                    WorkerHarvest(worker, baseList, ourResource);
                    --workerLimit;
                } else {    // free workers as melee units
                    meleeList.add(worker);
                }
            }
    }

    /*
        1. train melee units until we have lightRate lights, heavyRates heavies, rangedRate rangeds;
        2. then keep training follow the scale.
     */
    protected void BarracksBehavior(List<Unit> barrackList, List<Unit> meleeList, int lightRate,
                                    int heavyRate, int rangedRate, Player p) {
        int totalRate = lightRate + heavyRate + rangedRate;
        int lightNum = 0, heavyNum = 0, rangedNum = 0;
        for (Unit melee : meleeList)
            if (melee.getType() == lightType)
                ++lightNum;
            else if (melee.getType() == heavyType)
                ++heavyNum;
            else if (melee.getType() == rangedType)
                ++rangedNum;
        int totalNum = lightNum + heavyNum + rangedNum;

        for (Unit barrack : barrackList)
            if (lightRate > 0 && (lightNum < lightRate || lightNum * totalRate <= lightRate * totalNum)) {
                if (p.getResources() - resourceUsed >= lightType.cost) {
                    train(barrack, lightType);
                    resourceUsed += lightType.cost;
                    ++lightNum;
                    ++totalNum;
                }
            }
            else if (heavyRate > 0 && (heavyNum < heavyRate || heavyNum * totalRate <= heavyRate * totalNum)) {
                if (p.getResources() - resourceUsed >= heavyType.cost) {
                    train(barrack, heavyType);
                    resourceUsed += heavyType.cost;
                    ++heavyNum;
                    ++totalNum;
                }
            }
            else if (rangedRate > 0 && (rangedNum < rangedRate || rangedNum * totalRate <= rangedRate * totalNum)) {
                if (p.getResources() - resourceUsed >= rangedType.cost) {
                    train(barrack, rangedType);
                    resourceUsed += rangedType.cost;
                    ++rangedNum;
                    ++totalNum;
                }
            }
    }

    public PlayerAction getAction(int player, GameState gs) {
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

        // Statistics
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

        // Find enemies that are around our bases
        List<Unit> ourBuildings = new LinkedList<>(baseList);
        ourBuildings.addAll(barrackList);
        for (Unit enemy : enemyMovableUnits)
            for (Unit base : baseList)
                if (ManhattanDistance(enemy, base) <= baseAlarmDistance) {
                    enemyAroundBase.add(enemy);
                    break;
                }

        if (enemyAroundBase.isEmpty()) {    // no enemy around our bases
            ArrayList<Unit> enemyUnits = new ArrayList<>(enemyMovableUnits);
            enemyUnits.addAll(enemyBuildings);

            // find if there is a path to enemies
            boolean pathToEnemyExists = false;
            if (!enemyUnits.isEmpty() && !baseList.isEmpty())
                pathToEnemyExists = PathExistsInRange(baseList.getFirst().getX(), baseList.getFirst().getY(),
                        enemyUnits.get(0).getX(), enemyUnits.get(0).getY(), 1, pgs);

            // find resources that are around our bases and around no bases
            LinkedList<Unit> freeResource = new LinkedList<>();
            LinkedList<Unit> ourResource = new LinkedList<>();
            for (Unit u : pgs.getUnits())
                if (u.getType().isResource) {
                    boolean baseAroundResource = false;
                    for (Unit v : pgs.getUnitsAround(u.getX(), u.getY(), resourceRange))
                        if (v.getType() == baseType) {
                            baseAroundResource = true;
                            if (v.getPlayer() == player &&
                                    PathExistsInRange(v.getX(), v.getY(), u.getX(), u.getY(), 1, pgs))
                                ourResource.add(u);
                            break;
                        }
                    if (!baseAroundResource)
                        freeResource.add(u);
                }

            if (pathToEnemyExists) {  // Exist a path to enemy
                if (barrackList.isEmpty()) {
                    return super.getAction(player, gs);
                } else {
                    resourceUsed = 0;
                    BasesBehavior(baseList, workerList, ourResource.size(), p, gs);
                    WorkersBehavior(baseList, workerList, ourResource, freeResource, meleeList, barrackList.size(), p, gs);
                    BarracksBehavior(barrackList, meleeList, 8, 2, 3, p);

                    // Melee Behavior
                    if (ourResource.isEmpty() || meleeList.size() >= attackThreshold)
                        AttackClosestEnemy(meleeList, enemyUnits);
                    else
                        for (Unit melee : meleeList)
                            MeleeStandby(melee, enemyUnits, pgs);
                }
            } else {  // No path to enemy
                resourceUsed = 0;
                BasesBehavior(baseList, workerList, ourResource.size(), p, gs);
                WorkersBehavior(baseList, workerList, ourResource, freeResource, meleeList, barrackList.size(), p, gs);
                BarracksBehavior(barrackList, meleeList, 0, 0, 1, p);

                // Melee Behavior
                for (Unit melee : meleeList)
                    MeleeStandby(melee, enemyUnits, pgs);
            }
        } else {    // have enemy around our bases
            AttackClosestEnemy(meleeList, enemyAroundBase);
            AttackClosestEnemy(workerList, enemyAroundBase);
        }

        return translateActions(player, gs);
    }
}
