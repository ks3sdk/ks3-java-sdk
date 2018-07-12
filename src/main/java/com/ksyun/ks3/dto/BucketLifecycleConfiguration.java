package com.ksyun.ks3.dto;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import java.util.Date;
import java.util.List;

import com.ksyun.ks3.exception.client.ClientIllegalArgumentException;
import com.ksyun.ks3.utils.StringUtils;

public class BucketLifecycleConfiguration {
	
	private List<Rule> rules;

	public List<Rule> getRules() {
		return this.rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public BucketLifecycleConfiguration(List<Rule> rules) {
		this.rules = rules;
	}

	public BucketLifecycleConfiguration() {
	}

	public String toString() {
		return "BucketLifecycleConfiguration(rules=" + getRules() + ")";
	}

	
	public static enum Status{
		ENABLED("Enabled"),
		DISABLED("Disabled");
		
		private Status(String status){
			this.status = status;
		}
		private String status;
		
		public String status2Str() {
			return status;
		}
		
		public static Status str2Status(String statusStr){
			Status status = null;
			for(Status senum : Status.values()){
				if(senum.status.equals(statusStr)){
					status = senum;
					break;
				}
			}
			return status;
		}
		
		
	}


	public static class Rule {
		private String id;
		private String prefix;
		private Status status;
		private Integer expirationInDays;

		private Date expirationDate;

		public void setId(String id) {
			this.id = id;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public void setExpirationInDays(Integer expirationInDays) {
			this.expirationInDays = expirationInDays;
		}


		public String getId() {
			return this.id;
		}


		public String getPrefix() {
			return this.prefix;
		}

		public Integer getExpirationInDays() {
			return this.expirationInDays;
		}

		public Status getStatus() {
			return this.status;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		public void setExpirationDate(Date expirationDate) {
			this.expirationDate = expirationDate;
		}

		public Date getExpirationDate() {
			return this.expirationDate;
		}


		public String toString() {
			return "BucketLifecycleConfiguration.Rule(id=" + getId() + ", prefix=" + getPrefix() + ", status="
					+ getStatus() + ", expirationInDays=" + getExpirationInDays()
					+ ", expirationDate=" + getExpirationDate() ;
		}
		
		

	    public void validate() throws ClientIllegalArgumentException {
	        validateRuleId();
//	        validateFilter();
	        validateExpiration();
	        validateStatus();
	    }
	    
	    private void validateRuleId() throws ClientIllegalArgumentException {
	        if (StringUtils.isBlank(id)) {
	            throw notNull("rule id");
	        }
	        
	        if (id.length() > StringUtils.MAXIMUM_ALLOWED_ID_LENGTH) {
	            throw between("rule id", id, "1", String.valueOf(StringUtils.MAXIMUM_ALLOWED_ID_LENGTH));
	        }
	    }
	    
	    private void validateFilter() throws ClientIllegalArgumentException {
	        if (prefix == null) {
	        	throw notNull("prefix");
	        }
	    }
	    
	    private void validateExpiration() throws ClientIllegalArgumentException {
	        if (expirationInDays == null && expirationDate == null) {
	        	throw notNull("expirationInDays","expirationDate");
	            //throw between("expirationInDays",String.valueOf(expirationInDays),String.valueOf(0),String.valueOf(10000));
	        }
	        if (expirationInDays !=null && expirationDate != null) {
	            throw new ClientIllegalArgumentException("conflict expirationInDays date and expirationDate");
	        }
	        
	        if(expirationInDays !=null){
	        	if(expirationInDays < 1 || expirationInDays >10000){
	        		throw between("expirationInDays",String.valueOf(expirationInDays),String.valueOf(1),String.valueOf(10000));
	        	}
	        }
	    }
	    
	    private void validateStatus() throws ClientIllegalArgumentException {
	        if (status == null) {
	            throw notNull("status");
	        }
	    }
	}
}