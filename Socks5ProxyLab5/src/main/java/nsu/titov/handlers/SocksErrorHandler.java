package nsu.titov.handlers;

import nsu.titov.models.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SocksErrorHandler extends Handler {
    public SocksErrorHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {}

    @Override
    public int write(SelectionKey selectionKey) throws IOException {
        int remaining = super.write(selectionKey);
        if(remaining == 0){
            var socket = (SocketChannel) selectionKey.channel();
            socket.close();
        }

        return remaining;
    }
}
