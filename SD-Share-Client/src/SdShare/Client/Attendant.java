/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SdShare.Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private final String PATH = "SD-SHARE/";
    
    public Attendant(Socket socket) throws IOException{
        //socket = new Socket(address, port);
        this.socket = socket;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new DataOutputStream(socket.getOutputStream());
    }
    
    @Override
    public void run(){
        while(true){
            try {
                LerMenssagens();
            } catch (Exception ex) {
                Logger.getLogger(Attendant.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void EnviarMensagem(String message) throws IOException{
        output.writeBytes(message + "\n");
    }
    
    private void LerMenssagens() throws IOException, Exception{
        String message = input.readLine();
        
        if(ArquivoExiste(message)){
            EnviarMensagem("[ATTENDANT]: true;"+ message); // Tem o arquivo
            EnviarArquivo(PATH + message);
        } else{
            EnviarMensagem("[ATTENDANT]: false"); // Não tem o arquivo
            System.out.println("[ATTENDANT]: arquivo não encontrado!");
        }
    } 
    
    private boolean ArquivoExiste(String nomeArquivo) {
        System.out.println(PATH + nomeArquivo);
        return new File(PATH + nomeArquivo).exists();
    }
    
    private void EnviarArquivo(String path) throws Exception {
        // abrindo arquivo.
        var file = new File(path);
        var fileInputStream = new FileInputStream(file);

        //
        output.writeLong(file.length());
        // Quebrando em partes.
        byte[] buffer = new byte[4 * 1024];
        int bytes;
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            // Enviando o arquivo.
            output.write(buffer, 0, bytes);
            output.flush();
        }
        // close the file here
        fileInputStream.close();
    }
}
