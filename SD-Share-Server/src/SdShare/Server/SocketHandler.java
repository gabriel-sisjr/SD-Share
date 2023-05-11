package SdShare.Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel Santana
 */
class SocketHandler extends Thread {

    private final String PATH = "SD-SHARE/";

    private final Socket s;
    private PrintStream toClient;
    private BufferedReader fromClient;

    // Arquivos
    private FileInputStream fileInputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    
    //Outros clientes
    private ArrayList<SocketHandler> conexoesAbertas;
    
    private int id;
    
    //Quem está conectado comingo
    public boolean  atendente; 
    
    public boolean falandoComAtendente;

    public SocketHandler(Socket socket, int id) {
        this.s = socket;
        this.id = id;
        //this.conexoesAbertas = conexoesAbertas;
        //conexoesAbertas.remove(this)
        //quemE = 0;
        
        conexoesAbertas = new ArrayList<SocketHandler>();
        falandoComAtendente = false;
        atendente = false;
    }

    @Override
    public void run() {
        try {
            // aceita conexão

            System.out.println("Conexão estabelecida. Cliente -> " + s.toString());

            // envia dados ao cliente
            toClient = new PrintStream(s.getOutputStream());

            // lê dados do cliente.
            fromClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // FILES
            dataInputStream = new DataInputStream(s.getInputStream());
            dataOutputStream = new DataOutputStream(s.getOutputStream());

            //
            while (true) {
                String str;

                // repete enquanto o cliente
                // não envia uma string nula
                // lê do cliente
                while ((str = fromClient.readLine()) != null) {
                    if(str.contains("[ATTENDANT]")) {
                        processaMensagemDoAtendente(str); 
                    } else {
                        ProcessaMensagemDoCliente(str);
                    }
                }

                FecharServidor();
                break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ProcessaMensagemDoCliente(String mensagem) throws Exception {
        System.out.println("[CLIENTE](" + id +  "): " + mensagem);
        
        
        if(ArquivoExiste(mensagem) || ArquivoExisteNosClientes(mensagem)){
            // Código para pegar o arquivos dos clientes
            toClient.println("achou=true;" + mensagem);
            EnviarArquivo(PATH + mensagem);
        } else {
            toClient.println("achou=false;");
        }
    }
 
    private void processaMensagemDoAtendente(String mensagem) throws Exception{
        falandoComAtendente = true;
        System.out.println("[ATTENDANT](" + id + "): " + mensagem);
        if(mensagem.contains("Atendente")){
            atendente = true;
        }
        else if (mensagem.contains("[ATTENDANT]: true")) {
            var nomeArquivo = mensagem.split(";")[1];
            //toClient.println("achou=true;" + mensagem);
            ReceberArquivo(nomeArquivo);
            System.out.println("O [SERVER] recebeu o arquivo do[ATTENDANT](" + id + ")!!");
        } else{
            System.out.println("Arquivo não encontrado no cliente " + id + "!!");
        }
        
        falandoComAtendente = false;
    }

    private boolean ArquivoExiste(String nomeArquivo) {
        System.out.println(PATH + nomeArquivo);
        return new File(PATH + nomeArquivo).exists();
    }
    
    // OBS: Melhorar a lógica
    private boolean ArquivoExisteNosClientes(String nomeDoArquivo) throws IOException, Exception{
        boolean arquivoExiste = false;
        for(SocketHandler socketHandler : conexoesAbertas){
            
            if(socketHandler.atendente){ // Verificando se é o atendente
                socketHandler.toClient.println(nomeDoArquivo);
                
                socketHandler.falandoComAtendente = true;
                do{
                }while(socketHandler.falandoComAtendente);
                
                if(ArquivoExiste(nomeDoArquivo)){
                //if(mensagem.contains("[ATTENDANT]: true")){
                    arquivoExiste = true;
                    break;
                }
            }
        }
        
        return arquivoExiste;
    }
    
    private void EnviarArquivo(String path) throws Exception {
        // abrindo arquivo.
        var file = new File(path);
        fileInputStream = new FileInputStream(file);

        dataOutputStream.writeLong(file.length());
        // Quebrando em partes.
        byte[] buffer = new byte[4 * 1024];
        int bytes;
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            // Enviando o arquivo.
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        // close the file here
        fileInputStream.close();
    }

    private void ReceberArquivo(String fileName) throws Exception {
        int bytes = 0;
        // read file size
        /*try (*/var fileOutputStream = new FileOutputStream(new File(PATH + fileName));/*) {*/
            // read file size
            var tamanho = dataInputStream.readLong();
            byte[] buffer = new byte[4 * 1024];
            while (tamanho > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, tamanho))) != -1) {
                fileOutputStream.flush();
                // Escrevendo o arquivo
                fileOutputStream.write(buffer, 0, bytes);
                tamanho -= bytes; // lendo até o tamanho do arquivo
            }
            
            fileOutputStream.close();
            // Recebendo o arquivo
            System.out.println("Arquivo Recebido");
        //}
    }

    public void FecharServidor() throws IOException {
        toClient.close();
        fromClient.close();
        s.close();
    }
   

    public void setConexoesAbertas(ArrayList<SocketHandler> conexoesAbertas) {
        System.out.println("Quantidade de conexões abertas (id = " + id + "): " + conexoesAbertas.size());
       
        for(SocketHandler socketHandler : conexoesAbertas){
            if(!this.conexoesAbertas.contains(socketHandler) && socketHandler.getIdDoCliente() != id){
                this.conexoesAbertas.add(socketHandler);
            }
        }        
    }

    public int getIdDoCliente() {
        return id;
    }
    
    
}
