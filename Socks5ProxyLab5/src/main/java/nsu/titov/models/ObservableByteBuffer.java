package nsu.titov.models;

import java.nio.ByteBuffer;

public class ObservableByteBuffer {
    private ByteBuffer byteBuffer;

    private boolean isShutdown = false;

    public interface BufferListener{
        void onUpdate();
    }

    private BufferListener bufferListener;

    public ObservableByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void notifyListener(){
        bufferListener.onUpdate();
    }

    public void registerBufferListener(BufferListener bufferListener){
        this.bufferListener = bufferListener;
    }

    public void shutdown() {
        isShutdown = true;
    }

    public boolean isReadyToClose(){
        return byteBuffer.remaining() == 0 && isShutdown;
    }
}
