package chord;

import java.net.InetSocketAddress;

public class ChordNodeTime implements ChordNameService {
    
    private ChordNameService deco;
    
    public ChordNodeTime(int port) {
        this.deco = new ChordNode(port);
    }

    public int keyOfName(InetSocketAddress name) {
        return deco.keyOfName(name);
    }

    public void createGroup() {
        deco.createGroup();
    }

    public void joinGroup(InetSocketAddress knownPeer) {
        deco.joinGroup(knownPeer);
    }

    public InetSocketAddress getChordName() {
        return deco.getChordName();
    }

    public void leaveGroup() {
        deco.leaveGroup();
    }

    public InetSocketAddress succ() {
        return deco.succ();
    }

    public InetSocketAddress pred() {
        return deco.pred();
    }

    public InetSocketAddress lookup(int key, InetSocketAddress origin) {
        return deco.lookup(key, origin);
    }

    public void run() {
        deco.run();
    }

}
