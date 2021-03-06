package org.hisp.dhis.importexport.dxf.converter;

/*
 * Copyright (c) 2004-2005, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the <ORGANIZATION> nor the names of its contributors may
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

/**
 *
 * @author bobj
 * @version created 15-Sep-2010
 */
import java.util.Collection;
import java.util.Map;

import org.amplecode.quick.BatchHandler;
import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.hr.InputType;
import org.hisp.dhis.hr.InputTypeService;
import org.hisp.dhis.importexport.HrExportParams;
import org.hisp.dhis.importexport.ImportHrObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLHrConverter;
import org.hisp.dhis.importexport.importer.InputTypeImporter;

/**
 * @author John Francis Mukulu <john.f.mukulu@gmail.com>
 * @version $Id$
 */
public class InputTypeConverter
    extends InputTypeImporter implements XMLHrConverter
{
    public static final String COLLECTION_NAME = "inputTypes";

    public static final String ELEMENT_NAME = "inputType";

    private static final String FIELD_ID = "inputTypeId";

    private static final String FIELD_NAME = "inputTypeName";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    /**
     * Constructor for write operations.
     * @param inputTypeService
     */
    public InputTypeConverter( InputTypeService inputTypeService )
    {
        this.inputTypeService = inputTypeService;
    }

    /**
     * Constructor for read operations.
     *
     * @param batchHandler the batchHandler to use.
     * @param importObjectService the importObjectService to use.
     * @param inputTypeService the inputTypeServiceService to use.
     */
    public InputTypeConverter( BatchHandler<InputType> batchHandler,
        ImportHrObjectService importObjectService,
        InputTypeService inputTypeService )
    {
        this.batchHandler = batchHandler;
        this.importObjectService = importObjectService;
        this.inputTypeService = inputTypeService;
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------
    
    public void write( XMLWriter writer, HrExportParams params )
    {
        Collection<InputType> inputTypes = inputTypeService.getAllInputType();

        if ( inputTypes != null && inputTypes.size() > 0 )
        {
            writer.openElement( COLLECTION_NAME );

            for ( InputType inputType : inputTypes )
            {
                writer.openElement( ELEMENT_NAME );

                writer.writeElement( FIELD_ID, String.valueOf( inputType.getId() ) );
                writer.writeElement( FIELD_NAME, inputType.getName() );

                writer.closeElement();
            }

            writer.closeElement();
        }
    }

    public void read( XMLReader reader, ImportParams params )
    {
        while ( reader.moveToStartElement( ELEMENT_NAME, COLLECTION_NAME ) )
        {
            final Map<String, String> values = reader.readElements( ELEMENT_NAME );

            final InputType inputType = new InputType();

            inputType.setId( Integer.parseInt( values.get( FIELD_ID ) ) );
            inputType.setName( values.get( FIELD_NAME ) );

            importObject( inputType, params );
        }
    }
}
