/**

 * CustomerPersistence.java    version: xxx

 

 * Copyright (c) 2005-2006 Grameen Foundation USA

 * 1029 Vermont Avenue, NW, Suite 400, Washington DC 20005

 * All rights reserved.

 

 * Apache License 
 * Copyright (c) 2005-2006 Grameen Foundation USA 
 * 

 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 *

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the 

 * License. 
 * 
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an explanation of the license 

 * and how it is applied. 

 *

 */

package org.mifos.application.customer.persistence;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.accounts.business.AccountActionDateEntity;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.checklist.business.CustomerCheckListBO;
import org.mifos.application.checklist.util.resources.CheckListConstants;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerPerformanceHistoryView;
import org.mifos.application.customer.business.CustomerStatusEntity;
import org.mifos.application.customer.business.CustomerView;
import org.mifos.application.customer.center.business.CenterBO;
import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.client.business.CustomerPictureEntity;
import org.mifos.application.customer.group.business.GroupBO;
import org.mifos.application.customer.util.helpers.CustomerConstants;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.customer.util.helpers.LoanCycleCounter;
import org.mifos.application.personnel.business.PersonnelView;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.application.productdefinition.business.PrdOfferingBO;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.framework.components.configuration.business.Configuration;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.HibernateProcessException;
import org.mifos.framework.exceptions.HibernateSearchException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.hibernate.helper.QueryFactory;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.persistence.Persistence;
import org.mifos.framework.struts.tags.DateHelper;

public class CustomerPersistence extends Persistence {

	public CustomerPersistence() {
	}

	public List<CustomerView> getChildrenForParent(Integer customerId,
			String searchId, Short officeId) throws ApplicationException {
		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("SEARCH_STRING", searchId + ".%");
		queryParameters.put("OFFICE_ID", officeId);
		List<CustomerView> queryResult = executeNamedQuery(
				NamedQueryConstants.GET_ACTIVE_CHILDREN_FORPARENT,
				queryParameters);
		return queryResult;

	}

	public List<CustomerBO> getCustomersUnderParent(String searchId,
			Short officeId) throws ApplicationException {
		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("SEARCH_STRING", searchId + "%");
		queryParameters.put("OFFICE_ID", officeId);
		List<CustomerBO> queryResult = executeNamedQuery(
				NamedQueryConstants.ACTIVE_CUSTOMERS_UNDER_PARENT,
				queryParameters);
		return queryResult;

	}

	public List<Integer> getChildrenForParent(String searchId, Short officeId)
			throws  ApplicationException {
		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("SEARCH_STRING", searchId + ".%");
		queryParameters.put("OFFICE_ID", officeId);
		List<Integer> queryResult = executeNamedQuery(
				NamedQueryConstants.GET_CHILDREN_FOR_PARENT, queryParameters);
		return queryResult;

	}

	public List<CustomerView> getActiveParentList(Short personnelId,
			Short customerLevelId, Short officeId) {
		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("personnelId", personnelId);
		queryParameters.put("customerLevelId", customerLevelId);
		queryParameters.put("officeId", officeId);

		List<CustomerView> queryResult = executeNamedQuery(
				NamedQueryConstants.GET_PARENTCUSTOMERS_FOR_LOANOFFICER,
				queryParameters);
		return queryResult;

	}

	public List<PrdOfferingBO> getLoanProducts(Date meetingDate,
			String searchId, Short personnelId) throws ApplicationException {

		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("meetingDate", meetingDate);
		queryParameters.put("searchId", searchId + "%");
		queryParameters.put("personnelId", personnelId);
		List<PrdOfferingBO> queryResult = executeNamedQuery(
				NamedQueryConstants.BULKENTRYPRODUCTS, queryParameters);
		return queryResult;

	}

	public List<PrdOfferingBO> getSavingsProducts(Date meetingDate,
			String searchId, Short personnelId) {

		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("meetingDate", meetingDate);
		queryParameters.put("searchId", searchId + "%");
		queryParameters.put("personnelId", personnelId);
		List<PrdOfferingBO> queryResult = executeNamedQuery(
				NamedQueryConstants.BULKENTRYSAVINGSPRODUCTS, queryParameters);
		return queryResult;

	}

	public Date getLastMeetingDateForCustomer(Integer customerId)
			throws ApplicationException {

		Date meetingDate = null;
		Date actionDate = new java.sql.Date(Calendar.getInstance().getTime()
				.getTime());
		HashMap<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("CUSTOMER_ID", customerId);
		queryParameters.put("ACTION_DATE", actionDate);
		List<AccountActionDateEntity> queryResult = executeNamedQuery(
				NamedQueryConstants.GET_LAST_MEETINGDATE_FOR_CUSTOMER,
				queryParameters);
		if (queryResult != null && queryResult.size() != 0)
			meetingDate = queryResult.get(0).getActionDate();
		return meetingDate;

	}

