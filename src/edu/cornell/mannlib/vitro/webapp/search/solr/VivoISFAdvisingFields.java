/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ContextNodeFields;

/**
 * DocumentModifier for adding rdfs:labels of individuals related via
 * a advising relationship.
 * 
 * @author bdc34
 *
 */
public class VivoISFAdvisingFields extends ContextNodeFields {

    private static String VIVONS = "http://vivoweb.org/ontology/core#";
        
    protected static final String prefix =               
            " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"
          + " prefix core: <" + VIVONS + ">  \n"
          + " prefix foaf: <http://xmlns.com/foaf/0.1/> \n"
          + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" 
          + " prefix obo: <http://purl.obolibrary.org/obo/> \n" ;
          
    static List<String> queries = new ArrayList<String>();
    static{
        queries.add( makeQueryForPeople() );
    }
    
    public VivoISFAdvisingFields(RDFServiceFactory rdfServiceFactory){                
        super(queries,rdfServiceFactory);        
    }
    
    /**
     * This query will get all the labels for the other
     * person in the relationship.  
     */
    private static String makeQueryForPeople(){        
        return prefix +
           "SELECT \n" +
           "(str(?result) as ?result) WHERE \n" +
           "{\n" +              
           " ?uri   core:relatedBy ?rel    . \n" +
           " ?rel   rdf:type       core:AdvisingRelationship   . \n" +
           " ?rel   core:relates   ?other  . \n" +
           " ?other rdfs:label     ?result . \n" + 
           " FILTER( ?other != ?uri ) \n" +          
           "}";         
    }
    
}
