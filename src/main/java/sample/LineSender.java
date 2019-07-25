package sample;

import com.mashape.unirest.http.exceptions.UnirestException;

public interface LineSender {

    /**
     * Set line access token
     *
     * @param  Line Access Token
     * @return 
     */
	public void setAccessToken(String access_token);
	
    /**
     * Send Line Message
     *
     * @param  body the message body
     * @return Boolean whether the message sent or not
     * @throws UnirestException Throws exception in case it fails to perform the request
     */
	public Boolean send(String body) throws UnirestException;
}
