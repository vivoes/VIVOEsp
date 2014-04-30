/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import static edu.cornell.mannlib.vitro.webapp.controller.VitroRequest.SPECIAL_WRITE_MODEL;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.SWITCH_TO_DISPLAY_MODEL;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.USE_DISPLAY_MODEL_PARAM;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.USE_MODEL_PARAM;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.USE_TBOX_MODEL_PARAM;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

/**
 * Handle model switching, if requested for the editing framework.
 */
public class ModelSwitcher {
	private static final Log log = LogFactory.getLog(ModelSwitcher.class);

	/**
	 * Are they authorized for whatever models they are asking for? 
	 */
	public static boolean authorizedForSpecialModel(HttpServletRequest req) {
		if (isParameterPresent(req, SWITCH_TO_DISPLAY_MODEL)) {
			return PolicyHelper.isAuthorizedForActions(req, SimplePermission.MANAGE_MENUS.ACTIONS);
		} else if (anyOtherSpecialProperties(req)){
			return PolicyHelper.isAuthorizedForActions(req, SimplePermission.ACCESS_SPECIAL_DATA_MODELS.ACTIONS);
		} else {
			return true;
		}
	}

	private static boolean anyOtherSpecialProperties(HttpServletRequest req) {
		return isParameterPresent(req, USE_MODEL_PARAM)
				|| isParameterPresent(req, USE_TBOX_MODEL_PARAM)
				|| isParameterPresent(req, USE_DISPLAY_MODEL_PARAM);
	}

	private static boolean isParameterPresent(HttpServletRequest req, String key) {
		return StringUtils.isNotEmpty(req.getParameter(key)); 
	}
	
	/**
	 * Check if special model is requested - this is for enabling the use of a different
	 * model for menu management. Also enables the use of a completely different
	 * model and tbox if uris are passed.
	 */
    public WebappDaoFactory checkForModelSwitching(VitroRequest vreq, WebappDaoFactory inputWadf) {
    	ServletContext _context = vreq.getSession().getServletContext();
        //TODO: Does the dataset in the vreq get set when using a special WDF? Does it need to?
        //TODO: Does the unfiltered WDF get set when using a special WDF? Does it need to?
        
    	// If this isn't a Jena WADF, then there's nothing to be done.
    	if (!(inputWadf instanceof WebappDaoFactoryJena)) {
    		log.warn("Can't set special models: " +
    				"WebappDaoFactory is not a WebappDaoFactoryJena");
        	removeSpecialWriteModel(vreq);
    		return inputWadf;
    	}

    	WebappDaoFactoryJena wadf = (WebappDaoFactoryJena) inputWadf;
    	
    	// If they asked for the display model, give it to them.
		if (isParameterPresent(vreq, SWITCH_TO_DISPLAY_MODEL)) {
			OntModel mainOntModel = ModelAccess.on(_context).getDisplayModel();
			OntModel tboxOntModel = ModelAccess.on(_context).getOntModel(ModelID.DISPLAY_TBOX);
	   		setSpecialWriteModel(vreq, mainOntModel);
	   		
	   		vreq.setAttribute(VitroRequest.ID_FOR_ABOX_MODEL, VitroModelSource.ModelName.DISPLAY.toString());
	   		vreq.setAttribute(VitroRequest.ID_FOR_TBOX_MODEL, VitroModelSource.ModelName.DISPLAY_TBOX.toString());
	   		vreq.setAttribute(VitroRequest.ID_FOR_DISPLAY_MODEL, VitroModelSource.ModelName.DISPLAY_DISPLAY.toString());
	   		vreq.setAttribute(VitroRequest.ID_FOR_WRITE_MODEL, VitroModelSource.ModelName.DISPLAY.toString());
	   		
			return createNewWebappDaoFactory(wadf, mainOntModel, tboxOntModel, null);
		}
    	
		// If they asked for other models by URI, set them.
		if (anyOtherSpecialProperties(vreq)) {
			DataSource bds = JenaDataSourceSetupBase.getApplicationDataSource(_context);
			String dbType = ConfigurationProperties.getBean(_context)
					.getProperty("VitroConnection.DataSource.dbtype", "MySQL");

	    	OntModel mainOntModel = createSpecialModel(vreq, USE_MODEL_PARAM, bds, dbType);	    	
	    	OntModel tboxOntModel = createSpecialModel(vreq, USE_TBOX_MODEL_PARAM, bds, dbType);
	    	OntModel displayOntModel = createSpecialModel(vreq, USE_DISPLAY_MODEL_PARAM, bds, dbType);
	    	
	    	vreq.setAttribute(VitroRequest.ID_FOR_ABOX_MODEL, vreq.getParameter(USE_MODEL_PARAM));
	    	vreq.setAttribute(VitroRequest.ID_FOR_WRITE_MODEL, vreq.getParameter(USE_MODEL_PARAM));
            vreq.setAttribute(VitroRequest.ID_FOR_TBOX_MODEL, vreq.getParameter(USE_TBOX_MODEL_PARAM));
            vreq.setAttribute(VitroRequest.ID_FOR_DISPLAY_MODEL, vreq.getParameter(USE_DISPLAY_MODEL_PARAM));            
            
	   		setSpecialWriteModel(vreq, mainOntModel);
	    	return createNewWebappDaoFactory(wadf, mainOntModel, tboxOntModel, displayOntModel);
		}
		
		// Otherwise, there's nothing special about this request.
    	removeSpecialWriteModel(vreq);
		return wadf;

    }

