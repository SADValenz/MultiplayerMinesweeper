package server.minesweepermultiplayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josue Millan
 */
public class Board {
    public static int size_min = 2;
    public static int size_max = 30;

    public Cell[][] grid;
    public List<Cell> bombs;
    public int size;
    public int minecount;
    public boolean initializated = false;
    public int unlocked = 0;

    public Board(){
        bombs = new ArrayList<>();
        //default values
        size = 8;
        minecount = 5;
    }

    public int get_random_range(int i) {
        double randomDouble = Math.random();
        randomDouble = randomDouble * i;
        int randomInt = (int) randomDouble;
        return randomInt;
    }

    public boolean is_inside_bounds(int x, int y) {
        return (x >= 0 && x < size) && (y >= 0 && y < size);
    }

    public Cell get_cell(int x, int y) {
        return is_inside_bounds(x,y) ? grid[x][y] : null;
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
    }

    public Cell generate_new_bomb() {
        while(true) {
            int x = get_random_range(size);
            int y = get_random_range(size);
            Cell cell = grid[x][y];
            if(cell.value != -1 && cell.visibility != 1){ //if it can get in because if it is a bomb... retry again
                cell.value = -1; 
                return cell;
            }
        }
    }

    public List<Cell> get_neighbors_cells(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        for(int x = -1; x <= 1; x++){
            for(int y = -1; y <= 1; y++){
                Cell neighbor = get_cell(cell.x-x, cell.y-y);
                if(neighbor != null && !cell.equals(neighbor)){
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    public void generate_new_board() {
        //check if is initializated
        if(!initializated){ grid_set_size(size); }

        //set mines in the field first
        for (int mine = 0; mine < minecount ; mine++ ) {
            bombs.add(generate_new_bomb());
        }

        //now with the bomb list update the neighbors cell mine count
        bombs.forEach((bomb) -> {
            List<Cell> neighbors = get_neighbors_cells(bomb);
            neighbors.forEach((neighbor) -> {
                neighbor.update_number();
            });
        });
    }

    public int get_total_size() {
        return size*size;
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
