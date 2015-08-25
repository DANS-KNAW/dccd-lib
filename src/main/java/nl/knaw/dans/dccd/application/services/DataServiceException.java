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
package nl.knaw.dans.dccd.application.services;

public class DataServiceException extends Exception {
	private static final long serialVersionUID = 3023533755830683795L;

    public DataServiceException()
    {
    	super();
    }

    public DataServiceException(String message)
    {
        super(message);
    }

    public DataServiceException(Throwable cause)
    {
        super(cause);
    }

    public DataServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

}