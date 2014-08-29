package org.hisp.dhis.calendar;

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

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class representing a specific calendar date.
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 * @see DateInterval
 * @see Calendar
 */
public class DateTimeUnit
{
    /**
     * Year of date. Required.
     */
    @NotNull
    private int year;

    /**
     * Month of date. Required.
     */
    @NotNull
    private int month;

    /**
     * Day of date. Required.
     */
    @NotNull
    private int day;

    /**
     * Day of week, numbering is unspecified and left up to user.
     */
    private int dayOfWeek;

    /**
     * Does dateUnit represent ISO 8601.
     */
    final boolean iso8601;

    /**
     * Hour of day, range is 1 - 24.
     */
    private int hour;

    /**
     * Minute of hour, range is 0 - 59.
     */
    private int minute;

    /**
     * Second  of minute, range is 0 - 59.
     */
    private int second;

    /**
     * Millisecond of second, range is 0 - 999.
     */
    private int millis;

    public DateTimeUnit( boolean iso8601 )
    {
        this.iso8601 = iso8601;
        this.hour = 0;
        this.minute = 0;
        this.second = 0;
        this.millis = 0;
    }

    public DateTimeUnit()
    {
        this( false );
    }

    public DateTimeUnit( DateTimeUnit dateTimeUnit )
    {
        this( dateTimeUnit.isIso8601() );
        this.year = dateTimeUnit.getYear();
        this.month = dateTimeUnit.getMonth();
        this.day = dateTimeUnit.getDay();
        this.dayOfWeek = dateTimeUnit.getDayOfWeek();
    }

    public DateTimeUnit( DateTimeUnit dateTimeUnit, boolean iso8601 )
    {
        this( iso8601 );
        this.year = dateTimeUnit.getYear();
        this.month = dateTimeUnit.getMonth();
        this.day = dateTimeUnit.getDay();
        this.dayOfWeek = dateTimeUnit.getDayOfWeek();
    }

    public DateTimeUnit( int year, int month, int day, boolean iso8601 )
    {
        this( iso8601 );
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public DateTimeUnit( int year, int month, int day )
    {
        this( year, month, day, false );
    }

    public DateTimeUnit( int year, int month, int day, int dayOfWeek, boolean iso8601 )
    {
        this( year, month, day, iso8601 );
        this.dayOfWeek = dayOfWeek;
    }

    public DateTimeUnit( int year, int month, int day, int dayOfWeek )
    {
        this( year, month, day, dayOfWeek, false );
    }

    public int getYear()
    {
        return year;
    }

    public void setYear( int year )
    {
        this.year = year;
    }

    public int getMonth()
    {
        return month;
    }

    public void setMonth( int month )
    {
        this.month = month;
    }

    public int getDay()
    {
        return day;
    }

    public void setDay( int day )
    {
        this.day = day;
    }

    public int getDayOfWeek()
    {
        return dayOfWeek;
    }

    public void setDayOfWeek( int dayOfWeek )
    {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isIso8601()
    {
        return iso8601;
    }

    public int getHour()
    {
        return hour;
    }

    public void setHour( int hour )
    {
        this.hour = hour;
    }

    public int getMinute()
    {
        return minute;
    }

    public void setMinute( int minute )
    {
        this.minute = minute;
    }

    public int getSecond()
    {
        return second;
    }

    public void setSecond( int second )
    {
        this.second = second;
    }

    public int getMillis()
    {
        return millis;
    }

    public void setMillis( int millis )
    {
        this.millis = millis;
    }

    /**
     * Converts dateUnit to Joda-Time DateTime
     *
     * @return Populated DateTime object
     */
    public DateTime toDateTime()
    {
        if ( !iso8601 )
        {
            throw new RuntimeException( "Cannot convert non-ISO8601 DateUnit to DateTime." );
        }

        return new DateTime( year, month, day, 12, 0, ISOChronology.getInstance() );
    }

    /**
     * Converts dateUnit to Joda-Time DateTime with a specific chronology.
     *
     * @param chronology Chronology to use
     * @return Populated DateTime object
     */
    public DateTime toJodaDateTime( Chronology chronology )
    {
        return new DateTime( year, month, day, 12, 0, chronology );
    }

    /**
     * Converts dateUnit to JDK Calendar
     *
     * @return Populated JDK Calendar object
     */
    public java.util.Calendar toJdkCalendar()
    {
        if ( !iso8601 )
        {
            throw new RuntimeException( "Cannot convert non-ISO8601 DateUnit to JDK Calendar." );
        }

        java.util.Calendar calendar = new GregorianCalendar( year, month - 1, day );
        calendar.setTime( calendar.getTime() );

        return calendar;
    }

    /**
     * Converts dateUnit to JDK Date
     *
     * @return Populated JDK Date object
     */
    public Date toJdkDate()
    {
        return toJdkCalendar().getTime();
    }

    /**
     * Converts from Joda-Time DateTime to DateUnit
     *
     * @param dateTime DateTime object
     * @return Populated DateUnit object
     */
    public static DateTimeUnit fromJodaDateTime( DateTime dateTime )
    {
        return new DateTimeUnit( dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getDayOfWeek() );
    }

    /**
     * Converts from Joda-Time DateTime to DateUnit
     *
     * @param dateTime DateTime object
     * @param iso8601  whether date time is iso8601
     * @return Populated DateUnit object
     */
    public static DateTimeUnit fromJodaDateTime( DateTime dateTime, boolean iso8601 )
    {
        return new DateTimeUnit( dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getDayOfWeek(), iso8601 );
    }

    /**
     * Converts from JDK Calendar to DateUnit
     *
     * @param calendar JDK Calendar object
     * @return Populated DateUnit object
     */
    public static DateTimeUnit fromJdkCalendar( java.util.Calendar calendar )
    {
        return new DateTimeUnit( calendar.get( java.util.Calendar.YEAR ), calendar.get( java.util.Calendar.MONTH ) + 1,
            calendar.get( java.util.Calendar.DAY_OF_MONTH ), calendar.get( java.util.Calendar.DAY_OF_WEEK ), true );
    }

    /**
     * Converts from JDK Date to DateUnit
     *
     * @param date JDK Date object
     * @return Populated DateUnit object
     */
    public static DateTimeUnit fromJdkDate( Date date )
    {
        return fromJodaDateTime( new DateTime( date.getTime() ), true );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        DateTimeUnit dateTimeUnit = (DateTimeUnit) o;

        if ( day != dateTimeUnit.day ) return false;
        if ( iso8601 != dateTimeUnit.iso8601 ) return false;
        if ( month != dateTimeUnit.month ) return false;
        if ( year != dateTimeUnit.year ) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + day;
        result = 31 * result + (iso8601 ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "DateUnit{" +
            "year=" + year +
            ", month=" + month +
            ", day=" + day +
            ", iso8601=" + iso8601 +
            '}';
    }
}
