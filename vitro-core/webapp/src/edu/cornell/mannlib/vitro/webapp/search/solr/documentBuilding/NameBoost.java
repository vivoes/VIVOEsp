/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

public class NameBoost implements DocumentModifier {

    /** 
     * These are the fields in the solr Document that
     * are related to the name.  If you modify the schema,
     * please consider if you need to change this list
     * of name fields to boost. 
     */
    static final VitroSearchTermNames term = new VitroSearchTermNames();
    String[] fieldsToBoost = {term.NAME_RAW,term.NAME_LOWERCASE,term.NAME_UNSTEMMED,term.NAME_STEMMED};
    
    
    final float boost;
    
    public NameBoost(float boost){
        this.boost = boost;
    }
    
    @Override
    public void modifyDocument(Individual individual, SolrInputDocument doc,
            StringBuffer addUri) {
        
        for( String fieldName : fieldsToBoost){
            SolrInputField field = doc.getField(fieldName);
            if( field != null ){                                
                field.setBoost(field.getBoost() + boost);
            }
        }        
    }

    @Override
    public void shutdown() {
        // do nothing.
    }

}
