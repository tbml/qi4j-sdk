/*
 * Copyright (c) 2007-2008, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007-2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.auth;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Date;
import javax.security.auth.Subject;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;

@AppliesTo( RequiresPermission.class )
public class AuthorizationConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @Invocation private RequiresPermission requiresPermission;

    @Service private Authorization authorizor;
    @This private ProtectedResource roleAssignments;
    @Structure private TransientBuilderFactory cbf;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        TransientBuilder<NamedPermission> cb = cbf.newTransientBuilder( NamedPermission.class );
        cb.prototype().name().set( requiresPermission.value() );
        Permission permission = cb.newInstance();

        Subject subject = Subject.getSubject( AccessController.getContext() );
        User user = subject.getPrincipals( UserPrincipal.class ).iterator().next().getUser();

        TransientBuilder<AuthorizationContext> authBuilder = cbf.newTransientBuilder( AuthorizationContext.class );
        AuthorizationContext authProps = authBuilder.prototypeFor( AuthorizationContext.class );
        authProps.user().set( user );
        authProps.time().set( new Date() );
        authProps.authenticationMethod().set( AuthenticationMethod.BASIC );
        AuthorizationContext context = authBuilder.newInstance();
        if( !authorizor.hasPermission( permission, roleAssignments, context ) )
        {
            throw new SecurityException( "User " + user + " does not have the required permission " + requiresPermission.value() );
        }
        return next.invoke( proxy, method, args );
    }
}
