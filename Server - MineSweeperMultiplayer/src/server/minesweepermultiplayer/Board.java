package server.minesweepermultiplayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josue Millan
 */
public class Board {
    public Cell[][] grid;
    public List<Cell> bombs;
    public int size;
    public int minecount;
    public boolean initializated = false;

    public Board(){
        bombs = new ArrayList<>();
        size = 8;
        minecount = 10;
    }

    private int get_random_range(int i) {
        double randomDouble = Math.random();
        randomDouble = randomDouble * i;
        int randomInt = (int) randomDouble;
        return randomInt;
    }

    public boolean is_inside_bounds(int x, int y) {
        return (x >= 0 && x < size) &&  (y >= 0 && y < size);
    }

    public Cell get_cell(int x, int y) {
        if( is_inside_bounds(x,y) ){
            return grid[x][y];
        }
        return null;
    }

    public void grid_set_size(int size) {
        this.size = size;
        this.grid = new Cell[size][size];
        //set correct coords to each cell
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                grid[x][y] = new Cell(x,y, this);
            }
        }
        return;
    }

    public void generate_new_board() {
        //check if is initializated
        if(!initializated){
            grid_set_size(8);
        }

        //set mines in the field first
        for (int mine = 0; mine < minecount ; mine++ ) {
            int x = get_random_range(size);
            int y = get_random_range(size);
            Cell field = grid[x][y];
            if(field.value != -1){field.value = -1; bombs.add(field);}
            else{mine--;} //if failed to put a mine in the cell, try again
        }

        //now with the bomb list update the neighbors cell mine count
        bombs.forEach((bomb) -> {
            for(int x = -1; x <= 1; x++){
                for(int y = -1; y <= 1; y++){
                    int _x = bomb.x + x;
                    int _y = bomb.y + y;
                    if( is_inside_bounds(_x, _y) ){
                        Cell check = grid[_x][_y];
                        if(check.value != -1){ check.value++; }
                    }
                }
            }
        });
    }
}
