/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.metrics.yammer;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import java.util.concurrent.TimeUnit;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.spi.metrics.MetricsCounter;
import org.qi4j.spi.metrics.MetricsCounterFactory;
import org.qi4j.spi.metrics.MetricsGauge;
import org.qi4j.spi.metrics.MetricsGaugeFactory;
import org.qi4j.spi.metrics.MetricsHealthCheck;
import org.qi4j.spi.metrics.MetricsHealthCheckFactory;
import org.qi4j.spi.metrics.MetricsHistogram;
import org.qi4j.spi.metrics.MetricsHistogramFactory;
import org.qi4j.spi.metrics.MetricsMeter;
import org.qi4j.spi.metrics.MetricsMeterFactory;
import org.qi4j.spi.metrics.MetricsProvider;
import org.qi4j.spi.metrics.MetricsProviderAdapter;
import org.qi4j.spi.metrics.MetricsTimer;
import org.qi4j.spi.metrics.MetricsTimerFactory;

public class YammerMetricsMixin extends MetricsProviderAdapter
    implements MetricsProvider, Activatable
{
    @Structure
    private Application app;

    @Override
    protected MetricsTimerFactory createMetricsTimerFactory()
    {
        return new MetricsTimerFactory()
        {
            @Override
            public MetricsTimer createTimer( Class<?> origin, String name, TimeUnit duration, TimeUnit rate )
            {
                return new YammerTimer( Metrics.newTimer( origin, name, app.name(), duration, rate ) );
            }
        };
    }

    @Override
    protected MetricsMeterFactory createMetricsMeterFactory()
    {
        return new MetricsMeterFactory()
        {
            @Override
            public MetricsMeter createMeter( Class<?> origin, String name, String eventType, TimeUnit rate )
            {
                return new YammerMeter( Metrics.newMeter( origin, name, app.name(), eventType, rate ) );
            }
        };
    }

    @Override
    protected MetricsHistogramFactory createMetricsHistogramFactory()
    {
        return new MetricsHistogramFactory()
        {
            @Override
            public MetricsHistogram createHistogram( Class<?> origin, String name )
            {
                return new YammerHistogram( Metrics.newHistogram( origin, name, app.name() ) );
            }
        };
    }

    @Override
    protected MetricsHealthCheckFactory createMetricsHealthCheckFactory()
    {
        return new MetricsHealthCheckFactory()
        {
            @Override
            public MetricsHealthCheck registerHealthCheck( Class<?> origin, String name, MetricsHealthCheck check )
            {
                return new YammerHealthCheck( origin, name, check );
            }
        };
    }

    @Override
    protected MetricsGaugeFactory createMetricsGaugeFactory()
    {
        return new MetricsGaugeFactory()
        {
            @Override
            public <T> MetricsGauge<T> registerGauge( Class<?> origin, String name, final MetricsGauge<T> gauge )
            {
                Gauge<T> yammer = Metrics.newGauge( origin, name, app.name(), new Gauge<T>()
                {

                    @Override
                    public T value()
                    {
                        return gauge.value();
                    }
                } );
                return new YammerGauge<T>( yammer );
            }
        };
    }

    @Override
    protected MetricsCounterFactory createMetricsCounterFactory()
    {
        return new MetricsCounterFactory()
        {
            @Override
            public MetricsCounter createCounter( Class<?> origin, String name )
            {
                Counter counter = Metrics.newCounter( origin, name, app.name() );
                return new YammerCounter( counter );
            }
        };
    }

    @Override
    public void activate()
        throws Exception
    {
    }

    @Override
    public void passivate()
        throws Exception
    {
        Metrics.shutdown();
    }

}
