package sgw.core.service_channel.thrift.transport;

import io.netty.buffer.ByteBuf;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ByteBufReadTransport extends TTransport {

    private ByteBuf buf;

    public ByteBufReadTransport(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public void close() {}

    @Override
    public boolean isOpen() { return true; }

    @Override
    public void open() {}

    @Override
    public void write(byte[] bytes, int off, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] bytes, int off, int len) {
        int bytesRemaining = buf.readableBytes();
        int bytesToRead = len > bytesRemaining ? bytesRemaining : len;
        if (bytesToRead > 0)
            buf.readBytes(bytes, off, bytesToRead);
        return bytesToRead;
    }

    @Override
    public int readAll(byte[] bytes, int off, int len) throws TTransportException {
        int bytesRemaining = buf.readableBytes();
        if (len > bytesRemaining)
            throw new TTransportException("not enough reamining bytes for readAll");
        buf.readBytes(bytes, off, len);
        return len;
    }

}
