package io.github.jonloucks.contracts.impl.test;


import io.github.jonloucks.contracts.test.Tests;
import io.github.jonloucks.contracts.test.Tools;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(RunTests.MyExtension.class)
public class RunTests implements Tests {

    public static class MyExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

        public MyExtension() {

        }

        @Override
        public void afterTestExecution(ExtensionContext extensionContext) {
            Tools.clean();
        }
        
        @Override
        public void beforeTestExecution(ExtensionContext extensionContext){
            Tools.clean();
        }
    }
}
