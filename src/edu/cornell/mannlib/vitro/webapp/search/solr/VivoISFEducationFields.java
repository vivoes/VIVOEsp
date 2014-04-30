/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ContextNodeFields;

public class VivoISFEducationFields extends ContextNodeFields {
    private static String VIVONS = "http://vivoweb.org/ontology/core#";
    
    protected static final String prefix =               
            " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"
          + " prefix core: <" + VIVONS + ">  \n"
          + " prefix foaf: <http://xmlns.com/foaf/0.1/> \n"
          + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" 
          + " prefix obo: <http://purl.obolibrary.org/obo/> \n" ;
    
    public VivoISFEducationFields(RDFServiceFactory rdfServiceFactory){                
        super(queries,rdfServiceFactory);        
    }
    
    /**
     * This query will get all the labels for the degree.
     */
    private static String queryForDegree =        
           prefix +
           "SELECT \n" +
           "(str(?result) as ?result) WHERE \n" +
           "{\n" +              
           " ?uri   core:relates ?deg    . \n" +
           " ?deg   rdf:type     core:AwardedDegree . \n" +           
           " ?deg   rdfs:label   ?result . \n" +           
           "}";            
    
    /**
     * This will get all labels for the organization. 
     */
    private static String queryForOrganization =        
           prefix +
           "SELECT \n" +
           "(str(?result) as ?result) WHERE \n" +
           "{\n" +              
           " ?uri   core:relates ?deg    . \n" +
           " ?deg   rdf:type     core:AwardedDegree . \n" +
           " ?deg   core:assignedBy ?org . \n" +
           " ?org   rdfs:label   ?result . \n" +           
           "}";
    
    static List<String> queries = new ArrayList<String>();
    
    static{
        queries.add( queryForDegree );
        queries.add( queryForOrganization );
    }
}
