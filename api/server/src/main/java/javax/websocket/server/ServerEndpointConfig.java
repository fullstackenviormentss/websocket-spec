/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package javax.websocket.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;

/**
 * The ServerEndpointConfig is a special kind of endpoint configuration object that contains
 * web socket configuration information specific only to server endpoints. For developers
 * deploying programmatic endpoints, ServerEndpointConfig objects
 * can be created using a {@link ServerEndpointConfig.Builder}. Certain configuration
 * operations can be customized by providing a {@link ServerEndpointConfig.Configurator}
 *
 * @author dannycoward
 * @since DRAFT 001
 */
public interface ServerEndpointConfig extends EndpointConfig {

    /**
     * Returns the Class of the endpoint this configuration is configuring. If 
     * the endpoint is an annotated endpoint, the value is the class of the Java class
     * annotated with @ServerEndpoint. if the endpoint is a programmatic, the value
     * is the class of the subclass of Endpoint.
     *
     * @return the class of the endpoint, annotated or programmatic.
     */
    Class<?> getEndpointClass();

    /**
     * Return the path for this endpoint configuration. The path is the URI or 
     * URI-template relative to the websocket root of the server to which the 
     * endpoint using this configuration will be mapped. The path is always non-null
     * and always begins with a leading "/". 
     *
     * @return the relative path for this configuration.
     */
    String getPath();
    
    /**
     * Return the websocket subprotocols configured. 
     *
     * @return the list of subprotocols, the empty list if none
     */
    List<String> getSubprotocols();
    
    /**
     * Return the websocket extensions configured. 
     *
     * @return the list of extensions, the empty list if none.
     */
    List<Extension> getExtensions();
    
    /** 
     * Return the {@link ServerEndpointConfig.Configurator} this configuration
     * is using. If none was set by calling 
     * {@link ServerEndpointConfigBuilder#configurator(javax.websocket.server.ServerEndpointConfig.Configurator) }
     * this methods returns the platform default configurator.
     * 
     * @return the configurator in use.
     */
    ServerEndpointConfig.Configurator getConfigurator();
    
    
       /** 
    * The ServerEndpointConfig.Configurator interface may be implemented by developers who want to
    * provide custom configuration algorithms, such as intercepting the opening handshake, or
    * providing arbitrary methods and algorithms that can be accessed from each endpoint
    * instance configured with this configurator.
    * 
    * The implementation must provide a platform default configurator loading using the service
    * loader.
    */
    public abstract class Configurator {
        private ServerEndpointConfig.Configurator containerDefaultConfigurator;

        static ServerEndpointConfig.Configurator fetchContainerDefaultConfigurator() {
            for (ServerEndpointConfig.Configurator impl : ServiceLoader.load(javax.websocket.server.ServerEndpointConfig.Configurator.class)) {
                return impl;
            }
            throw new RuntimeException("Cannot load platform configurator");
        }

        ServerEndpointConfig.Configurator getContainerDefaultConfigurator() {
            if (this.containerDefaultConfigurator == null) {
                this.containerDefaultConfigurator = fetchContainerDefaultConfigurator();
            }
            return this.containerDefaultConfigurator;

        }

        /**
         * Return the subprotocol the server endpoint has chosen from the requested
         * list supplied by a client who wishes to connect, or none if there wasn't one
         * this server endpoint liked. See
         * <a href="http://tools.ietf.org/html/rfc6455#section-4.2.2">Sending the 
         * Server's Opening Handshake</a>. Subclasses may provide custom algorithms 
         * based on other factors.
         * <br><br>
         * The default platform implementation of this method returns the first 
         * subprotocol in the list sent by the client that the server supports, 
         * or the empty string if there isn't one.
         * 
         * @param requested the requested subprotocols from the client endpoint
         * @param supported the subprotocols supported by the server endpoint
         * @return the negotiated subprotocol or the empty string if there isn't one.
         */

        public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
            return this.getContainerDefaultConfigurator().getNegotiatedSubprotocol(supported, requested);
        } 

