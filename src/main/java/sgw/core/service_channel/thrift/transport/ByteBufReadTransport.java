package sgw.core.service_channel.thrift.transport;

import io.netty.buffer.ByteBuf;
import org.apache.thrift.transport.TTransport;

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
        buf.readBytes(bytes, off, len);
        return len;
    }

}
