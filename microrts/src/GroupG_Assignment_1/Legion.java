package GroupG_Assignment_1;

import rts.units.Unit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Legion {
    private LinkedList<Unit> _uList; // TODO: 分兵种
    private int _centerX, _centerY;
    private int _heading;
    private int _player;

    public static final int HEADING_UP = 0,
                            HEADING_RIGHT = 1,
                            HEADING_DOWN = 2,
                            HEADING_LEFT = 3,
                            HEADING_NONE = -1;

    protected void calcCenterFromExistUnits() {
        _centerX = 0;
        _centerY = 0;
        for (Unit u : _uList) {
            _centerX += u.getX();
            _centerY += u.getY();
        }
        _centerX = (int)((double)_centerX / _uList.size() + 0.5);
        _centerY = (int)((double)_centerY / _uList.size() + 0.5);
    }

    public Legion(List<Unit> unitList) {
        _uList = new LinkedList<>(unitList);
        calcCenterFromExistUnits();
        _heading = HEADING_NONE;
        _player = _uList.get(0).getPlayer();
    }

    public Legion(Legion another) {
        _uList = new LinkedList<>(another._uList);
        _centerX = another._centerX;
        _centerY = another._centerY;
        _heading = another._heading;
        _player = another._player;
    }

    public void attack(Legion target) {
        // TODO: attack target method
    }

    public void attack(int X, int Y) {
        // TODO: attack location method
    }

    public void attack(int X, int Y, int heading) {
        // TODO: attack location and turn heading mothod
    }

    public void retreat(int X, int Y) {
        // TODO: retreat method
    }
}
