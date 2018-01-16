package news.zomia.zomianews.data.service;

/**
 * Created by Andrey on 16.01.2018.
 */

public class UserSessionInfo {

    String token;

    public UserSessionInfo(){
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
}
