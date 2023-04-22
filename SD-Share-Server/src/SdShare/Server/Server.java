package SdShare.Server;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Só pra nao criar mais de 1 arquivo.
class Main {

    public static void main(String[] args) throws IOException {
        var s = new Server();
        s.run();
    }
}

class Server extends Thread {

    private final String PATH = "SD-SHARE/";

    private final ServerSocket ss;
    private Socket s;
    private PrintStream toClient;
    private BufferedReader fromClient;

    // Arquivos
    private FileInputStream fileInputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public Server() throws IOException {
        // Criar o server socket
        ss = new ServerSocket(888);

        System.out.println("Esperando por clientes...");

        CriarDiretorio();
    }

    @Override
    public void run() {
        super.run();

        try {
            // aceita conexão
            s = ss.accept();

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
                    ProcessaMensagem(str);
                }

                FecharServidor();
                System.exit(0);
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ProcessaMensagem(String mensagem) throws Exception {
        System.out.println("[CLIENTE]: " + mensagem);

        if (ArquivoExiste(mensagem)) {
            toClient.println("achou=true;" + mensagem);
            EnviarArquivo(PATH + mensagem);
        } else {
            toClient.println("achou=false;");
        }
    }

    private boolean CriarDiretorio() {
        var f = new File(PATH);

        if (!f.exists()) {
            f.mkdir();
        }

        return f.exists();
    }

    private boolean ArquivoExiste(String nomeArquivo) {
        System.out.println(PATH + nomeArquivo);
        return new File(PATH + nomeArquivo).exists();
    }

    // 
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
        try (var fileOutputStream = new FileOutputStream(fileName)) {
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
        ss.close();
        s.close();
    }
}
