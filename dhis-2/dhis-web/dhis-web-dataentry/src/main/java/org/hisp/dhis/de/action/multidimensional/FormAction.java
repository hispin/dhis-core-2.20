package org.hisp.dhis.de.action.multidimensional;

/*
 * Copyright (c) 2004-2007, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static org.hisp.dhis.options.SystemSettingManager.KEY_ZERO_VALUE_SAVE_MODE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.customvalue.CustomValue;
import org.hisp.dhis.customvalue.CustomValueService;
import org.hisp.dhis.dataelement.CalculatedDataElement;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datalock.DataSetLock;
import org.hisp.dhis.datalock.DataSetLockService;
import org.hisp.dhis.dataset.DataEntryForm;
import org.hisp.dhis.dataset.DataEntryFormService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.de.comments.StandardCommentsManager;
import org.hisp.dhis.de.screen.DataEntryScreenManager;
import org.hisp.dhis.de.state.SelectedStateManager;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.minmax.MinMaxDataElement;
import org.hisp.dhis.minmax.MinMaxDataElementService;
import org.hisp.dhis.options.SystemSettingManager;
import org.hisp.dhis.options.displayproperty.DisplayPropertyHandler;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;

import com.opensymphony.xwork2.Action;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Abyot Asalefew
 * @version $Id$
 */
