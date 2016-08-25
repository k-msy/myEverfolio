package thirdparty.json;

import java.util.Map;
import javax.enterprise.context.RequestScoped;

@RequestScoped
public class Karma {

    Map<String, String> karmaMap;

    public Map<String, String> getKarmaMap() {
        return this.karmaMap;
    }

    public void setKarmaMap(Map<String, String> karmaMap) {
        this.karmaMap = karmaMap;
    }
}
