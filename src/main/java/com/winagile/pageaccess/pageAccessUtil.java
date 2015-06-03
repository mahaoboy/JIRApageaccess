package com.winagile.pageaccess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.winagile.jira.activeobject.AccessSaveService;
import com.winagile.jira.activeobject.SaveAccess;

public class pageAccessUtil {
	private List<Map<String, String>> realList = new ArrayList<Map<String, String>>();
	private Map<String, String> pageInfoAccessDate = new HashMap<String, String>();
	public static int START_INDEX = 0;
	public static int COUNT_ON_EACH_PAGE = 20;
	private int totalPage;
	private int totalItem = 0;
	private String groupName = null;
	private String spaceName = null;

	private Long fromDate = null;
	private Long toDate = null;
	private List<String> groupList = new ArrayList<String>();
	private List<String> spaceList = new ArrayList<String>();

	private String pageTitle = null;
	private String userKey = null;

	final private SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
	final private AccessSaveService as;
	final private IssueManager im;
	final private GroupManager gm;
	final private UserUtil uu;
	final private ProjectManager pm;
	private static final Logger log = LogManager
			.getLogger(pageAccessUtil.class);
	private final PluginLicenseManager licenseManager;
	private final I18nResolver i18n;

	public pageAccessUtil(AccessSaveService as, IssueManager im,
			GroupManager gm, UserUtil uu, ProjectManager pm,
			PluginLicenseManager licenseManager, I18nResolver i18n) {
		this.as = as;
		this.im = im;
		this.gm = gm;
		this.uu = uu;
		this.pm = pm;
		this.i18n = i18n;
		this.licenseManager = licenseManager;
	}

	public void checkLicenseStatus() throws MyException {
//		if (!licenseManager.getLicense().iterator().hasNext()) {
//			throw new MyException(
//					i18n.getText("PageAccess.exception.licenseerror"));
//		}
//		for (PluginLicense pluginLicense : licenseManager.getLicense()) {
//			if (pluginLicense.getError().isDefined()) {
//				throw new MyException(
//						i18n.getText("PageAccess.exception.licenseerror"));
//			}
//		}
	}

	public void getAllRealContext(int pageNum) {
		List<SaveAccess> saR = as.filterWithLimit(COUNT_ON_EACH_PAGE,
				calculateStartIndex(pageNum, COUNT_ON_EACH_PAGE));
		System.out.println("saR size:" + saR.size());

		realList = new ArrayList<Map<String, String>>();
		pageInfoAccessDate = new HashMap<String, String>();

		getRealList(saR);

		if (totalItem == 0) {
			totalItem = as.getCurrentItemsNum();
		}

		totalPage = calculateTotalPage(totalItem, COUNT_ON_EACH_PAGE);
	}

	public void getFilteredRealContext(int pageNum) {
		if (groupName == null && fromDate == null && toDate == null
				&& userKey == null && pageTitle == null && spaceName == null) {
			getAllRealContext(pageNum);
		} else {
			List<SaveAccess> saR;
			if (fromDate != null && toDate != null) {
				saR = as.filterWithDate(fromDate, toDate,
						userKey == null ? null : userKey);
			} else if (fromDate != null && toDate == null) {
				saR = as.filterWithStartDate(fromDate, userKey == null ? null
						: userKey);
			} else if (fromDate == null && toDate != null) {
				saR = as.filterWithEndDate(toDate, userKey == null ? null
						: userKey);
			} else {
				saR = as.all(userKey == null ? null : userKey);
			}

			if (pageTitle != null) {
				saR = filterPageTitle(saR);
			}

			if (groupName != null) {
				saR = filterGroup(saR);
			}
			if (spaceName != null) {
				saR = filterSpace(saR);
			}

			if (totalItem == 0) {
				totalItem = saR.size();
			}

			realList = new ArrayList<Map<String, String>>();
			pageInfoAccessDate = new HashMap<String, String>();

			totalPage = calculateTotalPage(totalItem, COUNT_ON_EACH_PAGE);
			int startIndex = calculateStartIndex(pageNum, COUNT_ON_EACH_PAGE);
			int endindex = startIndex + COUNT_ON_EACH_PAGE > saR.size() ? saR
					.size() : startIndex + COUNT_ON_EACH_PAGE;
			getRealList(saR.subList(startIndex, endindex));
		}
	}

	private List<SaveAccess> filterPageTitle(List<SaveAccess> saR) {
		List<SaveAccess> saRnew = new ArrayList<SaveAccess>();
		if (saR != null && !saR.isEmpty()) {
			for (SaveAccess saRI : saR) {
				if (im.getIssueObject(saRI.getPageId()) != null
						&& im.getIssueObject(saRI.getPageId()).getSummary()
								.equals(pageTitle)) {
					saRnew.add(saRI);
				}
			}
		}
		return saRnew;
	}

	private List<SaveAccess> filterGroup(List<SaveAccess> saR) {
		List<SaveAccess> saRnew = new ArrayList<SaveAccess>();
		if (saR != null && !saR.isEmpty()) {
			for (SaveAccess saRI : saR) {
				if (uu.getUserByKey(saRI.getUserKey()) != null
						&& gm.isUserInGroup(ApplicationUsers.toDirectoryUser(uu
								.getUserByKey(saRI.getUserKey())), groupName)) {
					saRnew.add(saRI);
				}
			}
		}
		return saRnew;
	}

