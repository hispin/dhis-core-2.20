package org.hisp.dhis.system.util;

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

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Lars Helge Overland
 */
public class PaginatedListTest
{
    @Test
    public void testNextPage()
    {
        PaginatedList<String> list = new PaginatedList<String>( Arrays.asList( "A", "B", "C" ), 2 );
        
        List<String> page = list.nextPage();
        
        assertNotNull( page );
        assertEquals( 2, page.size() );
        assertTrue( page.contains( "A" ) );
        assertTrue( page.contains( "B" ) );
        
        page = list.nextPage();
        
        assertNotNull( page );
        assertEquals( 1, page.size() );
        assertTrue( page.contains( "C" ) );
        
        page = list.nextPage();
        
        assertNull( page );
    }
    
    @Test
    public void testGetPageEmpty()
    {
        PaginatedList<String> list = new PaginatedList<String>( new ArrayList<String>(), 2 );
        
        List<String> page = list.nextPage();
        
        assertNull( page );
    }
    
    @Test
    public void testPageCount()
    {
        PaginatedList<String> list = new PaginatedList<String>( Arrays.asList( "A", "B", "C" ), 2 );
        
        assertEquals( 2, list.pageCount() );
        
        list = new PaginatedList<String>( Arrays.asList( "A", "B", "C", "D" ), 2 );
        
        assertEquals( 2, list.pageCount() );

        list = new PaginatedList<String>( Arrays.asList( "A", "B", "C", "D", "E" ), 2 );
        
        assertEquals( 3, list.pageCount() );
    }
    
    @Test
    public void testReset()
    {
        PaginatedList<String> list = new PaginatedList<String>( Arrays.asList( "A", "B", "C" ), 2 );
        
        assertTrue( list.nextPage().contains( "A" ) );
        
        list.reset();

        assertTrue( list.nextPage().contains( "A" ) );        
    }
}
