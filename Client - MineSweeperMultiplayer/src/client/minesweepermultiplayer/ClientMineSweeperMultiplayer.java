package client.minesweepermultiplayer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientMineSweeperMultiplayer extends JFrame {
    final int Tam=30;
    Socket socket;
    Scanner Entrada;
    public static PrintWriter Salida;
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16,30);
    Tablero Tab = null;

    public ClientMineSweeperMultiplayer() {
        setTitle("MineSweeper Multiplayer v0.01 alpha");
        textField.setEnabled(false);
        messageArea.setEnabled(false);
        textField.addActionListener((ActionEvent e) -> {
            Salida.println(textField.getText());
            textField.setText("");
        });
        //Tab.Llenar();
        add(textField,BorderLayout.SOUTH); 
        add(new JScrollPane(messageArea),BorderLayout.EAST);
        pack();
        
    }

    public void getname() {
        String name = JOptionPane.showInputDialog(this, "Ingresa tu nombre", "Dar nombre", JOptionPane.PLAIN_MESSAGE);
        System.out.println("NAME: " + name);
        Salida.println(name);
    }
    
    public void settings(){
        String minas = JOptionPane.showInputDialog(this, "Ingresa cantidad de minas", "Minas", JOptionPane.PLAIN_MESSAGE);
        Salida.println("/set_minecount "+minas);
        Salida.println("/set_size "+Tam);
    }

    public boolean ValidarIP(String IP) {
        String Patron = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return IP.matches(Patron);
    }

    public boolean ValidarPuerto(String Puerto) {
        try {
            int p = Integer.valueOf(Puerto);
            return (p >= 1024 && p <= 65535);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean EntrarServidor() {
        boolean conectar = false;
        String[] direccion = JOptionPane.showInputDialog("Ingresa IP y número de puerto"
                + " del servidor separado por \":\"").split(":");
        if(direccion==null){
            
        }else if (direccion.length == 2) {
            String IP = direccion[0];
            String Puerto = direccion[1];
            if (ValidarIP(IP) && ValidarPuerto(Puerto)) {
                try {
                    socket = new Socket(IP, Integer.valueOf(Puerto));

                    conectar = true;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error al conectarse al servidor", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            System.out.println("Error en la definición de parámetros");
        }
        return conectar;
    }

    public void run() {
        
        try {
            Entrada = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("Error al construir entrada de datos");
        }
        try {
            Salida = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException ex) {
            System.out.println("Error al construir salida de datos");
        }
        try {
            while (Entrada.hasNextLine()) {
                String linea = Entrada.nextLine();
                int div = linea.indexOf(" ");
                String comando = div > 0 ? linea.substring(0, div) : linea;
                String argumento = div > 0 ? linea.substring(div + 1) : "";
                String[] args = argumento.split(" ");
                switch (comando) {
                    case "SUBMITNAME":
                        getname();
                        break;
                    case "NAMEACCEPTED":
                        textField.setEnabled(true);
                        break;
                    case "LOBBYSETTINGS":
                        settings(); break;
                    case "MESSAGE":
                        messageArea.append(argumento+"\n"); 
                        messageArea.setCaretPosition(messageArea.getText().length());
                    break;
                    case "GAMESTART":
                        Tab = new Tablero(Integer.valueOf(args[0]),Integer.valueOf(args[0]));
                        Tab.Llenar();
                        Tab.Habilitar(true); 
                        add(Tab);
                        pack();
                    break;
                    case "CELLREVEAL":
                        Tab.RevelarCasilla(Integer.valueOf(args[0]), Integer.valueOf(args[1]),Integer.valueOf(args[2]));
                        break;
                    case "FLAG":
                        Tab.PonerBandera(Integer.valueOf(args[0]), Integer.valueOf(args[1]),Integer.valueOf(args[2])); 
                    break;
                    case "UNFLAG":
                        Tab.QuitarBandera(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
                    break;
                    case "URDEAD":
                        JOptionPane.showMessageDialog(this,"Has muerto, F","Se han cometido errores",JOptionPane.PLAIN_MESSAGE);
                        Tab.Habilitar(false);
                        break;
                    case "GAMEEND":
                        String name = "";
                        int max = -9999;
                        for(String values : args){
                            String[] value = values.split(":");
                            int score = Integer.valueOf(value[1]);
                            if(max < score){
                                max = score;
                                name = value[2];
                            }
                        }
                        JOptionPane.showMessageDialog(this,"Gano: " + name + " Con un Score de: " + max);
                        Tab.Habilitar(false);break;
                }
            }
        } finally {
            this.setVisible(false);
            this.dispose();
        }
    }

    public static void main(String args[]) {
        ClientMineSweeperMultiplayer Cl = new ClientMineSweeperMultiplayer();
        while (true) {
            if (Cl.EntrarServidor()) {
                break;
            }
        }
        Cl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Cl.setResizable(false);
        Cl.setLocationRelativeTo(null);
        Cl.setVisible(true);
        Cl.run();
    }

}
