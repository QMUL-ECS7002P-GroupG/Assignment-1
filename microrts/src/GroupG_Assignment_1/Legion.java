package GroupG_Assignment_1;

import rts.units.Unit;

import java.util.LinkedList;
import java.util.List;

public class Legion {
    private LinkedList<Unit> _workerList, _lightList, _heavyList, _rangedList, _reserves;
    private int _centerX, _centerY;
    private int _heading;
    final private int _player;

    public static final int HEADING_UP = 0,
                            HEADING_RIGHT = 1,
                            HEADING_DOWN = 2,
                            HEADING_LEFT = 3,
                            HEADING_NONE = -1;

    protected void calcCenterFromExistUnits() {
        _centerX = 0;
        _centerY = 0;
        for (Unit u : _workerList) {
            _centerX += u.getX();
            _centerY += u.getY();
        }
        for (Unit u : _lightList) {
            _centerX += u.getX();
            _centerY += u.getY();
        }
        for (Unit u : _heavyList) {
            _centerX += u.getX();
            _centerY += u.getY();
        }
        for (Unit u : _rangedList) {
            _centerX += u.getX();
            _centerY += u.getY();
        }
        double legionSize = _workerList.size() + _lightList.size() + _heavyList.size() + _rangedList.size();
        _centerX = (int)(_centerX / legionSize + 0.5);
        _centerY = (int)(_centerY / legionSize + 0.5);
    }

    public Legion(List<Unit> unitList) throws Exception {
        if (unitList.isEmpty())
            throw new Exception("Unit list is empty while constructing a new legion.");
        _player = unitList.get(0).getPlayer();
        for (Unit u : unitList)
            add(u);
        calcCenterFromExistUnits();
        _heading = HEADING_NONE;
        _reserves = new LinkedList<>();
    }

    public Legion(Legion another) {
        _workerList = new LinkedList<>(another._workerList);
        _rangedList = new LinkedList<>(another._rangedList);
        _lightList = new LinkedList<>(another._lightList);
        _heavyList = new LinkedList<>(another._heavyList);
        _reserves = new LinkedList<>(another._reserves);
        _centerX = another._centerX;
        _centerY = another._centerY;
        _heading = another._heading;
        _player = another._player;
    }

    public Legion(int X, int Y, int player) {
        _centerY = Y;
        _centerX = X;
        _player = player;
        _workerList = new LinkedList<>();
        _lightList = new LinkedList<>();
        _heavyList = new LinkedList<>();
        _rangedList = new LinkedList<>();
        _reserves = new LinkedList<>();
    }

    public void attack(Legion target) {
        // TODO: attack target method
    }

    public void attack(int X, int Y) {
        // TODO: attack location method
    }

    public void attack(int X, int Y, int heading) {
        // TODO: attack location and turn heading method
    }

    public void retreat(int X, int Y) {
        // TODO: retreat method
    }

    public void gather() {
        // TODO: gather method
    }

    public void gather(int heading) {
        _heading = heading;
        gather();
    }

    protected void add(Unit u) throws Exception {
        if (u.getPlayer() == _player)
            switch (u.getType().name) {
                case "Worker":
                    _workerList.add(u);
                    break;
                case "Light":
                    _lightList.add(u);
                    break;
                case "Heavy":
                    _heavyList.add(u);
                    break;
                case "Ranged":
                    _rangedList.add(u);
                    break;
                default:
                    throw new Exception("Unexpected UnitType added to a legion.");
            }
        else
            throw new Exception("Unit that not controlled by player added to a legion.");
    }

    public void join(Unit u) throws Exception {
        if (u.getPlayer() == _player)
            _reserves.add(u);
        else
            throw new Exception("Unit that not controlled by player joined to a legion.");
    }

    public void remove(Unit u) {
        if (_reserves.contains(u))
            _reserves.remove(u);
        else
            switch (u.getType().name) {
                case "Worker":
                    _workerList.remove(u);
                    break;
                case "Light":
                    _lightList.remove(u);
                    break;
                case "Heavy":
                    _heavyList.remove(u);
                    break;
                case "Ranged":
                    _rangedList.remove(u);
                    break;
            }
    }
}
