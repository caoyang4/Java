package src.rhino.service.command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by zhen on 2018/11/29.
 */
public interface RhinoServerHandler {

    Command decode(DataInputStream inputStream) throws Exception;

    void encode(Object data, DataOutputStream outputStream) throws IOException;
}
