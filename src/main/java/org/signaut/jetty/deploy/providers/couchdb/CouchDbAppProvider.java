package org.signaut.jetty.deploy.providers.couchdb;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.Authenticator.Factory;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import org.signaut.jetty.deploy.providers.couchdb.CouchDbDocumentCallback.WebAppDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDbAppProvider extends AbstractLifeCycle implements AppProvider {

    public interface SessionManagerProvider {
        /**
         * Create a new instance of a SessionManager
         * 
         * @return
         */
        SessionManager get();
    }

    private DeploymentManager deploymentManager;
    private final CouchDeployerProperties couchDeployerProperties;
    private final Authenticator.Factory authenticatorFactory;
    private final SessionManagerProvider sessionManagerProvider;
    private final File tempDirectory;
    private final CouchDbClient couchDbClient;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicLong sequence = new AtomicLong();

    private String serverClasses[] = { "com.google.inject." };
    private String systemClasses[] = { "org.slf4j." };

    public CouchDbAppProvider(CouchDeployerProperties couchDeployerProperties, Factory authenticatorFactory,
                              File tempDirectory, SessionManagerProvider sessionManagerProvider) {
        this.couchDeployerProperties = couchDeployerProperties;
        this.authenticatorFactory = authenticatorFactory;
        this.tempDirectory = tempDirectory;
        this.sessionManagerProvider = sessionManagerProvider;
        couchDbClient = new CouchDbClient(couchDeployerProperties.getDatabaseUrl(), couchDeployerProperties.getUsername(),
                couchDeployerProperties.getPassword());
    }

    @Override
    protected void doStart() throws Exception {
        final CouchChangesAppCallback appCallback = new CouchChangesAppCallback(deploymentManager, this);
        while (isRunning()) {
            couchDbClient.dispatchChanges(couchDeployerProperties.getDesignDocument(), couchDeployerProperties.getFilter(),
                                          sequence.get(), appCallback);
        }
    }

    /**
     * Set the latest couchdb sequence. Used in the event the connection between
     * this and couchdb is broken. If we did not have the latest sequence, all
     * apps would be redeployed, and we don't want that.
     * 
     * @param val
     * @return
     */
    public long setSequence(long val) {
        return sequence.getAndSet(val);
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        if (isRunning()) {
            throw new IllegalStateException("running");
        }
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        final CouchDbDocumentCallback callback = new CouchDbDocumentCallback(couchDbClient, tempDirectory);
        couchDbClient.dispatchGetDocument(app.getOriginId(), callback).waitForDone();
        return createContext(callback.getWebApp());
    }

    private ContextHandler createContext(WebAppDocument desc) {
        log.info("Creating new context for " + desc);
        final WebAppContext context = new WebAppContext(desc.getName(), desc.getContextPath());
        context.setServerClasses(concat(context.getServerClasses(), serverClasses));
        context.setSystemClasses(concat(context.getSystemClasses(), systemClasses));

        // context.setWar(home + desc.war);
        final ErrorHandler errorHandler = new JsonErrorHandler();
        errorHandler.setShowStacks(desc.isShowingFullStacktrace());
        context.setErrorHandler(errorHandler);
        context.getSecurityHandler().setAuthenticatorFactory(authenticatorFactory);
        context.setSessionHandler(new SessionHandler(sessionManagerProvider.get()));
        context.setParentLoaderPriority(false);
        return context;
    }

    public void setServerClasses(String[] serverClasses) {
        this.serverClasses = serverClasses;
    }

    public void setSystemClasses(String[] systemClasses) {
        this.systemClasses = systemClasses;
    }

    private final String[] concat(String[] l, String[] r) {
        if (l == null || l.length == 0) {
            return r;
        }
        if (r == null || r.length == 0) {
            return l;
        }
        final String combined[] = new String[l.length + r.length];
        System.arraycopy(l, 0, combined, 0, l.length);
        System.arraycopy(r, 0, combined, l.length, r.length);
        return combined;
    }

}
