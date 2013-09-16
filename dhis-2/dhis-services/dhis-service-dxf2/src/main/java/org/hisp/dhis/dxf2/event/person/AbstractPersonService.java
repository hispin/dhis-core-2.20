package org.hisp.dhis.dxf2.event.person;

/*
 * Copyright (c) 2004-2013, University of Oslo
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

import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientIdentifier;
import org.hisp.dhis.patient.PatientIdentifierService;
import org.hisp.dhis.patient.PatientIdentifierType;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patient.util.PatientIdentifierGenerator;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.program.Program;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hisp.dhis.system.util.TextUtils.nullIfEmpty;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Transactional
public abstract class AbstractPersonService implements PersonService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientIdentifierService patientIdentifierService;

    @Autowired
    private PatientAttributeValueService patientAttributeValueService;

    @Autowired
    private IdentifiableObjectManager manager;

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    public Persons getPersons()
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getAllPatients() );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( OrganisationUnit organisationUnit )
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getPatients( organisationUnit ) );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( Gender gender )
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getPatiensByGender( gender.getValue() ) );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( Program program )
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getPatients( program ) );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( Program program, Gender gender )
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getPatients( program, gender.getValue() ) );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( OrganisationUnit organisationUnit, Program program )
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getPatients( organisationUnit, program ) );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( OrganisationUnit organisationUnit, Gender gender )
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getPatients( organisationUnit, gender.getValue() ) );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( OrganisationUnit organisationUnit, Program program, Gender gender )
    {
        List<Patient> patients = new ArrayList<Patient>( patientService.getPatients( organisationUnit, program, gender.getValue() ) );
        return getPersons( patients );
    }

    @Override
    public Persons getPersons( Collection<Patient> patients )
    {
        Persons persons = new Persons();

        for ( Patient patient : patients )
        {
            persons.getPersons().add( getPerson( patient ) );
        }

        return persons;
    }

    @Override
    public Person getPerson( String uid )
    {
        return getPerson( patientService.getPatient( uid ) );
    }

    @Override
    public Person getPerson( Patient patient )
    {
        Person person = new Person();
        person.setPerson( patient.getUid() );
        person.setOrgUnit( patient.getOrganisationUnit().getUid() );

        person.setName( patient.getName() );
        person.setGender( Gender.fromString( patient.getGender() ) );

        person.setDeceased( patient.getIsDead() );
        person.setDateOfDeath( patient.getDeathDate() );

        Contact contact = new Contact();
        contact.setPhoneNumber( nullIfEmpty( patient.getPhoneNumber() ) );

        if ( contact.getPhoneNumber() != null )
        {
            person.setContact( contact );
        }

        DateOfBirth dateOfBirth;

        Character dobType = patient.getDobType();

        if ( dobType != null && patient.getBirthDate() != null )
        {
            if ( dobType.equals( Patient.DOB_TYPE_VERIFIED ) || dobType.equals( Patient.DOB_TYPE_DECLARED ) )
            {
                dateOfBirth = new DateOfBirth( patient.getBirthDate(),
                    DateOfBirthType.fromString( String.valueOf( dobType ) ) );
            }
            else
            {
                // assume APPROXIMATE
                dateOfBirth = new DateOfBirth( patient.getIntegerValueOfAge() );
            }

            person.setDateOfBirth( dateOfBirth );
        }

        person.setDateOfRegistration( patient.getRegistrationDate() );

        for ( PatientIdentifier patientIdentifier : patient.getIdentifiers() )
        {
            String identifierType = patientIdentifier.getIdentifierType() == null ? null : patientIdentifier.getIdentifierType().getUid();

            Identifier identifier = new Identifier( identifierType, patientIdentifier.getIdentifier() );
            person.getIdentifiers().add( identifier );
        }

        for ( PatientAttribute patientAttribute : patient.getAttributes() )
        {
            PatientAttributeValue patientAttributeValue = patientAttributeValueService.getPatientAttributeValue( patient, patientAttribute );

            Attribute attribute = new Attribute();
            attribute.setType( patientAttribute.getUid() );
            attribute.setValue( patientAttributeValue.getValue() );

            person.getAttributes().add( attribute );
        }

        return person;
    }

    public Patient getPatient( Person person )
    {
        Assert.hasText( person.getOrgUnit() );

        Patient patient = new Patient();
        patient.setName( person.getName() );
        patient.setGender( person.getGender().getValue() );

        OrganisationUnit organisationUnit = manager.get( OrganisationUnit.class, person.getOrgUnit() );
        Assert.notNull( organisationUnit );

        patient.setOrganisationUnit( organisationUnit );

        DateOfBirth dateOfBirth = person.getDateOfBirth();

        if ( dateOfBirth != null )
        {
            if ( dateOfBirth.getDate() != null && (dateOfBirth.getType().equals( DateOfBirthType.DECLARED ) ||
                dateOfBirth.getType().equals( DateOfBirthType.VERIFIED )) )
            {
                char dobType = dateOfBirth.getType().getValue().charAt( 0 );
                patient.setDobType( dobType );
                patient.setBirthDate( dateOfBirth.getDate() );
            }
            else if ( dateOfBirth.getAge() != null && dateOfBirth.getType().equals( DateOfBirthType.APPROXIMATE ) )
            {
                char dobType = dateOfBirth.getType().getValue().charAt( 0 );
                patient.setDobType( dobType );
                patient.setBirthDateFromAge( dateOfBirth.getAge(), dobType );
            }
        }

        if ( person.getContact() != null && person.getContact().getPhoneNumber() != null )
        {
            patient.setPhoneNumber( person.getContact().getPhoneNumber() );
        }

        patient.setIsDead( person.isDeceased() );

        if ( person.isDeceased() && person.getDateOfDeath() != null )
        {
            patient.setDeathDate( person.getDateOfDeath() );
        }

        patient.setRegistrationDate( person.getDateOfRegistration() );
        updateIdentifiers( person, patient );

        return patient;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public ImportSummary savePerson( Person person )
    {
        ImportSummary importSummary = new ImportSummary();
        importSummary.setDataValueCount( null );

        List<ImportConflict> importConflicts = new ArrayList<ImportConflict>();
        importConflicts.addAll( checkForRequiredIdentifiers( person ) );
        importConflicts.addAll( checkForRequiredAttributes( person ) );

        importSummary.setConflicts( importConflicts );

        if ( !importConflicts.isEmpty() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.getImportCount().incrementIgnored();
            return importSummary;
        }

        updateSystemIdentifier( person );

        Patient patient = getPatient( person );
        patientService.savePatient( patient );

        updateAttributeValues( person, patient );
        patientService.updatePatient( patient );

        importSummary.setStatus( ImportStatus.SUCCESS );
        importSummary.setReference( patient.getUid() );
        importSummary.getImportCount().incrementImported();

        return importSummary;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public ImportSummary updatePerson( Person person )
    {
        System.err.println( "Person: " + person );

        ImportSummary importSummary = new ImportSummary();
        importSummary.setDataValueCount( null );

        Patient patient = manager.get( Patient.class, person.getPerson() );
        System.err.println( "Patient: " + person );
        OrganisationUnit organisationUnit = manager.get( OrganisationUnit.class, person.getOrgUnit() );

        List<ImportConflict> importConflicts = new ArrayList<ImportConflict>();
        importConflicts.addAll( checkForRequiredIdentifiers( person ) );
        importConflicts.addAll( checkForRequiredAttributes( person ) );

        if ( organisationUnit == null )
        {
            importConflicts.add(
                new ImportConflict( "OrganisationUnit", "orgUnit " + person.getOrgUnit() + " does not point to valid organisation unit" ) );
        }

        DateOfBirth dateOfBirth = person.getDateOfBirth();

        if ( dateOfBirth == null )
        {
            importConflicts.add(
                new ImportConflict( "DateOfBirth", "dateOfBirth is not present" ) );
        }

        importSummary.setConflicts( importConflicts );

        if ( !importConflicts.isEmpty() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.getImportCount().incrementIgnored();
            return importSummary;
        }

        patient.setName( person.getName() );
        patient.setGender( person.getGender().getValue() );
        patient.setIsDead( person.isDeceased() );
        patient.setDeathDate( person.getDateOfDeath() );
        // TODO should we allow update of this property?
        patient.setRegistrationDate( person.getDateOfRegistration() );

        String phoneNumber = person.getContact() != null ? person.getContact().getPhoneNumber() : null;
        patient.setPhoneNumber( phoneNumber );

        if ( DateOfBirthType.APPROXIMATE.equals( dateOfBirth.getType() ) )
        {
            dateOfBirth = new DateOfBirth( dateOfBirth.getAge() );
        }

        patient.setDobType( dateOfBirth.getType().getValue().charAt( 0 ) );
        patient.setBirthDate( dateOfBirth.getDate() );

        updateSystemIdentifier( person );
        removeIdentifiers( patient );
        removeAttributeValues( patient );
        patientService.updatePatient( patient );

        updateIdentifiers( person, patient );
        updateAttributeValues( person, patient );
        patientService.updatePatient( patient );

        System.err.println( "Patient: " + getPerson( patient ) );

        importSummary.setStatus( ImportStatus.SUCCESS );
        importSummary.setReference( patient.getUid() );
        importSummary.getImportCount().incrementImported();

        return importSummary;
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public void deletePerson( Person person )
    {
        Patient patient = patientService.getPatient( person.getPerson() );

        if ( patient != null )
        {
            patientService.deletePatient( patient );
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private List<ImportConflict> checkForRequiredIdentifiers( Person person )
    {
        List<ImportConflict> importConflicts = new ArrayList<ImportConflict>();
        Collection<PatientIdentifierType> patientIdentifierTypes = manager.getAll( PatientIdentifierType.class );
        Map<String, String> cacheMap = new HashMap<String, String>();
        Patient patient = manager.get( Patient.class, person.getPerson() );

        for ( Identifier identifier : person.getIdentifiers() )
        {
            if ( identifier.getValue() != null )
            {
                cacheMap.put( identifier.getType(), identifier.getValue() );
            }
        }

        for ( PatientIdentifierType patientIdentifierType : patientIdentifierTypes )
        {
            if ( patientIdentifierType.isMandatory() )
            {
                if ( !cacheMap.keySet().contains( patientIdentifierType.getUid() ) )
                {
                    importConflicts.add(
                        new ImportConflict( "Identifier.type", "Missing required identifier type " + patientIdentifierType.getUid() ) );
                }
            }

            List<PatientIdentifier> patientIdentifiers = new ArrayList<PatientIdentifier>( patientIdentifierService.getAll(
                patientIdentifierType, cacheMap.get( patientIdentifierType.getUid() ) ) );

            if ( !patientIdentifiers.isEmpty() )
            {
                // if .size() > 1, there is something wrong with the db.. but we for-loop for now
                for ( PatientIdentifier patientIdentifier : patientIdentifiers )
                {
                    if ( !patientIdentifier.getPatient().equals( patient ) )
                    {
                        importConflicts.add(
                            new ImportConflict( "Identifier.value", "Value already exists for patient " + patientIdentifier.getPatient().getUid()
                                + " with identifier type " + patientIdentifierType.getUid() ) );
                    }
                }
            }
        }

        return importConflicts;
    }

    private List<ImportConflict> checkForRequiredAttributes( Person person )
    {
        List<ImportConflict> importConflicts = new ArrayList<ImportConflict>();
        Collection<PatientAttribute> patientAttributes = manager.getAll( PatientAttribute.class );
        Set<String> cache = new HashSet<String>();

        for ( Identifier identifier : person.getIdentifiers() )
        {
            if ( identifier.getValue() != null )
            {
                cache.add( identifier.getType() );
            }
        }

        for ( PatientAttribute patientAttribute : patientAttributes )
        {
            if ( patientAttribute.isMandatory() )
            {
                if ( !cache.contains( patientAttribute.getUid() ) )
                {
                    importConflicts.add(
                        new ImportConflict( "Identifier.type", "Missing required attribute type " + patientAttribute.getUid() ) );
                }
            }
        }

        return importConflicts;
    }

    private void updateAttributeValues( Person person, Patient patient )
    {
        for ( Attribute attribute : person.getAttributes() )
        {
            PatientAttribute patientAttribute = manager.get( PatientAttribute.class, attribute.getType() );

            if ( patientAttribute != null )
            {
                PatientAttributeValue patientAttributeValue = new PatientAttributeValue();
                patientAttributeValue.setPatient( patient );
                patientAttributeValue.setValue( attribute.getValue() );
                patientAttributeValue.setPatientAttribute( patientAttribute );

                patientAttributeValueService.savePatientAttributeValue( patientAttributeValue );

                patient.getAttributes().add( patientAttribute );
            }
        }
    }

    private void updateSystemIdentifier( Person person )
    {
        Date birthDate = person.getDateOfBirth() != null ? person.getDateOfBirth().getDate() : null;
        String gender = person.getGender() != null ? person.getGender().getValue() : null;

        if ( birthDate == null || gender == null )
        {
            birthDate = new Date();
            gender = "F";
        }

        Iterator<Identifier> iterator = person.getIdentifiers().iterator();

        // remove any old system identifiers
        while ( iterator.hasNext() )
        {
            Identifier identifier = iterator.next();

            if ( identifier.getType() == null )
            {
                iterator.remove();
            }
        }

        Identifier identifier = generateSystemIdentifier( birthDate, gender );

        if ( person.getPerson() != null )
        {
            identifier = generateSystemIdentifier( birthDate, gender );
            Patient patient = manager.get( Patient.class, person.getPerson() );

            for ( PatientIdentifier patientIdentifier : patient.getIdentifiers() )
            {
                if ( patientIdentifier.getIdentifierType() == null && patientIdentifier.getIdentifier() != null )
                {
                    identifier = new Identifier( patientIdentifier.getIdentifier() );
                    break;
                }
            }
        }

        person.getIdentifiers().add( identifier );
    }

    private Identifier generateSystemIdentifier( Date birthDate, String gender )
    {
        String systemId = PatientIdentifierGenerator.getNewIdentifier( birthDate, gender );

        PatientIdentifier patientIdentifier = patientIdentifierService.get( null, systemId );

        while ( patientIdentifier != null )
        {
            systemId = PatientIdentifierGenerator.getNewIdentifier( birthDate, gender );
            patientIdentifier = patientIdentifierService.get( null, systemId );
        }

        Identifier identifier = new Identifier();
        identifier.setValue( systemId );

        return identifier;
    }

    // add identifiers from person => patient
    private void updateIdentifiers( Person person, Patient patient )
    {
        for ( Identifier identifier : person.getIdentifiers() )
        {
            if ( identifier.getType() == null )
            {
                PatientIdentifier patientIdentifier = new PatientIdentifier();
                patientIdentifier.setIdentifier( identifier.getValue().trim() );
                patientIdentifier.setIdentifierType( null );
                patientIdentifier.setPatient( patient );

                patient.getIdentifiers().add( patientIdentifier );

                continue;
            }

            PatientIdentifierType type = manager.get( PatientIdentifierType.class, identifier.getType() );

            if ( type != null && nullIfEmpty( identifier.getValue() ) != null )
            {
                PatientIdentifier patientIdentifier = new PatientIdentifier();
                patientIdentifier.setIdentifier( identifier.getValue().trim() );
                patientIdentifier.setIdentifierType( type );
                patientIdentifier.setPatient( patient );

                patient.getIdentifiers().add( patientIdentifier );
            }
        }
    }

    private void removeIdentifiers( Patient patient )
    {
        for ( PatientIdentifier patientIdentifier : patient.getIdentifiers() )
        {
            patientIdentifierService.deletePatientIdentifier( patientIdentifier );
        }

        patient.setIdentifiers( new HashSet<PatientIdentifier>() );
        patientService.updatePatient( patient );
    }

    private void removeAttributeValues( Patient patient )
    {
        patientAttributeValueService.deletePatientAttributeValue( patient );
        patient.setAttributes( new HashSet<PatientAttribute>() );
        patientService.updatePatient( patient );
    }
}
