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

    public String convertToOiginal(String unicode) {
        String[] codeStrs = unicode.split("\\\\u");
        int[] codePoints = new int[codeStrs.length - 1];
        for (int i = 0; i < codePoints.length; i++) {
            codePoints[i] = Integer.parseInt(codeStrs[(i + 1)], 16);
        }
        String encodedText = new String(codePoints, 0, codePoints.length);
        return encodedText;
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
