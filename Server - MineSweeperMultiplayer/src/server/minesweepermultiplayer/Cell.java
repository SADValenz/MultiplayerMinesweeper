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

    public int GAMEMAKER_DEPTH = 0;

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

        List<Cell> neighbors = board.get_neighbors_cells(this);

        neighbors.forEach((neighbor) -> {
            if(neighbor.visibility == 0 || neighbor.visibility == 2){
                neighbor.set_visibility(1, user);
                revealed.add(neighbor);
                neighbor.GAMEMAKER_DEPTH = this.GAMEMAKER_DEPTH+1;
                if(neighbor.value == 0){ to_check.add(neighbor); }
            }
        });

        to_check.forEach((check) ->{//do the same operation to all other founded 0 value fields
            check.reveal_neighbors(user, revealed);
        });
    }

    public void update_number() {
        if(value != -1){
            value++;
        }
    }

    public List<Cell> reveal(User user) {
        List<Cell> revealed = new ArrayList<>();
        System.out.println("check visibility and owner stuff");
        System.out.println("visibility: " + visibility + ", owner: " + (owner != null ? owner.id : "none") + ", user: " + user.id);
        if( visibility == 0 || (visibility == 2 && !owner.equals(user)) ){
            set_visibility(1, user);
            revealed.add(this);

            if(value == 0) { this.reveal_neighbors(user, revealed); }
            user.first_cell = false;
        }
        return revealed;
    }

    public boolean set_flag(User user) {
        if(visibility == 0){
            user.flags.add(this);
            set_visibility(2, user);
            return true;
        }
        return false;
    }

    public boolean unset_flag(User user){
        if(visibility == 2 && owner == user){
            user.flags.remove(this);
            set_visibility(0, null);
            return true;
        }
        return false;
    }
}