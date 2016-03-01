package se.kth.id2203.jbstore.system.network;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.SerializationUtils;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.test.THeader;

import java.io.Serializable;

public class NetMsgSerializer implements Serializer {

    @Override
    public int identifier() {
        return 201;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        NetMsg netMsg = (NetMsg) o;
        Serializers.toBinary(netMsg.header, buf);                                   // write THeader
        buf.writeLong(netMsg.rid);                                                  // write long
        buf.writeByte(netMsg.comp);                                                 // write byte
        buf.writeByte(netMsg.cmd);                                                  // write byte
        byte[] data = SerializationUtils.serialize(netMsg.body);
        buf.writeInt(data.length);                                                  // write int
        buf.writeBytes(data);                                                       // write x * byte
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        THeader header = (THeader) Serializers.fromBinary(buf, Optional.absent());  // read THeader
        long rid   = buf.readLong();                                                // read long
        byte comp = buf.readByte();                                                 // read byte
        byte cmd = buf.readByte();                                                  // read byte
        byte[] data = new byte[buf.readInt()];                                      // read int
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.readByte();                                               // read x * byte
        }
        return new NetMsg(header.src, header.dst, rid, comp, cmd, (Serializable) SerializationUtils.deserialize(data));
    }
}
