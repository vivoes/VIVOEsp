/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.utils.fields.FieldUtils;

public class IndividualsViaObjectPropetyOptions implements FieldOptions {
    
    private static final Log log = LogFactory.getLog(IndividualsViaObjectPropetyOptions.class);
    
    private static final String LEFT_BLANK = "";
    private String subjectUri;
    private String predicateUri;    
    private List<VClass> rangeTypes;
    private String objectUri;
    
    private String defaultOptionLabel;
    
    public IndividualsViaObjectPropetyOptions(String subjectUri,
            String predicateUri, List<VClass> rangeTypes, String objectUri) throws Exception {
        super();
        
        if (subjectUri == null || subjectUri.equals("")) {
            throw new Exception("no subjectUri found for field ");
        }
        if (predicateUri == null || predicateUri.equals("")) {
            throw new Exception("no predicateUri found for field ");                    
        } 

        this.subjectUri = subjectUri;
        this.predicateUri = predicateUri;
        this.rangeTypes = rangeTypes;
        this.objectUri = objectUri;
    }
    
    public IndividualsViaObjectPropetyOptions(String subjectUri,
            String predicateUri, String objectUri) throws Exception {
        this (subjectUri, predicateUri, null, objectUri);
    }

    public IndividualsViaObjectPropetyOptions setDefaultOptionLabel(String label){
        this.defaultOptionLabel = label;
        return this;
    }
    
    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) {
        HashMap<String, String> optionsMap = new LinkedHashMap<String, String>();
        int optionsCount = 0;
 
        // first test to see whether there's a default "leave blank"
        // value specified with the literal options                
        if ((defaultOptionLabel ) != null) {
            optionsMap.put(LEFT_BLANK, defaultOptionLabel);
        }
        
        Individual subject = wDaoFact.getIndividualDao().getIndividualByURI(subjectUri);                    

        //get all vclasses applicable to the individual subject
        HashSet<String> vclassesURIs = getApplicableVClassURIs(subject, wDaoFact);
        
        if (!rangeTypes.isEmpty()) {
            vclassesURIs = filterToSubclassesOfRange(vclassesURIs, rangeTypes, wDaoFact);
        }
                
        if (vclassesURIs.size() == 0) {           
            return optionsMap;
        }
        
        List<Individual> individuals = new ArrayList<Individual>();
        HashSet<String> uriSet = new HashSet<String>();        
        for (String vclassURI: vclassesURIs) {
            List<Individual> inds = wDaoFact.getIndividualDao().getIndividualsByVClassURI(vclassURI, -1, -1);
            for (Individual ind : inds) {
                if (!uriSet.contains(ind.getURI())) {
                    uriSet.add(ind.getURI());
                    individuals.add(ind);
                }
            }
        }

        List<ObjectPropertyStatement> stmts = subject.getObjectPropertyStatements();

        individuals = FieldUtils.removeIndividualsAlreadyInRange(
                individuals, stmts, predicateUri, objectUri);
        // Collections.sort(individuals,new compareIndividualsByName());a

        for (Individual ind : individuals) {
            String uri = ind.getURI();
            if (uri != null) {
                optionsMap.put(uri, ind.getName().trim());
                ++optionsCount;
            }
        }

        return optionsMap;
    }

    private HashSet<String> getApplicableVClassURIs(Individual subject, WebappDaoFactory wDaoFact) {
        HashSet<String> vclassesURIs = new HashSet<String>();
        if (!rangeTypes.isEmpty()) {
            StringBuffer rangeBuff = new StringBuffer();
            for (VClass rangeType : rangeTypes) {
                vclassesURIs.add(rangeType.getURI());
                rangeBuff.append(rangeType.getURI()).append(", ");
            }
            log.debug("individualsViaObjectProperty using rangeUri " + rangeBuff.toString());            
            return vclassesURIs;
        } 
        
        log.debug("individualsViaObjectProperty not using any rangeUri");
        
        List<VClass> subjectVClasses = subject.getVClasses();
        
        //using hashset to prevent duplicates
        
        //Get the range vclasses applicable for the property and each vclass for the subject
        for(VClass subjectVClass: subjectVClasses) {
            List<VClass> vclasses = wDaoFact.getVClassDao().getVClassesForProperty(subjectVClass.getURI(), predicateUri);
            //add range vclass to hash
            if(vclasses != null) {
                for(VClass v: vclasses) {
                    vclassesURIs.add(v.getURI());
                }
            }
        }
        
        return vclassesURIs;
    }
    
    private HashSet<String> filterToSubclassesOfRange(HashSet<String> vclassesURIs, 
                                              List<VClass> rangeTypes, 
                                              WebappDaoFactory wDaoFact) {
        HashSet<String> filteredVClassesURIs = new HashSet<String>();
        VClassDao vcDao = wDaoFact.getVClassDao();
        for (String vclass : vclassesURIs) {
            for (VClass rangeType : rangeTypes) {
                if (vclass.equals(rangeType.getURI()) || vcDao.isSubClassOf(vclass, rangeType.getURI())) {
                    filteredVClassesURIs.add(vclass);
                }
            }
        }
        return filteredVClassesURIs;
    }
    
   
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }
    
    
}
