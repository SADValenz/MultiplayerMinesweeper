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
    public int unlocked = 0;

    public Board(){
        bombs = new ArrayList<>();
        size = 24;
        minecount = 18;
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
        for(int y = 0; y < size; y++){
            for(int x = 0; x < size; x++){
                grid[x][y] = new Cell(x,y, this);
            }
        }
        return;
    }

    public void generate_new_board() {
        //check if is initializated
        if(!initializated){
            grid_set_size(size);
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

    public boolean check_line(int game_id) {
        String message = "game_id: " + game_id;
        switch(game_id){
            case 0: //top
                for(int i = 0; i < size; i++){
                    Cell check = grid[i][0];
                    message += ", ("+ i + ", " + 0 + "= " + check.visibility + ")";
                    if(check.visibility==0){return true;}
                }
            break;
            case 1://bottom
                for(int i = 0; i < size; i++){
                    Cell check = grid[i][size-1];
                    message += ", ("+ i + ", " + (size-1) + "= " + check.visibility + ")";
                    if(check.visibility==0){return true;}
                }
            break;
            case 2://right
                for(int i = 0; i < size; i++){
                    Cell check = grid[size-1][i];
                    message += ", ("+ (size-1) + ", " + i + "= " + check.visibility + ")";
                    if(check.visibility==0){return true;}
                }
            break;
            case 3://left
                for(int i = 0; i < size; i++){
                    Cell check = grid[0][i];
                    message += ", ("+ 0 + ", " + i + "= " + check.visibility + ")";
                    if(check.visibility==0){return true;}
                }
            break;
        }
        System.out.println(message);
        return false;
    }
}
