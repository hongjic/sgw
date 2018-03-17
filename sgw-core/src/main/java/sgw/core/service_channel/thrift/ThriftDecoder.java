package sgw.core.service_channel.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.thrift.transport.ByteBufReadTransport;

import java.util.List;

public class ThriftDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(ThriftDecoder.class);

    private final byte[] i32buf = new byte[4];
    private int frameSize;
    private boolean sizeDecoded;
    private ThriftChannelContext chanCtx;

    public ThriftDecoder(ThriftChannelContext chanCtx) {
        sizeDecoded = false;
        this.chanCtx = chanCtx;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (!sizeDecoded) {
            tryDecodeFrameSize(buf);
        }

        if (sizeDecoded && buf.readableBytes() >= frameSize) {
            ThriftOrderedResponse response = decodeFrame(buf);
            sizeDecoded = false;
            out.add(response);
        }
    }

    private void tryDecodeFrameSize(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {
            // read frame size
            buf.readBytes(i32buf, 0, 4);
            frameSize = TFramedTransport.decodeFrameSize(i32buf);
            sizeDecoded = true;
        }
    }

    private ThriftOrderedResponse decodeFrame(ByteBuf buf) throws Exception {

        TTransport transport = new ByteBufReadTransport(buf);
        TProtocol protocol = new TCompactProtocol.Factory().getProtocol(transport);

        TMessage msg = protocol.readMessageBegin();
        ThriftRequestContext tReqCtx = chanCtx.getRequestContext(msg.seqid);
        logger.debug("Request {}: Received thrift suresponse, start decoding thrift response.",
                tReqCtx.getHttpGlRequestId());
        if (msg.type == TMessageType.EXCEPTION) {
            TApplicationException x = new TApplicationException();
            x.read(protocol);
            protocol.readMessageEnd();
            throw x;
        }
        ThriftInvokerDef invokerDef = (ThriftInvokerDef) tReqCtx.getRpcInvokerDef();
        TBase result = invokerDef.getThriftResultClazz().newInstance();
        result.read(protocol);
        protocol.readMessageEnd();

        ThriftOrderedResponse response = new ThriftOrderedResponse();
        response.setResult(result);
        response.setChannelRequestId(msg.seqid);
        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
