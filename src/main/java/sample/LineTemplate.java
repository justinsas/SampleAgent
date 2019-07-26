package sample;

public class LineTemplate {

    protected String recipient_id;
    
    protected String reply_token;

	protected String message_text;
    
    protected String message_type;
    
    protected String image_url;

	protected String message_string;
	
	protected String stamp_val;

	/**
     * Set Recipient ID
     *
     * @param recipient_id the recipient id
     */
    public void setRecipientId(String recipient_id)
    {
        this.recipient_id = recipient_id;
    }

	/**
     * Set Reply token
     *
     * @param reply_token the reply token
     */
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

    /**
     * Get Message type
     *
     * @return String the message type
     */
    public String getMessage_type() {
		return message_type;
	}

    /**
     * Set Message type
     *
     * @param message_type the message type
     */
	public void setMessage_type(String message_type) {
		this.message_type = message_type;
	}
    
    /**
     * Get Image URL
     *
     * @return String the image URL
     */
	public String getImage_url() {
		return image_url;
	}

    /**
     * Set Image URL
     *
     * @param image_url the image URL
     */
	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

    /**
     * Get Sticker number
     *
     * @return String the Sticker number
     */
    public String getStamp_val() {
		return stamp_val;
	}

    /**
     * Set Sticker number
     *
     * @param stamp_val the sticker number
     */
	public void setStamp_val(String stamp_val) {
		this.stamp_val = stamp_val;
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
        	this.message_string += addQuotation("to", ":");
        	this.message_string += addQuotation(this.recipient_id, ",");
        } else if( this.reply_token != null ) {
        	this.message_string += addQuotation("replyToken", ":");
        	this.message_string += addQuotation(this.reply_token, ",");
        }
        
        this.message_string += addQuotation("messages", ":[{");
        this.message_string += addQuotation("type", ":");
        this.message_string += addQuotation(this.message_type, ",");

        if ("text".equals(this.message_type)) {
        	this.message_string += addQuotation("text", ":");
        	this.message_string += addQuotation(this.message_text, "");
        } else if ("image".equals(this.message_type)) {
        	this.message_string += addQuotation("originalContentUrl", ":");
        	this.message_string += addQuotation(this.image_url, ",");
        	this.message_string += addQuotation("previewImageUrl", ":");
        	this.message_string += addQuotation(this.image_url, "");
        }  else if ("sticker".equals(this.message_type)) {
        	this.message_string += addQuotation("packageId", ":");
        	this.message_string += addQuotation("11537", ",");
        	this.message_string += addQuotation("stickerId", ":");
        	this.message_string += addQuotation(this.stamp_val, "");
        }
        
        //this.message_string = this.message_string.replaceAll(",$", "");
        this.message_string += "}]";
        //this.message_string = this.message_string.replaceAll(",$", "");
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
    
    /**
     * Add double quotation and additional string
     *
     * @param instr   Input message
     * @param addstr  Additional string
     */
	private String addQuotation(String instr, String addstr) {
		String outstr = "";
		outstr = "\"" + instr + "\"" + addstr;
		return outstr;
	}
}
