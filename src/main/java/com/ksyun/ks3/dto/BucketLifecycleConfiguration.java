package com.ksyun.ks3.dto;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import java.util.Date;
import java.util.List;

import com.ksyun.ks3.exception.client.ClientIllegalArgumentException;
import com.ksyun.ks3.service.common.StorageClass;
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
		
		private List<Transition> storageTransitions;

		public List<Transition> getStorageTransitions() {
			return storageTransitions;
		}

		public void setStorageTransitions(List<Transition> storageTransitions) {
			this.storageTransitions = storageTransitions;
		}

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
					+ ", expirationDate=" + getExpirationDate() 
				    + ", storageTransitions=" + getStorageTransitions() ;
		}
		
		

	    public void validate() throws ClientIllegalArgumentException {
	        validateRuleId();
//	        validateFilter();
	        validateExpiration();
	        validateStatus();
	        validateStorageTransition();
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
	        if (expirationInDays == null && expirationDate == null && (storageTransitions == null || storageTransitions.size() == 0)) {
	        	throw notNull("expiration","transition");
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
	    private void validateStorageTransition() throws ClientIllegalArgumentException {
	        if (storageTransitions != null) {
	            for(Transition transition : storageTransitions){
	            	transition.validate();
	            }
	        }
	    }
	    
	    private void validateStatus() throws ClientIllegalArgumentException {
	        if (status == null) {
	            throw notNull("status");
	        }
	    }
	}
	
	public static class Transition{
		
		private Integer transDays;
		private Date transDate;
		private StorageClass storageClass;
		
		public Transition() {
			
		}
		
		public Transition(Integer transDays, StorageClass storageClass) {
			this.transDays = transDays;
			this.storageClass = storageClass;
		}

		public Transition(Date transDate, StorageClass storageClass) {
			this.transDate = transDate;
			this.storageClass = storageClass;
		}

		public Integer getTransDays() {
			return transDays;
		}

		public void setTransDays(Integer transDays) {
			this.transDays = transDays;
		}

		public Date getTransDate() {
			return transDate;
		}

		public void setTransDate(Date transDate) {
			this.transDate = transDate;
		}

		public StorageClass getStorageClass() {
			return storageClass;
		}

		public void setStorageClass(StorageClass storageClass) {
			this.storageClass = storageClass;
		}

		@Override
		public String toString() {
			return "Transition [transDays=" + transDays + ", transDate=" + transDate + ", storageClass=" + storageClass
					+ "]";
		}
		
		   public void validate() throws ClientIllegalArgumentException {
		    	
		    	if(storageClass == null){
		    		throw new ClientIllegalArgumentException("missing transition storageClass");
		    	}
		    	
		    	
		        if (transDays == null && transDate == null) {
		            throw new ClientIllegalArgumentException("missing transition transDate and days");
		        }
		        
		        if (transDays != null && transDate != null) {
		            throw new ClientIllegalArgumentException("conflict transition transDate and days");
		        }
		        
		        
		        if (transDays != null) {
		                if (transDays <= 0) {
		                    throw new ClientIllegalArgumentException("transDays must be positive");
		                }
		                
		                if (transDays > 10000) {
		                    throw new ClientIllegalArgumentException("transDays cannot be greater than 10000");
		                }
		        }
		    }
		
		
		
		
		
	}
	
}