package se.kth.id2203.jbstore.network;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.SerializationUtils;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.test.THeader;

import java.io.Serializable;

public class MsgSerializer implements Serializer {

    @Override
    public int identifier() {
        return 201;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        Msg msg = (Msg) o;
        Serializers.toBinary(msg.header, buf);                                      // write THeader
        buf.writeLong(msg.time);                                                    // write long
        buf.writeByte(msg.desc);                                                    // write byte
        byte[] data = SerializationUtils.serialize(msg.body);
        buf.writeInt(data.length);                                                  // write int
        buf.writeBytes(data);                                                       // write x * byte
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        THeader header = (THeader) Serializers.fromBinary(buf, Optional.absent());  // read THeader
        long time = buf.readLong();                                                 // read long
        byte desc = buf.readByte();                                                 // read byte
        byte[] data = new byte[buf.readInt()];                                      // read int
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.readByte();                                               // read x * byte
        }
        return new Msg(header.src, header.dst, time, desc, (Serializable) SerializationUtils.deserialize(data));
    }
}
