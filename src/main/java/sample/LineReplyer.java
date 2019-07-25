package sample;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class LineReplyer implements LineSender {
	// Line message Pull type API
    protected String remote_url = "https://api.line.me/v2/bot/message/reply";
    
    private String access_token;

    @Override
    public Boolean send(String body) throws UnirestException
    {
        String url = this.remote_url;
        HttpResponse<String> response = Unirest.post(url)
        									.header("Content-Type", "application/json")
        									.header("Authorization", "Bearer " + this.access_token)
        									.body(body).asString();
        System.out.println(response.getBody());
        return true;
    }
    
	@Override
	public void setAccessToken(String access_token) {
		// TODO Auto-generated method stub
		this.access_token = access_token;
	}
}