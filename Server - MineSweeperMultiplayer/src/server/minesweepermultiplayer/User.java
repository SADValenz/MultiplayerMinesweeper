package server.minesweepermultiplayer;

import java.io.PrintWriter;
import java.util.Scanner;

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

    public User(String name, PrintWriter out) {
        this.id = id_counter++;
        this.name = name;
        this.out = out;
        this.alive = true;
        this.first_cell = true;
    }
}
