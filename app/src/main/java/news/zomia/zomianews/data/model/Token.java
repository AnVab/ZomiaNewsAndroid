package news.zomia.zomianews.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by Andrey on 26.12.2017.
 */

public class Token {

    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("user_id")
    @Expose
    private Integer userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("token", token).append("userId", userId).toString();
    }
}
