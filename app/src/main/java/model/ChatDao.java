package model;

public class ChatDao {

    private String msg_body;
    private String key;
    private boolean seen;
    private String msg_type;
    private long time_stamp;
    private String from;
    private String to;
    private String msg_name;
    private String fromUsername;
    private String toUsername;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMsg_name() {
        return msg_name;
    }

    public void setMsg_name(String msg_name) {
        this.msg_name = msg_name;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }


    public String getMsg_body() {
        return msg_body;
    }

    public void setMsg_body(String msg_body) {
        this.msg_body = msg_body;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public ChatDao(String msg_body, String key, boolean seen, String msg_type, long time_stamp, String from, String to, String msg_name, String fromUsername, String toUsername) {
        this.msg_body = msg_body;
        this.key = key;
        this.seen = seen;
        this.msg_type = msg_type;
        this.time_stamp = time_stamp;
        this.from = from;
        this.to = to;
        this.msg_name = msg_name;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
    }

    public ChatDao(){

    }

}
