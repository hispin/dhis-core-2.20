/*
 * Copyright (c) 2004-2009, University of Oslo
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

package org.hisp.dhis.caseentry.action.visitplan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.nextvisit.NextVisitGenerator;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class VisitPlanAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitSelectionManager selectionManager;

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    private PatientService patientService;

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    private ProgramService programService;

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    private ProgramInstanceService programInstanceService;

    public void setProgramInstanceService( ProgramInstanceService programInstanceService )
    {
        this.programInstanceService = programInstanceService;
    }

    private PatientAttributeValueService patientAttributeValueService;

    public void setPatientAttributeValueService( PatientAttributeValueService patientAttributeValueService )
    {
        this.patientAttributeValueService = patientAttributeValueService;
    }

    private PatientAttributeService patientAttributeService;

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    private NextVisitGenerator nextVisitGenerator;

    public void setNextVisitGenerator( NextVisitGenerator nextVisitGenerator )
    {
        this.nextVisitGenerator = nextVisitGenerator;
    }

    // -------------------------------------------------------------------------
    // Input/output
    // -------------------------------------------------------------------------

    private Integer sortingAttributeId;

    public void setSortingAttributeId( Integer sortingAttributeId )
    {
        this.sortingAttributeId = sortingAttributeId;
    }

    public Integer getSortingAttributeId()
    {
        return sortingAttributeId;
    }

    private PatientAttribute sortingAttribute;

    public PatientAttribute getSortingAttribute()
    {
        return sortingAttribute;
    }

    private Collection<PatientAttribute> attributes;

    public Collection<PatientAttribute> getAttributes()
    {
        return attributes;
    }

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private Map<Patient, Set<ProgramStageInstance>> visitsByPatients = new HashMap<Patient, Set<ProgramStageInstance>>();

    public Map<Patient, Set<ProgramStageInstance>> getVisitsByPatients()
    {
        return visitsByPatients;
    }

    private Map<Integer, Collection<PatientAttributeValue>> attributeValueMap = new HashMap<Integer, Collection<PatientAttributeValue>>();

    public Map<Integer, Collection<PatientAttributeValue>> getAttributeValueMap()
    {
        return attributeValueMap;
    }

    private Collection<Patient> sortedPatients = new ArrayList<Patient>();

    public Collection<Patient> getSortedPatients()
    {
        return sortedPatients;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {

        // ---------------------------------------------------------------------
        // Make attributes available so that users can sort based on attributes
        // ---------------------------------------------------------------------

        attributes = patientAttributeService.getAllPatientAttributes();

        // ---------------------------------------------------------------------
        // Get the facility planning to do a visit
        // ---------------------------------------------------------------------

        organisationUnit = selectionManager.getSelectedOrganisationUnit();

        // ---------------------------------------------------------------------
        // Get all the programs the facility is providing
        // ---------------------------------------------------------------------

        Collection<Program> programs = programService.getPrograms( organisationUnit );
        

        if ( programs.size() > 0 )
        {

            // -----------------------------------------------------------------
            // For all the programs a facility is servicing get the active
            // instances completed = false
            // -----------------------------------------------------------------

            Collection<ProgramInstance> programInstances = programInstanceService.getProgramInstances( programs, false );

            // -----------------------------------------------------------------
            // For all the active program instances determine the next visits
            // and group these visits based on the patient to be visited
            // -----------------------------------------------------------------

            visitsByPatients = nextVisitGenerator.getNextVisits( programInstances );

            if ( !visitsByPatients.keySet().isEmpty() )
            {
                Collection<Patient> patientsToBeVisted = visitsByPatients.keySet();

                // -------------------------------------------------------------
                // Get all the attributes of the patients to be visited (in case
                // users want to make sorting based on attributes
                // -------------------------------------------------------------

                attributeValueMap = patientAttributeValueService
                    .getPatientAttributeValueMapForPatients( patientsToBeVisted );

                // -------------------------------------------------------------
                // Sort patients to be visited based on the chosen attribute
                // -------------------------------------------------------------

                if ( attributes.size() > 0 )
                {
                    sortingAttribute = attributes.iterator().next();
                }

                if ( sortingAttributeId != null )
                {
                    sortingAttribute = patientAttributeService.getPatientAttribute( sortingAttributeId );
                }

                if ( sortingAttribute != null )
                {
                    sortedPatients = patientService.sortPatientsByAttribute( patientsToBeVisted, sortingAttribute );
                }
                else
                {
                    sortedPatients = patientsToBeVisted;
                }
            }
        }

        return SUCCESS;
    }
}
