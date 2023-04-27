/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SdShare.Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 55799
 */
public class Attendant extends Thread{
    private Socket socket;
    private BufferedReader input;
    private DataOutputStream output;
    
    public Attendant(String address, int port) throws IOException{
        socket = new Socket(address, port);
        input = new BufferedReader(new InputStreamReaderd);
        fromAttendant = new DataOutputStream(socket.getOutputStream());
    }
    
    @Override
    public void run(){
        while(true){
            ReceberMensagem(){
            
            }
        }
    }
    
    public void EnviarMensagem(String message) throws IOException{
        output.writeBytes(message);
    }
    
    private void ReceberMensagem(){
        input.
    }        
}
