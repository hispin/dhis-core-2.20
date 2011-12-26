package org.hisp.dhis.tallysheet;

/*
 * Copyright (c) 2004-2012, University of Oslo
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

import java.util.List;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;

/**
 * @author Haavard Tegelsrud, Oddmund Stroemme, Joergen Froeysadal, Ruben Wangberg
 * @version $Id$
 */
public interface TallySheetService
{
    final String ID = TallySheetService.class.getName();
    
    /**
     * Creates a tally sheet by passing information about organisation unit,
     * data set, elements in the data set, formating of the printable tally
     * sheet, whether to display the name of the organisation unit and the name
     * of the tally sheet.
     * 
     * @param organisationUnit the organisation unit
     * @param dataElements the data elements
     * @param a3Format the format of the printable tally sheet
     * @param displayFacilityName whether or not to display the name of the
     *        organisation unit
     * @param selectedDataSet the selected data set
     * @param tallySheetName the name of the tally sheet
     */
    TallySheet createTallySheet( OrganisationUnit organisationUnit, List<DataElement> dataElements, boolean a3Format,
        boolean displayFacilityName, DataSet selectedDataSet, String tallySheetName );
}
