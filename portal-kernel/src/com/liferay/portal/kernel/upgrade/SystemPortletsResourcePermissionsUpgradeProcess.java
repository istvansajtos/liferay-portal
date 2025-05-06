/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.upgrade;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Istvan Sajtos
 */
public class SystemPortletsResourcePermissionsUpgradeProcess
	extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		CompanyLocalServiceUtil.forEachCompanyId(
			companyId -> _removeGuestResourcePermissions(companyId));
	}

	private long _getGuestRoleId(long companyId) throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select ctCollectionId, roleId from Role_ where companyId = " +
					"? and name = ?")) {

			preparedStatement.setLong(1, companyId);
			preparedStatement.setString(2, RoleConstants.GUEST);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (!resultSet.next()) {
					throw new IllegalStateException(
						"Unable to find Guest role for company " + companyId);
				}

				return resultSet.getLong("roleId");
			}
		}
	}

	private void _removeGuestResourcePermissions(long companyId)
		throws Exception {

		for (String portletName : _systemPortlets) {
			try (PreparedStatement preparedStatement =
				connection.prepareStatement(
					"delete from ResourcePermission where companyId = ? and " +
						"name = ? and roleId = ?")) {

					preparedStatement.setLong(1, companyId);
					preparedStatement.setString(2, portletName);
					preparedStatement.setLong(3, _getGuestRoleId(companyId));

					preparedStatement.execute();
			}
		}
	}

	private final String[] _systemPortlets = new String[]{
		//Product Menu
		//============
		//Design:
		"com_liferay_style_book_web_internal_portlet_StyleBookPortlet",
		"com_liferay_fragment_web_portlet_FragmentPortlet",
		"com_liferay_template_web_internal_portlet_TemplatePortlet",
		"com_liferay_layout_page_template_admin_web_portlet_LayoutPageTemplatesPortlet",
		//Site Builder:
		"com_liferay_layout_admin_web_portlet_GroupPagesPortlet",
		"com_liferay_site_navigation_admin_web_portlet_SiteNavigationAdminPortlet",
		"com_liferay_asset_list_web_portlet_AssetListPortlet",
		//Content & Data
		"com_liferay_portal_workflow_kaleo_forms_web_portlet_KaleoFormsAdminPortlet",
		"com_liferay_journal_web_portlet_JournalPortlet",
		"com_liferay_blogs_web_portlet_BlogsAdminPortlet",
		"com_liferay_bookmarks_web_portlet_BookmarksAdminPortlet",
		"com_liferay_document_library_web_portlet_DLAdminPortlet",
		"com_liferay_dynamic_data_mapping_form_web_portlet_DDMFormAdminPortlet",
		"com_liferay_dynamic_data_mapping_data_provider_web_portlet_DDMDataProviderPortlet",
		"com_liferay_knowledge_base_web_portlet_AdminPortlet",
		"com_liferay_message_boards_web_portlet_MBAdminPortlet",
		"com_liferay_wiki_web_portlet_WikiAdminPortlet",
		"com_liferay_translation_web_internal_portlet_TranslationPortlet",
		//Categorization
		"com_liferay_asset_categories_admin_web_portlet_AssetCategoriesAdminPortlet",
		"com_liferay_asset_tags_admin_web_portlet_AssetTagsAdminPortlet",
		//Recycle Bin
		"com_liferay_trash_web_portlet_TrashPortlet",
		//People
		"com_liferay_site_memberships_web_portlet_SiteMembershipsPortlet",
		"com_liferay_site_teams_web_portlet_SiteTeamsPortlet",
		"com_liferay_segments_web_internal_portlet_SegmentsPortlet",
		//Configuration
		"com_liferay_portal_reports_engine_console_web_admin_portlet_AdminPortlet",
		"com_liferay_site_admin_web_portlet_SiteSettingsPortlet",
		"com_liferay_redirect_web_internal_portlet_RedirectPortlet",
		"com_liferay_layout_locked_layouts_web_internal_portlet_LockedLayoutsPortlet",
		"com_liferay_portal_workflow_web_internal_portlet_SiteAdministrationWorkflowPortlet",
		//Publishing
		"com_liferay_staging_processes_web_portlet_StagingProcessesPortlet",
		"com_liferay_exportimport_web_portlet_ExportPortlet",
		"com_liferay_exportimport_web_portlet_ImportPortlet",
		//Applications Menu > Applications
		//================================
		//Content
		"com_liferay_depot_web_portlet_DepotAdminPortlet",
		"com_liferay_content_dashboard_web_portlet_ContentDashboardAdminPortlet",
		//Publications
		"com_liferay_change_tracking_web_portlet_PublicationsPortlet",
		//Workflow
		"com_liferay_portal_workflow_web_portlet_ControlPanelWorkflowPortlet",
		"com_liferay_portal_workflow_kaleo_designer_web_portlet_KaleoDesignerPortlet",
		"com_liferay_portal_workflow_metrics_web_internal_portlet_WorkflowMetricsPortlet",
		"com_liferay_portal_workflow_web_internal_portlet_ControlPanelWorkflowInstancePortlet",
		//Search Experiences
		"com_liferay_search_experiences_web_internal_blueprint_admin_portlet_SXPBlueprintAdminPortlet",
		//Search Tuning
		"com_liferay_portal_search_tuning_synonyms_web_internal_portlet_SynonymsPortlet",
		"com_liferay_portal_search_tuning_rankings_web_internal_portlet_ResultRankingsPortlet",
		//Communication
		"com_liferay_announcements_web_portlet_AnnouncementsAdminPortlet",
		//Custom apps
		"com_liferay_client_extension_web_internal_portlet_ClientExtensionAdminPortlet",
		//Applications Menu > Commerce
		//============================
		//Order management
		"com_liferay_commerce_order_web_internal_portlet_CommerceOrderPortlet",
		"com_liferay_commerce_order_web_internal_portlet_CommerceOrderTypePortlet",
		"com_liferay_commerce_order_rule_web_internal_portlet_COREntryPortlet",
		"com_liferay_commerce_shipment_web_internal_portlet_CommerceShipmentPortlet",
		"com_liferay_commerce_subscription_web_internal_portlet_CommerceSubscriptionEntryPortlet",
		"com_liferay_commerce_term_web_internal_portlet_CommerceTermEntryPortlet",
		//Inventory management
		"com_liferay_commerce_inventory_web_internal_portlet_CommerceInventoryPortlet",
		"com_liferay_commerce_warehouse_web_internal_portlet_CommerceInventoryWarehousePortlet",
		// Pricing
		"com_liferay_commerce_pricing_web_internal_portlet_CommercePriceListPortlet",
		"com_liferay_commerce_pricing_web_internal_portlet_CommercePromotionPortlet",
		"com_liferay_commerce_pricing_web_internal_portlet_CommerceDiscountPortlet",
		"com_liferay_commerce_pricing_web_internal_portlet_CommercePricingClassesPortlet",
		"com_liferay_commerce_product_tax_category_web_internal_portlet_CPTaxCategoryPortlet",
		//Payment management
		"com_liferay_commerce_payment_web_internal_portlet_CommercePaymentPortlet",
		//Product management
		"com_liferay_commerce_catalog_web_internal_portlet_CommerceCatalogsPortlet",
		"com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet",
		"com_liferay_commerce_product_options_web_internal_portlet_CPOptionsPortlet",
		"com_liferay_commerce_product_options_web_internal_portlet_CPSpecificationOptionsPortlet",
		//Store  management
		"com_liferay_commerce_channel_web_internal_portlet_CommerceChannelsPortlet",
		"com_liferay_commerce_currency_web_internal_portlet_CommerceCurrencyPortlet",
		//Setting
		"com_liferay_commerce_availability_estimate_web_internal_portlet_CommerceAvailabilityEstimatePortlet",
		"com_liferay_commerce_product_measurement_unit_web_internal_portlet_CPMeasurementUnitPortlet",
		"com_liferay_commerce_avalara_connector_web_internal_portlet_CommerceAvalaraPortlet",
		"com_liferay_commerce_health_status_web_internal_portlet_CommerceHealthCheckPortlet",
		"com_liferay_commerce_dashboard_web_internal_portlet_CommerceDashboardStatusChartPortlet",
		"com_liferay_commerce_order_web_internal_portlet_CommerceReturnPortlet",
		"com_liferay_commerce_tax_web_internal_portlet_CommerceTaxMethodPortlet",
		"com_liferay_commerce_account_web_internal_portlet_CommerceAccountPortlet",
		"com_liferay_commerce_payment_web_internal_portlet_CommercePaymentMethodPortlet",
		"com_liferay_commerce_shipping_web_internal_portlet_CommerceShippingMethodPortlet",
		"com_liferay_commerce_dashboard_web_internal_portlet_CommerceDashboardHistoryChartPortlet",
		"com_liferay_commerce_product_definitions_web_internal_portlet_CPConfigurationListsPortlet",
		"com_liferay_commerce_dashboard_web_internal_portlet_CommerceDashboardOverviewChartPortlet",
		//Applications Menu > Control Panel
		//=================================
		//USERS
		"com_liferay_users_admin_web_portlet_UsersAdminPortlet",
		"com_liferay_user_groups_admin_web_portlet_UserGroupsAdminPortlet",
		"com_liferay_roles_admin_web_portlet_RolesAdminPortlet",
		"com_liferay_monitoring_web_portlet_MonitoringPortlet",
		"com_liferay_users_admin_web_portlet_ServiceAccountsPortlet",
		//SITES
		"com_liferay_site_admin_web_portlet_SiteAdminPortlet",
		//?????
		//ACCOUNTS
		"com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet",
		"com_liferay_account_admin_web_internal_portlet_AccountUsersAdminPortlet",
		"com_liferay_account_admin_web_internal_portlet_AccountGroupsAdminPortlet",
		// CONFIGURATION
		"com_liferay_configuration_admin_web_portlet_SystemSettingsPortlet",
		"com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet",
		"com_liferay_push_notifications_web_portlet_PushNotificationsPortlet",
		"com_liferay_plugins_admin_web_portlet_PluginsAdminPortlet",
		"com_liferay_portal_search_admin_web_portlet_SearchAdminPortlet",
		"com_liferay_expando_web_portlet_ExpandoPortlet",
		"com_liferay_portal_language_override_web_internal_portlet_PLOPortlet",
		"com_liferay_adaptive_media_web_portlet_AMPortlet",
		"com_liferay_dispatch_web_internal_portlet_DispatchPortlet",
		"com_liferay_address_web_internal_portlet_CountriesManagementAdminPortlet",
		//NOTIFICATIONS
		"com_liferay_notification_web_internal_portlet_NotificationTemplatesPortlet",
		"com_liferay_notification_web_internal_portlet_NotificationQueueEntriesPortlet",
		//OBJECT
		"com_liferay_object_web_internal_object_definitions_portlet_ObjectDefinitionsPortlet",
		"com_liferay_object_web_internal_list_type_portlet_portlet_ListTypeDefinitionsPortlet",
		//SECURITY
		"com_liferay_portal_security_audit_web_portlet_AuditPortlet",
		"com_liferay_oauth_client_admin_web_internal_portlet_OAuthClientAdminPortlet",
		"com_liferay_oauth2_provider_web_internal_portlet_OAuth2AdminPortlet",
		"com_liferay_password_policies_admin_web_portlet_PasswordPoliciesAdminPortlet",
		"com_liferay_saml_web_internal_portlet_SamlAdminPortlet",
		"com_liferay_portal_security_service_access_policy_web_portlet_SAPPortlet",
		//SYSTEM
		"com_liferay_server_admin_web_portlet_ServerAdminPortlet",
		"com_liferay_marketplace_app_manager_web_portlet_MarketplaceAppManagerPortlet",
		"com_liferay_gogo_shell_web_internal_portlet_GogoShellPortlet",
		"com_liferay_portal_instances_web_portlet_PortalInstancesPortlet",
		"com_liferay_on_demand_admin_web_internal_portlet_OnDemandAdminPortlet",
		//MARKETPLACE
		"com_liferay_marketplace_store_web_portlet_MarketplacePurchasedPortlet",
		"com_liferay_marketplace_store_web_portlet_MarketplaceStorePortlet",
		"com_liferay_license_manager_web_portlet_LicenseManagerPortlet",
		//User Profile Menu
		//=================
		"com_liferay_item_selector_web_portlet_ItemSelectorPortlet",
		//My Profile
		//My Dashboard
		//---
		"com_liferay_notifications_web_portlet_NotificationsPortlet",
		"com_liferay_sharing_web_portlet_SharedAssetsPortlet",
		"com_liferay_portal_workflow_web_internal_portlet_UserWorkflowPortlet",
		"com_liferay_portal_workflow_task_web_portlet_MyWorkflowTaskPortlet",
		//---
		"com_liferay_my_account_web_portlet_MyAccountPortlet",
		"com_liferay_oauth2_provider_web_internal_portlet_OAuth2ConnectedApplicationsPortlet",
		"com_liferay_users_admin_web_portlet_MyOrganizationsPortlet",
		//My Subscriptions (Can be added to pages)
		//WHITELISTED via portlet.add.default.resource.check.whitelist property
		//=====================================================================
		"com_liferay_login_web_portlet_FastLoginPortlet",
		"com_liferay_portlet_configuration_css_web_portlet_PortletConfigurationCSSPortlet",
		"com_liferay_portlet_configuration_web_portlet_PortletConfigurationPortlet",
		"com_liferay_product_navigation_simulation_web_portlet_SimulationPortlet",
		"com_liferay_staging_bar_web_portlet_StagingBarPortlet",
		//Product Menu > Configuration > Site Settings: Configurable on Site level.
		//=============================================
		"com_liferay_ai_creator_openai_web_internal_portlet_AICreatorOpenAIPortlet",
		"com_liferay_digital_signature_web_internal_portlet_DigitalSignaturePortlet",
		//Misc:
		//=====
		//TO EVERY USER:
		"com_liferay_accessibility_menu_web_portlet_AccessibilityMenuPortlet",
		"com_liferay_asset_display_web_portlet_AssetDisplayPortlet",
		"com_liferay_asset_tags_compiler_web_portlet_AssetTagsCompilerPortlet",
		"com_liferay_asset_web_portlet_AssetPortlet",
		//90 (Login?/Portal?)
		"com_liferay_comment_web_portlet_CommentPortlet",
		"com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet",
		"com_liferay_dynamic_data_lists_web_portlet_DDLDisplayPortlet",
		"com_liferay_flags_web_portlet_FlagsPortlet",
		"com_liferay_image_uploader_web_portlet_ImageUploaderPortlet",
		"com_liferay_mentions_web_portlet_MentionsPortlet",
		"com_liferay_multi_factor_authentication_portlet_web_internal_portlet_MFAEmailOTPVerifyPortlet",
		"com_liferay_multi_factor_authentication_web_portlet_MFAVerifyPortlet",
		"com_liferay_oauth2_provider_web_internal_portlet_OAuth2AuthorizePortlet",
		"com_liferay_product_navigation_control_menu_web_portlet_ProductNavigationControlMenuPortlet",
		"com_liferay_product_navigation_personal_menu_web_internal_portlet_PersonalMenuPortlet",
		"com_liferay_product_navigation_product_menu_web_portlet_ProductMenuPortlet",
		"com_liferay_product_navigation_user_personal_bar_web_portlet_ProductNavigationUserPersonalBarPortlet",
		"com_liferay_reading_time_web_portlet_ReadingTimePortlet",
		"com_liferay_sharing_web_portlet_SharingPortlet",
		"com_liferay_subscription_web_internal_portlet_UnsubscribePortlet",
		// TO SITE ADMINS:
		"com_liferay_calendar_web_portlet_CalendarAdminPortlet",
		"com_liferay_digital_signature_web_internal_portlet_CollectDigitalSignaturePortlet",
		"com_liferay_dynamic_data_lists_web_portlet_DDLPortlet",
		"com_liferay_dynamic_data_mapping_web_portlet_DDMPortlet",
		"com_liferay_dynamic_data_mapping_web_portlet_PortletDisplayTemplatePortlet",
		"com_liferay_depot_web_portlet_DepotSettingsPortlet",
		"com_liferay_exportimport_web_portlet_ExportImportPortlet",
		"com_liferay_layout_content_page_editor_web_internal_portlet_ContentPageToolbarPortlet",
		"com_liferay_layout_content_page_editor_web_internal_portlet_ContentPageEditorPortlet",
		"com_liferay_layout_prototype_web_portlet_LayoutPrototypePortlet",
		"com_liferay_layout_set_prototype_web_portlet_LayoutSetPrototypePortlet",
		"com_liferay_layout_set_prototype_web_portlet_SiteTemplateSettingsPortlet",
		"com_liferay_locked_items_web_internal_portlet_LockedItemsPortlet",
		"com_liferay_portlet_configuration_sharing_web_portlet_PortletConfigurationSharingPortlet",
		"com_liferay_scim_configuration_web_internal_portlet_ScimPortlet",
		"com_liferay_segments_experiment_web_internal_portlet_SegmentsExperimentPortlet",
		"com_liferay_segments_simulation_web_internal_portlet_SegmentsSimulationPortlet",
		"com_liferay_site_initializer_extender_web_SiteInitializerPortlet",
		"com_liferay_staging_configuration_web_portlet_StagingConfigurationPortlet",
		// TO ADMINS:
		"com_liferay_account_admin_web_internal_portlet_AccountUsersRegistrationPortlet",
		"com_liferay_analytics_reports_web_internal_portlet_AnalyticsReportsPortlet",
		"com_liferay_batch_planner_web_internal_portlet_BatchPlannerPortlet",
		"com_liferay_exportimport_web_portlet_ChangesetPortlet",
		"com_liferay_exportimport_web_portlet_CompanyExportPortlet",
		"com_liferay_exportimport_web_portlet_CompanyImportPortlet",
		"com_liferay_frontend_data_set_admin_web_internal_portlet_FDSAdminPortlet",
		"com_liferay_headless_builder_web_internal_portlet_HeadlessBuilderPortlet",
		"com_liferay_portal_background_task_web_internal_portlet_BackgroundTaskPortlet",
		"com_liferay_portal_company_log_web_internal_portlet_PortalCompanyLogPortlet",
		"com_liferay_product_navigation_applications_menu_web_internal_portlet_ProductNavigationApplicationsMenuPortlet",
		"com_liferay_search_experiences_web_internal_power_tools_portlet_SXPPowerToolsPortlet",
		"com_liferay_user_associated_data_web_portlet_UserAssociatedDataPortlet"
	};

}