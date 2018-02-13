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
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.thrift.transport.ByteBufReadTransport;

import java.util.List;

public class ThriftDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(ThriftDecoder.class);

    // TODO: support configuration
    private static final String RESULT_PATH_FORMAT = "examples.thrift_service.%s$%s_result";

    private final byte[] i32buf = new byte[4];
    private int frameSize;
    private boolean sizeDecoded;
    private RpcInvokerDef invokerDef;

    public ThriftDecoder(RpcInvokerDef invokerDef) {
        sizeDecoded = false;
        this.invokerDef = invokerDef;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        logger.info("Start decoding Thrift response.");
        if (!sizeDecoded) {
            tryDecodeFrameSize(buf);
        }

        if (sizeDecoded && buf.readableBytes() >= frameSize) {
            out.add(decodeFrame(buf));
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

    private Object decodeFrame(ByteBuf buf) throws Exception {
        TTransport transport = new ByteBufReadTransport(buf);
        TProtocol basicProtocol = new TCompactProtocol.Factory().getProtocol(transport);
        TProtocol protocol = new TMultiplexedProtocol(basicProtocol, invokerDef.getServiceName().toLowerCase());

        String clazzName = String.format(RESULT_PATH_FORMAT,
                invokerDef.getServiceName(), invokerDef.getMethodName());
        TBase result;

        try {
            Class<?> clazz = Class.forName(clazzName);
            result = (TBase) clazz.newInstance();

            TMessage msg = protocol.readMessageBegin();
            if (msg.type == TMessageType.EXCEPTION) {
                TApplicationException x = new TApplicationException();
                x.read(protocol);
                protocol.readMessageEnd();
                throw x;
            }
            logger.info("Received {}", msg.seqid);
            // TODO: netty reuse connection? Is a out of order situation possible?
            result.read(protocol);
            protocol.readMessageEnd();
        } catch (ClassNotFoundException e) {
            // Deal wiht ClassNotFoundException separately here. Later all Exceptions will be
            // converted into DecoderException.
            logger.error("Thrift class named as {} can not be found.", clazzName);
            throw e;
        } catch (TApplicationException e) {
            logger.error("Respose type: Exception");
            throw e;
        }
        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();
        ctx.close();
    }

}
