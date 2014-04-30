/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ContextNodeFields;

/*
 * This DocumentModifier is for the ISF style grants.
 * It will 
 *   add people's names to the grant's Solr Document
 *   add the grant's name to the people's Solr Document
 *   add the grant's name to the Organization's Solr Document
 *   add the organization's name to the grant's Solr Document
 *   add the grant's names to the project's Solr Document
 *   add the people's names to the project's Solr Document
 *   add the project's name to the grant's Solr Document
 *   add the project's name to the people's Solr Document
 */
public class VivoISFGrantFields extends ContextNodeFields {
 private static String VIVONS = "http://vivoweb.org/ontology/core#";
    
    protected static final String prefix =               
            " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"
          + " prefix core: <" + VIVONS + ">  \n"
          + " prefix foaf: <http://xmlns.com/foaf/0.1/> \n"
          + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" 
          + " prefix obo: <http://purl.obolibrary.org/obo/> \n" ;
    
    public VivoISFGrantFields(RDFServiceFactory rdfServiceFactory){                
        super(queries,rdfServiceFactory);        
    }
    
    /**
     * Query to add people's names to the grant's Solr Document.
     * ?uri is the URI of a grant.
     */
    private static String  peopleForGrant =        
           prefix +
           "SELECT (str(?result) as ?result) WHERE {\n" +
           "  ?uri    rdf:type     core:Grant . \n" +
           "  ?uri    core:relates ?person . \n" +            
           "  ?person rdf:type     foaf:Person . \n" +           
           "  ?person rdfs:label   ?result . \n" +           
           "}";

    /**
     * Query to add the grant's name to the people's Solr Document.
     * ?uri is the URI of a person.
     */
    private static String  grantsForPerson =        
           prefix +
           "SELECT \n" +
           "(str(?result) as ?result) WHERE \n" +
           "{\n" +
           " ?uri     rdf:type     foaf:Person . \n" +
           " ?grant   core:relates ?uri        . \n" +
           " ?grant   rdf:type     core:Grant  . \n" + 
           " ?grant   rdfs:label   ?result     . \n" +
           "}";

    /**
     * Query to add the grant's name to the Organization's Solr Document.
     * ?uri is the URI of an Organization.
     */
    private static String  grantsForOrganization =        
           prefix +
           "SELECT (str(?result) as ?result) WHERE {\n" +              
           " ?uri   rdf:type     foaf:Organization . \n" +
           " ?grant core:relates ?uri              . \n" +
           " ?grant rdf:type     core:Grant        . \n" +           
           " ?grant rdfs:label   ?result           . \n" +           
           "}";

    /**
     * Query to add the organization's name to the grant's Solr Document.
     * ?uri is the URI of a grant.
     */
    private static String  organizationsForGrant =
           prefix +
           "SELECT (str(?result) as ?result) WHERE {\n" +                   
           "  ?uri  rdf:type     core:Grant . \n" + 
           "  ?uri  core:relates ?org . \n" +
           "  ?org  rdf:type     foaf:Organization . \n" +
           "  ?org  rdfs:label   ?result . \n" +           
           "}";

    /**
     * Query to add the grant's names to the project's Solr Document.
     * ?uir is the URI of a Project.
     */
    private static String  grantsForProject =        
           prefix +
           "SELECT (str(?result) as ?result) WHERE {\n" +              
           " ?uri   rdf:type        core:Project . \n" +
           " ?role  obo:BFO_0000054 ?uri . \n" +
           " ?grant core:relates    ?role . \n" +
           " ?grant rdf:type        core:Grant . \n" +            
           " ?grant rdfs:label      ?result . \n" +           
           "}";

    /**
     * Query to add the people's names to the project's Solr Document.
     * ?uri is the URI of a Project.
     */
    private static String  peopleForProject =         
           prefix +
           "SELECT (str(?result) as ?result) WHERE {\n" +              
           " ?uri    rdf:type        core:Project . \n" +
           " ?role   obo:BFO_0000054 ?uri . \n" +
           " ?role   obo:RO_0000053  ?person . \n" +
           " ?person rdf:type        foaf:Person . \n" + 
           " ?person rdfs:label      ?result . \n" +           
           "}";

    /**
     * Query to add the project's name to the grant's Solr Document.
     * ?uri is the URI of a grant.
     */
    private static String  projectsForGrant =        
           prefix +
           "SELECT \n" +
           "(str(?result) as ?result) WHERE \n" +
           "{\n" +              
           " ?uri     rdf:type        core:Grant. \n" +
           " ?uri     core:relates    ?role . \n" +
           " ?role    obo:BFO_0000054 ?project . \n" +
           " ?project rdf:type        core:Project . \n" + 
           " ?project rdfs:label      ?result . \n" +                      
           "}";

    /**
     * Query to add the project's name to the people's Solr Document.
     * ?uri is the URI of a person.
     */
    private static String  projectsForPerson =        
           prefix +
           "SELECT (str(?result) as ?result) WHERE {\n" +
           " ?uri     rdf:type        foaf:Person . \n" +
           " ?uri     obo:RO_0000053  ?role . \n" +
           " ?role    obo:BFO_0000054 ?project . \n" +
           " ?project rdf:type        core:Project . \n" + 
           " ?project rdfs:label      ?result . \n" +
           "}";
        
    static List<String> queries = new ArrayList<String>();
    
    static{
        queries.add( peopleForGrant );
        queries.add( grantsForPerson );
        queries.add( grantsForOrganization );
        queries.add( organizationsForGrant );
        queries.add( grantsForProject );
        queries.add( peopleForProject );
        queries.add( projectsForGrant );
        queries.add( projectsForPerson );
    }
}
