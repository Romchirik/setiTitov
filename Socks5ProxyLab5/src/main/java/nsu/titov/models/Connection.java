package nsu.titov.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
    // user write to
    private ObservableByteBuffer outputBuffer;

    // user read from
    private ObservableByteBuffer inputBuffer;

    private SocketChannel associate;

    private int writeStartPosition = 0;

    public Connection(ObservableByteBuffer outputBuffer, ObservableByteBuffer inputBuffer) {
        this.outputBuffer = outputBuffer;
        this.inputBuffer = inputBuffer;
    }

    public Connection(int buffLength) {
        this.inputBuffer = new ObservableByteBuffer(ByteBuffer.allocate(buffLength));
        this.outputBuffer = new ObservableByteBuffer(ByteBuffer.allocate(buffLength));
    }

    public ByteBuffer getOutputBuffer() {
        return outputBuffer.getByteBuffer();
    }

    public void setAssociate(SocketChannel associate) {
        this.associate = associate;
    }

    public ByteBuffer getInputBuffer() {
        return inputBuffer.getByteBuffer();
    }

    public ObservableByteBuffer getObservableOutputBuffer() {
        return outputBuffer;
    }

    public ObservableByteBuffer getObservableInputBuffer() {
        return inputBuffer;
    }

    public void registerBufferListener(ObservableByteBuffer.BufferListener bufferListener){
        inputBuffer.registerBufferListener(bufferListener);
    }

    public void notifyBufferListener(){
        outputBuffer.notifyListener();
    }

    public void closeAssociate() throws IOException {
        if(associate != null) {
            System.out.println("Socket closed: " + associate.getRemoteAddress());
            associate.close();
        }
    }

    public void shutdown(){
        outputBuffer.shutdown();
    }

    public boolean isAssociateShutDown(){
        return inputBuffer.isReadyToClose();
    }

    public void prepareToWrite(){
        var inputBuffer = getInputBuffer();
        inputBuffer.flip();
        inputBuffer.position(writeStartPosition);
    }

    public boolean isReadyToClose(){
        return outputBuffer.isReadyToClose() && inputBuffer.isReadyToClose();
    }

    public void resetWriteStartPosition() {
        this.writeStartPosition = 0;
    }

    public void setWriteStartPosition() {
        var inputBuffer = getInputBuffer();
        this.writeStartPosition = inputBuffer.position();

        var newStartPosition = inputBuffer.limit();
        inputBuffer.clear();
        inputBuffer.position(newStartPosition);
    }
}
