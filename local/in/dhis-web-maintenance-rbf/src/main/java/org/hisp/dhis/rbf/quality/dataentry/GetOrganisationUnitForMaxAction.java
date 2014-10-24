package org.hisp.dhis.rbf.quality.dataentry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.rbf.api.Lookup;
import org.hisp.dhis.rbf.api.LookupService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class GetOrganisationUnitForMaxAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private LookupService lookupService;

    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    @Autowired
    private OrganisationUnitGroupService orgUnitGroupService;

    // -------------------------------------------------------------------------
    // Input/output
    // -------------------------------------------------------------------------
    private String message;

    public String getMessage()
    {
        return message;
    }

    private String orgUnitId;

    public String getOrgUnitId()
    {
        return orgUnitId;
    }

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private String orgUnitGroupId;

    public void setOrgUnitGroupId( String orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }

    private List<DataSet> dataSets = new ArrayList<DataSet>();

    public List<DataSet> getDataSets()
    {
        return dataSets;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitId );

        OrganisationUnitGroup orgUnitGroup = orgUnitGroupService.getOrganisationUnitGroup( Integer.parseInt( orgUnitGroupId ) );

        List<Lookup> lookups = new ArrayList<Lookup>( lookupService.getAllLookupsByType( Lookup.DS_QUALITY_TYPE ) );

        List<DataSet> pbfDataSets = new ArrayList<DataSet>();

        for ( Lookup lookup : lookups )
        {
            Integer dataSetId = Integer.parseInt( lookup.getValue() );

            DataSet dataSet = dataSetService.getDataSet( dataSetId );
            if ( dataSet != null )
            {
                pbfDataSets.add( dataSet );

            }
        }

        /*
        Set<OrganisationUnit> groupMember = new TreeSet<OrganisationUnit>( orgUnitGroup.getMembers() );
        
        Set<DataSet> tempDataSets = new TreeSet<DataSet>();
        
        for( OrganisationUnit orgUnit : groupMember )
        {
            tempDataSets.addAll( orgUnit.getDataSets() );
        }
        
        dataSets.addAll( tempDataSets );
        */
        
        dataSets.retainAll( dataSetService.getDataSetsBySources( orgUnitGroup.getMembers() ) );
        
        //dataSets.addAll( orgUnitGroup.getDataSets() );

        //System.out.println( "Before : " + dataSets.size() );

        dataSets.retainAll( pbfDataSets );

        Collections.sort( dataSets );

        //System.out.println( "After : " + dataSets.size() );

        if ( dataSets.size() > 0 )
        {
            message = organisationUnit.getName();

            return SUCCESS;
        }
        else
        {
            message = organisationUnit.getName();

            return INPUT;
        }

    }

}
