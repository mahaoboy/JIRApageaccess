package com.winagile.pageaccess.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import com.winagile.pageaccess.MyException;
import com.winagile.pageaccess.pageAccessUtil;

public class PageAccessStatistics extends HttpServlet {
	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

	private WebSudoManager webSudoManager;
	private final pageAccessUtil pageUtil;
	private final LoginUriProvider loginUriProvider;
	private final UserManager userManager;
	private TemplateRenderer templateRenderer;
	private static final Logger log = LogManager
			.getLogger(PageAccessStatistics.class);

	public PageAccessStatistics(WebSudoManager webSudoManager,
			pageAccessUtil pageUtil, LoginUriProvider loginUriProvider,
			UserManager userManager, TemplateRenderer templateRenderer) {
		this.webSudoManager = webSudoManager;
		this.pageUtil = pageUtil;
		this.loginUriProvider = loginUriProvider;
		this.userManager = userManager;
		this.templateRenderer = templateRenderer;
	}

	private void redirectToLogin(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.sendRedirect(loginUriProvider.getLoginUri(getUri(request))
				.toASCIIString());
	}

	private URI getUri(HttpServletRequest request) {
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null) {
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			pageUtil.checkLicenseStatus();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Map<String, Object> context = Maps.newHashMap();
			context.put("ErrorMessage", e.getMessage());
			templateRenderer.render("/templates/pageAccessState.vm", context,
					resp.getWriter());
			return;
		}
		try {
			ApplicationUser loginUser = ComponentAccessor
					.getJiraAuthenticationContext().getUser();
			if (loginUser == null
					|| !userManager.isSystemAdmin(loginUser.getUsername())) {
				redirectToLogin(req, resp);
				return;
			}
			webSudoManager.willExecuteWebSudoRequest(req);
			int pageNum, totalItemNum;
			if (req.getParameterMap().containsKey("pageNumber")
					&& req.getParameter("pageNumber").length() > 0) {
				pageNum = Integer.valueOf(req.getParameter("pageNumber"));
			} else {
				pageNum = 1;
			}
			if (req.getParameterMap().containsKey("totalItem")
					&& req.getParameter("totalItem").length() > 0) {
				totalItemNum = Integer.valueOf(req.getParameter("totalItem"));
				pageUtil.setTotalItem(totalItemNum);
			} else {
				pageUtil.setTotalItem(0);
			}

			if (req.getParameterMap().containsKey("groupNameField")
					&& req.getParameter("groupNameField").length() > 0) {
				pageUtil.setGroupName(req.getParameter("groupNameField"));
			} else {
				pageUtil.setGroupName(null);
			}

			if (req.getParameterMap().containsKey("spaceNameField")
					&& req.getParameter("spaceNameField").length() > 0) {
				System.out.println("input spaceName : "
						+ req.getParameter("spaceNameField"));
				log.error("input spaceName : "
						+ req.getParameter("spaceNameField"));
				pageUtil.setSpaceName(URLDecoder.decode(
						req.getParameter("spaceNameField"), "UTF-8"));
			} else {
				pageUtil.setSpaceName(null);
			}
			if (req.getParameterMap().containsKey("fromDate")
					&& req.getParameter("fromDate").length() > 0) {
				System.out.println("req.getParameter(fromDate).length() : "
						+ req.getParameter("fromDate").length());
				pageUtil.setFromDate(req.getParameter("fromDate"));
			} else {
				pageUtil.setFromDate(null);
			}
			if (req.getParameterMap().containsKey("toDate")
					&& req.getParameter("toDate").length() > 0) {
				pageUtil.setToDate(req.getParameter("toDate"));
			} else {
				pageUtil.setToDate(null);
			}
			if (req.getParameterMap().containsKey("userNameField")
					&& req.getParameter("userNameField").length() > 0) {
				pageUtil.setUserKey(req.getParameter("userNameField"));
			} else {
				pageUtil.setUserKey(null);
			}
			if (req.getParameterMap().containsKey("pageTitleField")
					&& req.getParameter("pageTitleField").length() > 0) {
				pageUtil.setPageTitle(URLDecoder.decode(
						req.getParameter("pageTitleField"), "UTF-8"));
			} else {
				pageUtil.setPageTitle(null);
			}

			pageUtil.getFilteredRealContext(pageNum);

			Map<String, Object> context = Maps.newHashMap();
			context.put("asResult", pageUtil.getRealList());
			context.put("accessDate", pageUtil.getPageInfoAccessDate());

			context.put("pageNumber", pageNum);
			context.put("totalPage", pageUtil.getTotalPage());
			context.put("totalItem", pageUtil.getTotalItem());

			context.put("groupName", pageUtil.getGroupName());
			context.put("spaceName", pageUtil.getSpaceName());
			context.put("fromDate", pageUtil.getFromDate());
			context.put("toDate", pageUtil.getToDate());
			context.put("userName", pageUtil.getUserKey());
			context.put("pageTitle", pageUtil.getPageTitle());

			context.put("AllGroups", pageUtil.getGroupList());
			context.put("AllSpaces", pageUtil.getSpaceList());

			resp.setContentType("text/html;charset=utf-8");
			templateRenderer.render("/templates/pageAccessState.vm", context,
					resp.getWriter());
		} catch (WebSudoSessionException wes) {
			webSudoManager.enforceWebSudoProtection(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}