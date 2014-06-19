package org.hisp.dhis.dxf2.filter;

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
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;
import org.hisp.dhis.node.NodeTransformer;

import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class FieldMap extends ForwardingMap<String, FieldMap>
{
    private final Map<String, FieldMap> delegate = Maps.newHashMap();

    private NodeTransformer nodeTransformer;

    @Override
    protected Map<String, FieldMap> delegate()
    {
        return delegate;
    }

    public NodeTransformer getNodeTransformer()
    {
        return nodeTransformer;
    }

    public void setNodeTransformer( NodeTransformer nodeTransformer )
    {
        this.nodeTransformer = nodeTransformer;
    }

    public boolean haveNodeTransformer()
    {
        return nodeTransformer != null;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper( this )
            .add( "map", standardToString() )
            .add( "nodeTransformer", nodeTransformer )
            .toString();
    }
}
