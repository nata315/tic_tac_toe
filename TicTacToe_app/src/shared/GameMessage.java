package shared;

import java.io.Serializable;
import java.util.HashMap;

public class GameMessage implements Serializable {
    private String type;
    private HashMap<String, Object> data;

    public GameMessage(String type) {
        this.type = type;
        this.data = new HashMap<>();
    }

    public String getType() { return type; }

    public void addData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public boolean hasData(String key) {
        return data.containsKey(key);
    }
}