package tcp_net_drawer.drawer_protocol;

import java.io.Serializable;

public class DrawerMessage implements Serializable{
    public enum MessageType{
        MSG_POINT,
        MSG_REMOTE_POINTS_LIST,
        MSG_CLEAR
    }

    public MessageType messageType;
    public Object messageBody;

    public DrawerMessage(MessageType messageType, Object message){
        this.messageType = messageType;
        this.messageBody = message;
    }

    public DrawerMessage(MessageType messageType, RemotePoint point){
        this.messageType = messageType;
        this.messageBody = new RemotePoint[]{point};
    }

}
