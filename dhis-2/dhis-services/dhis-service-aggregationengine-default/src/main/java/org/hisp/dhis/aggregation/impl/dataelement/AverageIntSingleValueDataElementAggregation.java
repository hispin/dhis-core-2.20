package org.hisp.dhis.aggregation.impl.dataelement;

/*
 * Copyright (c) 2004-2010, University of Oslo
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

import static org.hisp.dhis.system.util.DateUtils.getDays;

import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.aggregation.AggregationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitHierarchy;
import org.hisp.dhis.period.Period;

/**
 * @author Lars Helge Overland
 * @version $Id: SumBoolDataElementAggregation.java 4753 2008-03-14 12:48:50Z larshelg $
 */
public class AverageIntSingleValueDataElementAggregation
    extends AbstractDataElementAggregation
{
    public double getAggregatedValue( DataElement dataElement, DataElementCategoryOptionCombo optionCombo, Date aggregationStartDate, Date aggregationEndDate,
        OrganisationUnit organisationUnit )
    {
        double[] sums = getSumAndRelevantDays( dataElement.getId(), optionCombo.getId(), aggregationStartDate, aggregationEndDate, 
            organisationUnit.getId() );

        if ( sums[1] > 0 )
        {
            return sums[0];
        }
        else
        {
            return AggregationService.NO_VALUES_REGISTERED;
        }
    }
    
    protected Collection<DataValue> getDataValues( int dataElementId, int optionComboId, int organisationUnitId,
        Date startDate, Date endDate )
    {
        OrganisationUnitHierarchy hierarchy = aggregationCache.getOrganisationUnitHierarchy();
        
        Collection<Integer> periods = aggregationCache.getIntersectingPeriodIds( startDate, endDate );
        
        Collection<DataValue> values = aggregationStore.getDataValues( hierarchy.getChildren( organisationUnitId ), dataElementId, optionComboId, periods );
        
        return values;
    }

    /**
     * The main performance disadvantage of average aggregation operations is that
     * the average must be calculated for each organisation unit individually, and
     * then summarized, in contrast to sum operations where the sum can be calculated
     * directly for all organisation units. Still, in cases where the aggregation
     * period duration is shorter than the data element period type duration, there
     * can only be one value registered for each organisation unit for that data
     * element. This implies that there is no need to calculate the average and
     * that the aggregate can be calculated directly, improving performance
     * dramatically. This method is performs the described behaviour by taking
     * the sum directly using whatever value found.
     * 
     * @param dataValues The datavalues to aggregate
     * @param startDate Start date of the period to aggregate over
     * @param endDate End date of the period to aggregate over
     * @param aggregationStartDate The original start date of the entire
     *        aggregation period
     * @param aggregationEndDate The original end date of the entire aggregation
     *        period
     */
    protected double[] getAggregateOfValues( Collection<DataValue> dataValues, Date startDate, Date endDate,
        Date aggregationStartDate, Date aggregationEndDate )
    {
        double totalSum = 0;
        double totalRelevantDays = 0;

        for ( DataValue dataValue : dataValues )
        {
            Period currentPeriod = aggregationCache.getPeriod( dataValue.getPeriod().getId() );
            Date currentStartDate = currentPeriod.getStartDate();
            Date currentEndDate = currentPeriod.getEndDate();

            double value = 0;

            try
            {
                value = Double.parseDouble( dataValue.getValue() );
            }
            catch ( Exception ex )
            {
            }

            double currentPeriodDuration = ( getDays( currentEndDate ) - getDays( currentStartDate ) );

            if ( currentPeriodDuration > 0 )
            {
                long relevantDays = 0;

                if ( currentStartDate.compareTo( endDate ) <= 0 && currentEndDate.compareTo( startDate ) >= 0 ) // Value is intersecting
                {
                    relevantDays = getDays( endDate ) - getDays( startDate );
                    totalSum += value;
                }

                totalRelevantDays += relevantDays;
            }
        }

        double[] fraction = { totalSum, totalRelevantDays };

        return fraction;
    }
}
