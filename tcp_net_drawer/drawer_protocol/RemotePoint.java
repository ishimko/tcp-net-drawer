package tcp_net_drawer.drawer_protocol;

import java.io.Serializable;

public class RemotePoint implements Serializable{
    public Point point;
    public int clientID;

    public RemotePoint(Point p, int clientID){
        point = p;
        this.clientID = clientID;
    }
}
