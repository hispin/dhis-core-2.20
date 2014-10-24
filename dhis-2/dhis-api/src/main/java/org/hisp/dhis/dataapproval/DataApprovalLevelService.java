package org.hisp.dhis.dataapproval;

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

import org.hisp.dhis.dataelement.CategoryOptionGroup;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.user.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jim Grace
 */
public interface DataApprovalLevelService
{
    String ID = DataApprovalLevelService.class.getName();

    /**
     * Integer that can be used in place of approval level
     * for data that has not been approved at any level.
     */
    public static final int APPROVAL_LEVEL_UNAPPROVED = 999;

    /**
     * Gets the data approval level with the given id.
     *
     * @param id the id.
     * @return a data approval level.
     */
    DataApprovalLevel getDataApprovalLevel( int id );

    /**
     * Gets the data approval level with the given name.
     *
     * @param name the name.
     * @return a data approval level.
     */
    DataApprovalLevel getDataApprovalLevelByName( String name );

    /**
     * Gets the data approval level with the given level number.
     *
     * @param levelNumber number of the level to return.
     * @return a data approval level.
     */
    DataApprovalLevel getDataApprovalLevelByLevelNumber( int levelNumber );

    /**
     * Gets the highest approval level for a given organisation unit and
     * (optionally) a set of attribute option groups. Returns the first
     * approval level matching both the orgUnit's level and (optionally)
     * having a category option group set containing one of the category
     * option groups.
     *
     * @param orgUnit organisation unit to look for.
     * @param cogs attribute option groups (if any) to look for.
     * @return a data approval level, or null if not found.
     */
    DataApprovalLevel getHighestDataApprovalLevel( OrganisationUnit orgUnit, Set<CategoryOptionGroup> cogs );

    /**
     * Gets the lowest approval level for a given organisation unit and
     * (optionally) a set of attribute options. Returns the last
     * approval level matching both the orgUnit's level and (optionally)
     * having a category option group set containing one of the category
     * option groups containing one of the options.
     *
     * @param orgUnit organisation unit to look for.
     * @param attributeOptionCombo attribute option combination.
     * @return a data approval level, or null if not found.
     */
    DataApprovalLevel getLowestDataApprovalLevel( OrganisationUnit orgUnit, DataElementCategoryOptionCombo attributeOptionCombo );

    /**
     * Gets the lowest approval level for a given attribute option (for
     * any possible organisation unit.)
     *
     * @param option option (if any) that may belong to an approval level.
     * @return lowest approval level that could contain the option.
     */
    DataApprovalLevel getLowestOptionApprovalLevel( DataElementCategoryOption option );

    /**
     * Gets a list of all data approval levels.
     *
     * @return List of all data approval levels, ordered from 1 to n.
     */
    List<DataApprovalLevel> getAllDataApprovalLevels();
    
    Map<Integer, DataApprovalLevel> getDataApprovalLevelMap();
    
    List<DataApprovalLevel> getUserDataApprovalLevels();

    /**
     * Gets data approval levels by org unit level.
     * 
     * @param orgUnitLevel the org unit level.
     * @return a list of data approval levels.
     */
    List<DataApprovalLevel> getDataApprovalLevelsByOrgUnitLevel( int orgUnitLevel );
    
    /**
     * Retrieves all org unit levels which have approval levels associated.
     * 
     * @return a list of org unit levels.
     */
    Set<OrganisationUnitLevel> getOrganisationUnitApprovalLevels();
    
    /**
     * Tells whether a level can move down in the list (can switch places with
     * the level below.)
     *
     * @param level the level to test.
     * @return true if the level can move down, otherwise false.
     */
    boolean canDataApprovalLevelMoveDown( int level );

    /**
     * Tells whether a level can move up in the list (can switch places with
     * the level above.)
     *
     * @param level the level to test.
     * @return true if the level can move up, otherwise false.
     */
    boolean canDataApprovalLevelMoveUp( int level );

    /**
     * Moves a data approval level down in the list (switches places with the
     * level below).
     *
     * @param level the level to move down.
     */
    void moveDataApprovalLevelDown( int level );

    /**
     * Moves a data approval level up in the list (switches places with the
     * level above).
     *
     * @param level the level to move up.
     */
    void moveDataApprovalLevelUp( int level );

    /**
     * Determines whether level already exists with the same organisation
     * unit level and category option group set (but not necessarily the
     * same level number.)
     *
     * @param level Data approval level to test for existence.
     * @return true if it exists, otherwise false.
     */
    public boolean dataApprovalLevelExists ( DataApprovalLevel level );

    /**
     * Adds a new data approval level. Adds the new level at the highest
     * position possible (to facilitate the use case where users add the
     * approval levels from low to high.)
     *
     * @param newLevel the new level to add.
     * @return the identifier of the added level, or -1 if not well formed or duplicate.
     */
    int addDataApprovalLevel( DataApprovalLevel newLevel );
    
    /**
     * Adds a new data approval level. Sets the level epxlicitl.
     * 
     * @param approvalLevel the new level to add.
     * @param level the level.
     * @return the identifier of the added level, or -1 if not well formed or duplicate.
     */
    int addDataApprovalLevel( DataApprovalLevel approvalLevel, int level );

    /**
     * Removes a data approval level.
     *
     * @param dataApprovalLevel the data approval level to delete.
     */
    void deleteDataApprovalLevel( DataApprovalLevel dataApprovalLevel );

    /**
     * Gets the approval level at which this user may make approval actions
     * (if the user is authorized for any) on this organisation unit.
     *
     * @param orgUnit org unit to test
     * @param includeDataViewOrgUnits include data view org units in test?
     * @return approval level
     */
    DataApprovalLevel getUserApprovalLevel( OrganisationUnit orgUnit, boolean includeDataViewOrgUnits );
    
    DataApprovalLevel getUserApprovalLevel( User user, OrganisationUnit orgUnit, boolean includeDataViewOrgUnits );

    /**
     * By organisation unit subhierarchy, returns the lowest data approval
     * level at which the user may see data within that subhierarchy, if
     * data viewing is being restricted to approved data from lower levels.
     * <p>
     * Returns the value APPROVAL_LEVEL_UNAPPROVED for a subhierarchy if
     * the user may see unapproved data.
     * <p>
     * (Note that the "lowest" approval level means the "highest" approval
     * level number.)
     *
     * @return For each organisation unit subhierarchy available to the user,
     *         the minimum data approval level within that subhierarchy.
     */
    Map<OrganisationUnit, Integer> getUserReadApprovalLevels();
}
