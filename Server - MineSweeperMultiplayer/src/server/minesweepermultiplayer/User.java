package server.minesweepermultiplayer;

import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author Josue Millan
 */
public class User {
    public String name;
    public PrintWriter out;
    public boolean active;

    public User(String name, PrintWriter out) {
        this.name = name;
        this.out = out;
        this.active = true;
    }
}
