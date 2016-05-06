/*
* Copyright (c) 2015, Daniel Doboga
* All rights reserved.
* 	
* Redistribution and use in source and binary forms, with or without modification, 
* are permitted provided that the following conditions are met:
* 
*   1. Redistributions of source code must retain the above copyright notice, this 
*   list of conditions and the following disclaimer.
*   
*   2. Redistributions in binary form must reproduce the above copyright notice, this 
*   list of conditions and the following disclaimer in the documentation and/or other 
*   materials provided with the distribution.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
* IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
* NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
* POSSIBILITY OF SUCH DAMAGE.
*/

package org.infiniquery.web.spring.controller;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.infiniquery.model.ExecutableQuery;
import org.infiniquery.model.view.PossibleValuesView;
import org.infiniquery.model.view.QueryResultsView;
import org.infiniquery.service.DatabaseAccessService;
import org.infiniquery.service.DefaultDatabaseAccessService;
import org.infiniquery.service.DefaultQueryModelService;
import org.infiniquery.service.QueryModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST controller to serve the communication between the user interface and the infiniquery-core framework.
 * @author Daniel Doboga
 * @since 1.0.0
 */
@Controller
@RequestMapping("/queryModel")
public class QueryModelController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryModelController.class);
	
	//don't start from zero, to avoid giving eventual hints to end users (like how many crashes have been already).
	private static final int SOME_RANDOM_INTEGER_WITH_NO_SIGNIFICANCE = 456782;
	private static final AtomicInteger errorIndexId = new AtomicInteger(SOME_RANDOM_INTEGER_WITH_NO_SIGNIFICANCE);
	
	@Autowired
	private ObjectMapper jacksonObjectMapper;

    private QueryModelService queryModelService;

    @PostConstruct
    private void init() {
        if(queryModelService == null) {
            DefaultQueryModelService defaultQueryModelService = new DefaultQueryModelService();
            DatabaseAccessService databaseAccessService = new DefaultDatabaseAccessService();
            defaultQueryModelService.setDatabaseAccessService(databaseAccessService);
            queryModelService = defaultQueryModelService;
        }
        jacksonObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    /**
     * Offer the queryModelService to other beans in the context, in the case it is lazy instantiated during init.
     * @return queryModelService
     */
    public QueryModelService getQueryModelService() {
		return queryModelService;
	}

    /**
     * 
     * @param queryModelService the QueryModelService
     */
	public void setQueryModelService(QueryModelService queryModelService) {
		this.queryModelService = queryModelService;
	}

	/**
	 * 
	 * @return the alias of the "SELECT" keyword to be displayed in UI
	 */
    @RequestMapping(value = "/findKeyword", method= RequestMethod.GET)
    @ResponseBody
    public String getFindKeyword() {
        return queryModelService.getFindKeyword();
    }

    /**
     * 
     * @return String[] containing all the entity aliases that are available for the UI
     */
	@RequestMapping(value = "/entities", method = RequestMethod.GET)
    @ResponseBody
    public String[] getEntityNames() {
        return queryModelService.getEntityDisplayNames().toArray(new String[0]);
    }

	/**
	 * 
	 * @param entityDisplayName the display name (or configured alias) of the entity whose attributes are requested.
	 * @return String[] containing all attribute aliases to expose in UI for the given entity
	 */
    @RequestMapping(value = "/entityAttributes/{entityDisplayName}", method = RequestMethod.GET)
    @ResponseBody
    public String[] getEntityAttributeNames(@PathVariable final String entityDisplayName) {
        return queryModelService.getEntityAttributeDisplayNames(entityDisplayName).toArray(new String[0]);
    }

    /**
     * Get the aliases of the operators applicable for the given attribute of the given entity.
     * @param entityDisplayName the display name (or configured alias) of the entity.
     * @param attributeDisplayName the display name (or configured alias) of the entity attribute.
     * @return String[] representing the aliases to display in UI for the entity attribute operators
     * that are applicable to the given attribute of the given entity.
     */
    @RequestMapping(value = "/entityAttributeOperators/{entityDisplayName}/{attributeDisplayName}", method = RequestMethod.GET)
    @ResponseBody
    public String[] getEntityAttributeOperatorNames(@PathVariable final String entityDisplayName, @PathVariable final String attributeDisplayName) {
        return queryModelService.getEntityAttributeOperatorNames(entityDisplayName, attributeDisplayName);
    }

    /**
     * Get the possible values to suggest for a specific attribute of an entity in combination with a specific operator.
     * @param entityDisplayName the display name (or configured alias) of the entity.
     * @param attributeDisplayName the display name (or configured alias) of the entity attribute.
     * @param operatorDisplayName the display name of the operator.
     * @return PossibleValuesView encapsulating the information necessary to determine the type for dynamically creating the input control type for entering a value
     */
    @RequestMapping(value = "/entityAttributeOperatorValue/{entityDisplayName}/{attributeDisplayName}/{operatorDisplayName}", method= RequestMethod.GET)
    @ResponseBody
    public PossibleValuesView getEntityAttributeOperatorValue(@PathVariable final String entityDisplayName, @PathVariable final String attributeDisplayName, @PathVariable final String operatorDisplayName) {
        return queryModelService.getEntityAttributeOperatorValue(entityDisplayName, attributeDisplayName, operatorDisplayName);
    }

    /**
     * 
     * @return String[] representing the displayable aliases of the condition separators (such as OR and AND).
     */
    @RequestMapping(value = "/conditionSeparatorValues", method= RequestMethod.GET)
    @ResponseBody
    public String[] getConditionSeparatorValues() {
        return queryModelService.getConditionSeparatorNames();
    }

    /**
     * 
     * @param executableQuery the {@link ExecutableQuery} encapsulating the logical query to be executed.
     * @return QueryResultsView a bean encapsulating the results of a query execution
     */
    @RequestMapping(value = "/executeQuery", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public QueryResultsView executeQuery(@RequestBody final ExecutableQuery executableQuery) {

    	QueryResultsView result = queryModelService.executeQuery(executableQuery);
    	return result;
    }

//    //TODO comment this method, or secure it properly; it's for debug only
//    @RequestMapping(value = "/compileQuery", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public String compileQuery(@RequestBody final ExecutableQuery executableQuery) throws Exception {
//
//    	String result = queryModelService.compileQuery(executableQuery);
//    	return result;
//    }

    /**
     * Reload the query context (which is the equivalent of clearing the cache). 
     * This makes possible to load eventual changes in infiniquery-config.xml, without restarting the application.
     */
    @RequestMapping(value = "/reloadQueryContext", method = RequestMethod.POST)
    public void reloadQueryContext() {
        queryModelService.reloadQueryContext();
    }

    /**
     * Exception handler for any kind of Throwable
     * @param t the Throwable to treat.
     * @return ResponseEntity&lt;String&gt; representing the message to pass to the UI, when an error (or exception) occurs.
     */
	@ExceptionHandler(Throwable.class)
	public ResponseEntity<String> genericExceptionHandler(Throwable t) {
		int errorId = errorIndexId.getAndIncrement();
		LOGGER.error("(Error ID " + errorId + ") " + t.getMessage(), t);
		String errorMessage = "An internal server error occurred. The error id is " + errorId + ". Please contact the maintenance team and provide them this number for reference.";
		return new ResponseEntity<String>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
