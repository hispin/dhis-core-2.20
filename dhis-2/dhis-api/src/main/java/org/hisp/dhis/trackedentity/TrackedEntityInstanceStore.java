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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public interface TrackedEntityInstanceStore
    extends GenericIdentifiableObjectStore<TrackedEntityInstance>
{
    final String ID = TrackedEntityInstanceStore.class.getName();

    final int MAX_RESULTS = 50000;

    List<Map<String, String>> getTrackedEntityInstances( TrackedEntityInstanceQueryParams params );

    int getTrackedEntityInstanceCount( TrackedEntityInstanceQueryParams params );

    /**
     * Search entityInstances who registered in a certain organisation unit
     * 
     * @param organisationUnit Organisation unit where entityInstances
     *        registered
     * @param min
     * @param max
     * 
     * @return List of entityInstances
     */
    Collection<TrackedEntityInstance> getByOrgUnit( OrganisationUnit organisationUnit, Integer min, Integer max );

    /**
     * Search instances who has the same representative
     * 
     * @param instances Representative
     * 
     * @return List of entityInstances
     */
    Collection<TrackedEntityInstance> getRepresentatives( TrackedEntityInstance instances );

    /**
     * Returns null if valid, a descriptive, non-null string if invalid.
     * 
     * @param instance
     * @param attributeValue
     * @param program
     * @return
     */
    String validate( TrackedEntityInstance instance, TrackedEntityAttributeValue attributeValue, Program program );
}
