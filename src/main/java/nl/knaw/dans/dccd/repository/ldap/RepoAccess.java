/*******************************************************************************
 * Copyright 2015 DANS - Data Archiving and Networked Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package nl.knaw.dans.dccd.repository.ldap;

import nl.knaw.dans.dccd.model.InternalErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain classes sometimes need access to store or repository data access points, in order to lazily create attributes.
 * For instance: a User wants to instantiate the Groups it belongs to, a Dataset wants to instantiate the user that is
 * the depositor of the dataset.
 * <p/>
 * The overall logic of business processes remains situated in the business layer so the RepoAccessDelegator should be
 * confined to simple getter-methods like 'getUser', 'getGroups', etc.
 *
 * @author ecco Nov 19, 2009
 */
public final class RepoAccess
{

    private static final Logger        logger = LoggerFactory.getLogger(RepoAccess.class);

    private static RepoAccessDelegator DELEGATOR;

    private RepoAccess()
    {
        // never instantiate
    }

    /**
     * Set the delegate for repository access
     *
     * @param delegator
     *        the {@link RepoAccessDelegator}
     */
    public static void setDelegator(RepoAccessDelegator delegator)
    {
        DELEGATOR = delegator;
    }

    /**
     * Get the delegate for repository access.
     *
     * @return the {@link RepoAccessDelegator}
     * @throws ApplicationException
     *         if no delegate was set on {@link RepoAccess}
     */
    public static RepoAccessDelegator getDelegator()
    {
        if (DELEGATOR == null)
        {
            String msg = "No delegator set on " + RepoAccess.class.getName();
            logger.error(msg);

            //throw new ApplicationException(msg);
            throw new InternalErrorException(msg);
        }
        return DELEGATOR;
    }

}
