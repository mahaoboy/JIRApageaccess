package com.winagile.pageaccess.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.winagile.jira.activeobject.AccessSaveService;
import com.winagile.pageaccess.MyException;
import com.winagile.pageaccess.pageAccessUtil;

/**
 * A resource of message.
 */
@Path("/message")
public class AddPageAccess {

	private static String SUCC = "success";
	private static String FAIL = "failed";
	private static String NOTLOGINPAGENOTEXIST = "notLoginOrPageNotExist";
	private AccessSaveService accessSaveService;
	private IssueManager im;
	private final pageAccessUtil pageUtil;

	public AddPageAccess(AccessSaveService accessSaveService, IssueManager im,
			pageAccessUtil pageUtil) {
		this.accessSaveService = accessSaveService;
		this.im = im;
		this.pageUtil = pageUtil;
	}

	@GET
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{key}")
	public Response getMessage(@PathParam("key") String key)
			throws GenericEntityException {
		try {
			pageUtil.checkLicenseStatus();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(500)
					.entity(new AddPageAccessModel("License Error")).build();
		}
		return Response.ok(new AddPageAccessModel(addAccessInfo(key))).build();
	}

	private String addAccessInfo(String issuekey) throws GenericEntityException {
		final String[] splittedStr = issuekey.split("_");
		final String issuekeys = splittedStr[0];
		final String respTime = splittedStr[1] != null ? splittedStr[1] : "";
		if (ComponentAccessor.getJiraAuthenticationContext().getUser() != null
				&& im.isExistingIssueKey(issuekeys)) {

			if (accessSaveService.add(im.getIssueObject(issuekeys).getId(),
					ComponentAccessor.getJiraAuthenticationContext().getUser()
							.getKey(), respTime) != null) {
				return SUCC;
			} else {
				return FAIL;
			}
		} else {
			return NOTLOGINPAGENOTEXIST;
		}
	}
}