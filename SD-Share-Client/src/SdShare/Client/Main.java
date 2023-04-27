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

    public void Init() throws IOException {
        // criando o client socket
        s = new Socket("localhost", 888);

        // enviar dados ao servidor
        toServer = new DataOutputStream(s.getOutputStream());

        // para receber os dados vindos do servidor
        fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));

        dataInputStream = new DataInputStream(s.getInputStream());
        dataOutputStream = new DataOutputStream(s.getOutputStream());
    }

    public void EnviarMensagem(String mensagem) throws IOException {
        // envia dados para o servidor
        toServer.writeBytes(mensagem + "\n");
    }

    public String LerMensagem() throws IOException, Exception {
        // recebe dados do servidor
        var resposta = fromServer.readLine();
        if (resposta.contains("achou=true")) {
            var nomeArquivo = resposta.split(";")[1];
            ReceberArquivo(nomeArquivo);
            return "[SERVER]: O arquivo -> " +  nomeArquivo + " <- foi baixado com sucesso!";
        } if(resposta.contains("Arquivo: ")){
            String[] nomeDoArquivo = resposta.split(" ");
            
            // CLIENTE verifica se possui o arquivo solicitado pelo SERVIDOR
            if(ArquivoExiste(nomeDoArquivo[1])){
                EnviarArquivo(PATH + nomeDoArquivo[1]);
                return "[CLIENT]: enviando arquivo solicitado pelo [SERVER]!";
            } else{
                return "[CLIENT]: O arquivo solicitado não foi encontrado!";
            }
        }
        else {
         return "[SERVER]: O arquivo solicitado não foi encontrado!";   
        }
    }
    
    private boolean ArquivoExiste(String nomeArquivo) {
        System.out.println(PATH + nomeArquivo);
        return new File(PATH + nomeArquivo).exists();
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
    
    // 
    private void EnviarArquivo(String path) throws Exception {
        // abrindo arquivo.
        var file = new File(path);
        var fileInputStream = new FileInputStream(file);

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

    public void FecharConexao() throws IOException {
        // close connection.
        toServer.close();
        fromServer.close();
        s.close();
    }
}