	private void setSpecialWriteModel(VitroRequest vreq, OntModel mainOntModel) {	    
		if (mainOntModel != null) {    
			ModelAccess.on(vreq).setJenaOntModel(mainOntModel);
			vreq.setAttribute(SPECIAL_WRITE_MODEL, mainOntModel);
		}
	}

	private void removeSpecialWriteModel(VitroRequest vreq) {
		if (vreq.getAttribute(SPECIAL_WRITE_MODEL) != null) {
			vreq.removeAttribute(SPECIAL_WRITE_MODEL);
		}
	}
	
	/**
	 * The goal here is to return a new WDF that is set to
	 * have the mainOntModel as its ABox, the tboxOntModel as it
	 * TBox and displayOntModel as it display model.
	 * 
	 * Right now this is achieved by creating a copy of 
	 * the WADF, and setting the special models onto it.
	 *  
	 * If a model is null, it will have no effect.
	 */
	private WebappDaoFactory createNewWebappDaoFactory(
			WebappDaoFactoryJena inputWadf, OntModel mainOntModel,
			OntModel tboxOntModel, OntModel displayOntModel) {
	    
		WebappDaoFactoryJena wadfj = new WebappDaoFactoryJena(inputWadf);
		wadfj.setSpecialDataModel(mainOntModel, tboxOntModel, displayOntModel);
		return wadfj;
	}

	/**
	 * If the request asks for a special model by URI, create it from the
	 * Database.
	 * 
	 * @return the model they asked for, or null if they didn't ask for one.
	 * @throws IllegalStateException
	 *             if it's not found.
	 */
	private OntModel createSpecialModel(VitroRequest vreq, String key,
			DataSource bds, String dbType) {
		if (!isParameterPresent(vreq, key)) {
			return null;
		}
		
		String modelUri = vreq.getParameter(key);
		Model model = JenaDataSourceSetupBase.makeDBModel(bds, modelUri,
				OntModelSpec.OWL_MEM,
				JenaDataSourceSetupBase.TripleStoreType.RDB, dbType, vreq.getSession().getServletContext());
		if (model != null) {
			return ModelFactory
					.createOntologyModel(OntModelSpec.OWL_MEM, model);
		} else {
			throw new IllegalStateException("Main Model Uri " + modelUri
					+ " did not retrieve model");
		}
	}

}
