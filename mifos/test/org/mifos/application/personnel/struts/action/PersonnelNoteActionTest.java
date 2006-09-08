package org.mifos.application.personnel.struts.action;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mifos.application.customer.business.CustomFieldView;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.personnel.business.service.PersonnelBusinessService;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.application.personnel.util.helpers.PersonnelLevel;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.Methods;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.business.util.Name;
import org.mifos.framework.components.fieldConfiguration.util.helpers.FieldConfigImplementer;
import org.mifos.framework.components.fieldConfiguration.util.helpers.FieldConfigItf;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.security.util.ActivityContext;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.struts.plugin.helper.EntityMasterData;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.Flow;
import org.mifos.framework.util.helpers.FlowManager;
import org.mifos.framework.util.helpers.ResourceLoader;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.framework.util.valueobjects.Context;

public class PersonnelNoteActionTest extends MifosMockStrutsTestCase {

	private String flowKey;

	private UserContext userContext;

	private OfficeBO createdBranchOffice;

	PersonnelBO personnel;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try {
			setServletConfigFile(ResourceLoader.getURI("WEB-INF/web.xml")
					.getPath());
			setConfigFile(ResourceLoader.getURI(
					"org/mifos/framework/util/helpers/struts-config.xml")
					.getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		userContext = TestObjectFactory.getUserContext();
		request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
		addRequestParameter("recordLoanOfficerId", "1");
		addRequestParameter("recordOfficeId", "1");
		ActivityContext ac = new ActivityContext((short) 0, userContext
				.getBranchId().shortValue(), userContext.getId().shortValue());
		request.getSession(false).setAttribute("ActivityContext", ac);
		Flow flow = new Flow();
		flowKey = String.valueOf(System.currentTimeMillis());
		FlowManager flowManager = new FlowManager();
		flowManager.addFLow(flowKey, flow);
		request.getSession(false).setAttribute(Constants.FLOWMANAGER,
				flowManager);
		EntityMasterData.getInstance().init();
		FieldConfigItf fieldConfigItf = FieldConfigImplementer.getInstance();
		fieldConfigItf.init();
		FieldConfigImplementer.getInstance();
		getActionServlet().getServletContext().setAttribute(
				Constants.FIELD_CONFIGURATION,
				fieldConfigItf.getEntityMandatoryFieldMap());
		request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		addRequestParameter("input", "CreateUser");
		PersonnelBusinessService personnelBusinessService = new PersonnelBusinessService();
		SessionUtils
				.setAttribute(PersonnelConstants.OFFICE,
						personnelBusinessService.getOffice(Short.valueOf("1")),
						request);
		SessionUtils.setAttribute(PersonnelConstants.ROLES_LIST,
				personnelBusinessService.getRoles(), request);
		SessionUtils.setAttribute(PersonnelConstants.ROLEMASTERLIST,
				personnelBusinessService.getRoles(), request);

		personnelBusinessService = null;

	}

	@Override
	protected void tearDown() throws Exception {
		userContext = null;
		TestObjectFactory.cleanUp(personnel);
		TestObjectFactory.cleanUp(createdBranchOffice);
		HibernateUtil.closeSession();
		super.tearDown();
	}

	public void testSuccessLoadPersonnelNote() throws Exception {
		createPersonnelAndSetInSession(getBranchOffice(),
				PersonnelLevel.LOAN_OFFICER);
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.load.toString());
		addRequestParameter("personnelId", personnel.getPersonnelId()
				.toString());
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		verifyNoActionErrors();
		verifyNoActionMessages();
		verifyForward(ActionForwards.load_success.toString());
	}

	public void testFailurePreviewWithNotesValueNull() throws Exception {
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.preview.toString());
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		assertEquals(1, getErrrorSize());
		assertEquals("Notes", 1,
				getErrrorSize(PersonnelConstants.ERROR_MANDATORY_TEXT_AREA));
		verifyInputForward();
	}

	public void testFailurePreviewWithNotesValueExceedingMaxLength()
			throws Exception {
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter(
				"comment",
				"Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters "
						+ "Testing for comment length exceeding by 500 characters "
						+ "Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters"
						+ "Testing for comment length exceeding by 500 characters");
		addRequestParameter("method", Methods.preview.toString());
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		assertEquals(1, getErrrorSize());
		assertEquals("Notes", 1,
				getErrrorSize(PersonnelConstants.MAXIMUM_LENGTH));
		verifyInputForward();
	}

	public void testSuccessPreviewPersonnelNote() throws Exception {
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.preview.toString());
		addRequestParameter("comment", "Test");
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		verifyNoActionErrors();
		verifyNoActionMessages();
		verifyForward(ActionForwards.preview_success.toString());
	}

	public void testSuccessPreviousPersonnelNote() {
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.previous.toString());
		addRequestParameter("comment", "Test");
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		verifyForward(ActionForwards.previous_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
	}

	public void testSuccessCancelPersonnelNote() {
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.cancel.toString());
		addRequestParameter("comment", "Test");
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		verifyForward(ActionForwards.cancel_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
	}

	public void testSuccessCreatePersonnelNote() throws Exception {
		createPersonnelAndSetInSession(getBranchOffice(),
				PersonnelLevel.LOAN_OFFICER);
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.create.toString());
		addRequestParameter("personnelId", personnel.getPersonnelId()
				.toString());
		addRequestParameter("comment", "Test");
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		verifyForward(ActionForwards.create_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
	}

	public void testSuccessSearch() throws Exception {
		Context context = new Context();
		SessionUtils.setAttribute(Constants.CONTEXT, context, request
				.getSession());
		createPersonnelAndSetInSession(getBranchOffice(),
				PersonnelLevel.LOAN_OFFICER);
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.load.toString());
		addRequestParameter("personnelId", personnel.getPersonnelId()
				.toString());
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.preview.toString());
		addRequestParameter("comment", "Notes created");
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();

		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.create.toString());
		addRequestParameter("comment", "Notes created");
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();

		setRequestPathInfo("/personnelNoteAction.do");
		addRequestParameter("method", Methods.search.toString());
		addRequestParameter(Constants.CURRENTFLOWKEY, (String) request
				.getAttribute(Constants.CURRENTFLOWKEY));
		actionPerform();
		verifyForward(ActionForwards.search_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();

		context = (Context) SessionUtils.getAttribute(Constants.CONTEXT,
				request.getSession());
		assertEquals("Size of the search result should be 1", 1, context
				.getSearchResult().getSize());
	}

	private void createPersonnelAndSetInSession(OfficeBO office,
			PersonnelLevel personnelLevel) throws Exception {
		List<CustomFieldView> customFieldView = new ArrayList<CustomFieldView>();
		customFieldView.add(new CustomFieldView(Short.valueOf("1"), "123456",
				Short.valueOf("1")));
		Address address = new Address("abcd", "abcd", "abcd", "abcd", "abcd",
				"abcd", "abcd", "abcd");
		Name name = new Name("XYZ", null, null, "Last Name");
		Date date = new Date();
		personnel = new PersonnelBO(personnelLevel, office, Integer
				.valueOf("1"), Short.valueOf("1"), "ABCD", "XYZ",
				"xyz@yahoo.com", null, customFieldView, name, "111111", date,
				Integer.valueOf("1"), Integer.valueOf("1"), date, date,
				address, userContext.getId());
		personnel.save();
		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();
		personnel = (PersonnelBO) HibernateUtil.getSessionTL().get(
				PersonnelBO.class, personnel.getPersonnelId());
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, personnel, request);
	}

	public OfficeBO getBranchOffice() {
		return TestObjectFactory.getOffice(Short.valueOf("3"));
	}
}
