package org.hisp.dhis.rbf.report.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.period.CalendarPeriodType;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.system.filter.PastAndCurrentPeriodFilter;
import org.hisp.dhis.system.util.FilterUtils;

import com.opensymphony.xwork2.Action;

public class PBFInvoiceReportFormAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private List<Period> periods = new ArrayList<Period>();

    public Collection<Period> getPeriods()
    {
        return periods;
    }

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    private String periodTypeName;

    public String getPeriodTypeName()
    {
        return periodTypeName;
    }

    public void setPeriodTypeName( String periodTypeName )
    {
        this.periodTypeName = periodTypeName;
    }

    private String birtPath;
    
    public String getBirtPath()
    {
        return birtPath;
    }
    
    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {
        birtPath = System.getenv( "DHIS2_HOME" );
        
        birtPath += File.separator + "birtreports" + File.separator + "PBFInvoice.rptdesign";
        
        periodTypeName = QuarterlyPeriodType.NAME;

        CalendarPeriodType _periodType = (CalendarPeriodType) CalendarPeriodType.getPeriodTypeByName( periodTypeName );

        Calendar cal = PeriodType.createCalendarInstance();

        periods = _periodType.generatePeriods( cal.getTime() );

        // periods = new ArrayList<Period>(
        // periodService.getPeriodsByPeriodType( periodType ) );

        FilterUtils.filter( periods, new PastAndCurrentPeriodFilter() );

        Collections.reverse( periods );
        // Collections.sort( periods );
        for ( Period period : periods )
        {
            period.setName( format.formatPeriod( period ) );
            
        }

        return SUCCESS;
    }

}
