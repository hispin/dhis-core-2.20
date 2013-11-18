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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;
import org.hisp.dhis.validation.ValidationCriteria;
import org.hisp.dhis.validation.ValidationCriteriaService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chau Thu Tran
 * 
 * @version $ PatientServiceTest.java Nov 5, 2013 10:35:31 AM $
 */
public class PatientServiceTest
    extends DhisSpringTest
{
    @Autowired
    private PatientService patientService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private PatientAttributeService patientAttributeService;

    @Autowired
    private PatientIdentifierTypeService identifierTypeService;

    @Autowired
    private PatientAttributeValueService patientAttributeValueService;

    @Autowired
    private ValidationCriteriaService validationCriteriaService;

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    private Patient patientA1;

    private Patient patientA2;

    private Patient patientA3;

    private Patient patientB1;

    private Patient patientB2;

    private PatientAttribute patientAttribute;

    private int attributeId;

    private Program programA;

    private Program programB;

    private OrganisationUnit organisationUnit;

    private Date date = new Date();

    @Override
    public void setUpTest()
    {
        organisationUnit = createOrganisationUnit( 'A' );
        organisationUnitService.addOrganisationUnit( organisationUnit );
        
        OrganisationUnit organisationUnitB = createOrganisationUnit( 'B' );
        organisationUnitService.addOrganisationUnit( organisationUnitB );

        PatientIdentifierType patientIdentifierType = createPatientIdentifierType( 'A' );
        identifierTypeService.savePatientIdentifierType( patientIdentifierType );

        patientAttribute = createPatientAttribute( 'A' );
        attributeId = patientAttributeService.savePatientAttribute( patientAttribute );

        patientA1 = createPatient( 'A', "F", organisationUnit );
        patientA2 = createPatient( 'A', "F", organisationUnitB );
        patientA3 = createPatient( 'A', organisationUnit, patientIdentifierType );
        patientB1 = createPatient( 'B', "M", organisationUnit );
        patientB2 = createPatient( 'B', organisationUnit );

        programA = createProgram( 'A', new HashSet<ProgramStage>(), organisationUnit );
        programB = createProgram( 'B', new HashSet<ProgramStage>(), organisationUnit );
    }

    @Test
    public void testSavePatient()
    {
        int idA = patientService.savePatient( patientA1 );
        int idB = patientService.savePatient( patientB1 );

        assertNotNull( patientService.getPatient( idA ) );
        assertNotNull( patientService.getPatient( idB ) );
    }

    @Test
    public void testDeletePatient()
    {
        int idA = patientService.savePatient( patientA1 );
        int idB = patientService.savePatient( patientB1 );

        assertNotNull( patientService.getPatient( idA ) );
        assertNotNull( patientService.getPatient( idB ) );

        patientService.deletePatient( patientA1 );

        assertNull( patientService.getPatient( idA ) );
        assertNotNull( patientService.getPatient( idB ) );

        patientService.deletePatient( patientB1 );

        assertNull( patientService.getPatient( idA ) );
        assertNull( patientService.getPatient( idB ) );
    }

    @Test
    public void testUpdatePatient()
    {
        int idA = patientService.savePatient( patientA1 );

        assertNotNull( patientService.getPatient( idA ) );

        patientA1.setName( "B" );
        patientService.updatePatient( patientA1 );

        assertEquals( "B", patientService.getPatient( idA ).getName() );
    }

    @Test
    public void testGetPatientById()
    {
        int idA = patientService.savePatient( patientA1 );
        int idB = patientService.savePatient( patientB1 );

        assertEquals( patientA1, patientService.getPatient( idA ) );
        assertEquals( patientB1, patientService.getPatient( idB ) );
    }

    @Test
    public void testGetPatientByUid()
    {
        patientA1.setUid( "A1" );
        patientB1.setUid( "B1" );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientB1 );

        assertEquals( patientA1, patientService.getPatient( "A1" ) );
        assertEquals( patientB1, patientService.getPatient( "B1" ) );
    }

    @Test
    public void testGetAllPatients()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientB1 );

        assertTrue( equals( patientService.getAllPatients(), patientA1, patientB1 ) );
    }

    @Test
    public void testGetByNameGenderBirthdate()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );

        Collection<Patient> patients = patientService.getPatients( "NameA", patientA1.getBirthDate(),
            patientA1.getGender() );

        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientA2 ) );
    }

    @Test
    public void testGetPatientsByNames()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );

        Collection<Patient> patients = patientService.getPatientsByNames( "NameA", null, null );

        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientA2 ) );
    }

    @Test
    public void testSearchByLikeNames()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        Collection<Patient> patients = patientService.getPatients( "B", 0, 10 );

        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientB1 ) );
        assertTrue( patients.contains( patientB2 ) );
    }

    @Test
    public void testGetPatientsByOu()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );

        Collection<Patient> patients = patientService.getPatients( organisationUnit, null, null );
        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientA3 ) );
    }

    @Test
    public void testGetPatientsByProgram()
    {
        programService.addProgram( programA );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );

        programInstanceService.enrollPatient( patientA1, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientA3, programA, date, date, organisationUnit, null );

        Collection<Patient> patients = patientService.getPatients( programA );
        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientA3 ) );
    }

    @Test
    public void testGetPatientsbyOuProgram()
    {
        programService.addProgram( programA );
        
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );

        programInstanceService.enrollPatient( patientA1, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientA2, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientA3, programA, date, date, organisationUnit, null );

        Collection<Patient> patients = patientService.getPatients( organisationUnit, programA );
        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientA3 ) );
    }

    @Test
    public void testGetPatientsLikeName()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );

        Collection<Patient> patients = patientService.getPatientsLikeName( organisationUnit, "A", null, null );
        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientA3 ) );
    }

    @Test
    public void testGetPatientsByAttribute()
    {
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        PatientAttributeValue attributeValue = createPatientAttributeValue( 'A', patientA3, patientAttribute );
        Set<PatientAttributeValue> patientAttributeValues = new HashSet<PatientAttributeValue>();
        patientAttributeValues.add( attributeValue );

        patientService.createPatient( patientA3, null, null, patientAttributeValues );
        
        Collection<Patient> patients = patientService.getPatient( null, attributeId, "AttributeA" );

        assertEquals( 1, patients.size() );
        assertTrue( patients.contains( patientA3 ) );
        
        Patient patient = patients.iterator().next();
        assertEquals( 1, patient.getAttributeValues().size() );
        assertTrue( patient.getAttributeValues().contains( attributeValue ) );
    }

    @Test
    public void testGetPatientsByProgramOu()
    {
        programService.addProgram( programA );
        programService.addProgram( programB );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientB2 );

        programInstanceService.enrollPatient( patientA1, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientB1, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientA2, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientB2, programB, date, date, organisationUnit, null );

        Collection<Patient> patients = patientService.getPatients( organisationUnit, programA, 0, 100 );

        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientB1 ) );

        patients = patientService.getPatients( organisationUnit, programB, 0, 100 );

        assertEquals( 1, patients.size() );
        assertTrue( patients.contains( patientB2 ) );
    }

    @Test
    public void testGetRepresentatives()
    {
        patientService.savePatient( patientB1 );

        patientA1.setRepresentative( patientB1 );
        patientA2.setRepresentative( patientB1 );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );

        assertEquals( 2, patientService.getRepresentatives( patientB1 ).size() );
    }

    @Test
    public void testCountGetPatientsByNameIdentifier()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        assertEquals( 2, patientService.countGetPatients( "b" ) );
    }

    @Test
    public void testCountGetPatientsByName()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );

        assertEquals( 3, patientService.countGetPatientsByName( "a" ) );
    }

    @Test
    public void testCreatePatientAndRelative()
    {
        int idB = patientService.savePatient( patientB1 );

        RelationshipType relationshipType = createRelationshipType( 'A' );
        int relationshipTypeId = relationshipTypeService.saveRelationshipType( relationshipType );

        patientA1.setUnderAge( true );
        PatientAttributeValue attributeValue = createPatientAttributeValue( 'A', patientA1, patientAttribute );
        Set<PatientAttributeValue> patientAttributeValues = new HashSet<PatientAttributeValue>();
        patientAttributeValues.add( attributeValue );

        int idA = patientService.createPatient( patientA1, idB, relationshipTypeId, patientAttributeValues );
        assertNotNull( patientService.getPatient( idA ) );
    }

    @Test
    public void testUpdatePatientAndRelative()
    {
        int idB = patientService.savePatient( patientB1 );

        RelationshipType relationshipType = createRelationshipType( 'A' );
        int relationshipTypeId = relationshipTypeService.saveRelationshipType( relationshipType );

        patientA3.setUnderAge( true );
        patientA3.setName( "B" );
        PatientAttributeValue attributeValue = createPatientAttributeValue( 'A', patientA3, patientAttribute );
        Set<PatientAttributeValue> patientAttributeValues = new HashSet<PatientAttributeValue>();
        patientAttributeValues.add( attributeValue );
        int idA = patientService.createPatient( patientA3, idB, relationshipTypeId, patientAttributeValues );
        assertNotNull( patientService.getPatient( idA ) );

        attributeValue.setValue( "AttributeB" );
        List<PatientAttributeValue> attributeValues = new ArrayList<PatientAttributeValue>(); //TODO use set
        attributeValues.add( attributeValue );

        patientService.updatePatient( patientA3, idB, relationshipTypeId, attributeValues, new ArrayList<PatientAttributeValue>(), new ArrayList<PatientAttributeValue>() );
        assertEquals( "B", patientService.getPatient( idA ).getName() );
    }

    @Test
    public void testCountGetPatientsByOrgUnit()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );

        assertEquals( 2, patientService.countGetPatientsByOrgUnit( organisationUnit ) );
    }

    @Test
    public void testCountGetPatientsByOrgUnitProgram()
    {
        programService.addProgram( programA );
        programService.addProgram( programB );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientB2 );

        programInstanceService.enrollPatient( patientA1, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientB1, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientA2, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientB2, programB, date, date, organisationUnit, null );

        assertEquals( 2, patientService.countGetPatientsByOrgUnitProgram( organisationUnit, programA ) );
        assertEquals( 1, patientService.countGetPatientsByOrgUnitProgram( organisationUnit, programB ) );
    }

    @Test
    public void testSearchPatients()
    {
        int idA = programService.addProgram( programA );
        programService.addProgram( programB );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        PatientAttributeValue attributeValue = createPatientAttributeValue( 'A', patientA3, patientAttribute );
        patientAttributeValueService.savePatientAttributeValue( attributeValue );

        programInstanceService.enrollPatient( patientA3, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientB1, programA, date, date, organisationUnit, null );

        List<String> searchKeys = new ArrayList<String>();
        searchKeys.add( Patient.PREFIX_IDENTIFIER_TYPE + Patient.SEARCH_SAPERATE + "a" + Patient.SEARCH_SAPERATE
            + organisationUnit.getId() );
        searchKeys.add( Patient.PREFIX_PATIENT_ATTRIBUTE + Patient.SEARCH_SAPERATE + attributeId
            + Patient.SEARCH_SAPERATE + "a" );
        searchKeys.add( Patient.PREFIX_PROGRAM + Patient.SEARCH_SAPERATE + idA );

        Collection<OrganisationUnit> orgunits = new HashSet<OrganisationUnit>();
        orgunits.add( organisationUnit );

        Collection<Patient> patients = patientService.searchPatients( searchKeys, orgunits, null, null, null,
            ProgramStageInstance.ACTIVE_STATUS, null, null );

        assertEquals( 1, patients.size() );
        assertTrue( patients.contains( patientA3 ) );
    }

    @Test
    public void testCountSearchPatients()
    {
        int idA = programService.addProgram( programA );
        programService.addProgram( programB );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        PatientAttributeValue attributeValue = createPatientAttributeValue( 'A', patientA3, patientAttribute );
        patientAttributeValueService.savePatientAttributeValue( attributeValue );

        programInstanceService.enrollPatient( patientA3, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientB1, programA, date, date, organisationUnit, null );

        List<String> searchKeys = new ArrayList<String>();
        searchKeys.add( Patient.PREFIX_IDENTIFIER_TYPE + Patient.SEARCH_SAPERATE + "a" + Patient.SEARCH_SAPERATE
            + organisationUnit.getId() );
        searchKeys.add( Patient.PREFIX_PATIENT_ATTRIBUTE + Patient.SEARCH_SAPERATE + attributeId
            + Patient.SEARCH_SAPERATE + "a" );
        searchKeys.add( Patient.PREFIX_PROGRAM + Patient.SEARCH_SAPERATE + idA );

        Collection<OrganisationUnit> orgunits = new HashSet<OrganisationUnit>();
        orgunits.add( organisationUnit );

        assertEquals( 1,
            patientService.countSearchPatients( searchKeys, orgunits, null, ProgramStageInstance.ACTIVE_STATUS ) );
    }

    @Test
    public void testGetPatientPhoneNumbers()
    {
        int idA = programService.addProgram( programA );
        programService.addProgram( programB );

        patientA3.setPhoneNumber( "123456789" );
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        PatientAttributeValue attributeValue = createPatientAttributeValue( 'A', patientA3, patientAttribute );
        patientAttributeValueService.savePatientAttributeValue( attributeValue );

        programInstanceService.enrollPatient( patientA3, programA, date, date, organisationUnit, null );
        programInstanceService.enrollPatient( patientB1, programA, date, date, organisationUnit, null );

        List<String> searchKeys = new ArrayList<String>();
        searchKeys.add( Patient.PREFIX_IDENTIFIER_TYPE + Patient.SEARCH_SAPERATE + "a" + Patient.SEARCH_SAPERATE
            + organisationUnit.getId() );
        searchKeys.add( Patient.PREFIX_PATIENT_ATTRIBUTE + Patient.SEARCH_SAPERATE + attributeId
            + Patient.SEARCH_SAPERATE + "a" );
        searchKeys.add( Patient.PREFIX_PROGRAM + Patient.SEARCH_SAPERATE + idA );

        Collection<OrganisationUnit> orgunits = new HashSet<OrganisationUnit>();
        orgunits.add( organisationUnit );

        Collection<String> phoneNumbers = patientService.getPatientPhoneNumbers( searchKeys, orgunits, null,
            ProgramStageInstance.ACTIVE_STATUS, null, null );

        assertEquals( 1, phoneNumbers.size() );
    }

    @Test
    public void testGetPatientsByPhone()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientA3 );

        Collection<Patient> patients = patientService.getPatientsByPhone( "123456789", null, null );
        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientA1 ) );
        assertTrue( patients.contains( patientA2 ) );
    }

    @Test
    public void testGetPatientByFullname()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientA2 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        Collection<Patient> patients = patientService.getPatientByFullname( "NameA", organisationUnit );

        assertEquals( 1, patients.size() );
        assertTrue( patients.contains( patientA1 ) );

        patients = patientService.getPatientByFullname( "NameB", organisationUnit );

        assertEquals( 2, patients.size() );
        assertTrue( patients.contains( patientB1 ) );
        assertTrue( patients.contains( patientB2 ) );
    }

    @Test
    public void testGetRegistrationOrgunitIds()
    {
        patientService.savePatient( patientA1 );
        patientService.savePatient( patientB1 );
        patientService.savePatient( patientB2 );

        Calendar yesterday = Calendar.getInstance();
        yesterday.add( Calendar.DATE, -1 );
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add( Calendar.DATE, 1 );

        Collection<Integer> orgunitIds = patientService.getRegistrationOrgunitIds( yesterday.getTime(),
            tomorrow.getTime() );

        assertEquals( 1, orgunitIds.size() );
        assertEquals( organisationUnit.getId(), orgunitIds.iterator().next().intValue() );
    }

    @Test
    public void testValidatePatient()
    {
        programService.addProgram( programA );

        ValidationCriteria validationCriteria = createValidationCriteria( 'A', "gender", 0, "F" );
        validationCriteriaService.saveValidationCriteria( validationCriteria );

        programA.getPatientValidationCriteria().add( validationCriteria );
        programService.updateProgram( programA );

        patientService.savePatient( patientA1 );
        patientService.savePatient( patientB1 );

        int validatePatientA1 = patientService.validatePatient( patientA1, programA );
        int validatePatientB1 = patientService.validatePatient( patientB1, programA );

        assertEquals( 0, validatePatientA1 );
        assertEquals( 2, validatePatientB1 );
    }
}
