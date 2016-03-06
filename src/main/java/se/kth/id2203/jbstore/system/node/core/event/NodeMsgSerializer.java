package se.kth.id2203.jbstore.system.node.core.event;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.SerializationUtils;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.test.THeader;

public class NodeMsgSerializer implements Serializer {

    @Override
    public int identifier() {
        return 251;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        NodeMsg nodeMsg = (NodeMsg) o;
        Serializers.toBinary(nodeMsg.header, buf);                                  // write THeader
        buf.writeLong(nodeMsg.rid);                                                 // write long
        buf.writeByte(nodeMsg.comp);                                                // write byte
        buf.writeByte(nodeMsg.cmd);                                                 // write byte
        buf.writeInt(nodeMsg.inst);                                                 // write int
        byte[] data = SerializationUtils.serialize(nodeMsg.body);
        buf.writeInt(data.length);                                                  // write int
        buf.writeBytes(data);                                                       // write x * byte
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        THeader header = (THeader) Serializers.fromBinary(buf, Optional.absent());  // read THeader
        long rid = buf.readLong();                                                  // read long
        byte comp = buf.readByte();                                                 // read byte
        byte cmd = buf.readByte();                                                  // read byte
        int inst = buf.readInt();                                                   // read int
        byte[] data = new byte[buf.readInt()];                                      // read int
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.readByte();                                               // read x * byte
        }
        return new NodeMsg(header.src, header.dst, rid, comp, cmd, inst, SerializationUtils.deserialize(data));
    }
}
