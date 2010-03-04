package org.hisp.dhis.importexport.ixf;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.InputStream;

import org.hisp.dhis.DhisTest;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.ImportService;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.importexport.util.ImportExportUtils;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.junit.Test;

public class IXFImportServiceTest
    extends DhisTest
{
    private InputStream inputStreamA;

    private ImportService importService;
    
    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        inputStreamA = classLoader.getResourceAsStream( "ixfA.zip" );
        
        importService = (ImportService) getBean( "org.hisp.dhis.importexport.IXFImportService" );

        dataElementService = (DataElementService) getBean( DataElementService.ID );
        
        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );
        
        periodService = (PeriodService) getBean( PeriodService.ID );
        
        dataValueService = (DataValueService) getBean( DataValueService.ID );
    }

    @Override
    public void tearDownTest()
        throws Exception
    {
        inputStreamA.close();
    }

    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }
    
    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    public void testImport() throws Exception
    {
        ImportParams importParams = ImportExportUtils.getImportParams( ImportStrategy.NEW_AND_UPDATES, false, true, false );
        
        importService.importData( importParams, inputStreamA );
        
        assertEquals( 14, dataElementService.getAllDataElements().size() );
        assertEquals( 20, periodService.getPeriodsByPeriodType( new MonthlyPeriodType() ).size() );
        assertEquals( 3, organisationUnitService.getAllOrganisationUnits().size() );
        assertEquals( 280, dataValueService.getAllDataValues().size() );
        
        for ( DataElement element : dataElementService.getAllDataElements() )
        {
            assertNotNull( element.getName() );
            assertNotNull( element.getUuid() );
            assertNotNull( element.getShortName() );
            assertNotNull( element.getCategoryCombo() );
        }
        
        for ( Period period : periodService.getPeriodsByPeriodType( new MonthlyPeriodType() ) )
        {
            assertNotNull( period.getStartDate() );
            assertNotNull( period.getEndDate() );
            assertEquals( MonthlyPeriodType.class, period.getPeriodType().getClass() );
        }

        for ( OrganisationUnit unit : organisationUnitService.getAllOrganisationUnits() )
        {
            assertNotNull( unit.getName() );
            assertNotNull( unit.getUuid() );
            assertNotNull( unit.getShortName() );
        }
        
        for ( DataValue value : dataValueService.getAllDataValues() )
        {
            assertNotNull( value.getDataElement() );
            assertNotNull( value.getPeriod() );
            assertNotNull( value.getSource() );
            assertNotNull( value.getOptionCombo() );
            assertNotNull( value.getValue() );
        }
    }
}