	public CustomerBO getCustomer(Integer customerId) {
		Session session = HibernateUtil.getSessionTL();
		CustomerBO customer = (CustomerBO) session.get(CustomerBO.class,
				customerId);
		return customer;
	}

	public CustomerBO findBySystemId(String globalCustNum)
			throws PersistenceException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		CustomerBO customer = null;
		queryParameters.put("globalCustNum", globalCustNum);
		try {
			List<CustomerBO> queryResult = executeNamedQuery(
					NamedQueryConstants.CUSTOMER_FIND_ACCOUNT_BY_SYSTEM_ID,
					queryParameters);
			if (null != queryResult && queryResult.size() > 0) {
				customer = queryResult.get(0);
			}
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}

		return customer;
	}

	public CustomerBO getBySystemId(String globalCustNum, Short levelId)
			throws PersistenceException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		CustomerBO customer = null;
		queryParameters.put("globalCustNum", globalCustNum);
		try {

			if (levelId.shortValue() == CustomerConstants.CENTER_LEVEL_ID) {
				List<CenterBO> queryResult = executeNamedQuery(
						NamedQueryConstants.GET_CENTER_BY_SYSTEMID,
						queryParameters);
				if (null != queryResult && queryResult.size() > 0) {
					customer = queryResult.get(0);
					initializeCustomer(customer);
				}
			} else if (levelId.shortValue() == CustomerConstants.GROUP_LEVEL_ID) {
				List<GroupBO> queryResult = executeNamedQuery(
						NamedQueryConstants.GET_GROUP_BY_SYSTEMID,
						queryParameters);
				if (null != queryResult && queryResult.size() > 0) {
					customer = queryResult.get(0);
					initializeCustomer(customer);
				}

			} else if (levelId.shortValue() == CustomerConstants.CLIENT_LEVEL_ID) {
				List<ClientBO> queryResult = executeNamedQuery(
						NamedQueryConstants.GET_CLIENT_BY_SYSTEMID,
						queryParameters);
				if (null != queryResult && queryResult.size() > 0) {
					customer = queryResult.get(0);
					initializeCustomer(customer);
				}

			}
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}

		return customer;
	}

	private void initializeCustomer(CustomerBO customer) {
		customer.getGlobalCustNum();
		customer.getOffice().getOfficeId();
		customer.getOffice().getOfficeName();
		customer.getCustomerLevel().getId();
		customer.getDisplayName();
		if (customer.getParentCustomer() != null) {
			customer.getParentCustomer().getGlobalCustNum();
			customer.getParentCustomer().getCustomerId();
			customer.getParentCustomer().getCustomerLevel().getId();
			if (customer.getParentCustomer().getParentCustomer() != null) {
				customer.getParentCustomer().getParentCustomer()
						.getGlobalCustNum();
				customer.getParentCustomer().getParentCustomer()
						.getCustomerId();
				customer.getParentCustomer().getParentCustomer()
						.getCustomerLevel().getId();
			}
		}

	}

	public List<CustomerBO> getChildrenForParent(String searchId,
			Short officeId, Short customerLevelId) throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("SEARCH_STRING", searchId + ".%");
			queryParameters.put("OFFICE_ID", officeId);
			queryParameters.put("LEVEL_ID", customerLevelId);
			List<CustomerBO> queryResult = executeNamedQuery(
					NamedQueryConstants.GET_CHILDREN, queryParameters);
			return queryResult;
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}

	public List<SavingsBO> retrieveSavingsAccountForCustomer(Integer customerId)
			throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("customerId", customerId);
			return (List<SavingsBO>) executeNamedQuery(
					NamedQueryConstants.RETRIEVE_SAVINGS_ACCCOUNT_FOR_CUSTOMER,
					queryParameters);
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}

	public CustomerPerformanceHistoryView numberOfMeetings(boolean isPresent,
			Integer customerId) throws HibernateProcessException {
		Session session = null;
		Query query = null;
		CustomerPerformanceHistoryView customerPerformanceHistoryView = new CustomerPerformanceHistoryView();
		try {
			session = HibernateUtil.getSessionTL();
			String systemDate = DateHelper.getCurrentDate(Configuration
					.getInstance().getSystemConfig().getMFILocale());
			Date localDate = DateHelper
					.getLocaleDate(Configuration.getInstance()
							.getSystemConfig().getMFILocale(), systemDate);
			Calendar currentDate = new GregorianCalendar();
			currentDate.setTime(localDate);
			currentDate.add(currentDate.YEAR, -1);
			Date dateOneYearBefore = new Date(currentDate.getTimeInMillis());
			if (isPresent) {
				query = session
						.getNamedQuery(NamedQueryConstants.NUMBEROFMEETINGSATTENDED);
				query.setInteger("CUSTOMERID", customerId);
				query.setDate("DATEONEYEARBEFORE", dateOneYearBefore);
				customerPerformanceHistoryView
						.setMeetingsAttended((Integer) query.uniqueResult());
			} else {
				query = session
						.getNamedQuery(NamedQueryConstants.NUMBEROFMEETINGSMISSED);
				query.setInteger("CUSTOMERID", customerId);
				query.setDate("DATEONEYEARBEFORE", dateOneYearBefore);
				customerPerformanceHistoryView
						.setMeetingsMissed((Integer) query.uniqueResult());
			}
		} catch (HibernateException he) {
			throw he;
		}

		return customerPerformanceHistoryView;
	}

	public CustomerPerformanceHistoryView getLastLoanAmount(Integer customerId)
			throws PersistenceException {
		try {
			Query query = null;
			Session session = HibernateUtil.getSessionTL();
			CustomerPerformanceHistoryView customerPerformanceHistoryView = null;
			if (null != session) {
				query = session
						.getNamedQuery(NamedQueryConstants.GETLASTLOANAMOUNT);
			}
			query.setInteger("CUSTOMERID", customerId);
			Object obj = query.uniqueResult();
			if (obj != null) {
				customerPerformanceHistoryView = new CustomerPerformanceHistoryView();
				customerPerformanceHistoryView.setLastLoanAmount(query
						.uniqueResult().toString());
			}

			return customerPerformanceHistoryView;

		} catch (HibernateException he) {
			he.printStackTrace();
			throw new PersistenceException(he);
		}
	}

	public List<CustomerStatusEntity> getCustomerStates(Short optionalFlag)
			throws PersistenceException {
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("OPTIONAL_FLAG", optionalFlag);
		List<CustomerStatusEntity> queryResult = null;
		try {
			queryResult = executeNamedQuery(
					NamedQueryConstants.GET_CUSTOMER_STATES, queryParameters);
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
		return queryResult;
	}

	public List<Integer> getCustomersWithUpdatedMeetings()
			throws PersistenceException {
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("updated", YesNoFlag.YES.getValue());
		List<Integer> queryResult = null;
		try {
			queryResult = executeNamedQuery(
					NamedQueryConstants.GET_UPDATED_CUSTOMER_MEETINGS,
					queryParameters);
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
		return queryResult;
	}

	public List<AccountBO> retrieveAccountsUnderCustomer(String searchId,
			Short officeId, Short accountTypeId) throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("SEARCH_STRING1", searchId);
			queryParameters.put("SEARCH_STRING2", searchId + ".%");
			queryParameters.put("OFFICE_ID", officeId);
			queryParameters.put("ACCOUNT_TYPE_ID", accountTypeId);
			return (List<AccountBO>) executeNamedQuery(
					NamedQueryConstants.RETRIEVE_ACCCOUNTS_FOR_CUSTOMER,
					queryParameters);
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}

	public List<CustomerBO> getAllChildrenForParent(String searchId,
			Short officeId, Short customerLevelId) throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("SEARCH_STRING", searchId + ".%");
			queryParameters.put("OFFICE_ID", officeId);
			queryParameters.put("LEVEL_ID", customerLevelId);
			List<CustomerBO> queryResult = executeNamedQuery(
					NamedQueryConstants.GET_ALL_CHILDREN, queryParameters);
			return queryResult;
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}

	public List<Integer> getCustomers(Short customerLevelId)
			throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("LEVEL_ID", customerLevelId);
			List<Integer> queryResult = executeNamedQuery(
					NamedQueryConstants.GET_ALL_CUSTOMERS, queryParameters);
			return queryResult;
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}


	public List<CustomerCheckListBO> getStatusChecklist(Short statusId, Short customerLevelId) throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("CHECKLIST_STATUS", CheckListConstants.STATUS_ACTIVE);	
			queryParameters.put("STATUS_ID", statusId);
			queryParameters.put("LEVEL_ID", customerLevelId);
			List<CustomerCheckListBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_CUSTOMER_STATE_CHECKLIST, queryParameters);
			return queryResult;
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}

	
	public int getCustomerCountForOffice(CustomerLevel customerLevel, Short officeId)
			throws PersistenceException {
		try {
			int count = 0;
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("LEVEL_ID", customerLevel.getValue());
			queryParameters.put("OFFICE_ID", officeId);
			List queryResult = executeNamedQuery(NamedQueryConstants.GET_CUSTOMER_COUNT_FOR_OFFICE, queryParameters);
			if(queryResult.size()>0 && queryResult.get(0)!=null)
				count = (Integer)queryResult.get(0);
			
			return count;
		} catch (HibernateException he) {
			he.printStackTrace();
			throw new PersistenceException(he);
		}
	}
	

	public List<LoanCycleCounter> fetchLoanCycleCounter(Integer customerId) {
		HashMap<String, Integer> queryParameters = new HashMap<String, Integer>();
		queryParameters.put("customerId", customerId);
		List queryResult = executeNamedQuery(
				NamedQueryConstants.FETCH_LOANCOUNTERS, queryParameters);
		if (null != queryResult && queryResult.size() > 0) {
			MifosLogManager.getLogger(LoggerConstants.CLIENTLOGGER).debug(
					"Fetch loan cycle counter query returned : "
							+ queryResult.size() + " rows");
			List<LoanCycleCounter> loanCycleCounters = new ArrayList<LoanCycleCounter>();
			for (Object obj : queryResult) {
				String prdOfferingName = (String) obj;
				MifosLogManager.getLogger(LoggerConstants.CLIENTLOGGER).debug(
						"Prd offering name of the loan account is "
								+ prdOfferingName);
				int counter = 1;
				LoanCycleCounter loanCycleCounter = new LoanCycleCounter(
						prdOfferingName, counter);
				if (!loanCycleCounters.contains(loanCycleCounter)) {
					MifosLogManager
							.getLogger(LoggerConstants.CLIENTLOGGER)
							.debug(
									"Prd offering name "
											+ prdOfferingName
											+ " does not already exist in the list hence adding it to the list");
					loanCycleCounters.add(loanCycleCounter);
				} else {
					MifosLogManager
							.getLogger(LoggerConstants.CLIENTLOGGER)
							.debug(
									"Prd offering name "
											+ prdOfferingName
											+ " already exists in the list hence incrementing the counter.");
					for (LoanCycleCounter loanCycle : loanCycleCounters) {
						if (loanCycle.getOfferingName().equals(prdOfferingName)) {
							loanCycle.incrementCounter();
						}
					}
				}
			}
			return loanCycleCounters;
		}
		MifosLogManager.getLogger(LoggerConstants.CLIENTLOGGER).debug(
				"Fetch loan cycle counter query returned : 0 rows");
		return null;
	}

	public List<CustomerStatusEntity> retrieveAllCustomerStatusList(Short levelId) throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("LEVEL_ID", levelId);
			List<CustomerStatusEntity> queryResult = executeNamedQuery(NamedQueryConstants.GET_CUSTOMER_STATUS_LIST, queryParameters);
			return queryResult;
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}
	
	public QueryResult getAllCustomerNotes(Integer customerId) throws PersistenceException, HibernateSearchException, HibernateProcessException {
		QueryResult notesResult=null;
		try{
			Session session=null;
			 notesResult = QueryFactory.getQueryResult("NotesSearch");
			 session = notesResult.getSession();
	 		Query query= session.getNamedQuery(NamedQueryConstants.GETALLCUSTOMERNOTES);
	 		query.setInteger("CUSTOMER_ID",customerId);
	 		notesResult.executeQuery(query);
	 	}
		catch(HibernateProcessException  hpe) {		
			throw hpe;
		}
      return notesResult;
	}

	public List<PersonnelView> getFormedByPersonnel(Short levelId, Short officeId) throws PersistenceException {
		try {
			Map<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("levelId", levelId);
			queryParameters.put("officeId", officeId);
			queryParameters.put("statusId", PersonnelConstants.ACTIVE);
			List<PersonnelView> queryResult = executeNamedQuery(NamedQueryConstants.FORMEDBY_LOANOFFICERS_LIST, queryParameters);
			return queryResult;
		} catch (HibernateException he) {
			throw new PersistenceException(he);
		}
	}
	
	public CustomerPictureEntity retrievePicture(Integer customerId)throws PersistenceException {
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put("customerId",customerId);
		List queryResult=null;
		try{
			queryResult = executeNamedQuery(NamedQueryConstants.GET_CUSTOMER_PICTURE, queryParameters);
		}catch(HibernateException he){
			throw new PersistenceException(he);
		}
		return (CustomerPictureEntity)queryResult.get(0);
	}
}
