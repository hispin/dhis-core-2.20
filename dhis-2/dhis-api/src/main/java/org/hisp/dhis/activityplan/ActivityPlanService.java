/**
 * 
 */
package org.hisp.dhis.activityplan;

import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.program.ProgramStageInstance;

/**
 * @author abyotag_adm
 *
 */
public interface ActivityPlanService
{
    String ID = ActivityPlanService.class.getName();
    
    Collection<Activity> getActivitiesByProvider( OrganisationUnit organisationUnit );
    
    Collection<Activity> getActivitiesByBeneficiary( Patient beneficiary );
    
    Collection<Activity> getActivitiesByTask( ProgramStageInstance task );
    
    Collection<Activity> getActivitiesByDueDate( Date dueDate );
    
    Collection<Activity> getActivitiesWithInDate( Date startDate, Date endDate );    

}
