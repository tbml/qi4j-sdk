/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.orthogon.internal;

import org.ops4j.orthogon.ObjectFactory;
import org.ops4j.orthogon.pointcut.AspectRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator
    implements BundleActivator
{
    private ServiceRegistration m_factoryRegistration;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        MixinFactory mixinFactory = new MixinFactory();
        AdviceFactoryImpl adviceFactory = new AdviceFactoryImpl();
        AspectRegistry aspectRegistry = new AspectRegistry();
        ObjectFactory factory = new ObjectFactoryImpl( mixinFactory, adviceFactory, aspectRegistry );
        m_factoryRegistration = bundleContext.registerService( ObjectFactory.class.getName(), factory, null );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        m_factoryRegistration.unregister();
    }
}
