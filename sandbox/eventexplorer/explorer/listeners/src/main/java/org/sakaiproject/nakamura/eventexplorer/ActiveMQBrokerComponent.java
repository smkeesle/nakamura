/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.eventexplorer;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.network.NetworkConnector;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

@Component(immediate = true, name = "org.sakaiproject.nakamura.eventexplorer.ActiveMQBrokerComponent", label = "%activemqbroker.name", description = "%activemqbroker.description", metatype = true)
@Properties(value = { @Property(name = "service.vendor", value = "The Sakai Foundation"),
    @Property(name = "service.description", value = "AMQ Broker Component.") })
public class ActiveMQBrokerComponent {

  @Property(description = "Federated broker URL to enable connection to other AMQ Brokers, by default federated connection will not be attempted")
  private static final String ACTIVEMQ_FEDERATED_BROKER_URL = "federated.broker.url";

  @Property(value = "tcp://localhost:61616", description = "URL for the local borker to conect to default is tcp://localhost:61616")
  private static final String ACTIVEMQ_BROKER_URL = "broker.url";

  private static final Logger LOG = LoggerFactory
      .getLogger(ActiveMQBrokerComponent.class);

  private BrokerService broker;

  @Activate
  protected void activate(ComponentContext componentContext) throws Exception {
    LOG.info("Starting Activation of AMQ Broker ");
    try {
      Dictionary<?, ?> properties = componentContext.getProperties();
      BundleContext bundleContext = componentContext.getBundleContext();
      String brokerUrl = (String) properties.get(ACTIVEMQ_BROKER_URL);

      broker = new BrokerService();

      // generate a full path
      String slingHome = bundleContext.getProperty("sling.home");
      String dataPath = slingHome + "/activemq-data";
      LOG.info("Setting Data Path to  [{}] [{}] ", new Object[] { slingHome, dataPath });
      broker.setDataDirectory(dataPath);

      String federatedBrokerUrl = (String) properties.get(ACTIVEMQ_FEDERATED_BROKER_URL);

      if (federatedBrokerUrl != null && federatedBrokerUrl.length() > 0) {
        LOG.info("Federating ActiveMQ  [" + federatedBrokerUrl + "]");
        NetworkConnector connector = broker.addNetworkConnector(federatedBrokerUrl);
        connector.setDuplex(true);
      }

      // configure the broker
      LOG.info("Adding ActiveMQ connector [" + brokerUrl + "]");
      broker.addConnector(brokerUrl);

      broker.start();
    } catch (Exception e) {
      LOG.info(e.getMessage(), e);
      throw e;
    }
  }

  @Deactivate
  protected void deactivate(ComponentContext componentContext) throws Exception {
    if (broker != null && broker.isStarted()) {
      broker.stop();
    }
    broker = null;
  }
}
