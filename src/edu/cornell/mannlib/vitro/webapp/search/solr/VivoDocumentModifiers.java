/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.DocumentModifier;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.ExcludeBasedOnNamespace;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.SearchIndexExcluder;

public class VivoDocumentModifiers implements javax.servlet.ServletContextListener{
    
    /** 
     * Exclude from the search index individuals who's URIs start with these namespaces. 
     */
    private static final String[] INDIVIDUAL_NS_EXCLUDES={
        //bdc34: seems that there are a lot of odd OBO things in the search, exclude them
        "http://purl.obolibrary.org/obo/"
    };
    
    @SuppressWarnings("unchecked")
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        ServletContext context = sce.getServletContext();
		RDFServiceFactory rdfServiceFactory = RDFServiceUtils.getRDFServiceFactory(context);
        
        Dataset dataset = DatasetFactory.create(ModelAccess.on(context).getJenaOntModel());
        
        /* Put DocumentModifiers into servlet context for use later in startup by SolrSetup 
         * This adds the code for VIVO specific additions to the building
         * of solr Documents. */        
        List<DocumentModifier> modifiers = (List<DocumentModifier>)context.getAttribute("DocumentModifiers");
        if( modifiers == null ){                    
            modifiers = new ArrayList<DocumentModifier>();
            context.setAttribute("DocumentModifiers", modifiers);
        }
        
        modifiers.add(new CalculateParameters(dataset));   
        modifiers.add( new VIVOValuesFromVcards( rdfServiceFactory ));
        modifiers.add( new VivoISFBasicFields( rdfServiceFactory ));
        modifiers.add( new VivoISFAdvisingFields( rdfServiceFactory ));
        modifiers.add( new VivoISFEducationFields( rdfServiceFactory ));
        modifiers.add( new VivoISFGrantFields( rdfServiceFactory ));
        modifiers.add( new VivoISFMemberFields( rdfServiceFactory ));        
        modifiers.add(new VivoInformationResourceContextNodeFields(rdfServiceFactory));                
        
        /*
         * Add VIVO specific code that excludes Individuals from the search index. 
         */        
        List<SearchIndexExcluder> excludes = 
            (List<SearchIndexExcluder>)context.getAttribute("SearchIndexExcludes");
        
        if( excludes == null ){
            excludes = new ArrayList<SearchIndexExcluder>();
            context.setAttribute("SearchIndexExcludes", excludes);
        }
                
        excludes.add(new ExcludeBasedOnNamespace(INDIVIDUAL_NS_EXCLUDES ));        
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // do nothing.        
    }    
}
