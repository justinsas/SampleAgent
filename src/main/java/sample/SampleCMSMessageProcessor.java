package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.sas.mkt.agent.sdk.CMSInterface;

public class SampleCMSMessageProcessor implements CMSInterface {
	String cmsURL;
	
	public SampleCMSMessageProcessor(String cmsURL) {
		this.cmsURL=cmsURL;
	}

	@Override
	public String getDataFromCMS(String path, String queryParam) {
		String cmsJson = null;
        String urlValue = cmsURL;
        if(path != null) {
        	urlValue +="&path="+path;
        }
        if(queryParam != null) {
        	urlValue +="&q="+queryParam;
        }
        try {
			URL url = new URL(urlValue);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
	        urlc.setDoOutput(true);
	        urlc.setRequestMethod("GET");
	        urlc.setAllowUserInteraction(false);
	        StringBuilder sb = new StringBuilder();
	        BufferedReader br = new BufferedReader(new InputStreamReader(urlc //I18NOK:IOE
	                .getInputStream()));
	            String l;
	            while ((l=br.readLine())!=null) {
	                sb.append(l);
	            }
	            br.close();
	       cmsJson = sb.toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cmsJson;
	}

	@Override
	public String convertDataToSAS360(String json) {
		return json;
	}

}
