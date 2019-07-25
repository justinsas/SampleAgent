package sample;

public class LineTemplate {

    protected String recipient_id;
    
    protected String reply_token;

	protected String message_text;
    
    protected String message_type;
    
    protected String image_url;

	protected String message_string;

    /**
     * Set Recipient ID
     *
     * @param recipient_id the recipient id
     */
    public void setRecipientId(String recipient_id)
    {
        this.recipient_id = recipient_id;
    }

    public String getReply_token() {
		return reply_token;
	}

	public void setReply_token(String reply_token) {
		this.reply_token = reply_token;
	}
    
    /**
     * Set Message Text
     *
     * @param message_text the message text
     */
    public void setMessageText(String message_text)
    {
        this.message_text = message_text;
    }

    /**
     * Get Recipient ID
     *
     * @return String the recipient id
     */
    public String getRecipientId()
    {
        return this.recipient_id;
    }

    /**
     * Get Message Text
     *
     * @return String the message text
     */
    public String getMessageText()
    {
        return this.message_text;
    }

    public String getMessage_type() {
		return message_type;
	}

	public void setMessage_type(String message_type) {
		this.message_type = message_type;
	}
    
	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}
	
    /**
     * Build and get message as a string
     *
     * @return String the final message
     */
    public String build()
    {
        this.message_string  = "{";

        if( this.recipient_id != null ) {
            this.message_string += "\"to\": \"" + this.recipient_id + "\",";
        }
        else if( this.reply_token != null ) {
            this.message_string += "\"replyToken\": \"" + this.reply_token + "\",";
        }
        
        this.message_string += "\"messages\": [{\"type\":\"";
        this.message_string += this.message_type + "\",";

        if (this.message_text != null) {
            this.message_string += "\"text\":\"";
            this.message_string += this.message_text + "\"";
        }
        else if (this.image_url != null) {
        	this.message_string += "\"originalContentUrl\":\"";
        	this.message_string += this.image_url + "\",";
        	this.message_string += "\"previewImageUrl\":\"";
        	this.message_string += this.image_url + "\"";
        }
        
        this.message_string = this.message_string.replaceAll(",$", "");
        this.message_string += "}]";
        this.message_string = this.message_string.replaceAll(",$", "");
        this.message_string += "}";
        
        System.out.println(message_string + "Ç™ê∂ê¨Ç≥ÇÍÇ‹ÇµÇΩÅB");

        return this.message_string;
    }

    /**
     * Set or override message
     *
     * @param message_string the message text
     */
    public void setMessageString(String message_string)
    {
        this.message_string = message_string;
    }

    /**
     * Get message as a string
     *
     * @return String the message text
     */
    public String getMessageString()
    {
        return this.message_string;
    }
}
