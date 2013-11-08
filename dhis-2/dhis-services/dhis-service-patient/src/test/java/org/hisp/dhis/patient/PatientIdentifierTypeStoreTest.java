/*
 * Copyright (c) 2004-2013, University of Oslo
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

package org.hisp.dhis.patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.hisp.dhis.DhisSpringTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chau Thu Tran
 * 
 * @version $ PatientIdentifierTypeServiceTest.java Nov 5, 2013 3:11:48 PM $
 */
public class PatientIdentifierTypeStoreTest
    extends DhisSpringTest
{
    @Autowired
    private PatientIdentifierTypeStore identifierTypeStore;

    private PatientIdentifierType identifierTypeA;

    private PatientIdentifierType identifierTypeB;

    private PatientIdentifierType identifierTypeC;

    @Override
    public void setUpTest()
    {
        identifierTypeA = createPatientIdentifierType( 'A' );
        identifierTypeB = createPatientIdentifierType( 'B' );
        identifierTypeC = createPatientIdentifierType( 'C' );
    }

    @Test
    public void testGetPatientIdentifierTypesByMandatory()
    {
        identifierTypeA.setMandatory( true );
        identifierTypeB.setMandatory( true );

        identifierTypeStore.save( identifierTypeA );
        identifierTypeStore.save( identifierTypeB );
        identifierTypeStore.save( identifierTypeC );

        Collection<PatientIdentifierType> identifierTypes = identifierTypeStore.get( true );
        assertEquals( 2, identifierTypes.size() );
        assertTrue( equals( identifierTypes, identifierTypeA, identifierTypeB ) );

        identifierTypes = identifierTypeStore.get( false );
        assertEquals( 1, identifierTypes.size() );
        assertTrue( equals( identifierTypes, identifierTypeC ) );
    }

    @Test
    public void testGetDisplayedPatientIdentifierTypes()
    {
        identifierTypeA.setPersonDisplayName( true );
        identifierTypeB.setPersonDisplayName( true );

        identifierTypeStore.save( identifierTypeA );
        identifierTypeStore.save( identifierTypeB );
        identifierTypeStore.save( identifierTypeC );

        Collection<PatientIdentifierType> identifierTypes = identifierTypeStore.getByDisplayed( true );
        assertEquals( 2, identifierTypes.size() );
        assertTrue( equals( identifierTypes, identifierTypeA, identifierTypeB ) );

        identifierTypes = identifierTypeStore.getByDisplayed( false );
        assertEquals( 1, identifierTypes.size() );
        assertTrue( equals( identifierTypes, identifierTypeC ) );
    }
}
