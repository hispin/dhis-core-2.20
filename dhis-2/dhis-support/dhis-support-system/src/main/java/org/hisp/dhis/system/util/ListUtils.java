package org.hisp.dhis.system.util;

/*
 * Copyright (c) 2004-2012, University of Oslo
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lars Helge Overland
 */
public class ListUtils
{
    /**
     * Checks whether the given list contains duplicates. List entries are compared
     * using the given comparator.
     * 
     * @param list the list.
     * @param comparator the comparator.
     * @return true if the list contains duplicates, false if not.
     */
    public static <T> boolean containsDuplicates( List<T> list, Comparator<T> comparator )
    {
        Collections.sort( list, comparator );
        
        T previous = null;
        
        for ( T entry : list )
        {
            if ( previous != null && previous.equals( entry ) )
            {
                return true;
            }
            
            previous = entry;
        }
        
        return false;
    }
    
    /**
     * Returns the duplicates in the given list. List entries are compared
     * using the given comparator.
     * 
     * @param list the list.
     * @param comparator the comparator.
     * @return a set of duplicates from the given list.
     */
    public static <T> Collection<T> getDuplicates( List<T> list, Comparator<T> comparator )
    {
        Set<T> duplicates = new HashSet<T>();
        
        Collections.sort( list, comparator );
        
        T previous = null;
        
        for ( T entry : list )
        {
            if ( previous != null && previous.equals( entry ) )
            {
                duplicates.add( entry );
            }
            
            previous = entry;
        }
        
        return duplicates;
    }
    
    /**
     * Returns a Collection with the given items.
     * 
     * @param items the items which should be included in the Collection.
     * @return a Collection.
     */
    public static <T> Collection<T> getCollection( T... items )
    {
        List<T> list = new ArrayList<T>();
        
        for ( T item : items )
        {
            list.add( item );
        }
        
        return list;
    }
}
