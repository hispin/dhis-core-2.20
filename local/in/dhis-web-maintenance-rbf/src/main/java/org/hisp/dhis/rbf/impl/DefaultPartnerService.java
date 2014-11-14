package org.hisp.dhis.rbf.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.rbf.api.Partner;
import org.hisp.dhis.rbf.api.PartnerService;
import org.hisp.dhis.rbf.api.PartnerStore;

/**
 * @author Mithilesh Kumar Thakur
 */
public class DefaultPartnerService implements PartnerService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PartnerStore partnerStore;

    public void setPartnerStore( PartnerStore partnerStore )
    {
        this.partnerStore = partnerStore;
    }


    // -------------------------------------------------------------------------
    // Partner
    // -------------------------------------------------------------------------

    @Override
    public void addPartner( Partner partner )
    {
        partnerStore.addPartner( partner );;
    }

    @Override
    public void updatePartner( Partner partner )
    {
        partnerStore.updatePartner( partner );
    }

    @Override
    public void deletePartner( Partner partner )
    {
        partnerStore.deletePartner( partner );
    }
    
    @Override
    public Partner getPartner( OrganisationUnit organisationUnit, DataSet dataSet, DataElement dataElement, Date startDate, Date endDate )
    {
        return partnerStore.getPartner( organisationUnit, dataSet, dataElement, startDate, endDate );
    }    
    
    @Override
    public Collection<Partner> getAllPartner()
    {
        return partnerStore.getAllPartner();
    }

    @Override
    public Collection<Partner> getPartner( OrganisationUnit organisationUnit, DataSet dataSet )
    {
        return partnerStore.getPartner( organisationUnit, dataSet );
    }

    @Override
    public Collection<Partner> getPartner( OrganisationUnit organisationUnit, DataElement dataElement )
    {
        return partnerStore.getPartner( organisationUnit, dataElement );
    }    
    
    public Map<String, Integer> getOrgUnitCountFromPartner( Integer organisationUnitId, Integer dataSetId, Integer dataElementId, Integer optionId, String startDate, String endDate )
    {
        return partnerStore.getOrgUnitCountFromPartner( organisationUnitId, dataSetId, dataElementId, optionId, startDate, endDate );
    }    
    
}