        /**
         * Return the ordered list of extensions that t server endpoint will support 
         * given the requested extension list passed in, the empty list if none. See 
         * <a href="http://tools.ietf.org/html/rfc6455#section-9.1">Negotiating Extensions</a>
         * <br><br>
         * The default platform implementation of this method returns a list 
         * containing all of the requested extensions passed to this method that 
         * it supports, using the order in the requested extensions, the empty 
         * list if none.
         * 
         * @param installed the installed extensions on the implementation.
         * @param requested the requested extensions, in the order they were 
         * requested by the client
         * @return the list of extensions negotiated, the empty list if none.
         */
        public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
            return this.getContainerDefaultConfigurator().getNegotiatedExtensions(installed, requested);
        }




        /**
         * Check the value of the Origin header (<a href="http://tools.ietf.org/html/rfc6454">See Origin Header</a>) the client passed during the opening
         * handshake.
         * <br><br>
         * The platform default implementation of this method makes a check of the 
         * validity of the Origin header sent along with 
         * the opening handshake following the recommendation at: 
         * <a href="http://tools.ietf.org/html/rfc6455#section-4.2">Sending 
         * the Server's Opening Handshake<a>.
         * 
         * @param originHeaderValue the value of the origin header passed
         * by the client.
         * @return whether the check passed or not
         */
        public boolean checkOrigin(String originHeaderValue) {
            return this.getContainerDefaultConfigurator().checkOrigin(originHeaderValue);
        }

        /**
         * This default implementation matches the incoming path to the configuration's URI or URI template if and only if
         * it is an exact match in the case the configuration is a URI, and if and only if it is a valid
         * expansion of the configuration URI template, in the case where the configuration is a URI template. Subclasses may override this method to provide
         * different matching policies.
         *
         * @param uri the URL of the incoming request
         * @return whether it matched this configuration or not.
         */
         /**
         * Answers whether the incoming handshake request uri matches the
         * path of the server endpoint. This method may be overridden
         * by implementations with any number of algorithms for determining a match.
         * If the path is a URI-template, and the implementation of this method
         * determines there is a match of some kind, the implementation must add
         * the path variable and values to the template expansion map passed into 
         * this method.
         * <br><br>
         * The platform default implementation matches the incoming uri to the 
         * configuration's path if and only if it is an exact match 
         * in the case the path is a URI, and if and only if it is a valid
         * URI-template expansion of the path, in the case where the 
         * path is a URI template. In this latter case, all the path variables
         *  and values are added to the template expansion map.
         *
         * @param templateExpansion an empty Map to hold URI-template expansion
         * names and values.
         * @param path the path (URI or URI template) of the server endpoint
         * @param requestUri the uri of the incoming handshake.
         * @return whether there was a match
         */
        public boolean matchesURI(String path, URI requestUri, Map<String, String> templateExpansion) {
            return this.getContainerDefaultConfigurator().matchesURI(path, requestUri, templateExpansion);
        }

         /**
         * The default server configuration does not make any changes to the response. 
         * Subclasses may override this method in order to inspect the Http request 
         * headers of the opening handshake, for example to track cookies
         * sent by the client. Additionally subclasses may choose to override this 
         * method to modify the outgoing handshake response.
         * the outgoing handshake response
         *
         * @param request  the handshake request from the client
         * @param response the handshake response formulated by the container.
         */

        /**
         * Called by the container after it has formulated a handshake response resulting from
         * a well-formed handshake request. The container has already has already 
         * checked that this configuration has a matching URI, determined the 
         * validity of the origin using the checkOrigin method, and filled
         * out the negotiated subprotocols and extensions based on this configuration.
         * Custom configurations may override this method in order to inspect
         * the request parameters and modify the handshake response that the server has formulated.
         * and the URI checking also.
         * <br><br>
         * If the developer does not override this method, no further
         * modification of the request and response are made by the implementation.
         * 
         * @param request  the opening handshake request.
         * @param response the proposed opening handshake response
         */
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            // nothing.
        }
        
        /**
         * This method is called by the container each time a new client
         * connects to the logical endpoint this configurator configures.
         * Developers may override this method to control instantiation of
         * endpoint instances in order to customize the initialization
         * of the endpoint instance, or manage them in some other way.
         * If the developer overrides this method, services like
         * dependency injection that are otherwise supported
         * may not be available. 
         * The platform default implementation of this method returns a new
         * endpoint instance per call, thereby ensuring that there is one
         * endpoint instance per client, the default deployment cardinality.
         * 
         * @return an instance of the endpoint that will handle all
         * interactions from a new client.
         * @throws InstantiationException if there was an error producing the
         * endpoint instance.
         */
        
        public Object getEndpointInstance() throws InstantiationException {
            return this.getContainerDefaultConfigurator().getEndpointInstance();
        } 

    }


    /**
     * The ServerEndpointConfig.Builder is a class used for creating
     * {@link ServerEndpointConfig.Builder} objects for the purposes of
     * deploying a client endpoint.
     * <br><br>Here are some examples:<br>
     * <br>Building a plain configuration for an endpoint with just a path.<br><br>
     * <code>
     * ServerEndpointConfig config = ServerEndpointConfig.Builder.create(ProgrammaticEndpoint.class, "/foo").build();<br>
     * </code>
     * 
     * <br><br>Building a configuration with no subprotocols and a custom configurator.<br><br>
     * <code>
     * ServerEndpointConfig config = ServerEndpointConfig.Builder.create(ProgrammaticEndpoint.class, "/bar")<br>
                    .subprotocols(subprotocols)<br>
                    .configurator(new MyServerConfigurator())<br>
                    .build();<br>
     * </code>
     * 
     * @author dannycoward
     */
    public final class Builder {
        private String path;
        private Class endpointClass;
        private List<String> subprotocols = new ArrayList<String>();
        private List<Extension> extensions = new ArrayList<Extension>();
        private List<Encoder> encoders = new ArrayList<Encoder>();
        private List<Decoder> decoders = new ArrayList<Decoder>();
        private ServerEndpointConfig.Configurator serverEndpointConfigurator;

        /**
         * Creates the builder with the mandatory information of the endpoint class 
         * (programmatic or annotated), the relative URI or URI-template to use,
         * and with no subprotocols, extensions, encoders, decoders or custom
         * configurator.
         * @param endpointClass the class of the endpoint to configure
         * @param path The URI or URI template where the endpoint will be deployed.
         * A trailing "/" will be ignored and the path must begin with /.
         * @return a new instance of ServerEndpointConfig.Builder
         */
        public static Builder create(Class endpointClass, String path) {
            return new Builder(endpointClass, path);
        }

        // only one way to build them
        private Builder() {

        }
        /**
         * Builds the configuration object using the current attributes
         * that have been set on this builder object.
         * 
         * @return a new ServerEndpointConfig object.
         */
        public ServerEndpointConfig build() {
            return new DefaultServerEndpointConfig(
                    this.endpointClass,
                    this.path,
                    Collections.unmodifiableList(this.subprotocols),
                    Collections.unmodifiableList(this.extensions),
                    Collections.unmodifiableList(this.encoders),
                    Collections.unmodifiableList(this.decoders),
                    this.serverEndpointConfigurator
                 );
        }

        private Builder(Class endpointClass, String path) {
            if (endpointClass == null) {
                throw new IllegalArgumentException("endpointClass cannot be null");
            }
            this.endpointClass = endpointClass;
            if (path == null || !path.startsWith("/")) {
                throw new IllegalStateException("Path cannot be null and must begin with /");
            }
            this.path = path;
        }



        /** 
         * Sets the list of encoders for this builder.
         * 
         * @param encoders the encoders
         * @return this builder instance
         */
        public ServerEndpointConfig.Builder encoders(List<Encoder> encoders) {
            this.encoders = (encoders == null) ? new ArrayList<Encoder>() : encoders;
            return this;
        }


        /**
         * Sets the decoders to use in the configuration.
         * 
         * @param decoders the decoders
         * @return this builder instance.
         */
        public ServerEndpointConfig.Builder decoders(List<Decoder> decoders) {
            this.decoders = (decoders == null) ? new ArrayList<Decoder>() : decoders;
            return this;
        }

        /**
         * Sets the subprotocols to use in the configuration.
         * 
         * @param subprotocols the subprotocols.
         * @return this builder instance
         */
        public ServerEndpointConfig.Builder subprotocols(List<String> subprotocols) {
            this.subprotocols = (subprotocols == null) ? new ArrayList<String>() : subprotocols;
            return this;
        }


        /**
         * Sets the extensions to use in the configuration.
         * 
         * @param extensions the extensions to use.
         * @return this builder instance.
         */
        public ServerEndpointConfig.Builder extensions(List<Extension> extensions) {
            this.extensions = (extensions == null) ? new ArrayList<Extension>() : extensions;
            return this;
        }

        /** 
         * Sets the custom configurator to use on the configuration
         * object built by this builder.
         * 
         * @param serverEndpointConfigurator the configurator
         * @return this builder instance
         */
        public ServerEndpointConfig.Builder configurator(ServerEndpointConfig.Configurator serverEndpointConfigurator) {
            this.serverEndpointConfigurator = serverEndpointConfigurator;
            return this;
        }



    }

    
}
