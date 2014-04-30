/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.solr;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.apache.solr.common.SolrInputDocument;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;


public class VivoAgentContextNodeFieldsTest extends AbstractTestClass{
        
        static String SPCA = "http://vivo.mydomain.edu/individual/n8087";
        
        static RDFServiceFactory rdfServiceFactory;
        
        @BeforeClass
        public static void setup(){
                Model m = ModelFactory.createDefaultModel();
                InputStream stream = VivoAgentContextNodeFieldsTest
                 .class.getResourceAsStream("./NIHVIVO3853_DataSet1.rdf");
                
                long preloadSize = m.size();
                
                m.read(stream, null);
                assertTrue("expected to load statements from file", m.size() > preloadSize );                
                                 
                assertTrue("expect statements about SPCA",
                                m.contains(ResourceFactory.createResource(SPCA),(Property) null,(RDFNode) null));
                
        RDFService rdfService = new RDFServiceModel(m);
        rdfServiceFactory = new RDFServiceFactorySingle(rdfService);
        }
        
        @Test
        public void testJane(){
                Individual ind = new IndividualImpl();
        ind.setURI(SPCA);
        
        VivoAgentContextNodeFields vacnf = new VivoAgentContextNodeFields(rdfServiceFactory);
        StringBuffer sb = vacnf.getValues( ind );
        
        assertNotNull( sb );
        String values = sb.toString();

        boolean hasJane = values.toLowerCase().indexOf("jane") > 0;
        assertTrue("expected to have jane because SPCA advises jane", hasJane);
        }
        
        @Test
        public void testWonder(){
                Individual ind = new IndividualImpl();
        ind.setURI(SPCA);
        
        VivoAgentContextNodeFields vacnf = new VivoAgentContextNodeFields(rdfServiceFactory);
        StringBuffer sb = vacnf.getValues( ind );
        
        assertNotNull( sb );
        String values = sb.toString();
        
        boolean hasWonder = values.toLowerCase().indexOf("wonders") > 0;
        assertTrue("expected to have jane because SPCA won wonders award", hasWonder);
        }
        
        @Test
        public void testChimp(){
                Individual ind = new IndividualImpl();
        ind.setURI(SPCA);
        
        VivoAgentContextNodeFields vacnf = new VivoAgentContextNodeFields(rdfServiceFactory);
        StringBuffer sb = vacnf.getValues( ind );
        
        assertNotNull( sb );
        String values = sb.toString();
                        
        boolean hasNotChimp = ! (values.toLowerCase().indexOf("chimp") > 0);
        assertTrue("expected to not have chimp because jane won chimp award, not SPCA", hasNotChimp);
        }
}