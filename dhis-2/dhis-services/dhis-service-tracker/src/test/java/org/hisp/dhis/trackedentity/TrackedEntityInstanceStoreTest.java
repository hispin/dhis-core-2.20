package org.hisp.dhis.trackedentity;

/*
 * Copyright (c) 2004-2014, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.validation.ValidationCriteriaService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lars Helge Overland
 */
public class TrackedEntityInstanceStoreTest
    extends DhisSpringTest
{
    @Autowired
    private TrackedEntityInstanceStore entityInstanceStore;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private TrackedEntityAttributeService attributeService;

    @Autowired
    private TrackedEntityAttributeValueService attributeValueService;

    @Autowired
    private ValidationCriteriaService validationCriteriaService;

    private TrackedEntityInstance entityInstanceA1;

    private TrackedEntityInstance entityInstanceA2;

    private TrackedEntityInstance entityInstanceB1;

    private OrganisationUnit organisationUnit;
    
    @Override
    public void setUpTest()
    {
        organisationUnit = createOrganisationUnit( 'A' );
        organisationUnitService.addOrganisationUnit( organisationUnit );

        OrganisationUnit organisationUnitB = createOrganisationUnit( 'B' );
        organisationUnitService.addOrganisationUnit( organisationUnitB );

        TrackedEntityAttribute entityInstanceAttributeB = createTrackedEntityAttribute( 'B' );
        entityInstanceAttributeB.setUnique( true );
        attributeService.addTrackedEntityAttribute( entityInstanceAttributeB );
      
        entityInstanceA1 = createTrackedEntityInstance( 'A', organisationUnit );
        entityInstanceA2 = createTrackedEntityInstance( 'A', organisationUnitB );
        entityInstanceB1 = createTrackedEntityInstance( 'B', organisationUnit );
    }

    @Test
    public void testAddGet()
    {
        int idA = entityInstanceStore.save( entityInstanceA1 );
        int idB = entityInstanceStore.save( entityInstanceB1 );

        assertNotNull( entityInstanceStore.get( idA ) );
        assertNotNull( entityInstanceStore.get( idB ) );
    }

    @Test
    public void testAddGetbyOu()
    {
        int idA = entityInstanceStore.save( entityInstanceA1 );
        int idB = entityInstanceStore.save( entityInstanceB1 );

        assertEquals( entityInstanceA1.getName(), entityInstanceStore.get( idA ).getName() );
        assertEquals( entityInstanceB1.getName(), entityInstanceStore.get( idB ).getName() );
    }

    @Test
    public void testDelete()
    {
        int idA = entityInstanceStore.save( entityInstanceA1 );
        int idB = entityInstanceStore.save( entityInstanceB1 );

        assertNotNull( entityInstanceStore.get( idA ) );
        assertNotNull( entityInstanceStore.get( idB ) );

        entityInstanceStore.delete( entityInstanceA1 );

        assertNull( entityInstanceStore.get( idA ) );
        assertNotNull( entityInstanceStore.get( idB ) );

        entityInstanceStore.delete( entityInstanceB1 );

        assertNull( entityInstanceStore.get( idA ) );
        assertNull( entityInstanceStore.get( idB ) );
    }

    @Test
    public void testGetAll()
    {
        entityInstanceStore.save( entityInstanceA1 );
        entityInstanceStore.save( entityInstanceB1 );

        assertTrue( equals( entityInstanceStore.getAll(), entityInstanceA1, entityInstanceB1 ) );
    }

    @Test
    public void testGetRepresentatives()
    {
        entityInstanceStore.save( entityInstanceB1 );

        entityInstanceA1.setRepresentative( entityInstanceB1 );
        entityInstanceA2.setRepresentative( entityInstanceB1 );
        entityInstanceStore.save( entityInstanceA1 );
        entityInstanceStore.save( entityInstanceA2 );

        assertEquals( 2, entityInstanceStore.getRepresentatives( entityInstanceB1 ).size() );
    }
}
