/**
 * 
 */
package org.apache.maven.surefire.testng;

import java.util.List;

import org.apache.maven.surefire.util.internal.SelectorUtils;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

/**
 * For internal use only
 * @author Olivier Lamy
 * @since 2.7.3
 *
 */
public class MethodSelector implements IMethodSelector 
{
    public void setTestMethods( List arg0 )
    {
        // noop                    
    }
    
    public boolean includeMethod( IMethodSelectorContext context, ITestNGMethod testngMethod, boolean isTestMethod )
    {
        return SelectorUtils.match( TestNGExecutor.METHOD_NAME, testngMethod.getMethodName() );
    }
}
