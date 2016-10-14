/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.frontend.filter.TreeFilter;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListRhnSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * VirtualSystemsListSetupAction
 * @version $Rev$
 */
public class VirtualSystemSetupAction extends RhnAction
        implements Listable<VirtualSystemOverview> {

    private static final Logger LOG = Logger.getLogger(VirtualSystemSetupAction.class);

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        ListRhnSetHelper helper =
                new ListRhnSetHelper(this, request, RhnSetDecl.SYSTEMS);
        helper.execute();

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /**
     * Sets the status and entitlementLevel variables of each System Overview
     * @param dr The list of System Overviews
     * @param user The user viewing the System List
     */
    public void setStatusDisplay(DataResult<VirtualSystemOverview> dr, User user) {
        LOG.error("In VirtualSystemSetupAction.setStatusDisplay()");
        for (VirtualSystemOverview next : dr) {

            // If the system is not registered with RHN, we cannot show a status
            if (next.getSystemId() != null) {
                Long instanceId = next.getId();
                next.setId(next.getSystemId());
                SystemListHelper.setSystemStatusDisplay(user, next);
                next.setId(instanceId);
            }
        }
    }

    @Override
    public List<VirtualSystemOverview> getResult(RequestContext context) {
        User user = context.getCurrentUser();
        PageControl pc = new PageControl();
        pc.setIndexData(true);
        pc.setFilterColumn("name");
        pc.setFilter(true);
        TreeFilter filter = new TreeFilter();
        filter.setMatcher(new VirtualSystemsFilterMatcher());
        pc.setCustomFilter(filter);

        // if the lower/upper params don't exist, set to 1/user defined
        // respectively
        String lowBound = context.processPagination();

        int lower = StringUtil.smartStringToInt(lowBound, 1);
        if (lower <= 1) {
            lower = 1;
        }

        pc.setStart(lower);
        pc.setPageSize(user.getPageSize());
        String filterString =
                context.getRequest().getParameter(RequestContext.FILTER_STRING);
        if (!StringUtils.isBlank(filterString)) {
            createSuccessMessage(context.getRequest(), "filter.clearfilter",
                    context.getRequest().getRequestURI());
        }
        pc.setFilterData(filterString);

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        context.getRequest().setAttribute("set", set);

        DataResult<VirtualSystemOverview> dr = SystemManager.virtualSystemsList(user, pc);

        for (VirtualSystemOverview current : dr) {
            if (current.isFakeNode()) {
                continue;
            }
            else if (current.getUuid() == null && current.getHostSystemId() != null) {
                current.setSystemId(current.getHostSystemId());
            }
            else {
                current.setSystemId(current.getVirtualSystemId());
            }
        }

        //setStatusDisplay(dr, user);
        for (VirtualSystemOverview vso : dr) {
            LOG.error("RESULT: " + vso.getName() + "   " + vso.getId() + "  " +
                    vso.getServerName());
        }
        return dr;
    }

}
