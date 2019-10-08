package server.minesweepermultiplayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josue Millan
 */
public class Lobby {
    // Lobby settings
    private static int globalId = 0;
    public int id;
    public User owner = null;
    public List<User> myUsers = new ArrayList<>();
    public int lobby_size = 2;
    public int lobby_minumum = 2;
    public boolean open = true;
    public boolean started = false;

    //board settings
    public Field[][] grid;
    public List<Field> bombs = new ArrayList<>();
    public int size;
    public int minecount;
    

    public Lobby() {
        this.id = globalId++; //select the correct id
    }

    public void command_send_all(String message){
        myUsers.forEach((user) ->{
            user.out.println(message);
        });
    }

    public void add_user_owner(User user) {
        owner = user;
        this.add_user(user);
    }

    public void add_user(User user) {
        System.out.println(user.name + " has join the lobby " + id);
        myUsers.add(user);
        check_lobby_slots();
    }

    public void remove_user(User user) {
        //find user in the list
        System.out.println(user.name + " has left the lobby " + id);
        myUsers.remove(user);
        check_lobby_slots();
    }

    public void check_lobby_slots() {
        if (started) return;//don't change state if the game have started
        open = this.lobby_size > myUsers.size();
    }

    private int get_random_range(int i) {
        double randomDouble = Math.random();
        randomDouble = randomDouble * i;
        int randomInt = (int) randomDouble;
        return randomInt;
    }

    public void set_grid_size(int size) {
        this.size = size;
        this.grid = new Field[size][size];
        //set correct coords to each Field
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                grid[x][y] = new Field(x,y);
            }
        }
        return;
    }

    public void generate_new_board() {
        //set mines in the field first
        for (int mine = 0; mine < minecount ; mine++ ) {
            int x = get_random_range(size);
            int y = get_random_range(size);
            Field field = grid[x][y];
            if(field.value != -1){field.value = -1; bombs.add(field);}
            else{mine--;} //if failed to put a mine in the field, try again
        }

        //now with the bomb list update the neighbors fields mine count
        bombs.forEach((bomb) -> {
            for(int x = -1; x <= 1; x++){
                for(int y = -1; y <= 1; y++){
                    int _x = bomb.x + x;
                    int _y = bomb.y + y;
                    if(_x >= 0 && _x < size && _y >= 0 && _y < size){
                        Field check = grid[_x][_y];
                        if(check.value != -1){
                            check.value++;
                        }
                    }
                }
            }
        });
        return;
    }

    public void field_reveal_check_neighbors(User user, Field field){
        //if the value is 0... check for all the field neighbors 
        //reveal them
        List<Field> checked = new ArrayList<>();
        for(int x = -1; x <= 1; x++){
            for(int y = -1; y <= 1; y++){
                int _x = field.x + x;
                int _y = field.y + y;
                if(_x >= 0 && _x < size && _y >= 0 && _y < size){
                    Field check = grid[_x][_y];
                    if(check.visibility == 0){
                        check.visibility = 1;
                        check.owner = user;
                        command_send_all("MINEREVEAL "+x+" "+y+" "+field.value+" "+field.visibility);
                        if(field.value==0){
                            checked.add(check);
                        }
                    }
                }
            }
        }
        checked.forEach((check) ->{//do the same operation to all other founded 0 value fields
            field_reveal_check_neighbors(user, check);
        });
    }

    public void field_reveal(User user, int x, int y){
        //use the send information here 
        Field field = grid[x][y];
        if(field.visibility == 0){
            field.visibility = 1;
            field.owner = user;
            command_send_all("MINEREVEAL "+x+" "+y+" "+field.value+" "+field.visibility);
            if(field.value==0){
                field_reveal_check_neighbors(user, field);
            }
        }
        return;
    }

    public void field_flag(User user, int x, int y){
        Field field = grid[x][y];
        if(field.visibility == 0){
            field.visibility = 2;
            field.owner = user;
            command_send_all("MINEFLAG "+x+" "+y+" "+field.visibility);
        }
        return;
    }

    public class Field {
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

        public Field(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}
