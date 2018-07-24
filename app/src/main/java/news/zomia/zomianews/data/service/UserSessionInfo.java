package news.zomia.zomianews.data.service;

import javax.inject.Singleton;

/**
 * Created by Andrey on 16.01.2018.
 */

@Singleton
public class UserSessionInfo {
    private static UserSessionInfo instance = null;
    String token;

    public UserSessionInfo(){
    }

    public static UserSessionInfo getInstance() {
        if (instance == null) {
            instance = new UserSessionInfo();
        }
        return instance;
    }

    public String getTokenValue()
    {
        return token;
    }

    public String getTokenAuthValue()
    {
        return "token " + token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public boolean isEmpty()
    {
        if(token == null)
            return true;

        return token.isEmpty();
    }

    public void clear()
    {
        token = "";
    }
}
