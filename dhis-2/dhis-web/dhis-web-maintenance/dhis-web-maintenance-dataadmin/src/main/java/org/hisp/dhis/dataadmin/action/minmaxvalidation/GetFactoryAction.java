package org.hisp.dhis.dataadmin.action.minmaxvalidation;

/*
 * Copyright (c) 2004-2007, University of Oslo
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

import java.io.Serializable;

import org.hisp.dhis.options.SystemSettingManager;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * @version $Id$
 */

public class GetFactoryAction
    implements Action
{
    // -------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    // -------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------

    private Double factor;

    public Double getFactor()
    {
        return factor;
    }

    // -------------------------------------------------------------------
    // Implementation Action
    // -------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        Serializable value = systemSettingManager.getSystemSetting( SystemSettingManager.KEY_FACTOR_OF_DEVIATION );
        if ( value == null )
        {
            factor = 2.0;
            systemSettingManager.saveSystemSetting( SystemSettingManager.KEY_FACTOR_OF_DEVIATION, factor );
        }
        else
        {
            factor = (Double) systemSettingManager.getSystemSetting( SystemSettingManager.KEY_FACTOR_OF_DEVIATION );
        }

        return SUCCESS;
    }
    
}
