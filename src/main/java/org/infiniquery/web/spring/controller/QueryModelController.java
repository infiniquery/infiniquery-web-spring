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

import javax.annotation.PostConstruct;

import org.infiniquery.model.ExecutableQuery;
import org.infiniquery.model.view.PossibleValuesView;
import org.infiniquery.model.view.QueryResultsView;
import org.infiniquery.service.DatabaseAccessService;
import org.infiniquery.service.DefaultDatabaseAccessService;
import org.infiniquery.service.DefaultQueryModelService;
import org.infiniquery.service.QueryModelService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/queryModel")
public class QueryModelController {
	
	@Autowired
	private ObjectMapper jacksonObjectMapper;

    private QueryModelService queryModelService;
    
    private ApplicationContext applicationContext;

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

    public QueryModelService getQueryModelService() {
		return queryModelService;
	}

	public void setQueryModelService(QueryModelService queryModelService) {
		this.queryModelService = queryModelService;
	}

    @RequestMapping(value = "/findKeyword", method= RequestMethod.GET)
    @ResponseBody
    public String getFindKeyword() throws Exception {
        return queryModelService.getFindKeyword();
    }

	@RequestMapping(value = "/entities", method = RequestMethod.GET)
    @ResponseBody
    public String[] getEntityNames() throws Exception {
        return queryModelService.getEntityDisplayNames().toArray(new String[0]);
    }

    @RequestMapping(value = "/entityAttributes/{entityDisplayName}", method = RequestMethod.GET)
    @ResponseBody
    public String[] getEntityAttributeNames(@PathVariable final String entityDisplayName) throws Exception {
        return queryModelService.getEntityAttributeDisplayNames(entityDisplayName).toArray(new String[0]);
    }

    @RequestMapping(value = "/entityAttributeOperators/{entityDisplayName}/{attributeDisplayName}", method = RequestMethod.GET)
    @ResponseBody
    public String[] getEntityAttributeOperatorNames(@PathVariable final String entityDisplayName, @PathVariable final String attributeDisplayName) throws Exception {
        return queryModelService.getEntityAttributeOperatorNames(entityDisplayName, attributeDisplayName);
    }

    @RequestMapping(value = "/entityAttributeOperatorValue/{entityDisplayName}/{attributeDisplayName}/{operatorDisplayName}", method= RequestMethod.GET)
    @ResponseBody
    public PossibleValuesView getEntityAttributeOperatorValue(@PathVariable final String entityDisplayName, @PathVariable final String attributeDisplayName, @PathVariable final String operatorDisplayName) throws Exception {
        return queryModelService.getEntityAttributeOperatorValue(entityDisplayName, attributeDisplayName, operatorDisplayName);
    }

    @RequestMapping(value = "/conditionSeparatorValues", method= RequestMethod.GET)
    @ResponseBody
    public String[] getConditionSeparatorValues() throws Exception {
        return queryModelService.getConditionSeparatorNames();
    }

    @RequestMapping(value = "/executeQuery", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public QueryResultsView executeQuery(@RequestBody final ExecutableQuery executableQuery) throws Exception {

    	QueryResultsView result = queryModelService.executeQuery(executableQuery);
    	return result;
    }

    @RequestMapping(value = "/reloadQueryContext", method = RequestMethod.POST)
    public void reloadQueryContext() {
        queryModelService.reloadQueryContext();
    }

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
