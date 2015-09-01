package org.hisp.dhis.dxf2.sm.api;

import org.hisp.dhis.dataelement.DataElement;

import java.util.Collection;

/**
 * @author BHARATH
 */
public interface DataElementSynchStatusService
{
    String ID = DataElementSynchStatusService.class.getName();
    
    void addDataElementSynchStatus( DataElementSynchStatus dataElementSynchStatus );
    
    void updateDataElementSynchStatus( DataElementSynchStatus dataElementSynchStatus );
    
    void deleteDataElementSynchStatus( DataElementSynchStatus dataElementSynchStatus );
    
    DataElementSynchStatus getStatusByInstanceAndDataElement( SynchInstance instance, DataElement dataElement );
    
    Collection<DataElementSynchStatus> getStatusByInstance( SynchInstance instance );
    
    Collection<DataElement> getUpdatedDataElements();
    
    Collection<DataElementSynchStatus> getUpdatedDataElementSyncStatus();

    Collection<DataElement> getNewDataElements();
    
    Collection<DataElement> getApprovedDataElements();
    
    Collection<DataElementSynchStatus> getSynchStausByDataElements(Collection<DataElement> dataElements);
    
    Collection<DataElementSynchStatus> getAllDataElementSynchStatus();
    
    Collection<DataElementSynchStatus> getStatusByDataElement( DataElement dataElement );
    
    Collection<DataElementSynchStatus> getPendingDataElementSyncStatus( SynchInstance instance );
    
    Collection<DataElement> getDataElementListByInstance( SynchInstance instance );
    
    Collection<DataElement> getApprovedDataElementListByInstance( SynchInstance instance );
}
