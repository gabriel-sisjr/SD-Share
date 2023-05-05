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
    private int quemE; // 0: cliente, 1: atendente

    public SocketHandler(Socket socket, int id) {
        this.s = socket;
        this.id = id;
        //this.conexoesAbertas = conexoesAbertas;
        //conexoesAbertas.remove(this)
        quemE = 0;
        
        conexoesAbertas = new ArrayList<SocketHandler>();
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
                    if(quemE == 0) {
                        ProcessaMensagemDoCliente(str); 
                    } else {
                        processaMensagemDoAtendente(str);
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
        System.out.println("[CLIENTE]: " + mensagem);
        
        if(mensagem.contains("Atendente")){
            quemE = 1; 
            System.out.println("Sou Atedente!!");
        } else if (ArquivoExiste(mensagem)) {
            toClient.println("achou=true;" + mensagem);
            EnviarArquivo(PATH + mensagem);
        } else if(ArquivoExisteNosClientes(mensagem)){
            // Adicionar código para pegar arquivos dos clientes
            //ReceberArquivo(mensagem);
            toClient.println("achou=true;" + mensagem);
            EnviarArquivo(PATH + mensagem);
        } 
        else {
            toClient.println("achou=false;");
        }
    }
 
    private void processaMensagemDoAtendente(String mensagem) throws Exception{
        if (mensagem.contains("[ATTENDANT]: true")) {
            var nomeArquivo = mensagem.split(";")[1];
            //toClient.println("achou=true;" + mensagem);
            ReceberArquivo(nomeArquivo);
        } else{
            toClient.println("achou=false;");
        }
    }

    private boolean ArquivoExiste(String nomeArquivo) {
        System.out.println(PATH + nomeArquivo);
        return new File(PATH + nomeArquivo).exists();
        
        /*Path path = Paths.get(PATH + nomeArquivo);
        System.out.println(PATH + nomeArquivo);
        return Files.exists(path);*/
    }
    
    // OBS: Melhorar a lógica
    private boolean ArquivoExisteNosClientes(String nomeDoArquivo) throws IOException, Exception{
        //String mensagem = null;
        
        boolean arquivoExiste = false;
        for(SocketHandler socketHandler : conexoesAbertas){
            
            if(socketHandler.getQuemE() == 1){ // Verificando se é o atendente
                socketHandler.toClient.println(nomeDoArquivo);
                //System.out.println("mensagem do atendente: " + mensagem);
                if(ArquivoExiste(nomeDoArquivo)){
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

        //
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
        try (var fileOutputStream = new FileOutputStream(PATH + fileName)) {
            // read file size
            var tamanho = dataInputStream.readLong();
            byte[] buffer = new byte[4 * 1024];
            while (tamanho > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, tamanho))) != -1) {
                // Escrevendo o arquivo
                fileOutputStream.write(buffer, 0, bytes);
                tamanho -= bytes; // lendo até o tamanho do arquivo
            }
            // Recebendo o arquivo
            System.out.println("Arquivo Recebido");
        }
    }

    public void FecharServidor() throws IOException {
        toClient.close();
        fromClient.close();
        s.close();
    }
    
    public int getQuemE(){
        return quemE;
    }

    public void setConexoesAbertas(ArrayList<SocketHandler> conexoesAbertas) {
        System.out.println("Quantidade de conexões abertas (id = " + id + "): " + conexoesAbertas.size());
        
        
        for(SocketHandler socketHandler : conexoesAbertas){
            if(this.conexoesAbertas.contains(socketHandler) && socketHandler.getIdDoCliente() != id){
                this.conexoesAbertas.add(socketHandler);
            }
        }        
    }

    public int getIdDoCliente() {
        return id;
    }
    
    
}
