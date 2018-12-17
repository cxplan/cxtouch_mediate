/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.mediate.message;

/**
 * Created on 2017/5/17.
 *
 * @author kenny
 */
public class JID {

    public static JID createPhoneID(String id) {
        return new JID(id, Type.PHONE);
    }
    public static JID createNodeID(String id) {
        return new JID(id, Type.NODE);
    }

    private String id;//The identifier for backend.
    private Type type;//The type of backend

    public JID() {
        this(null, null);
    }
    public JID(String id, Type type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JID)) return false;

        JID jid = (JID) o;

        if (id != null ? !id.equals(jid.id) : jid.id != null) return false;
        return type == jid.type;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JID{" +
                "id='" + id + '\'' +
                ", type=" + type +
                '}';
    }

    public enum Type {
        NONE((byte)-1),
        PHONE((byte)0),
        NODE((byte)1),
        HUB((byte)2);

        Type(byte value) {
            this.value = value;
        }

        private byte value;
        public byte getValue() {
            return value;
        }
        public static Type getType(byte value) {
            switch (value) {
                case 0:
                    return PHONE;
                case 1:
                    return NODE;
                case 2:
                    return HUB;
                case -1:
                    return NONE;
                default:
                    throw new RuntimeException("The type value is illegal: " + value);
            }
        }
    }
}
