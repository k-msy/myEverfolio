package entity;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@RequestScoped
@Entity
@Table(name = "TOKEN_WITHINGS")
public class Token_withings
        implements Serializable {

    @Id
    @NotNull
    private String userId;
    private String request_token;
    private String request_token_secret;
    private String oauth_token;
    private String oauth_verifier;
    private String access_token;
    private String access_token_secret;
    private String wi_userId;

    public Token_withings() {
    }

    public Token_withings(String userId, String request_token, String request_token_secret, String oauth_token, String oauth_verifier, String access_token, String access_token_secret, String wi_userId) {
        this.userId = userId;
        this.request_token = request_token;
        this.request_token_secret = request_token_secret;
        this.oauth_token = oauth_token;
        this.oauth_verifier = oauth_verifier;
        this.access_token = access_token;
        this.access_token_secret = access_token_secret;
        this.wi_userId = wi_userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequest_token() {
        return this.request_token;
    }

    public void setRequest_token(String request_token) {
        this.request_token = request_token;
    }

    public String getRequest_token_secret() {
        return this.request_token_secret;
    }

    public void setRequest_token_secret(String request_token_secret) {
        this.request_token_secret = request_token_secret;
    }

    public String getOauth_token() {
        return this.oauth_token;
    }

    public void setOauth_token(String oauth_token) {
        this.oauth_token = oauth_token;
    }

    public String getOauth_verifier() {
        return this.oauth_verifier;
    }

    public void setOauth_verifier(String oauth_verifier) {
        this.oauth_verifier = oauth_verifier;
    }

    public String getAccess_token() {
        return this.access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getAccess_token_secret() {
        return this.access_token_secret;
    }

    public void setAccess_token_secret(String access_token_secret) {
        this.access_token_secret = access_token_secret;
    }

    public String getWi_userId() {
        return this.wi_userId;
    }

    public void setWi_userId(String wi_userId) {
        this.wi_userId = wi_userId;
    }
}
