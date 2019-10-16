package server.minesweepermultiplayer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josue Millan
 */
public class User {
    public static int id_counter = 0;

    public int id;
    public int id_game;
    public String name;
    public PrintWriter out;
    public boolean alive;
    public boolean first_cell;

    public List<Cell> flags;

    public User(String name, PrintWriter out) {
        this.id = id_counter++;
        this.id_game = -1;
        this.name = name;
        this.out = out;
        this.alive = true;
        this.first_cell = true;
        this.flags = new ArrayList<>();
    }

    public String info() {
        return this.id + " " + this.name + " " + this.id_game + " " + this.alive + " " + this.flags.size(); 
    }
}
