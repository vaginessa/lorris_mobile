package com.tassadar.lorrismobile.connections;


public class Connection {
    public static final int CONN_BT_SP   = 0;
    public static final int CONN_TCP     = 1;
    public static final int CONN_USB     = 2;

    public static final int ST_DISCONNECTED = 0;
    public static final int ST_CONNECTING   = 1;
    public static final int ST_CONNECTED    = 2;

    protected Connection(int type) {
        m_type = type;
        m_state = ST_DISCONNECTED;
        m_interfaces = new ConnectionInterface[0];
    }

    public void open() { }
    public void close() { }

    public boolean isOpen() { return m_state == ST_CONNECTED; }

    public synchronized void addInterface(ConnectionInterface in) {
        for(ConnectionInterface i : m_interfaces)
            if(i == in)
                return;

        ConnectionInterface[] newArray = new ConnectionInterface[m_interfaces.length+1];
        System.arraycopy(m_interfaces, 0, newArray, 0, m_interfaces.length);
        newArray[m_interfaces.length] = in;
        m_interfaces = newArray;
    }

    public synchronized void removeInterface(ConnectionInterface in) {
        int pos = 0;
        for(ConnectionInterface i : m_interfaces) {
            if(i == in) 
                break;
            ++pos;
        }

        if(pos >= m_interfaces.length)
            return;

        if(pos != m_interfaces.length-1) {
            ConnectionInterface tmp = m_interfaces[m_interfaces.length-1]; 
            m_interfaces[m_interfaces.length-1] = m_interfaces[pos];
            m_interfaces[pos] = tmp;
        }

        ConnectionInterface[] newArray = new ConnectionInterface[m_interfaces.length-1];
        System.arraycopy(m_interfaces, 0, newArray, 0, newArray.length);
        m_interfaces = newArray;
    }

    protected void sendConnected(boolean connected) {
        for(ConnectionInterface i : m_interfaces)
            i.connected(connected);
    }

    protected void sendStateChanged(int state) {
        for(ConnectionInterface i : m_interfaces)
            i.stateChanged(state);
    }

    protected void sendDisconnecting() {
        for(ConnectionInterface i : m_interfaces)
            i.disconnecting();
    }

    protected void sendDataRead(byte[] data) {
        for(ConnectionInterface i : m_interfaces)
            i.dataRead(data);
    }

    public String getName() {
        return "Connection";
    }

    public int getType() {
        return m_type;
    }

    public void addRef() {
        ++m_refCount;
    }

    public void rmRef() {
        if(--m_refCount <= 0) {
            close();
            ConnectionMgr.removeConnection(m_id);
        }
    }

    public void setId(int id) {
        m_id = id;
    }

    public int getId() {
        return m_id;
    }

    protected void setState(int state) {
        if(m_state == state)
            return;

        sendStateChanged(state);

        switch(state) {
            case ST_CONNECTED:
                sendConnected(true);
                break;
            case ST_DISCONNECTED:
                if(m_state == Connection.ST_CONNECTED)
                    sendConnected(false);
                break;
        }

        m_state = state;
    }

    public int getState() {
        return m_state;
    }

    public void write(byte[] data) {
        write(data, 0, data.length);
    }

    public void write(byte[] data, int offset, int count) {

    }

    public byte[] saveData() {
        return null;
    }

    protected int m_id;
    protected int m_type;
    protected int m_state;
    protected int m_refCount;
    protected ConnectionInterface[] m_interfaces;
}
