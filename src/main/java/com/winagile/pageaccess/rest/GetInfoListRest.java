package com.winagile.pageaccess.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;

/**
 * A resource of message.
 */
@Path("/infomessage")
@AnonymousAllowed
@Produces({ "application/json" })
public class GetInfoListRest {

	final private UserManager um;
	private static final String BREGEX = "\\b";
	final private static String repalceRegex = "[\\-\\[\\]\\/\\{\\}\\(\\)\\*\\+\\?\\.\\\\\\^\\$\\|]";
	final SearchService searchService = ComponentAccessor
			.getComponent(SearchService.class);
	User auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
			.getJiraAuthenticationContext().getUser());

	GetInfoListRest(UserManager um) {
		this.um = um;
	}

	@GET
	@Path("/user")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getUserMessage(@QueryParam("query") String query) {
		Collection<GetInfoItemListModel> getModel = new ArrayList<GetInfoItemListModel>();
		Pattern p = Pattern.compile(
				BREGEX + query.toLowerCase().replaceAll(repalceRegex, ""),
				Pattern.CASE_INSENSITIVE);
		for (ApplicationUser userinfo : um.getAllApplicationUsers()) {
			if (p.matcher(userinfo.getDisplayName()).find()) {
				getModel.add(new GetInfoItemListModel(
						userinfo.getDisplayName(), userinfo.getKey()));
			}
		}
		return Response.ok(new GetInfoListModel(getModel)).build();

	}

	@GET
	@Path("/page")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getPageMessage(@QueryParam("query") String queryString) {
		Collection<GetInfoItemListModel> getModel = new ArrayList<GetInfoItemListModel>();
		Pattern p = Pattern.compile(BREGEX
				+ queryString.toLowerCase().replaceAll(repalceRegex, ""),
				Pattern.CASE_INSENSITIVE);

		String jqlQuery = "";
		SearchService.ParseResult parseResult = searchService.parseQuery(auser,
				jqlQuery);

		if (parseResult.isValid()) {
			Query query = parseResult.getQuery();
			SearchResults results;
			try {
				results = searchService.search(auser, query,
						PagerFilter.getUnlimitedFilter());
				List<Issue> issues = results.getIssues();

				for (Issue is : issues) {
					if (p.matcher(is.getSummary()).find()) {
						getModel.add(new GetInfoItemListModel(is.getSummary(),
								is.getId().toString()));
					}
				}
			} catch (SearchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return Response.ok(new GetInfoListModel(getModel)).build();

	}
}
