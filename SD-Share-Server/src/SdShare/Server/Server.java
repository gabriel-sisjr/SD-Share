package SdShare.Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
    private final ArrayList<SocketHandler> conexoesAbertas;

    public Server() throws IOException {
        // Criar o server socket
        ss = new ServerSocket(888);

        System.out.println("Esperando por clientes...");

        conexoesAbertas = new ArrayList<>();

        CriarDiretorio();
    }

    @Override
    public void run() {
        super.run();

        for (;;) {
            try {
                SocketHandler socketHander = new SocketHandler(ss.accept(), conexoesAbertas);
                
                // Caso precise buscar em outros clientes, só pegar as infos daqui.
                conexoesAbertas.add(socketHander);

                // Iniciando
                socketHander.start();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private boolean CriarDiretorio() {
        var f = new File(PATH);

        if (!f.exists()) {
            f.mkdir();
        }

        return f.exists();
    }
}
