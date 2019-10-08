package server.minesweepermultiplayer;

import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author Josue Millan
 */
public class User {
    public String name;
    public Scanner in;
    public PrintWriter out;

    public User(String name, Scanner in, PrintWriter out) {
        this.name = name;
        this.in = in;
        this.out = out;
    }
}