	private List<SaveAccess> filterSpace(List<SaveAccess> saR) {
		List<SaveAccess> saRnew = new ArrayList<SaveAccess>();
		if (saR != null && !saR.isEmpty()) {
			for (SaveAccess saRI : saR) {
				if (im.getIssueObject(saRI.getPageId()) != null
						&& spaceName.equals(im.getIssueObject(saRI.getPageId())
								.getProjectObject().getName())) {
					saRnew.add(saRI);
				}
			}
		}
		return saRnew;
	}

	private String getIssueURI(Issue issuei) {
		return "/browse/" + issuei.getKey();
	}

	private void getRealList(List<SaveAccess> saR) {
		if (saR != null && !saR.isEmpty()) {
			for (SaveAccess saRI : saR) {
				Map<String, String> pageInfo = new HashMap<String, String>();
				Issue issueItem = im.getIssueObject(saRI.getPageId());
				ApplicationUser accessUser = uu.getUserByKey(saRI.getUserKey());
				if (accessUser != null && issueItem != null) {
					pageInfo.put("userName", accessUser.getDisplayName());

					StringBuffer groupNameList = new StringBuffer();
					if (gm.getGroupNamesForUser(accessUser) != null
							&& !gm.getGroupNamesForUser(accessUser).isEmpty()) {
						for (String groupName : gm
								.getGroupNamesForUser(accessUser)) {
							groupNameList.append(groupName);
							groupNameList.append("<br />");
						}
					}
					pageInfo.put("userGroups", groupNameList.append("")
							.toString());

					pageInfo.put("pageTitle", issueItem.getSummary());
					pageInfo.put("pageUrl", getIssueURI(issueItem));
					pageInfo.put("pageSpace", issueItem.getProjectObject()
							.getName());

					pageInfo.put("accessCount",
							String.valueOf(as.getAccessCount(saRI.getPageId(),
									saRI.getUserKey())));
					pageInfo.put("uniqueKey", saRI.getPageId().toString()
							+ saRI.getUserKey());

					getAccessTimeList(saRI.getPageId(), saRI.getUserKey());
					realList.add(pageInfo);
				} else {
					continue;
				}
				// System.out.println(realList.toString());
				// System.out.println(pageInfoAccessDate.toString());
			}
		}
	}

	private void getAccessTimeList(Long pageId, String userkey) {
		List<SaveAccess> saRT = new ArrayList<SaveAccess>();
		if (fromDate != null && toDate != null) {
			saRT = as.getAccessByFilterWithDate(pageId, userkey, fromDate,
					toDate);
		} else if (fromDate != null && toDate == null) {
			saRT = as.getAccessByFilterWithStartDate(pageId, userkey, fromDate);
		} else if (fromDate == null && toDate != null) {
			saRT = as.getAccessByFilterWithEndDate(pageId, userkey, toDate);
		} else {
			saRT = as.getAccessByFilter(pageId, userkey);
		}

		if (saRT != null && !saRT.isEmpty()) {
			StringBuffer extraAccessTimeList = new StringBuffer();
			for (SaveAccess saRTI : saRT) {
				extraAccessTimeList.append(sdf.format(new Date(saRTI
						.getAccessEntity())));
				extraAccessTimeList.append(" / ");
				extraAccessTimeList.append(saRTI.getRespTime());
				extraAccessTimeList.append("ms");
				extraAccessTimeList.append("<br />");
			}
			pageInfoAccessDate.put(pageId.toString() + userkey,
					extraAccessTimeList.append("").toString());
		}
	}

	protected int calculateTotalPage(int totalItem, int pageSize) {
		double dPageTotal = Math.ceil((double) totalItem / (double) pageSize);
		return (int) dPageTotal;
	}

	// method in protected scope for unit test
	protected int calculateStartIndex(int pageNumber, int pageSize) {
		return Math.max(0, (pageNumber - 1) * pageSize);
	}

	public List<Map<String, String>> getRealList() {
		return realList;
	}

	public void setRealList(List<Map<String, String>> realList) {
		this.realList = realList;
	}

	public Map<String, String> getPageInfoAccessDate() {
		return pageInfoAccessDate;
	}

	public void setPageInfoAccessDate(Map<String, String> pageInfoAccessDate) {
		this.pageInfoAccessDate = pageInfoAccessDate;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getTotalItem() {
		return totalItem;
	}

	public void setTotalItem(int totalItem) {
		this.totalItem = totalItem;
	}

	public List<String> getGroupList() {
		groupList = new ArrayList<String>();
		for (Group pg : gm.getAllGroups()) {
			groupList.add(pg.getName());
		}
		return groupList;
	}

	public void setGroupList(List<String> groupList) {
		this.groupList = groupList;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getFromDate() {
		return fromDate == null ? null : sdfDate.format(new Date(fromDate));
	}

	public void setFromDate(String fromDate) {
		try {
			this.fromDate = fromDate == null ? null : sdfDate.parse(fromDate)
					.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getToDate() {
		return toDate == null ? null : sdfDate.format(new Date(toDate));
	}

	public void setToDate(String toDate) {
		try {
			this.toDate = toDate == null ? null : sdfDate.parse(toDate)
					.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getUserKey() {
		return userKey == null ? null : uu.getUserByKey(userKey).getName();
	}

	public void setUserKey(String userName) {
		this.userKey = userName == null ? null : uu.getUserByName(userName)
				.getKey();
	}

	public List<String> getSpaceList() {
		spaceList = new ArrayList<String>();
		for (Project proj : pm.getProjectObjects()) {
			spaceList.add(proj.getName());
		}
		return spaceList;
	}

	public void setSpaceList(List<String> spaceList) {
		this.spaceList = spaceList;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

}
