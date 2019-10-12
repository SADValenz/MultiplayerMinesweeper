package server.minesweepermultiplayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josue Millan
 */
public class Cell {
    public int x = 0;//by default
    public int y = 0;//by default
    public int visibility = 0;
    /*
     *  0 not visible
     *  1 visible
     *  2 flag
     */
    public int value = 0;
    /*
     *  -1 means a bomb
     *  0 means clear field
     *  1-8 means a bomb is nearby
     */
    public User owner = null;
    public Board board = null;

    public Cell(int x, int y, Board board){
        this.x = x;
        this.y = y;
        this.board = board;
    }

    public void set_visibility(int value, User user) {
        this.visibility = value;
        this.owner = user; 
    }

    public void reveal_neighbors(User user, List<Cell> revealed) {
        List<Cell> to_check = new ArrayList<>();
        for(int x_offset = -1; x_offset <= 1; x_offset++){
            for(int y_offset = -1; y_offset <= 1; y_offset++){
                int x_to_check = this.x + x_offset;
                int y_to_check = this.y + y_offset;
                if( board.is_inside_bounds(x_to_check, y_to_check) ) {
                    Cell check = board.grid[x_to_check][y_to_check];
                    if(check.visibility == 0){
                        check.set_visibility(1, user);
                        revealed.add(check);
                        if(check.value == 0){ to_check.add(check); }
                    }
                }
            }
        }
        to_check.forEach((check) ->{//do the same operation to all other founded 0 value fields
            check.reveal_neighbors(user, revealed);
        });
    }

    public List<Cell> reveal(User user) {
        List<Cell> revealed = new ArrayList<>();
        if(visibility == 0){
            set_visibility(1, user);
            revealed.add(this);
            if(value == 0) { this.reveal_neighbors(user, revealed); }
        }
        return revealed;
    }

    public boolean set_flag(User user) {
        if(visibility == 0){
            set_visibility(2, user);
            return true;
        }
        return false;
    }
}