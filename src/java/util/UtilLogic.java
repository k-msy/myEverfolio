package util;

import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Named
@RequestScoped
public class UtilLogic {
    
    public int getSameValueIndex(ArrayList<String[]> list, String value) {
        for (int index = 0; index < list.size(); index++) {
            if (value.equals(((String[]) list.get(index))[0])) {
                return index;
            }
        }
        return -1;
    }

    /**
     * コンテキストルートの絶対パスを取得
     * @param request
     * @return 
     */
    public String getAbsoluteContextPath(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
