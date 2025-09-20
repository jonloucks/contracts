package io.github.jonloucks.contracts.impl.test;


import io.github.jonloucks.contracts.test.Tests;
import io.github.jonloucks.contracts.test.Tools;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@ExtendWith(RunTests.MyExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
