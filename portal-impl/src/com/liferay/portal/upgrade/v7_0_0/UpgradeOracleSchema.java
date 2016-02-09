/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.upgrade.v7_0_0;

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Ivica Cardic
 * @author Brian Wing Shun Chan
 * @author Istvan Sajtos
 */
public class UpgradeOracleSchema extends UpgradeProcess {

	protected void alterVarchar2Columns() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement(
				"select table_name, column_name, data_length from " +
					"user_tab_columns where data_type = 'VARCHAR2' and " +
						"char_used = 'B'");

			rs = ps.executeQuery();

			while (rs.next()) {
				String tableName = rs.getString(1);

				String columnName = rs.getString(2);
				int dataLength = rs.getInt(3);

				// LPS-44965

				if (ArrayUtil.contains(
						_AFFECTED_PORTAL_TABLE_NAMES, tableName, true)) {

					// LPS-33903
	
					if (!ArrayUtil.contains(
							_ORIGINAL_DATA_LENGTH_VALUES, dataLength)) {
	
						dataLength = dataLength / 4;
					}
	
					try {
						runSQL(
							"alter table " + tableName + " modify " + columnName +
								" varchar2(" + dataLength + " char)");
					}
					catch (SQLException sqle) {
						if (sqle.getErrorCode() == 1441) {
							if (_log.isWarnEnabled()) {
								StringBundler sb = new StringBundler(6);
	
								sb.append("Unable to alter length of column ");
								sb.append(columnName);
								sb.append(" for table ");
								sb.append(tableName);
								sb.append(
									" because it contains values that are ");
								sb.append("larger than the new column length");
	
								_log.warn(sb.toString());
							}
						}
						else {
							throw sqle;
						}
					}
				}
			}
		}
		finally {
			DataAccess.cleanUp(ps, rs);
		}
	}

	@Override
	protected void doUpgrade() throws Exception {
		DB db = DBManagerUtil.getDB();

		if (db.getDBType() != DBType.ORACLE) {
			return;
		}

		alterVarchar2Columns();
	}

	private static final String[] _AFFECTED_PORTAL_TABLE_NAMES = new String[] {
		"Account_", "Address", "AnnouncementsDelivery", "AnnouncementsEntry",
		"AnnouncementsFlag", "AssetCategory", "AssetCategoryProperty",
		"AssetEntries_AssetCategories", "AssetEntries_AssetTags", "AssetEntry",
		"AssetLink", "AssetTag", "AssetTagProperty", "AssetTagStats",
		"AssetVocabulary", "BlogsEntry", "BlogsStatsUser", "BookmarksEntry",
		"BookmarksFolder", "BrowserTracker", "CalEvent", "ClassName_",
		"ClusterGroup", "Company", "Contact_", "Counter", "Country",
		"CyrusUser", "CyrusVirtual", "DDLRecord", "DDLRecordSet",
		"DDLRecordVersion", "DDMContent", "DDMStorageLink", "DDMStructure",
		"DDMStructureLink", "DDMTemplate", "DLContent", "DLFileEntry",
		"DLFileEntryMetadata", "DLFileEntryType",
		"DLFileEntryTypes_DDMStructures", "DLFileEntryTypes_DLFolders",
		"DLFileRank", "DLFileShortcut", "DLFileVersion", "DLFolder", "DLSync",
		"EmailAddress", "ExpandoColumn", "ExpandoRow", "ExpandoTable",
		"ExpandoValue", "Group_", "Groups_Orgs", "Groups_Permissions",
		"Groups_Roles", "Groups_UserGroups", "IGFolder", "IGImage", "Image",
		"JournalArticle", "JournalArticleImage", "JournalArticleResource",
		"JournalContentSearch", "JournalFeed", "JournalStructure",
		"JournalTemplate", "Layout", "LayoutBranch", "LayoutPrototype",
		"LayoutRevision", "LayoutSet", "LayoutSetBranch", "LayoutSetPrototype",
		"ListType", "Lock_", "MBBan", "MBCategory", "MBDiscussion",
		"MBMailingList", "MBMessage", "MBMessageFlag", "MBStatsUser",
		"MBThread", "MBThreadFlag", "MDRAction", "MDRRule", "MDRRuleGroup",
		"MDRRuleGroupInstance", "MembershipRequest", "OrgGroupPermission",
		"OrgGroupRole", "OrgLabor", "Organization_", "PasswordPolicy",
		"PasswordPolicyRel", "PasswordTracker", "Permission_", "Phone",
		"PluginSetting", "PollsChoice", "PollsQuestion", "PollsVote",
		"PortalPreferences", "Portlet", "PortletItem", "PortletPreferences",
		"RatingsEntry", "RatingsStats", "Region", "Release_", "Repository",
		"RepositoryEntry", "ResourceAction", "ResourceBlock",
		"ResourceBlockPermission", "ResourceCode", "ResourcePermission",
		"ResourceTypePermission", "Resource_", "Role_", "Roles_Permissions",
		"SCFrameworkVersi_SCProductVers", "SCFrameworkVersion", "SCLicense",
		"SCLicenses_SCProductEntries", "SCProductEntry", "SCProductScreenshot",
		"SCProductVersion", "ServiceComponent", "Shard", "ShoppingCart",
		"ShoppingCategory", "ShoppingCoupon", "ShoppingItem",
		"ShoppingItemField", "ShoppingItemPrice", "ShoppingOrder",
		"ShoppingOrderItem", "SocialActivity", "SocialActivityAchievement",
		"SocialActivityCounter", "SocialActivityLimit", "SocialActivitySetting",
		"SocialEquityAssetEntry", "SocialEquityGroupSetting",
		"SocialEquityHistory", "SocialEquityLog", "SocialEquitySetting",
		"SocialEquityUser", "SocialRelation", "SocialRequest", "Subscription",
		"TagsAsset", "TagsAssets_TagsEntries", "TagsEntry", "TagsProperty",
		"TagsSource", "TagsVocabulary", "TasksProposal", "TasksReview", "Team",
		"Ticket", "UserGroup", "UserGroupGroupRole", "UserGroupRole",
		"UserGroups_Teams", "UserIdMapper", "UserNotificationEvent",
		"UserTracker", "UserTrackerPath", "User_", "Users_Groups", "Users_Orgs",
		"Users_Permissions", "Users_Roles", "Users_Teams", "Users_UserGroups",
		"VirtualHost", "Vocabulary", "WebDAVProps", "Website", "WikiNode",
		"WikiPage", "WikiPageResource", "WorkflowDefinitionLink",
		"WorkflowInstanceLink"
	};

	private static final int[] _ORIGINAL_DATA_LENGTH_VALUES = {
		75, 100, 150, 200, 255, 500, 1000, 1024, 2000, 4000
	};

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeOracleSchema.class);

}