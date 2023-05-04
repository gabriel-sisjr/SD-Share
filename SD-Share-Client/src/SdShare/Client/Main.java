package SdShare.Client;

import java.io.*;
import java.net.*;

class Main {

    private Socket s;
    private DataOutputStream toServer;
    private BufferedReader fromServer;

    // Arquivos
    private final String PATH = "SD-SHARE/";
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    
    //Atendente
    private Attendant attendant;

    public void Init() throws IOException {
        // criando o client socket
        s = new Socket("localhost", 888);
        
        // enviar dados ao servidor
        toServer = new DataOutputStream(s.getOutputStream());

        // para receber os dados vindos do servidor
        fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));

        dataInputStream = new DataInputStream(s.getInputStream());
        dataOutputStream = new DataOutputStream(s.getOutputStream());
        
        // Criando um ATENDENTE para responder aos pedidos do SERVIDOR
        attendant = new Attendant("localhost", 888);
        attendant.start();
        // Enviando mensagem para informar que ele é o ATTENDANT
        attendant.EnviarMensagem("Atendente");
    }

    public void EnviarMensagem(String mensagem) throws IOException {
        // envia dados para o servidorp
        toServer.writeBytes(mensagem + "\n");
    }

    public String LerMensagem() throws IOException, Exception {
        // recebe dados do servidor
        var resposta = fromServer.readLine();
        if (resposta.contains("achou=true")) {
            var nomeArquivo = resposta.split(";")[1];
            ReceberArquivo(nomeArquivo);
            return "[SERVER]: O arquivo -> " +  nomeArquivo + " <- foi baixado com sucesso!";
        } else {
         return "[SERVER]: O arquivo solicitado não foi encontrado!";   
        }
    }

    private void ReceberArquivo(String fileName) throws Exception {
        int bytes = 0;
        // read file size
        var fileOutputStream = new FileOutputStream(PATH + fileName);
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

    public void FecharConexao() throws IOException {
        // close connection.
        toServer.close();
        fromServer.close();
        s.close();
    }
}