public class FormAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private CustomValueService customValueService;

    public CustomValueService getCustomValueService()
    {
        return customValueService;
    }

    public void setCustomValueService( CustomValueService customValueService )
    {
        this.customValueService = customValueService;
    }

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    private DataEntryFormService dataEntryFormService;

    public void setDataEntryFormService( DataEntryFormService dataEntryFormService )
    {
        this.dataEntryFormService = dataEntryFormService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private StandardCommentsManager standardCommentsManager;

    public void setStandardCommentsManager( StandardCommentsManager standardCommentsManager )
    {
        this.standardCommentsManager = standardCommentsManager;
    }

    private MinMaxDataElementService minMaxDataElementService;

    public void setMinMaxDataElementService( MinMaxDataElementService minMaxDataElementService )
    {
        this.minMaxDataElementService = minMaxDataElementService;
    }

    private SelectedStateManager selectedStateManager;

    public void setSelectedStateManager( SelectedStateManager selectedStateManager )
    {
        this.selectedStateManager = selectedStateManager;
    }

    private DataEntryScreenManager dataEntryScreenManager;

    public void setDataEntryScreenManager( DataEntryScreenManager dataEntryScreenManager )
    {
        this.dataEntryScreenManager = dataEntryScreenManager;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }
    
    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    private DataSetLockService dataSetLockService;

    public void setDataSetLockService( DataSetLockService dataSetLockService )
    {
        this.dataSetLockService = dataSetLockService;
    }

    // -------------------------------------------------------------------------
    // Comparator
    // -------------------------------------------------------------------------

    private Comparator<DataElement> dataElementComparator;

    public void setDataElementComparator( Comparator<DataElement> dataElementComparator )
    {
        this.dataElementComparator = dataElementComparator;
    }

    // -------------------------------------------------------------------------
    // DisplayPropertyHandler
    // -------------------------------------------------------------------------

    private DisplayPropertyHandler displayPropertyHandler;

    public void setDisplayPropertyHandler( DisplayPropertyHandler displayPropertyHandler )
    {
        this.displayPropertyHandler = displayPropertyHandler;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private List<CustomValue> customValues = new ArrayList<CustomValue>();

    public List<CustomValue> getCustomValues()
    {
        return customValues;
    }

    private Map<DataElementCategoryCombo, Collection<DataElement>> orderedDataElements = new HashMap<DataElementCategoryCombo, Collection<DataElement>>();

    public Map<DataElementCategoryCombo, Collection<DataElement>> getOrderedDataElements()
    {
        return orderedDataElements;
    }

    private Map<String, DataValue> dataValueMap;

    public Map<String, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }

    private Map<CalculatedDataElement, Integer> calculatedValueMap;

    public Map<CalculatedDataElement, Integer> getCalculatedValueMap()
    {
        return calculatedValueMap;
    }

    private List<String> standardComments;

    public List<String> getStandardComments()
    {
        return standardComments;
    }

    private Map<String, String> dataElementValueTypeMap;

    public Map<String, String> getDataElementValueTypeMap()
    {
        return dataElementValueTypeMap;
    }

    private Map<String, MinMaxDataElement> minMaxMap;

    public Map<String, MinMaxDataElement> getMinMaxMap()
    {
        return minMaxMap;
    }

    private Integer integer = 0;

    public Integer getInteger()
    {
        return integer;
    }

    private Map<Integer, Map<Integer, Collection<DataElementCategoryOption>>> orderedOptionsMap = new HashMap<Integer, Map<Integer, Collection<DataElementCategoryOption>>>();

    public Map<Integer, Map<Integer, Collection<DataElementCategoryOption>>> getOrderedOptionsMap()
    {
        return orderedOptionsMap;
    }

    private Map<Integer, Collection<DataElementCategory>> orderedCategories = new HashMap<Integer, Collection<DataElementCategory>>();

    public Map<Integer, Collection<DataElementCategory>> getOrderedCategories()
    {
        return orderedCategories;
    }

    private Map<Integer, Integer> numberOfTotalColumns = new HashMap<Integer, Integer>();

    public Map<Integer, Integer> getNumberOfTotalColumns()
    {
        return numberOfTotalColumns;
    }

    private Map<Integer, Map<Integer, Collection<Integer>>> catColRepeat = new HashMap<Integer, Map<Integer, Collection<Integer>>>();

    public Map<Integer, Map<Integer, Collection<Integer>>> getCatColRepeat()
    {
        return catColRepeat;
    }

    private Map<Integer, Collection<DataElementCategoryOptionCombo>> orderdCategoryOptionCombos = new HashMap<Integer, Collection<DataElementCategoryOptionCombo>>();

    public Map<Integer, Collection<DataElementCategoryOptionCombo>> getOrderdCategoryOptionCombos()
    {
        return orderdCategoryOptionCombos;
    }

    Collection<DataElementCategoryOptionCombo> allOptionCombos = new ArrayList<DataElementCategoryOptionCombo>();

    public Collection<DataElementCategoryOptionCombo> getAllOptionCombos()
    {
        return allOptionCombos;
    }

    private List<DataElementCategoryCombo> orderedCategoryCombos = new ArrayList<DataElementCategoryCombo>();

    public List<DataElementCategoryCombo> getOrderedCategoryCombos()
    {
        return orderedCategoryCombos;
    }

    private Boolean cdeFormExists;

    public Boolean getCdeFormExists()
    {
        return cdeFormExists;
    }

    private DataEntryForm dataEntryForm;

    public DataEntryForm getDataEntryForm()
    {
        return this.dataEntryForm;
    }

    private String customDataEntryFormCode;

    public String getCustomDataEntryFormCode()
    {
        return this.customDataEntryFormCode;
    }

    private Boolean zeroValueSaveMode;

    public Boolean getZeroValueSaveMode()
    {
        return zeroValueSaveMode;
    }

    private Boolean disableDefaultForm;

    public Boolean getDisableDefaultForm()
    {
        return disableDefaultForm;
    }

    // -------------------------------------------------------------------------
    // Input/output
    // -------------------------------------------------------------------------

    private Integer selectedDataSetId;

    public void setSelectedDataSetId( Integer selectedDataSetId )
    {
        this.selectedDataSetId = selectedDataSetId;
    }

    public Integer getSelectedDataSetId()
    {
        return selectedDataSetId;
    }

    private Integer selectedPeriodIndex;

    public void setSelectedPeriodIndex( Integer selectedPeriodIndex )
    {
        this.selectedPeriodIndex = selectedPeriodIndex;
    }

    public Integer getSelectedPeriodIndex()
    {
        return selectedPeriodIndex;
    }

    private String disabled = " ";

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        disableDefaultForm = false;

        zeroValueSaveMode = (Boolean) systemSettingManager.getSystemSetting( KEY_ZERO_VALUE_SAVE_MODE, false );

        OrganisationUnit organisationUnit = selectedStateManager.getSelectedOrganisationUnit();

        DataSet dataSet = selectedStateManager.getSelectedDataSet();

        customValues = (List<CustomValue>) customValueService.getCustomValuesByDataSet( dataSet );

        Period period = selectedStateManager.getSelectedPeriod();

        DataSetLock dataSetLock = dataSetLockService.getDataSetLockByDataSetAndPeriod( dataSet, period );

        if ( dataSetLock != null && dataSetLock.getSources().contains( organisationUnit ) )
        {
            disabled = "disabled";
        }       
        
        List<DataElement> dataElements = new ArrayList<DataElement>( dataSet.getDataElements() );

        if ( dataElements.size() == 0 )
        {
            return SUCCESS;
        }      
        
        Collections.sort( dataElements, dataElementComparator );       
        
        orderedDataElements = dataElementService.getGroupedDataElementsByCategoryCombo( dataElements );
        
        orderedCategoryCombos = dataElementService.getDataElementCategoryCombos( dataElements );        

        for ( DataElementCategoryCombo categoryCombo : orderedCategoryCombos )
        {            
            Collection<DataElementCategoryOptionCombo> optionCombos = categoryService.sortOptionCombos( categoryCombo );

            allOptionCombos.addAll( optionCombos );

            orderdCategoryOptionCombos.put( categoryCombo.getId(), optionCombos );

            // ---------------------------------------------------------------------
            // Perform ordering of categories and their options so that they
            // could
            // be displayed as in the paper form. Note that the total number of
            // entry cells to be generated are the multiple of options from each
            // category.
            // ---------------------------------------------------------------------

            numberOfTotalColumns.put( categoryCombo.getId(), optionCombos.size() );

            orderedCategories.put( categoryCombo.getId(), categoryCombo.getCategories() );

            Map<Integer, Collection<DataElementCategoryOption>> optionsMap = new HashMap<Integer, Collection<DataElementCategoryOption>>();

            for ( DataElementCategory dec : categoryCombo.getCategories() )
            {
                optionsMap.put( dec.getId(), dec.getCategoryOptions() );
            }

            orderedOptionsMap.put( categoryCombo.getId(), optionsMap );

            // ---------------------------------------------------------------------
            // Calculating the number of times each category should be repeated
            // ---------------------------------------------------------------------

            int catColSpan = optionCombos.size();

            Map<Integer, Integer> catRepeat = new HashMap<Integer, Integer>();

            Map<Integer, Collection<Integer>> colRepeat = new HashMap<Integer, Collection<Integer>>();

            for ( DataElementCategory cat : categoryCombo.getCategories() )
            {
                catColSpan = catColSpan / cat.getCategoryOptions().size();
                int total = optionCombos.size() / (catColSpan * cat.getCategoryOptions().size());
                Collection<Integer> cols = new ArrayList<Integer>( total );

                for ( int i = 0; i < total; i++ )
                {
                    cols.add( i );
                }

                /*
                 * TODO Cols are made to be a collection simply to facilitate a
                 * for loop in the velocity template - there should be a better
                 * way of "for" doing a loop.
                 */

                colRepeat.put( cat.getId(), cols );

                catRepeat.put( cat.getId(), catColSpan );
            }

            catColRepeat.put( categoryCombo.getId(), colRepeat );
        }

        // ---------------------------------------------------------------------
        // Get the min/max values
        // ---------------------------------------------------------------------

        Collection<MinMaxDataElement> minMaxDataElements = minMaxDataElementService.getMinMaxDataElements(
            organisationUnit, dataElements );

        minMaxMap = new HashMap<String, MinMaxDataElement>( minMaxDataElements.size() );

        for ( MinMaxDataElement minMaxDataElement : minMaxDataElements )
        {
            minMaxMap.put( minMaxDataElement.getDataElement().getId() + ":"
                + minMaxDataElement.getOptionCombo().getId(), minMaxDataElement );
        }

        // ---------------------------------------------------------------------
        // Get the DataValues and create a map
        // ---------------------------------------------------------------------

        Collection<DataValue> dataValues = dataValueService.getDataValues( organisationUnit, period, dataElements,
            allOptionCombos );

        dataValueMap = new HashMap<String, DataValue>( dataValues.size() );

        for ( DataValue dataValue : dataValues )
        {
            Integer deId = dataValue.getDataElement().getId();
            Integer ocId = dataValue.getOptionCombo().getId();

            dataValueMap.put( deId.toString() + ':' + ocId.toString(), dataValue );
        }

        // ---------------------------------------------------------------------
        // Prepare values for unsaved CalculatedDataElements
        // ---------------------------------------------------------------------

        calculatedValueMap = dataEntryScreenManager.populateValuesForCalculatedDataElements( organisationUnit, dataSet,
            period );

        // ---------------------------------------------------------------------
        // Make the standard comments available
        // ---------------------------------------------------------------------

        standardComments = standardCommentsManager.getStandardComments();

        // ---------------------------------------------------------------------
        // Make the DataElement types available
        // ---------------------------------------------------------------------

        dataElementValueTypeMap = new HashMap<String, String>();
        dataElementValueTypeMap.put( DataElement.VALUE_TYPE_DATE, i18n.getString( "date" ) );
        dataElementValueTypeMap.put( DataElement.VALUE_TYPE_BOOL, i18n.getString( "yes_no" ) );
        dataElementValueTypeMap.put( DataElement.VALUE_TYPE_INT, i18n.getString( "number" ) );
        dataElementValueTypeMap.put( DataElement.VALUE_TYPE_STRING, i18n.getString( "text" ) );

        // ---------------------------------------------------------------------
        // Get the custom data entry form (if any)
        // ---------------------------------------------------------------------

        dataEntryForm = dataEntryFormService.getDataEntryFormByDataSet( dataSet );

        cdeFormExists = (dataEntryForm != null);

        if ( cdeFormExists )
        {
            customDataEntryFormCode = dataEntryScreenManager.populateCustomDataEntryScreenForMultiDimensional(
                dataEntryForm.getHtmlCode(), dataValues, calculatedValueMap, minMaxMap, disabled, zeroValueSaveMode,
                i18n, dataSet );
        }

        // ---------------------------------------------------------------------
        // Working on the display of dataelements
        // ---------------------------------------------------------------------

        List<DataElement> des = new ArrayList<DataElement>();

        for ( DataElementCategoryCombo categoryCombo : orderedCategoryCombos )
        {
            des = (List<DataElement>) orderedDataElements.get( categoryCombo );
          
            displayPropertyHandler.handle( des );

            orderedDataElements.put( categoryCombo, des );
        }

        return SUCCESS;
    }
}
