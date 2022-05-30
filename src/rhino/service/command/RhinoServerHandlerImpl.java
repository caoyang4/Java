package src.rhino.service.command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.mysql.cj.util.StringUtils;

//import src.rhino.util.SerializerUtils;

/**
 * Created by zhen on 2018/11/29.
 */
public class RhinoServerHandlerImpl implements RhinoServerHandler {

    private final static String DEFAULT_CHARSET = "UTF-8";

    @Override
    public Command decode(DataInputStream inputStream) throws Exception {
        int length = inputStream.readInt();
        byte[] bytes = new byte[length];
        inputStream.readFully(bytes);
        String content = new String(bytes, DEFAULT_CHARSET);
//        CommandProperties commandProperties = SerializerUtils.read(content, CommandProperties.class);
//        if (StringUtils.isNullOrEmpty(commandProperties.getRhinoKey())) {
//            throw new IllegalArgumentException("rhino key can not be blank");
//        }
//        return Command.Factory.create(commandProperties);
        return null;
    }

    @Override
    public void encode(Object data, DataOutputStream outputStream) throws IOException {
//        byte[] bytes = SerializerUtils.writeByte(data);
//        int length = bytes.length;
//        outputStream.writeInt(length);
//        outputStream.write(bytes);
    }
}
