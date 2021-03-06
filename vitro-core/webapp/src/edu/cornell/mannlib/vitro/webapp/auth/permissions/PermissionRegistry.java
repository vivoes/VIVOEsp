/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * An immutable collection of Permission objects, keyed by URI. Resides in the
 * ServletContext.
 * 
 * This is not thread-safe, so all Permissions should be added during context
 * initialization.
 */
public class PermissionRegistry {
	private static final Log log = LogFactory.getLog(PermissionRegistry.class);

	private static final String ATTRIBUTE_NAME = PermissionRegistry.class
			.getName();

	/**
	 * Has the registry been created yet?
	 */
	public static boolean isRegistryCreated(ServletContext ctx) {
		return ctx.getAttribute(ATTRIBUTE_NAME) instanceof PermissionRegistry;
	}

	/**
	 * Create the registry and store it in the context.
	 */
	public static void createRegistry(ServletContext ctx,
			Collection<? extends Permission> permissions) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}
		if (permissions == null) {
			throw new NullPointerException("permissions may not be null.");
		}
		if (ctx.getAttribute(ATTRIBUTE_NAME) != null) {
			throw new IllegalStateException(
					"PermissionRegistry has already been set.");
		}

		PermissionRegistry registry = new PermissionRegistry(permissions);
		ctx.setAttribute(ATTRIBUTE_NAME, registry);
	}

	/**
	 * Get the registry from the context. If there isn't one, throw an
	 * exception.
	 */
	public static PermissionRegistry getRegistry(ServletContext ctx) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}

		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o == null) {
			throw new IllegalStateException(
					"PermissionRegistry has not been set.");
		} else if (!(o instanceof PermissionRegistry)) {
			throw new IllegalStateException("PermissionRegistry was set to an "
					+ "invalid object: " + o);
		}

		return (PermissionRegistry) o;
	}

	private final Map<String, Permission> permissionsMap;

	public PermissionRegistry(Collection<? extends Permission> permissions) {
		Map<String, Permission> map = new HashMap<String, Permission>();
		for (Permission p : permissions) {
			String uri = p.getUri();
			if (map.containsKey(uri)) {
				throw new IllegalStateException("A Permission is already "
						+ "registered with this URI: '" + uri + "'.");
			}
			map.put(uri, p);
		}
		this.permissionsMap = Collections.unmodifiableMap(map);
	}

	/**
	 * Is there a Permission registered with this URI?
	 */
	public boolean isPermission(String uri) {
		return permissionsMap.containsKey(uri);
	}

	/**
	 * Get the permission that is registered with this URI. If there is no such
	 * Permission, return a BrokenPermission that always denies authorization.
	 * 
	 * If you want to know whether an actual Permission has been registered at
	 * this URI, call isPermission() instead.
	 */
	public Permission getPermission(String uri) {
		Permission p = permissionsMap.get(uri);
		if (p == null) {
			log.warn("No Permission is registered for '" + uri + "'");
			return new BrokenPermission(uri);
		}

		return p;
	}

	// ----------------------------------------------------------------------
	// Setup class
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);
			try {
				List<Permission> permissions = new ArrayList<Permission>();

				permissions.addAll(SimplePermission.getAllInstances());
				permissions.addAll(createDisplayByRolePermissions(ctx));
				permissions.addAll(createEditByRolePermissions(ctx));
				permissions.addAll(createPublishByRolePermissions(ctx));

				PermissionRegistry.createRegistry(ctx, permissions);

				ss.info(this, "Created the PermissionRegistry with "
						+ permissions.size() + " permissions.");
			} catch (Exception e) {
				ss.fatal(this, "Failed to initialize the PermissionRegistry.",
						e);
			}
		}

		/**
		 * There is no DisplayByRolePermission for self-editors. They get the
		 * same rights as PUBLIC. Other permissions give them their self-editing
		 * privileges.
		 */
		private Collection<Permission> createDisplayByRolePermissions(
				ServletContext ctx) {
			List<Permission> list = new ArrayList<Permission>();
			list.add(new DisplayByRolePermission("Admin", RoleLevel.DB_ADMIN,
					ctx));
			list.add(new DisplayByRolePermission("Curator", RoleLevel.CURATOR,
					ctx));
			list.add(new DisplayByRolePermission("Editor", RoleLevel.EDITOR,
					ctx));
			list.add(new DisplayByRolePermission("Public", RoleLevel.PUBLIC,
					ctx));
			return list;
		}

		/**
		 * There is no EditByRolePermission for PUBLIC or for self-editors. A
		 * property may be given an edit-level of "PUBLIC", but that may also
		 * simply be the default assigned to it when editing, and we don't want
		 * to recognize that.
		 * 
		 * Other permissions give self-editors their editing privileges.
		 */
		private Collection<Permission> createEditByRolePermissions(
				ServletContext ctx) {
			List<Permission> list = new ArrayList<Permission>();
			list.add(new EditByRolePermission("Admin", RoleLevel.DB_ADMIN, ctx));
			list.add(new EditByRolePermission("Curator", RoleLevel.CURATOR, ctx));
			list.add(new EditByRolePermission("Editor", RoleLevel.EDITOR, ctx));
			return list;
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			sce.getServletContext().removeAttribute(ATTRIBUTE_NAME);
		}

		/**
		 * There is no PublishByRolePermission for self-editors. They get the
		 * same rights as PUBLIC. Other permissions give them their self-editing
		 * privileges.
		 */
		private Collection<Permission> createPublishByRolePermissions(
				ServletContext ctx) {
			List<Permission> list = new ArrayList<Permission>();
			list.add(new PublishByRolePermission("Admin", RoleLevel.DB_ADMIN,
					ctx));
			list.add(new PublishByRolePermission("Curator", RoleLevel.CURATOR,
					ctx));
			list.add(new PublishByRolePermission("Editor", RoleLevel.EDITOR,
					ctx));
			list.add(new PublishByRolePermission("Public", RoleLevel.PUBLIC,
					ctx));
			return list;
		}
	}
}
