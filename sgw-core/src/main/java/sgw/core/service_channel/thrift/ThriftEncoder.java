package sgw.core.service_channel.thrift;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.thrift.transport.ByteBufWriteTransport;

public class ThriftEncoder extends MessageToByteEncoder<ThriftOrderedRequest> {

    private final Logger logger = LoggerFactory.getLogger(ThriftEncoder.class);

    private static final int INITIAL_BUFFER_SIZE = 128;
    private static final int MAX_BUFFER_SIZE = 1024*1024*1024;
    private final byte[] i32buf = new byte[4];

    private ThriftChannelContext thriftChanCtx;

    public ThriftEncoder(ThriftChannelContext thriftChanCtx) {
        this.thriftChanCtx = thriftChanCtx;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ThriftOrderedRequest request, ByteBuf out) throws TException {

        long tChReqId = request.channelMessageId();
        ThriftRequestContext treqCtx = thriftChanCtx.getRequestContext(tChReqId);
        logger.debug("Request {}: Message sent to thrift channel, start encoding thrift call...",
                treqCtx.getHttpGlRequestId());
        writeFrameBuffer(out, request);
        writeSizeBuffer(out);
    }

    private void writeSizeBuffer(ByteBuf buf) {
        int frameSize = buf.readableBytes() - 4;
        TFramedTransport.encodeFrameSize(frameSize, i32buf);
        // this op doesnt change write/read index.
        buf.setBytes(0, i32buf);
    }

    private void writeFrameBuffer(ByteBuf buf, ThriftOrderedRequest request) throws TException {
        TBase args = request.getArgs();
        TMessage message = new TMessage(request.getMethodName(), TMessageType.CALL, (int) request.channelMessageId());
        String serviceName = request.getServiceName();

        // Leave space to write frame size. Use the same Bytebuf to avoid data copy.
        buf.setIndex(0, 4);

        // write frame buffer
        TTransport transport = new ByteBufWriteTransport(buf);
        TProtocol basicProtocol = new TCompactProtocol.Factory().getProtocol(transport);
        TProtocol protocol = new TMultiplexedProtocol(basicProtocol, serviceName);
        protocol.writeMessageBegin(message);
        args.write(protocol);
        protocol.writeMessageEnd();
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, @SuppressWarnings("unused") ThriftOrderedRequest msg,
                                       boolean preferDirect) {
        if (preferDirect) {
            return ctx.alloc().ioBuffer(INITIAL_BUFFER_SIZE, MAX_BUFFER_SIZE);
        } else {
            return ctx.alloc().heapBuffer(INITIAL_BUFFER_SIZE, MAX_BUFFER_SIZE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
