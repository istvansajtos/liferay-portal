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

package com.liferay.layout.page.template.internal.upgrade.v3_4_4;

import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutModel;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Márk Gulácsy
 */
public class LayoutPageTemplateStructureUpgradeProcess extends UpgradeProcess {

	public LayoutPageTemplateStructureUpgradeProcess(
		LayoutLocalService layoutLocalService) {

		_layoutLocalService = layoutLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		
		// Delete orphan LayoutPageTemplateStructure-s
		DynamicQuery allPagesQuery = _layoutLocalService.dynamicQuery();
		List<Layout> layouts = _layoutLocalService.dynamicQuery(allPagesQuery);
		long[] plids = layouts.stream().mapToLong(LayoutModel::getPlid).toArray();

		String markers = StringUtils.repeat(",?", plids.length).substring(1);
		String sql = "delete from LayoutPageTemplateStructure where classPK not in (" + markers + ")";
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			for (int i = 0; i < plids.length; i++){
				preparedStatement.setString(i + 1, String.valueOf(plids[i]));
			}
			boolean isDeleted = preparedStatement.execute();
			if (!isDeleted) {
				System.out.println("Deletion failed!");
			}
		}

		// Delete the LayoutPageTemplateStructure-s of the widget pages and set the status of the widget pages to 0
		/*
		Get all the widget pages
		Get all of the LayoutPageTemplateStructure-s of the widget pages (classPK list will be the plids of the widget pages which have a LayoutPageTemplateStructure)
		Set the satus to 0 on the widget pages (classPK list)
		Delete the LayoutPageTemplateStructure-s
		*/

		DynamicQuery widgetLayoutsQuery = _layoutLocalService.dynamicQuery();
		widgetLayoutsQuery.add(RestrictionsFactoryUtil.eq("type", LayoutConstants.TYPE_PORTLET));
		List<Layout> widgetLayouts = _layoutLocalService.dynamicQuery(widgetLayoutsQuery);

		try (PreparedStatement preparedStatement = AutoBatchPreparedStatementUtil.autoBatch(connection.prepareStatement("select * from LayoutPageTemplateStructure where classPK = ?"))){
			for (Layout layout: widgetLayouts) {
				preparedStatement.setLong(1, layout.getPlid());
				preparedStatement.addBatch();
			}
			ResultSet results = preparedStatement.executeBatch();
			ArrayList<Long> plidsList = new ArrayList<Long>();
			while (results.next()){
				plidsList.add(results.getLong("classPK"));
			}

			DynamicQuery widgetLayoutsWithStructureQuery = _layoutLocalService.dynamicQuery();
			widgetLayoutsWithStructureQuery.add(RestrictionsFactoryUtil.in("plid", plidsList));
			List<Layout> widgetLayoutsWithStructure = _layoutLocalService.dynamicQuery(widgetLayoutsWithStructureQuery);

			Map<Long, Long> userIdsByPlids = widgetLayoutsWithStructure.stream().collect(Collectors.toMap(Layout::getPlid, Layout::getUserId));

			ServiceContext serviceContext = new ServiceContext();

			while (results.next()){
				long userId = userIdsByPlids.get(results.getLong("plid"));
				_layoutLocalService.updateStatus(userId, results.getLong("plid"),
					WorkflowConstants.STATUS_APPROVED, serviceContext);
			}

			try (PreparedStatement preparedStatement1 = AutoBatchPreparedStatementUtil.autoBatch(connection.prepareStatement("delete LayoutPageTemplateStructure where classPK = ?"))) {
				while (results.next()) {
					preparedStatement1.setLong(1, results.getLong("classPK"));
				}
				preparedStatement1.executeBatch();
			}
		}

		throw new Exception();
	}

	private final LayoutLocalService _layoutLocalService;

	private static final Log _log = LogFactoryUtil.getLog(LayoutPageTemplateStructureUpgradeProcess.class);

}