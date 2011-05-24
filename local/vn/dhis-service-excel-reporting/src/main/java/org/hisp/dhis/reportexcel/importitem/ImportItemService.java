package org.hisp.dhis.reportexcel.importitem;

import java.util.Collection;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.reportexcel.DataElementGroupOrder;

public interface ImportItemService
{
    String ID = ImportItemService.class.getName();

    // -------------------------------------------------------------------------
    // Import Report services
    // -------------------------------------------------------------------------

    public int addImportReport( ExcelItemGroup importReport );

    public void updateImportReport( ExcelItemGroup importReport );

    public void deleteImportReport( int id );

    public Collection<ExcelItemGroup> getAllImportReport();

    public ExcelItemGroup getImportReport( int id );
    
    public ExcelItemGroup getImportReport( String name );

    public Collection<ExcelItemGroup> getImportReports( OrganisationUnit organisationUnit );

    // -------------------------------------------------------------------------
    // Import item services
    // -------------------------------------------------------------------------

    public int addImportItem( ExcelItem importItem );

    public void updateImportItem( ExcelItem importItem );

    public void deleteImportItem( int id );

    public Collection<ExcelItem> getAllImportItem();

    public ExcelItem getImportItem( int id );
    
    public ExcelItem getImportItem( String name );

    // -------------------------------------------------------------------------
    // DataElement Order
    // -------------------------------------------------------------------------

    public DataElementGroupOrder getDataElementGroupOrder( Integer id );

    public void updateDataElementGroupOrder( DataElementGroupOrder dataElementGroupOrder );

    public void deleteDataElementGroupOrder( Integer id );

}
